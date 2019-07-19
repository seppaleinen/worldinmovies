import requests, json, os

from django.http import HttpResponse
from django.db import connection
from app.models import Movie, Genre
from app.importer import download_files, concurrent_stuff


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
    API_KEY = os.getenv('TMDB_API')
    url = "https://api.themoviedb.org/3/genre/movie/list?api_key={api_key}&language=en-US".format(api_key=API_KEY)
    response = requests.get(url, stream=True)
    if response.status_code == 200:
        genres_from_json = json.loads(response.content)['genres']
        for genre in genres_from_json:
            print(genre)
            Genre(id=genre['id'], name=genre['name']).save()
    for genresa in Genre.objects.all():
        print("Genre: %s" % genresa.name)
    return HttpResponse("hejhej")
