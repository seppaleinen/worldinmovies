import datetime, requests, gzip, json

from django.shortcuts import render
from django.http import HttpResponse

from app.models import Movie

def index(request):
    download_file()
    return HttpResponse("Hello, world. You're at the polls index.")


def get_movies(request):
    all_movies = Movie.objects.all()
    for movie in all_movies:
        print("Fetched: %s" % movie.original_title)
    print("Fetched size: %s" % all_movies.count())
    return HttpResponse("hejhej")


def download_file():
    todays_date = datetime.datetime.now().strftime("%m_%d_%Y")
    daily_export_url = "http://files.tmdb.org/p/exports/movie_ids_%s.json.gz" % todays_date
    response = requests.get(daily_export_url, stream=True)

    if response.status_code == 200:
        with open('movies.json.gz', 'wb') as f:
            f.write(response.content)

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
                    print("ID: %s, TITLE: %s, POPULARITY: %s" % (id, original_title, popularity))
                    movie = Movie(id=id, original_title=original_title, popularity=popularity)
                    movie.save()
            except Exception as e:
                print("This line fucked up: %s" % i)


def unzip_file():
    f = gzip.open('movies.json.gz', 'rt', encoding='utf-8')
    file_content = f.read()
    f.close()
    return file_content.splitlines()

