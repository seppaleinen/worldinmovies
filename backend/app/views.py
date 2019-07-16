import datetime, requests, gzip, json, os, sys, concurrent.futures, time

from django.shortcuts import render
from django.http import HttpResponse
from django.db import transaction
from app.models import Movie, Genre, AlternativeTitle, SpokenLanguage, ProductionCountries
import os




def index(request):
    all_movies = Movie.objects.all()
    if all_movies.count() > 0:
        return HttpResponse("Amount of movies in DB: %s, first is: %s" % (all_movies.count(), all_movies[0].id))
    else:
        return HttpResponse("No movies fetched yet")


def download_file(request):
    return HttpResponse(download_files())


@transaction.atomic
def download_files():
    todays_date = datetime.datetime.now()
    yesterday = datetime.date.today() - datetime.timedelta(days=1)
    yesterday_formatted = yesterday.strftime("%m_%d_%Y")
    daily_export_url = "http://files.tmdb.org/p/exports/movie_ids_%s.json.gz" % yesterday_formatted
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
                    movies.append(Movie(id=id, original_title=original_title, popularity=popularity, fetched=False))
            except Exception as e:
                print("This line fucked up: %s, because of %s" % (i, e))
        # Creates all, but crashes as soon as you try to update the list
        try:
            for i in range(0, len(movies), 100):
                chunk = movies[i:i + 100]
                Movie.objects.bulk_create(chunk)
            return "Amount of movies imported: %s" % len(contents)
        except Exception as e:
        	print("Error: %s" % e)
        	return "Exception: %s" % e
    else:
        return "Request failed with status: %s, and message: %s" % (response.status_code, response.content)


def unzip_file():
    f = gzip.open('movies.json.gz', 'rt', encoding='utf-8')
    file_content = f.read()
    f.close()
    return file_content.splitlines()


def fetch_movie(request):
    concurrent_stuff()
    #all_movies = Movie.objects.all()
    #for movie in all_movies:
    #    with open("%s.json" % movie.id, 'wb') as f:
    #        print("Fetching id: %s" % movie.id)
    #        f.write(fetch_movie_with_id(movie.id))


def fetch_movie_with_id(id, index):
    API_KEY = os.getenv('TMDB_API')
    url = "https://api.themoviedb.org/3/movie/{movie_id}?" \
          "api_key={api_key}&" \
          "language=en-US&" \
          "append_to_response=alternative_titles,credits,external_ids,images,account_states".format(movie_id=id, api_key=API_KEY)
    response = requests.get(url, stream=True)
    if response.status_code == 200:
        print("Fetched index: %s" % (index))
        return response.content
    elif response.status_code == 429:
        retryAfter = int(response.headers['Retry-After']) + 1
        print("RetryAfter: %s" % retryAfter)
        time.sleep(retryAfter)
        return fetch_movie_with_id(id, index)
    return None


CONNECTIONS = 5


def concurrent_stuff():
    movies = [movie for movie in Movie.objects.all() if not movie.fetched]
    length = len(movies)
    with concurrent.futures.ThreadPoolExecutor(max_workers=CONNECTIONS) as executor:
        future_to_url = (executor.submit(fetch_movie_with_id, movie.id, index) for index, movie in enumerate(movies))
        for future in concurrent.futures.as_completed(future_to_url):
            try:
                data = future.result()
                fetched_movie = json.loads(data)
                db_movie = Movie.objects.get(pk=fetched_movie['id'])
                db_movie.fetched = True
                db_movie.raw_response = data
                db_movie.budget = fetched_movie['budget']
                db_movie.imdb_id = fetched_movie['imdb_id']
                db_movie.original_language = fetched_movie['original_language']
                db_movie.overview = fetched_movie['overview']
                db_movie.poster_path = fetched_movie['poster_path']
                db_movie.release_date = fetched_movie['release_date']
                db_movie.revenue = fetched_movie['revenue']
                db_movie.runtime = fetched_movie['runtime']
                db_movie.vote_average = fetched_movie['vote_average']
                db_movie.vote_count = fetched_movie['vote_count']
                alt_titles = []
                for fetch_alt_title in fetched_movie['alternative_titles']['titles']:
                    alt_title = AlternativeTitle(movie_id=db_movie.id, iso_3166_1=fetch_alt_title['iso_3166_1'], title=fetch_alt_title['title'], type=fetch_alt_title['type'])
                    alt_title.save()
                    alt_titles.append(alt_title)
                db_movie.alternative_titles.set(alt_titles)
                spoken_langs = []
                for fetch_spoken_lang in fetched_movie['spoken_languages']:
                    spoken_lang = SpokenLanguage(movie_id=db_movie.id, iso_639_1=fetch_spoken_lang['iso_639_1'], name=fetch_spoken_lang['name'])
                    spoken_lang.save()
                    spoken_langs.append(spoken_lang)
                db_movie.spoken_languages.set(spoken_langs)

                prod_countries = []
                for fetch_prod_country in fetched_movie['production_countries']:
                    prod_country = ProductionCountries(movie_id=db_movie.id, iso_3166_1=fetch_prod_country['iso_3166_1'], name=fetch_prod_country['name'])
                    prod_country.save()
                    prod_countries.append(prod_country)
                db_movie.production_countries.set(prod_countries)

                print("Saving: %s" % db_movie)
                db_movie.save()
            except Exception as exc:
                print(exc)
                quit()


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