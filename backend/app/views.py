import json, csv, simplejson, time, datetime

from django.http import HttpResponse, JsonResponse, StreamingHttpResponse
from django.db import connection
from app.models import Movie, Genre
from app.importer import download_files, concurrent_stuff, import_genres, import_countries, import_languages, \
    base_import, import_imdb_ratings, check_which_movies_needs_update
from django.views.decorators.csrf import csrf_exempt
from fuzzywuzzy import fuzz


def index(request):
    movies_count = Movie.objects.count()
    if movies_count > 0:
        return HttpResponse("Amount of movies in DB: %s, first is: %s" % (movies_count, Movie.objects.first().id))
    else:
        return HttpResponse("No movies fetched yet")


def stream_response_generator():
    for x in range(1, 3):
        yield "%s<br/>" % x  # Returns a chunk of the response to the browser
        time.sleep(1)


def stream_response_test(request):
    return StreamingHttpResponse(stream_response_generator())


def import_status(request):
    with connection.cursor() as cursor:
        cursor.execute("""select 
                                sum(case when fetched is True then 1 else 0 end) as fetched, 
                                count(*) as total, 
                                sum(case when fetched is True then 1 else 0 end) * 100 / count(*) as percentage 
                                from app_movie""")
        result = cursor.fetchone()
        fetched = result[0]
        total = result[1]
        percent = result[2]
        if total == 0:
            return HttpResponse("Daily file export have not been imported yet. No movies to be fetched")
        else:
            return HttpResponse("There are {fetched} fetched movies out of {amount}, which is about {percent}%"
                                .format(fetched=fetched, amount=total, percent=percent))


def get_best_movies_from_all_countries(request):
    with connection.cursor() as cursor:
        cursor.execute("""select country.iso_3166_1, original_title, vote_average from app_productioncountries country 
                                join lateral (
                                    select * from app_movie movie
                                        inner join app_productioncountries_movies pcm on pcm.movie_id = movie.id
                                        inner join app_productioncountries pc on pc.id = pcm.productioncountries_id
                                        where pc.iso_3166_1 = country.iso_3166_1
                                        and movie.fetched is true
                                        and movie.vote_count > 20
										and movie.vote_average > 0
                                        order by (movie.vote_count / (cast(movie.vote_count as numeric) + 10)) * movie.vote_average + (10 / (cast(movie.vote_count as numeric) + 10)) desc
                                        limit 10
                                    ) p on true
                                order by country.iso_3166_1 asc""")
        result = []
        for row in cursor.fetchall():
            result.append({
                "title": row[0],
                "country_codes": row[1],
                "vote_average": row[2]
            })
        return HttpResponse(simplejson.dumps(result), content_type='application/json')


@csrf_exempt
def get_best_movies_from_country(request, country_code):
    if request.method != 'GET':
        return HttpResponse("Method not allowed", status=400)
    page = int(request.GET.get('page', 0)) * 10
    with connection.cursor() as cursor:
        cursor.execute("""
            select movie.imdb_id, movie.original_title, movie.release_date, movie.poster_path, movie.vote_average, movie.vote_count, count(*) OVER() as total_count, (select title from app_alternativetitle where movie_id = movie.id and iso_3166_1 in ('US', 'GB') limit 1) as en_title from app_movie movie
	            inner join app_productioncountries_movies pcm on pcm.movie_id = movie.id
	            inner join app_productioncountries pc on pc.id = pcm.productioncountries_id
	            where movie.fetched is True
	            and pc.iso_3166_1 = '{country_code}'
	            and movie.vote_count > 5
	            and movie.vote_average > 0
	            order by (movie.vote_count / (cast(movie.vote_count as numeric) + 10)) * movie.vote_average + (10 / (cast(movie.vote_count as numeric) + 10)) desc
	            limit 10
	            offset {offset}
        """.format(country_code=country_code, offset=page))
        result = {"result": [], "total_result": None}
        for row in cursor.fetchall():
            result['total_result'] = row[6]
            original_title = row[1]
            en_title = __alt_title(original_title, row[7])
            result['result'].append({
                'imdb_id': row[0],
                'original_title': row[1],
                'release_date': row[2],
                'poster_path': row[3],
                'vote_average': row[4],
                'vote_count': row[5],
                'en_title': en_title
            })
        return HttpResponse(simplejson.dumps(result, indent=2 * ' '), content_type='application/json; charset=utf-8')


def __alt_title(original_title, en_title):
    if not en_title:
        return None
    if en_title == original_title:
        return None
    if fuzz.token_set_ratio(original_title, en_title) > 80:
        return None
    else:
        return " (%s)" % en_title


@csrf_exempt
def ratings(request):
    """This should map incoming imdb ratings file, and try to match it with our dataset,
        and return it in a format we can use in frontend

        curl 'http://localhost:8000/ratings' -X POST -H 'Content-Type: multipart/form-data' -F file=@testdata/ratings.csv
    """
    print("Receiving stuff")
    if request.method == 'POST':
        if 'file' in request.FILES:
            file = request.FILES['file']
            csv_as_dicts = csv.DictReader(file.read().decode('cp1252').splitlines())
            # Const,Your Rating,Date Rated,Title,URL,Title Type,IMDb Rating,Runtime (mins),Year,Genres,Num Votes,Release Date,Directors
            result = {'found': {}, 'not_found': []}
            for i in csv_as_dicts:
                row_as_json = json.loads(json.dumps(i))
                try:
                    found_movie = Movie.objects.get(imdb_id=row_as_json['Const'])
                    for country in found_movie.production_countries.all():
                        result['found'].setdefault(country.iso_3166_1, []).append({
                            "title": found_movie.original_title,
                            "country_code": country.iso_3166_1,
                            "year": row_as_json['Year'],
                            "imdb_id": row_as_json['Const'],
                            "personal_rating": row_as_json['Your Rating'],
                            "rating": row_as_json['IMDb Rating']
                        })
                except Exception as exc:
                    result['not_found'].append({
                        "title": row_as_json['Title'],
                        "year": row_as_json['Year'],
                        "imdb_id": row_as_json['Const']
                    })

            return JsonResponse(result)

    return HttpResponse("Method: %s, not allowed" % request.method, status=400)


# Imports


def download_file(request):
    return HttpResponse(download_files())


def base_fetch(request):
    return HttpResponse(base_import())


def fetch_movie(request):
    return HttpResponse(concurrent_stuff())


def fetch_genres(request):
    return HttpResponse(import_genres())


def fetch_countries(request):
    return HttpResponse(import_countries())


def fetch_languages(request):
    return HttpResponse(import_languages())


def fetch_imdb_ratings(request):
    return HttpResponse(import_imdb_ratings())


def check_tmdb_for_changes(request):
    start_date = request.GET.get('start_date',
                                 (datetime.date.today() - datetime.timedelta(days=1)).strftime("%Y-%m-%d"))
    end_date = request.GET.get('end_date', datetime.date.today().strftime("%Y-%m-%d"))
    amount_to_be_updated = check_which_movies_needs_update(start_date, end_date)
    return HttpResponse("%s movies are set to be updated" % amount_to_be_updated)
