import datetime, requests, gzip, json

from django.shortcuts import render
from django.http import HttpResponse
from django.db import transaction

from app.models import Movie

def index(request):
    download_file()
    return HttpResponse("Hello, world. You're at the polls index.")


def get_movies(request):
    all_movies = Movie.objects.all()
    print("Fetched size: %s" % all_movies.count())
    return HttpResponse("hejhej")


@transaction.atomic
def download_file():
    todays_date = datetime.datetime.now().strftime("%m_%d_%Y")
    daily_export_url = "http://files.tmdb.org/p/exports/movie_ids_%s.json.gz" % todays_date
    response = requests.get(daily_export_url, stream=True)

    if response.status_code == 200:
        with open('movies.json.gz', 'wb') as f:
            f.write(response.content)

        movies = []
        contents = unzip_file()
        for i in contents:
            try:
                data = json.loads(i)
                id = data['id']
                adult = data['adult']
                original_title = data['original_title']
                video = data['video']
                popularity = data['popularity']
                if video is False and adult is False:
                    movies.append(Movie(id=id, original_title=original_title, popularity=popularity))
            except Exception as e:
                print("This line fucked up: %s, because of %s" % (i, e))
        # Creates all, but crashes as soon as you try to update the list
        try:
            Movie.objects.bulk_create(movies)
        except Exception as e:
            print("You done fucked up: %s" % e)


def unzip_file():
    f = gzip.open('movies.json.gz', 'rt', encoding='utf-8')
    file_content = f.read()
    f.close()
    return file_content.splitlines()

