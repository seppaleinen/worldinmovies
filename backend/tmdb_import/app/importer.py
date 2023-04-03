import datetime, \
    requests, \
    json, \
    gzip, \
    concurrent.futures, \
    os, \
    time

from asgiref.sync import async_to_sync
from channels.layers import get_channel_layer
from mongoengine import DoesNotExist
from requests.adapters import HTTPAdapter
from urllib3.util.retry import Retry
from app.models import Movie, SpokenLanguage, Genre, ProductionCountries
from app.kafka import produce
from itertools import chain


def base_import():
    def done():
        __send_data_to_channel("Base import is done")
    return chain(download_files(), import_genres(), import_countries(), import_languages(), done())


def download_files():
    yesterday = datetime.date.today() - datetime.timedelta(days=1)
    yesterday_formatted = yesterday.strftime("%m_%d_%Y")
    daily_export_url = "http://files.tmdb.org/p/exports/movie_ids_%s.json.gz" % yesterday_formatted
    response = requests.get(daily_export_url)

    layer = get_channel_layer()
    if response.status_code == 200:
        print("Downloading file")
        yield json.dumps({"message": "Downloading %s" % daily_export_url}) + "\n"
        with open('movies.json.gz', 'wb') as f:
            f.write(response.content)
        __send_data_to_channel(layer=layer, message="Downloaded %s" % daily_export_url)

        movies_to_add = []
        tmdb_movie_ids = set()
        contents = __unzip_file('movies.json.gz')
        for b in __chunks(contents, 100):
            chunk = []
            for i in b:
                try:
                    data = json.loads(i)
                    if data['video'] is False and data['adult'] is False:
                        id = data['id']
                        tmdb_movie_ids.add(id)
                        chunk.append(id)
                except Exception as e:
                    print("This line fucked up: %s, because of %s" % (i, e))
            matches = []
            for x in Movie.objects.filter(pk__in=chunk).values_list('id'):
                matches.append(x)
            new_movies = (set(chunk).difference(matches))
            for c in new_movies:
                movies_to_add.append(Movie(id=c, fetched=False))
            __send_data_to_channel(layer=layer, message="Parsed %s out of %s movies from downloaded file" % (len(b), len(contents)))

        a = len(movies_to_add)
        __send_data_to_channel(layer=layer, message="%s movies will be persisted" % a)
        all_unfetched_movie_ids = Movie.objects.filter(fetched=False).all().values_list('id')
        movie_ids_to_delete = (set(all_unfetched_movie_ids).difference(tmdb_movie_ids))
        b = 0
        try:
            print("Persisting %s movies" % a)
            for chunk in __chunks(movies_to_add, 100):
                b += len(chunk)
                Movie.objects.insert(chunk)
                __send_data_to_channel(layer=layer, message="Persisted %s movies out of %s" % (b, a))
            print("Deleting %s unfetched movies not in tmdb anymore" % len(movie_ids_to_delete))
            c = 0
            for movie_to_delete in movie_ids_to_delete:
                Movie.objects.get(pk=movie_to_delete).delete()
                c += 1
                __send_data_to_channel(layer=layer, message="Deleted %s movies out of %s" % (c, len(movie_ids_to_delete)))
        except Exception as e:
            print("Error: %s" % e)
            __send_data_to_channel(layer=layer, message="Error persisting or deleting data: %s" % e)
    else:
        __send_data_to_channel(layer=layer, message="Error downloading files: %s - %s" % (response.status_code, response.content))


def __unzip_file(file_name):
    f = gzip.open(file_name, 'rt', encoding='utf-8')
    file_content = f.read()
    f.close()
    return file_content.splitlines()


def __fetch_movie_with_id(id, index):
    api_key = os.getenv('TMDB_API', 'test')
    url = f"https://api.themoviedb.org/3/movie/{id}?api_key={api_key}&language=en-US&append_to_response=alternative_titles,credits,external_ids,images,account_states"
    try:
        session = requests.Session()
        retry = Retry(connect=3, backoff_factor=2)
        adapter = HTTPAdapter(max_retries=retry)
        session.mount('http://', adapter)
        session.mount('https://', adapter)

        response = session.get(url, timeout=10)
    except requests.exceptions.Timeout as exc:
        print("Timed out on id: %s... trying again in 10 seconds" % id)
        print(exc)
        time.sleep(10)
        return __fetch_movie_with_id(id, index)
    except requests.exceptions.ConnectionError as exc:
        print("ConnectionError: %s on url: %s\n Trying again in 10 seconds..." % (exc, url))
        time.sleep(30)
        return __fetch_movie_with_id(id, index)
    if response.status_code == 200:
        return response.json()
    elif response.status_code == 429 or response.status_code == 25:
        retryAfter = int(response.headers['Retry-After']) + 1
        time.sleep(retryAfter)
        return __fetch_movie_with_id(id, index)
    elif response.status_code == 404:
        Movie.objects.get(pk=id).delete()
        produce('DELETED', id)
        print("Deleting movie with id: %s as it's not in tmdb anymore" % id)
        return None
    else:
        print("What is going on?: id:%s, status:%s, response: w%s" % (id, response.status_code, response.content))
        raise Exception("Response: %s, Content: %s" % (response.status_code, response.content))


def fetch_tmdb_data_concurrently():
    movie_ids = Movie.objects.filter(fetched__exact=False).values_list('id')
    length = len(movie_ids)
    print("Starting import of %s unfetched movies" % length)
    with concurrent.futures.ThreadPoolExecutor(max_workers=5) as executor:
        future_to_url = (executor.submit(__fetch_movie_with_id, movie_id, index) for index, movie_id in enumerate(movie_ids))
        i = 0
        for future in __log_progress(concurrent.futures.as_completed(future_to_url), "TMDB Fetch", length=length):
            try:
                data = future.result()
                if data is not None:
                    db_movie = Movie.objects.get(pk=data['id'])
                    new_or_update = 'UPDATE' if db_movie.data else 'NEW'
                    db_movie.add_fetched_info(data)
                    db_movie.save()
                    produce(new_or_update, data['id'])
                i += 1
            except Exception as exc:
                print("Exception: %s" % exc)


def import_genres():
    print("Importing genres")
    api_key = os.getenv('TMDB_API', 'test')
    url = f"https://api.themoviedb.org/3/genre/movie/list?api_key={api_key}&language=en-US"
    response = requests.get(url, stream=True)
    layer = get_channel_layer()
    if response.status_code == 200:
        genres_from_json = json.loads(response.content)['genres']
        length = len(genres_from_json)
        i = 0
        for genre in __log_progress(genres_from_json, "TMDB Genres"):
            Genre(id=genre['id'], name=genre['name']).save()
            i += 1
            __send_data_to_channel(layer=layer, message="Fetched %s genres out of %s" % (i, length))
    else:
        __send_data_to_channel(layer=layer, message="Error importing countries: %s - %s" % (response.status_code, response.content))


def import_countries():
    print("Importing countries")
    api_key = os.getenv('TMDB_API', 'test')
    url = f"https://api.themoviedb.org/3/configuration/countries?api_key={api_key}"
    response = requests.get(url, stream=True)
    layer = get_channel_layer()
    if response.status_code == 200:
        countries_from_json = json.loads(response.content)
        length = len(countries_from_json)
        i = 0
        for country in __log_progress(countries_from_json, "TMDB Countries"):
            if not ProductionCountries.objects.all().filter(iso_3166_1=country['iso_3166_1']):
                ProductionCountries(iso_3166_1=country['iso_3166_1'], name=country['english_name']).save()
            i += 1
            __send_data_to_channel(layer=layer, message="Fetched %s countries out of %s" % (i, length))
    else:
        __send_data_to_channel(layer=layer, message="Error importing countries: %s - %s" % (response.status_code, response.content))


def import_languages():
    print("Importing languages")
    api_key = os.getenv('TMDB_API', 'test')
    url = f"https://api.themoviedb.org/3/configuration/languages?api_key={api_key}"
    response = requests.get(url, stream=True)
    layer = get_channel_layer()
    if response.status_code == 200:
        languages_from_json = json.loads(response.content)
        length = len(languages_from_json)
        i = 0
        for language in __log_progress(languages_from_json, "TMDB Languages"):
            i += 1
            __send_data_to_channel(layer=layer, message=f"Fetched {i} languages out of {length}")
            spoken_lang = SpokenLanguage.objects.all().filter(iso_639_1=language['iso_639_1'])
            if not spoken_lang:
                SpokenLanguage(iso_639_1=language['iso_639_1'], name=language['english_name']).save()
    else:
        print("response: %s" % response)
        __send_data_to_channel("Error importing languages: %s - %s" % (response.status_code, response.content))


def __chunks(__list, n):
    """Yield successive n-sized chunks from list."""
    for i in range(0, len(__list), n):
        yield __list[i:i + n]


def check_which_movies_needs_update(start_date, end_date):
    """
    :param start_date: Defaults to yesterday
    :param end_date: Defaults to today
    """
    api_key = os.getenv('TMDB_API', 'test')
    page = 1
    url = f"https://api.themoviedb.org/3/movie/changes?api_key={api_key}&start_date={start_date}&end_date={end_date}&page={page}"
    response = requests.get(url, stream=True)
    layer = get_channel_layer()
    if response.status_code == 200:
        data = json.loads(response.content)
        for movie in __log_progress(data['results'], "TMDB Changes"):
            if not movie['adult']:
                try:
                    db = Movie.objects.get(pk=movie['id'])
                    if db.fetched_date.strftime("%Y-%m-%d") < end_date:
                        Movie.objects.filter(pk=movie['id']).update(fetched=False)
                        __send_data_to_channel("Scheduling movieId:%s for update" % movie['id'], layer=layer)
                    else:
                        __send_data_to_channel("MovieId: %s has already been scheduled for update" % movie['id'])
                except DoesNotExist:
                    Movie(id=movie['id'], fetched=False).save()
    else:
        print("Response: %s:%s" % (response.status_code, response.content))


def cron_endpoint_for_checking_updateable_movies():
    start_date = (datetime.date.today() - datetime.timedelta(days=1)).strftime("%Y-%m-%d")
    end_date = (datetime.date.today()).strftime("%Y-%m-%d")
    check_which_movies_needs_update(start_date, end_date)


def __log_progress(iterable, message, length=None):
    datetime_format = "%Y-%m-%d %H:%M:%S"
    count = 1
    percentage = 0
    total_count = length if length else len(iterable)
    layer = get_channel_layer()
    for i in iterable:
        temp_perc = int(100 * count / total_count)
        if percentage != temp_perc:
            percentage = temp_perc
            __send_data_to_channel(layer=layer, message=f"{message} data handling in progress - {percentage}%")
            print(f"{datetime.datetime.now().strftime(datetime_format)} - {message} data handling in progress - {percentage}%")
        count += 1
        yield i


def __send_data_to_channel(message, layer=get_channel_layer()):
    async_to_sync(layer.group_send)('group', {"type": "events", "message": json.dumps(message)})
