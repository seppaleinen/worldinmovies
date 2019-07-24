import requests, json, os, csv

from django.http import HttpResponse, JsonResponse
from django.db import connection
from app.models import Movie, Genre
from app.importer import download_files, concurrent_stuff, import_genres, import_countries, import_languages, base_import


def index(request):
    movies_count = Movie.objects.count()
    if movies_count > 0:
        return HttpResponse("Amount of movies in DB: %s, first is: %s" % (movies_count, Movie.objects.first().id))
    else:
        return HttpResponse("No movies fetched yet")


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


def get_best_movies_by_country(request):
    with connection.cursor() as cursor:
        cursor.execute("""select country.iso_3166_1, original_title, vote_average from app_productioncountries country 
                                join lateral (
                                    select * from app_movie movie
                                        inner join app_productioncountries_movies pcm on pcm.movie_id = movie.id
                                        inner join app_productioncountries pc on pc.id = pcm.productioncountries_id
                                        where pc.iso_3166_1 = country.iso_3166_1
                                        and movie.fetched is true
                                        order by movie.vote_average desc
                                        limit 10
                                    ) p on true
                                order by country.iso_3166_1 asc""")
        return HttpResponse(cursor.fetchall())


def ratings(request):
    """This should map incoming imdb ratings file, and try to match it with our dataset,
        and return it in a format we can use in frontend
    """
    if request.method == 'POST':
        if 'file' in request.FILES:
            file = request.FILES['file']
            csv_as_dicts = csv.DictReader(file.read().decode('utf-8').splitlines())
            # Const,Your Rating,Date Rated,Title,URL,Title Type,IMDb Rating,Runtime (mins),Year,Genres,Num Votes,Release Date,Directors
            found = []
            not_found = []
            for i in csv_as_dicts:
                row_as_json = json.loads(json.dumps(i))
                # print(row_as_json)
                try:
                    found_movie = Movie.objects.get(imdb_id=row_as_json['Const'])
                    found.append({"title":found_movie.original_title})
                except Exception as exc:
                    not_found.append({"title":row_as_json['Title']})

            return JsonResponse({"found_responses":found, "not_found":not_found})

    return HttpResponse("NoCanDo")


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
