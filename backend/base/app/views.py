import json, csv, simplejson, time, datetime, ast

from django.http import HttpResponse, JsonResponse, StreamingHttpResponse
from django.db import connection
from app.models import Movie, Genre
from app.importer import download_files, concurrent_stuff, import_genres, import_countries, import_languages, \
    base_import, import_imdb_ratings, check_which_movies_needs_update, import_imdb_alt_titles
from django.views.decorators.csrf import csrf_exempt
from fuzzywuzzy import fuzz


def import_status(request):
    with connection.cursor() as cursor:
        cursor.execute("""select 
                                sum(case when fetched is True then 1 else 0 end) as fetched, 
                                count(*) as total, 
                                sum(case when fetched is True then 1 else 0 end) * 100 / count(*) as percentage 
                                from app_movie""")
        result = cursor.fetchone()
        fetched = result[0] if result[0] else 0
        total = result[1]
        percent = result[2] if result[2] else 0
        return HttpResponse(simplejson.dumps({"fetched": fetched, "total": total, "percentage_done": percent}), content_type='application/json')


"""
The formula for calculating the Top Rated 250 Titles gives a true Bayesian estimate:
weighted rating (WR) = (v ÷ (v+m)) × R + (m ÷ (v+m)) × C where:

R = average for the movie (mean) = (Rating)
v = number of votes for the movie = (votes)
m = minimum votes required to be listed in the Top 250 (currently 25000)
C = the mean vote across the whole report (currently 7.0)
"""
def get_best_movies_from_all_countries(request):
    with connection.cursor() as cursor:
        cursor.execute("""select country.iso_3166_1, original_title, vote_average from app_productioncountries country 
                                join lateral (
                                    select * from app_movie movie
                                        inner join app_productioncountries_movies pcm on pcm.movie_id = movie.id
                                        inner join app_productioncountries pc on pc.id = pcm.productioncountries_id
                                        where pc.iso_3166_1 = country.iso_3166_1
                                        and movie.fetched is true
                                        and movie.vote_count > 200
										and movie.vote_average > 0
                                        order by (movie.vote_count / (cast(movie.vote_count as numeric) + 200)) * movie.vote_average + (200 / (cast(movie.vote_count as numeric) + 200)) * 7 desc
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
    page = int(request.GET.get('page', 0)) * 20
    with connection.cursor() as cursor:
        cursor.execute("""
            select movie.imdb_id, movie.original_title, movie.release_date, movie.poster_path, movie.vote_average, movie.vote_count, count(*) OVER() as total_count, (select title from app_alternativetitle where movie_id = movie.id and iso_3166_1 in ('US', 'GB') limit 1) as en_title from app_movie movie
	            inner join app_productioncountries_movies pcm on pcm.movie_id = movie.id
	            inner join app_productioncountries pc on pc.id = pcm.productioncountries_id
	            where movie.fetched is True
	            and pc.iso_3166_1 = '{country_code}'
	            and movie.vote_count > 200
	            and movie.vote_average > 0
	            order by (movie.vote_count / (cast(movie.vote_count as numeric) + 200)) * movie.vote_average + (200 / (cast(movie.vote_count as numeric) + 200)) * 4 desc
	            limit 20
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
        return HttpResponse(simplejson.dumps(result), content_type='application/json; charset=utf-8')


def __alt_title(original_title, en_title):
    if not en_title:
        return None
    if en_title == original_title:
        return None
    if fuzz.token_set_ratio(original_title, en_title) > 80:
        return None
    else:
        return en_title


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
            csv_as_dicts = csv.DictReader(file.read().decode('utf8').splitlines())
            # Const,Your Rating,Date Rated,Title,URL,Title Type,IMDb Rating,Runtime (mins),Year,Genres,Num Votes,Release Date,Directors
            result = {'found': {}, 'not_found': []}
            for i in csv_as_dicts:
                row_as_json = json.loads(json.dumps(i))
                try:
                    found_movie = Movie.objects.get(imdb_id=row_as_json['Const'])
                    for country in found_movie.production_countries.all():
                        result['found'].setdefault(country.iso_3166_1, []).append({
                            'imdb_id': found_movie.imdb_id,
                            'original_title': found_movie.original_title,
                            'release_date': found_movie.release_date,
                            'poster_path': found_movie.poster_path,
                            'vote_average': found_movie.vote_average,
                            'vote_count': found_movie.vote_count,
                            'country_code': country.iso_3166_1
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
    return StreamingHttpResponse(download_files())


def base_fetch(request):
    return StreamingHttpResponse(base_import())


def fetch_movie(request):
    return StreamingHttpResponse(concurrent_stuff())


def fetch_genres(request):
    return StreamingHttpResponse(import_genres())


def fetch_countries(request):
    return StreamingHttpResponse(import_countries())


def fetch_languages(request):
    return StreamingHttpResponse(import_languages())


def fetch_imdb_ratings(request):
    return StreamingHttpResponse(import_imdb_ratings())


def fetch_imdb_titles(request):
    return StreamingHttpResponse(import_imdb_alt_titles())


def check_tmdb_for_changes(request):
    start_date = request.GET.get('start_date',
                                 (datetime.date.today() - datetime.timedelta(days=1)).strftime("%Y-%m-%d"))
    end_date = request.GET.get('end_date', datetime.date.today().strftime("%Y-%m-%d"))
    return StreamingHttpResponse(check_which_movies_needs_update(start_date, end_date))


def movie_details(request, imdb_id):
    return HttpResponse(Movie.objects.get(imdb_id=imdb_id).raw_response)


def dostuff(movie):
    return {"fetched": movie.fetched,
            "_id": movie.id,
            "data": json.loads(movie.raw_response),
            "fetched_date": {"$date": datetime.datetime.now().strftime('%Y-%m-%dT%H:%M:%S.%fZ')}}


def generate_datadump(request):
    with open('datadump.json', 'w') as f:
        for x in Movie.objects.all().iterator():
            f.write(json.dumps(dostuff(x)))
    return HttpResponse("Done")