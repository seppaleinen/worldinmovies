import datetime, \
    requests, \
    json, \
    sys, \
    gzip, \
    concurrent.futures, \
    os, \
    time, \
    csv
from django.db import transaction
from app.models import Movie, SpokenLanguage, AlternativeTitle, ProductionCountries, Genre


def base_import():
    daily = download_files()
    genres = import_genres()
    countries = import_countries()
    languages = import_languages()
    return '"daily_response":"{daily_response}","genres_response":"{genres_response}", "countries_response":"{countries_response}","languages_response":"{languages_response}"'\
        .format(daily_response=daily, genres_response=genres, countries_response=countries, languages_response=languages)


def download_files():
    yesterday = datetime.date.today() - datetime.timedelta(days=1)
    yesterday_formatted = yesterday.strftime("%m_%d_%Y")
    daily_export_url = "http://files.tmdb.org/p/exports/movie_ids_%s.json.gz" % yesterday_formatted
    response = requests.get(daily_export_url)

    if response.status_code == 200:
        print("Downloading file")
        with open('movies.json.gz', 'wb') as f:
            f.write(response.content)

        movies_to_add = []
        tmdb_movie_ids = set()
        contents = __unzip_file('movies.json.gz')
        for i in __log_progress(contents, "TMDB Daily Export"):
            try:
                data = json.loads(i)
                adult = data['adult']
                video = data['video']
                if video is False and adult is False:
                    id = data['id']
                    tmdb_movie_ids.add(id)
                    if not Movie.objects.filter(pk=id).exists():
                        movies_to_add.append(Movie(id=id, original_title=data['original_title'], popularity=data['popularity'], fetched=False))
            except Exception as e:
                print("This line fucked up: %s, because of %s" % (i, e))
        all_unfetched_movie_ids = Movie.objects.filter(fetched=False).all().values_list('id', flat=True)
        movie_ids_to_delete = (set(all_unfetched_movie_ids).difference(tmdb_movie_ids))
        print("ASD: %s" % movie_ids_to_delete)
        try:
            print('Persisting stuff - Having this until progressbar actually shows in docker-compose')
            for chunk in __chunks(movies_to_add, 100):
                Movie.objects.bulk_create(chunk)
            print("Deleting unfetched movies not in tmdb anymore")
            for movie_to_delete in movie_ids_to_delete:
                Movie.objects.get(pk=movie_to_delete).delete()
            return "Imported: %s new movies, and deleted: %s" % (len(movies_to_add), len(movie_ids_to_delete))
        except Exception as e:
            print("Error: %s" % e)
            return "Exception: %s" % e
    else:
        return "Request failed with status: %s, and message: %s" % (response.status_code, response.content)


def __unzip_file(file_name):
    f = gzip.open(file_name, 'rt', encoding='utf-8')
    file_content = f.read()
    f.close()
    return file_content.splitlines()


def __fetch_movie_with_id(id, index):
    api_key = os.getenv('TMDB_API', 'test')
    url = "https://api.themoviedb.org/3/movie/{movie_id}?" \
          "api_key={api_key}&" \
          "language=en-US&" \
          "append_to_response=alternative_titles,credits,external_ids,images,account_states".format(movie_id=id,
                                                                                                    api_key=api_key)
    try:
        response = requests.get(url, timeout=10)
    except requests.exceptions.Timeout:
        print("Timed out on id: %s... trying again in 10 seconds" % id)
        time.sleep(10)
        return __fetch_movie_with_id(id, index)
    except requests.exceptions.ConnectionError as exc:
        print("ConnectionError: %s on url: %s\n Trying again in 10 seconds..." % (exc, url))
        # traceback.print_exc()
        time.sleep(30)
        return __fetch_movie_with_id(id, index)
    if response.status_code == 200:
        return response.content
    elif response.status_code == 429 or response.status_code == 25:
        retryAfter = int(response.headers['Retry-After']) + 1
        # print("RetryAfter: %s" % retryAfter)
        time.sleep(retryAfter)
        return __fetch_movie_with_id(id, index)
    elif response.status_code == 404:
        print("Ignoring movie with id: %s as it's not in tmdb anymore - but will be attempted to fetch next time" % id)
        return None
    else:
        print("What is going on?: id:%s, status:%s, response: w%s" % (id, response.status_code, response.content))
        raise Exception("Response: %s, Content: %s" % (response.status_code, response.content))


def concurrent_stuff():
    movie_ids = Movie.objects.filter(fetched__exact=False).values_list('id', flat=True)
    length = len(movie_ids)
    with concurrent.futures.ThreadPoolExecutor(max_workers=5) as executor:
        future_to_url = (executor.submit(__fetch_movie_with_id, movie_id, index) for index, movie_id in enumerate(movie_ids))
        #bar = progressbar.ProgressBar(max_value=length, redirect_stdout=True, prefix='Fetching data from TMDB').start()
        i = 0
        for future in __log_progress(concurrent.futures.as_completed(future_to_url), "TMDB Fetch", length=length):
            try:
                data = future.result()
                if data is not None:
                    fetched_movie = json.loads(data)
                    db_movie = Movie.objects.get(pk=fetched_movie['id'])
                    db_movie.add_fetched_info(fetched_movie)
                    for fetch_alt_title in fetched_movie['alternative_titles']['titles']:
                        title = fetch_alt_title['title'] if len(fetch_alt_title['title']) < 500 else (fetch_alt_title['title'][:498] + '..')
                        alt_title = AlternativeTitle(movie_id=db_movie.id,
                                                     iso_3166_1=fetch_alt_title['iso_3166_1'],
                                                     title=title,
                                                     type=fetch_alt_title['type'])
                        alt_title.save()
                        db_movie.alternative_titles.add(alt_title)
                    for fetch_spoken_lang in fetched_movie['spoken_languages']:
                        db_movie.spoken_languages.add(SpokenLanguage.objects.get(iso_639_1=fetch_spoken_lang['iso_639_1']))
                    for fetch_prod_country in fetched_movie['production_countries']:
                        db_movie.production_countries.add(ProductionCountries.objects.get(iso_3166_1=fetch_prod_country['iso_3166_1']))
                    db_movie.save()
                i+=1
            except Exception as exc:
                print("Exception: %s" % exc)
                return "Failed with exception: %s" % exc
    return "Fetched and saved: %s movies" % length


def import_genres():
    print("Importing genres")
    api_key = os.getenv('TMDB_API', 'test')
    url = "https://api.themoviedb.org/3/genre/movie/list?api_key={api_key}&language=en-US".format(api_key=api_key)
    response = requests.get(url, stream=True)
    if response.status_code == 200:
        genres_from_json = json.loads(response.content)['genres']
        for genre in __log_progress(genres_from_json, "TMDB Genres"):
            Genre(id=genre['id'], name=genre['name']).save()
        return "Imported: %s genres" % len(genres_from_json)
    else:
        return "Request failed with status: %s, and message: %s" % (response.status_code, response.content)


def import_countries():
    print("Importing countries")
    api_key = os.getenv('TMDB_API', 'test')
    url = "https://api.themoviedb.org/3/configuration/countries?api_key={api_key}".format(api_key=api_key)
    response = requests.get(url, stream=True)
    if response.status_code == 200:
        countries_from_json = json.loads(response.content)
        for country in __log_progress(countries_from_json, "TMDB Countries"):
            if not ProductionCountries.objects.all().filter(iso_3166_1=country['iso_3166_1']).exists():
                ProductionCountries.objects.update_or_create(iso_3166_1=country['iso_3166_1'], name=country['english_name'])
        return "Imported: %s countries" % len(countries_from_json)
    else:
        return "Request failed with status: %s, and message: %s" % (response.status_code, response.content)


def import_languages():
    print("Importing languages")
    api_key = os.getenv('TMDB_API', 'test')
    url = "https://api.themoviedb.org/3/configuration/languages?api_key={api_key}".format(api_key=api_key)
    response = requests.get(url, stream=True)
    if response.status_code == 200:
        languages_from_json = json.loads(response.content)
        for language in __log_progress(languages_from_json, "TMDB Languages"):
            spoken_lang = SpokenLanguage.objects.all().filter(iso_639_1=language['iso_639_1']).exists()
            if not spoken_lang:
                SpokenLanguage(iso_639_1=language['iso_639_1'], name=language['english_name']).save()
        return "Imported: %s languages" % len(languages_from_json)
    else:
        return "Request failed with status: %s, and message: %s" % (response.status_code, response.content)


def __chunks(__list, n):
    """Yield successive n-sized chunks from list."""
    for i in range(0, len(__list), n):
        yield __list[i:i + n]


def import_imdb_ratings():
    """Data-dump of imdbs ratings of all films
       TSV Headers are: tconst, averageRating, numVotes
       and file is about 1 million rows, which takes awhile to process...
       While we only have around 450k rows in our database.
    """
    url = 'https://datasets.imdbws.com/title.ratings.tsv.gz'
    response = requests.get(url)
    with open('title.ratings.tsv.gz', 'wb') as f:
        f.write(response.content)
    if response.status_code == 200:
        counter = 0
        contents = __unzip_file('title.ratings.tsv.gz')
        reader = csv.reader(contents, delimiter='\t')
        # chunks_of_reader_maybe = __chunks(reader, 50)
        all_imdb_ids = Movie.objects.filter(fetched=True) \
            .exclude(imdb_id__isnull=True)\
            .exclude(imdb_id__exact='')\
            .all()\
            .values_list('imdb_id', flat=True)

        # Multithread this maybe?
        for row in __log_progress(list(reader), "IMDB Ratings"):
            tconst = row[0]
            if tconst in all_imdb_ids:
                try:
                    movie = Movie.objects.get(imdb_id=tconst)
                    movie.imdb_vote_average = row[1]
                    movie.imdb_vote_count = row[2]
                    movie.save()
                    counter = counter + 1
                except Movie.DoesNotExist:
                    pass
        return "Imported: %s" % counter
    else:
        return "Response: %s, %s" % (response.status_code, response.content)


def import_imdb_alt_titles():
    """titleId ordering title region language types attributes isOriginalTitle
    columns of interest: titleId, title, region
    """
    print("Dowloading title.akas.tsv.gz")
    url = 'https://datasets.imdbws.com/title.akas.tsv.gz'
    response = requests.get(url)
    with open('title.akas.tsv.gz', 'wb') as f:
        f.write(response.content)
    if response.status_code == 200:
        contents = __unzip_file('title.akas.tsv.gz')
        count = len(contents)
        csv.field_size_limit(sys.maxsize)
        reader = csv.reader(contents, delimiter='\t', quoting=csv.QUOTE_NONE)
        print("Processing IMDB Titles")
        for row in __log_progress(reader, "IMDB Titles", count):
            tconst = row[0]
            try:
                movie = Movie.objects.get(imdb_id=tconst)
                title=row[2]
                if row[3] != r'\N' and not movie.alternative_titles.filter(title=title).exists():
                    alt_title = AlternativeTitle(movie_id=movie.id,
                                        iso_3166_1=row[3],
                                        title=title,
                                        type='IMDB')
                    with transaction.atomic():
                        alt_title.save()
                        movie.alternative_titles.add(alt_title)

            except Movie.DoesNotExist:
                pass
        return 'Imported titles'
    return "Something went wrong %s" % response.content


def check_which_movies_needs_update(start_date, end_date):
    """
    :param start_date: Defaults to yesterday
    :param end_date: Defaults to today
    """
    api_key = os.getenv('TMDB_API', 'test')
    page = 1
    url = "https://api.themoviedb.org/3/movie/changes?api_key={api_key}&start_date={start_date}&end_date={end_date}&page={page}"\
        .format(api_key=api_key, start_date=start_date, end_date=end_date, page=page)
    response = requests.get(url, stream=True)
    if response.status_code == 200:
        data = json.loads(response.content)
        count = 0
        for movie in __log_progress(data['results'], "TMDB Changes"):
            if not movie['adult']:
                count += 1
                Movie.objects.filter(pk=movie['id']).update(fetched=False)
        return count


def cron_endpoint_for_checking_updateable_movies():
    start_date = (datetime.date.today() - datetime.timedelta(days=1)).strftime("%Y-%m-%d")
    end_date = (datetime.date.today()).strftime("%Y-%m-%d")
    check_which_movies_needs_update(start_date, end_date)


def __log_progress(iterable, message, length=None):
    datetime_format = "%Y-%m-%d %H:%M:%S"
    count = 1
    percentage = 0
    total_count = length if length else len(iterable)
    for i in iterable:
        temp_perc = int(100 * count / total_count)
        if percentage != temp_perc:
            percentage = temp_perc
            print("{time} - {message} data handling in progress - {percentage}%".format(time=datetime.datetime.now().strftime(datetime_format), message=message, percentage=percentage))
        count += 1
        yield i
