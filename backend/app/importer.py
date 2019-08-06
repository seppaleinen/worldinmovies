import datetime, \
    requests, \
    json, \
    progressbar, \
    gzip, \
    concurrent.futures, \
    os, \
    time, \
    csv
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
    response = requests.get(daily_export_url, stream=True)

    if response.status_code == 200:
        print("Downloading file")
        with open('movies.json.gz', 'wb') as f:
            f.write(response.content)

        movies = []
        movies_to_update = []
        movie_ids_to_delete = []
        contents = __unzip_file('movies.json.gz')
        # Map<ID, Movie>
        # List of movies to create
        # List of movies to update
        # List of movies to delete (unfetched movies, not in tmdb anymore)
        # bulk_create(movies_to_create)
        # for movie in movies_to_update: movie.save()
        # Movie.objects.filter(pk__in=movies_to_delete).delete()
        for i in contents:
            try:
                data = json.loads(i)
                id = data['id']
                adult = data['adult']
                original_title = data['original_title']
                video = data['video']
                popularity = data['popularity']
                if video is False and adult is False:
                    try:
                        movie = Movie.objects.get(pk=id)
                        # If data has changed. then update
                        if movie.popularity != popularity or movie.original_title != original_title:
                            movie.original_title = original_title
                            movie.popularity = popularity
                            movies_to_update.append(movie)
                    except Exception as exc:
                        # Film not yet imported
                        movies.append(Movie(id=id, original_title=original_title, popularity=popularity, fetched=False))
            except Exception as e:
                print("This line fucked up: %s, because of %s" % (i, e))
        all_unfetched_movie_ids = Movie.objects.filter(fetched=False).all().values_list('id', flat=True)
        for potential_id_to_delete in all_unfetched_movie_ids:
            if any(movie.id == potential_id_to_delete for movie in movies):
                pass
            elif any(movie.id == potential_id_to_delete for movie in movies_to_update):
                pass
            else:
                movie_ids_to_delete.append(potential_id_to_delete)
        # Creates all, but crashes as soon as you try to update the list
        try:
            print('Persisting stuff - Having this until progressbar actually shows in docker-compose')
            for chunk in __chunks(movies, 100):
                Movie.objects.bulk_create(chunk)
            print("Updating movies")
            for movie_to_update in movies_to_update:
                movie_to_update.save()
            print("Deleting movies")
            for movie_to_delete in movie_ids_to_delete:
                Movie.objects.get(pk=movie_to_delete).delete()
            return "Imported: %s new movies, updated: %s, and deleted: %s" % (len(movies), len(movies_to_update), len(movie_ids_to_delete))
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
        bar = progressbar.ProgressBar(max_value=length, redirect_stdout=True, prefix='Fetching data from TMDB').start()
        i = 0
        for future in concurrent.futures.as_completed(future_to_url):
            try:
                data = future.result()
                if data is not None:
                    fetched_movie = json.loads(data)
                    db_movie = Movie.objects.get(pk=fetched_movie['id'])
                    db_movie.add_fetched_info(fetched_movie)
                    for fetch_alt_title in fetched_movie['alternative_titles']['titles']:
                        alt_title = AlternativeTitle(movie_id=db_movie.id,
                                                     iso_3166_1=fetch_alt_title['iso_3166_1'],
                                                     title=fetch_alt_title['title'],
                                                     type=fetch_alt_title['type'])
                        alt_title.save()
                        db_movie.alternative_titles.add(alt_title)
                    for fetch_spoken_lang in fetched_movie['spoken_languages']:
                        db_movie.spoken_languages.add(SpokenLanguage.objects.get(iso_639_1=fetch_spoken_lang['iso_639_1']))
                    for fetch_prod_country in fetched_movie['production_countries']:
                        db_movie.production_countries.add(ProductionCountries.objects.get(iso_3166_1=fetch_prod_country['iso_3166_1']))
                    db_movie.save()
                i+=1
                bar.update(i)
            except Exception as exc:
                print("Exception: %s" % exc)
                return "Failed with exception: %s" % exc
        bar.finish()
    return "Fetched and saved: %s movies" % length


def import_genres():
    print("Importing genres")
    api_key = os.getenv('TMDB_API', 'test')
    url = "https://api.themoviedb.org/3/genre/movie/list?api_key={api_key}&language=en-US".format(api_key=api_key)
    response = requests.get(url, stream=True)
    if response.status_code == 200:
        genres_from_json = json.loads(response.content)['genres']
        for genre in genres_from_json:
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
        for country in countries_from_json:
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
        for language in languages_from_json:
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
        all_imdb_ids = Movie.objects.filter(fetched=True).all().values_list('imdb_id', flat=True)
        # Multithread this maybe?
        for row in reader:
            tconst = row[0]
            if tconst in all_imdb_ids:
                try:
                    # map rows into map<tconst, {'vote_average','vote_count}
                    # create sublists up to 100
                    # objects.filter(imdb_id__in=list_of_tconst
                    # Entry.objects.bulk_update(objects, batch_size=50)
                    movie = Movie.objects.get(imdb_id=tconst)
                    movie.imdb_vote_average = row[1]
                    movie.imdb_vote_count = row[2]
                    movie.save()
                    counter = counter + 1
                except Exception:
                    pass
            if counter % 100 == 0:
                print("Persisted: %s imdb ratings" % counter)
        return "Imported: %s" % counter
    else:
        return "Response: %s, %s" % (response.status_code, response.content)


def check_which_movies_needs_update():
    api_key = os.getenv('TMDB_API', 'test')
    yesterday = datetime.date.today() - datetime.timedelta(days=1)
    start_date=yesterday.strftime("%Y-%m-%d")
    end_date=datetime.date.today().strftime("%Y-%m-%d")
    page=1
    url = "https://api.themoviedb.org/3/movie/changes?api_key={api_key}&start_date={start_date}&end_date={end_date}&page={page}"\
        .format(api_key=api_key, start_date=start_date, end_date=end_date, page=page)
    response = requests.get(url, stream=True)
    if response.status_code == 200:
        print("Hurra")