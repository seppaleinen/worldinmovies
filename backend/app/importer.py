import datetime, \
    requests, \
    json, \
    progressbar, \
    gzip, \
    concurrent.futures, \
    os, \
    time
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
        contents = __unzip_file()
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
            print('Persisting stuff - Having this until progressbar actually shows in docker-compose')
            for i in progressbar.progressbar(range(0, len(movies), 100), redirect_stdout=True, prefix='Saving Movie IDs: '):
                chunk = movies[i:i + 100]
                Movie.objects.bulk_create(chunk)
            return "Imported: %s movies" % len(contents)
        except Exception as e:
            print("Error: %s" % e)
            return "Exception: %s" % e
    else:
        return "Request failed with status: %s, and message: %s" % (response.status_code, response.content)


def __unzip_file():
    f = gzip.open('movies.json.gz', 'rt', encoding='utf-8')
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
        print("Request: %s" % url)
        response = requests.get(url, timeout=10)
        print("Response received")
    except requests.exceptions.Timeout:
        print("Timed out on id: %s... trying again in 10 seconds" % id)
        time.sleep(10)
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
                    print("Before json loads")
                    fetched_movie = json.loads(data)
                    print("After json loads")
                    db_movie = Movie.objects.get(pk=fetched_movie['id'])
                    db_movie.add_fetched_info(fetched_movie)
                    print("Before alt titles")
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
                    print("Before save")
                    db_movie.save()
                    print("After save")
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
            spokenLang = SpokenLanguage.objects.all().filter(iso_639_1=language['iso_639_1']).exists()
            if not spokenLang:
                SpokenLanguage(iso_639_1=language['iso_639_1'], name=language['english_name']).save()
        return "Imported: %s languages" % len(languages_from_json)
    else:
        return "Request failed with status: %s, and message: %s" % (response.status_code, response.content)
