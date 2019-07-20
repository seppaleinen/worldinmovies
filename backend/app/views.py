import requests, json, os

from django.http import HttpResponse
from django.db import connection
from app.models import Movie, Genre
from app.importer import download_files, concurrent_stuff, fetch_genres, fetch_countries, fetch_languages


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



# Imports

def download_file(request):
    return HttpResponse(download_files())


def fetch_movie(request):
    return HttpResponse(concurrent_stuff())


def fetch_genres(request):
    return HttpResponse(fetch_genres())


def fetch_countries(request):
    return HttpResponse(fetch_countries())


def fetch_languages(request):
    return HttpResponse(fetch_languages())
