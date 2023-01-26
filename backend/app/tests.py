import datetime, os, responses, json, io, gzip

from django.test import TransactionTestCase
from django.db import transaction
from app.models import Movie, Genre, SpokenLanguage, ProductionCountries, AlternativeTitle

BASE_DIR = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))


class SuperClass(TransactionTestCase):
    def setUp(self):
        self._environ = dict(os.environ)
        os.environ['TMDB_API'] = 'test'
        SpokenLanguage(iso_639_1='en', name='English').save()
        SpokenLanguage(iso_639_1='es', name='Spanish').save()
        ProductionCountries(iso_3166_1='US', name='United States of america').save()
        ProductionCountries(iso_3166_1='AU', name='Australia').save()
        ProductionCountries(iso_3166_1='GB', name='Great Britain').save()

    def tearDown(self):
        os.environ.clear()
        os.environ.update(self._environ)


def __gzip_string(string):
    out = io.StringIO()
    with gzip.GzipFile(fileobj=out, mode="w") as f:
        f.write(string)
    return out.getvalue()


# Create your tests here.
class ImportTests(SuperClass):
    @responses.activate
    def test_daily_file_import(self):
        yesterday = datetime.date.today() - datetime.timedelta(days=1)
        yesterday_formatted = yesterday.strftime("%m_%d_%Y")
        daily_export_url = "http://files.tmdb.org/p/exports/movie_ids_%s.json.gz" % yesterday_formatted
        with open('testdata/movie_ids.json.gz', 'rb') as img1:
            responses.add(
                responses.GET, daily_export_url,
                body=img1.read(), status=200,
                content_type='application/javascript',
                stream=True
            )

        response = self.client.get('/import/tmdb/daily')
        self.assertEqual(response.status_code, 200)
        self.assertEqual(response.getvalue(), b'{"message": "Downloading http://files.tmdb.org/p/exports/movie_ids_01_25_2023.json.gz"}\n{"message": "0 movies will be persisted"}\n')

    @responses.activate
    def test_daily_file_import_delete(self):
        Movie(id=604, original_title='Worst movie ever', popularity=0, fetched=False).save()
        Movie(id=603, original_title='Second worst movie ever', popularity=0, fetched=True).save()

        yesterday = datetime.date.today() - datetime.timedelta(days=1)
        yesterday_formatted = yesterday.strftime("%m_%d_%Y")
        daily_export_url = "http://files.tmdb.org/p/exports/movie_ids_%s.json.gz" % yesterday_formatted
        with open('testdata/movie_ids.json.gz', 'rb') as img1:
            responses.add(
                responses.GET, daily_export_url,
                body=img1.read(), status=200,
                content_type='application/javascript',
                stream=True
            )

        response = self.client.get('/import/tmdb/daily')
        self.assertEqual(response.status_code, 200)
        self.assertEqual(response.getvalue(), b'{"message": "Downloading http://files.tmdb.org/p/exports/movie_ids_01_25_2023.json.gz"}\n{"message": "0 movies will be persisted"}\n{"message": "Deleted 1 movies out of 1"}\n')
        self.assertFalse(Movie.objects.filter(pk=604).exists())

    @responses.activate
    def test_fetch_3_unfetched_out_of_4(self):
        Movie(id=601, original_title="title1", popularity=36.213, fetched=False).save()
        Movie(id=602, original_title="title2", popularity=36.213, fetched=False).save()
        Movie(id=603, original_title="title3", popularity=36.213, fetched=False).save()
        Movie(id=604, original_title="title4", popularity=36.213, fetched=True).save()

        for i in [601, 602, 603]:
            url = "https://api.themoviedb.org/3/movie/{movie_id}?" \
                  "api_key={api_key}&" \
                  "language=en-US&" \
                  "append_to_response=alternative_titles,credits,external_ids,images,account_states".format(
                api_key='test', movie_id=i)
            with open("testdata/%s.json" % i, 'rt') as img1:
                responses.add(responses.GET,
                              url,
                              body=img1.read(), status=200,
                              content_type='application/javascript',
                              stream=True
                              )

        response = self.client.get('/import/tmdb/data')
        self.assertEqual(response.status_code, 200)
        self.assertEqual(response.getvalue(), b'{"fetched": 0, "total": 3}\n{"fetched": 1, "total": 3}\n{"fetched": 2, "total": 3}\n')

    @responses.activate
    def test_fetch_of_failing_movie_avatar(self):
        movie = Movie(id=19995, original_title='Avatar', popularity=36.213, fetched=False)
        movie.save()

        url = "https://api.themoviedb.org/3/movie/{movie_id}?" \
              "api_key={api_key}&" \
              "language=en-US&" \
              "append_to_response=alternative_titles,credits,external_ids,images,account_states".format(api_key='test',
                                                                                                        movie_id=movie.id)
        with open("testdata/failing_movie.json", 'rt') as img1:
            responses.add(responses.GET,
                          url,
                          body=img1.read(), status=200,
                          content_type='application/javascript',
                          stream=True
                          )

        response = self.client.get('/import/tmdb/data')
        self.assertEqual(response.status_code, 200)
        self.assertEqual(response.getvalue(), b'{"fetched": 0, "total": 1}\n')

    @responses.activate
    def test_fetch_id_thats_removed_from_tmdb(self):
        movie = Movie(id=123, original_title='removed_movie', popularity=36.213, fetched=False)
        movie.save()

        url = "https://api.themoviedb.org/3/movie/{movie_id}?" \
              "api_key={api_key}&" \
              "language=en-US&" \
              "append_to_response=alternative_titles,credits,external_ids,images,account_states".format(api_key='test',
                                                                                                        movie_id=movie.id)
        responses.add(responses.GET,
                      url,
                      json={"status_code": 34, "status_message": "The resource you requested could not be found."},
                      status=404,
                      content_type='application/javascript',
                      stream=True
                      )

        response = self.client.get('/import/tmdb/data')
        content = response.getvalue().decode('utf8')
        self.assertEqual(response.status_code, 200)
        self.assertTrue('{"deleted": true}\n' in content)
        self.assertFalse(Movie.objects.filter(pk=123))

    @responses.activate
    def test_fetch_only_movies_marked_as_fetched_false(self):
        to_be_fetched = Movie(id=601, original_title='to_be_fetched', popularity=36.213, fetched=False)
        to_be_fetched.save()
        already_fetched = Movie(id=602, original_title='already_fetched', popularity=36.213, fetched=True)
        already_fetched.save()

        url = "https://api.themoviedb.org/3/movie/{movie_id}?" \
              "api_key={api_key}&" \
              "language=en-US&" \
              "append_to_response=alternative_titles,credits,external_ids,images,account_states".format(api_key='test',
                                                                                                        movie_id=to_be_fetched.id)
        with open("testdata/601.json", 'rt') as img1:
            responses.add(responses.GET,
                          url,
                          body=img1.read(), status=200,
                          content_type='application/javascript',
                          stream=True
                          )

        response = self.client.get('/import/tmdb/data')
        content = response.getvalue()
        self.assertEqual(len(responses.calls), 1)
        self.assertEqual(responses.calls[0].request.url, url)
        self.assertEqual(response.status_code, 200)
        self.assertEqual(content, b'{"fetched": 0, "total": 1}\n')
        self.assertTrue(Movie.objects.get(pk=to_be_fetched.id).fetched)


class StatusTests(SuperClass):
    def test_status_0_fetched_out_of_3(self):
        Movie(id=1, original_title="title1", popularity=36.213, fetched=False).save()
        Movie(id=2, original_title="title2", popularity=36.213, fetched=False).save()
        Movie(id=3, original_title="title3", popularity=36.213, fetched=False).save()

        response = self.client.get('/status')
        self.assertEqual(response.status_code, 200)
        self.assertEqual(response.content, b'{"fetched": 0, "total": 3, "percentage_done": 0}')

    def test_status_1_fetched_out_of_3(self):
        Movie(id=1, original_title="title1", popularity=36.213, fetched=True).save()
        Movie(id=2, original_title="title2", popularity=36.213, fetched=False).save()
        Movie(id=3, original_title="title3", popularity=36.213, fetched=False).save()

        response = self.client.get('/status')
        self.assertEqual(response.status_code, 200)
        self.assertEqual(response.content, b'{"fetched": 1, "total": 3, "percentage_done": 33}')

    def test_status_3_fetched_out_of_3(self):
        Movie(id=1, original_title="title1", popularity=36.213, fetched=True).save()
        Movie(id=2, original_title="title2", popularity=36.213, fetched=True).save()
        Movie(id=3, original_title="title3", popularity=36.213, fetched=True).save()

        response = self.client.get('/status')
        self.assertEqual(response.status_code, 200)
        self.assertEqual(response.content, b'{"fetched": 3, "total": 3, "percentage_done": 100}')

    def test_status_0_fetched_out_of_0(self):
        response = self.client.get('/status')
        self.assertEqual(response.status_code, 200)
        self.assertEqual(response.content, b'{"fetched": 0, "total": 0, "percentage_done": 0}')


class FetchBaseData(SuperClass):
    @responses.activate
    def test_fetch_countries(self):
        url = "https://api.themoviedb.org/3/configuration/countries?api_key=test"
        with open("testdata/countries.json", 'rt') as img1:
            responses.add(responses.GET,
                          url,
                          body=img1.read(), status=200,
                          content_type='application/javascript',
                          stream=True
                          )

        response = self.client.get('/import/tmdb/countries')
        content = response.getvalue()
        self.assertEqual(response.status_code, 200)
        self.assertEqual(len(responses.calls), 1)
        self.assertEqual(responses.calls[0].request.url, url)
        self.assertTrue('{"fetched": 1, "total": 247}' in content.decode('utf8'))
        self.assertTrue('{"fetched": 247, "total": 247}' in content.decode('utf8'))
        self.assertEqual(ProductionCountries.objects.count(), 247)

    @responses.activate
    def test_fetch_languages(self):
        url = "https://api.themoviedb.org/3/configuration/languages?api_key=test"
        with open("testdata/languages.json", 'rt') as img1:
            responses.add(responses.GET,
                          url,
                          body=img1.read(), status=200,
                          content_type='application/javascript',
                          stream=True
                          )

        response = self.client.get('/import/tmdb/languages')
        content = response.getvalue()
        self.maxDiff = None
        self.assertEqual(response.status_code, 200)
        self.assertEqual(len(responses.calls), 1)
        self.assertEqual(responses.calls[0].request.url, url)
        self.assertTrue('{"fetched": 2, "total": 187}' in content.decode('utf8'))
        self.assertTrue('{"fetched": 187, "total": 187}' in content.decode('utf8'))
        self.assertEqual(SpokenLanguage.objects.count(), 187)

    @responses.activate
    def test_fetch_genres(self):
        url = "https://api.themoviedb.org/3/genre/movie/list?api_key=test&language=en-US"
        with open("testdata/genres.json", 'rt') as img1:
            responses.add(responses.GET,
                          url,
                          body=img1.read(), status=200,
                          content_type='application/javascript',
                          stream=True
                          )

        response = self.client.get('/import/tmdb/genres')
        content = response.getvalue()
        self.assertEqual(response.status_code, 200)
        self.assertEqual(len(responses.calls), 1)
        self.assertEqual(responses.calls[0].request.url, url)
        self.assertTrue('{"fetched": 1, "total": 19}' in content.decode('utf8'))
        self.assertTrue('{"fetched": 19, "total": 19}' in content.decode('utf8'))
        self.assertEqual(Genre.objects.count(), 19)

    @responses.activate
    def test_base_fetch(self):
        genres_url = "https://api.themoviedb.org/3/genre/movie/list?api_key=test&language=en-US"
        with open("testdata/genres.json", 'rt') as img1:
            responses.add(responses.GET,
                          genres_url,
                          body=img1.read(), status=200,
                          content_type='application/javascript',
                          stream=True
                          )
        languages_url = "https://api.themoviedb.org/3/configuration/languages?api_key=test"
        with open("testdata/languages.json", 'rt') as img1:
            responses.add(responses.GET,
                          languages_url,
                          body=img1.read(), status=200,
                          content_type='application/javascript',
                          stream=True
                          )
        countries_url = "https://api.themoviedb.org/3/configuration/countries?api_key=test"
        with open("testdata/countries.json", 'rt') as img1:
            responses.add(responses.GET,
                          countries_url,
                          body=img1.read(), status=200,
                          content_type='application/javascript',
                          stream=True
                          )
        yesterday = datetime.date.today() - datetime.timedelta(days=1)
        yesterday_formatted = yesterday.strftime("%m_%d_%Y")
        daily_url = "http://files.tmdb.org/p/exports/movie_ids_%s.json.gz" % yesterday_formatted
        with open('testdata/movie_ids.json.gz', 'rb') as img1:
            responses.add(responses.GET,
                          daily_url,
                          body=img1.read(), status=200,
                          content_type='application/javascript',
                          stream=True
                          )

        response = self.client.get('/import/base')
        content = response.getvalue().decode('utf8')
        self.maxDiff = None
        self.assertEqual(response.status_code, 200)
        self.assertEqual(len(responses.calls), 4)
        self.assertEqual(responses.calls[0].request.url, daily_url)
        self.assertEqual(responses.calls[1].request.url, genres_url)
        self.assertEqual(responses.calls[2].request.url, countries_url)
        self.assertEqual(responses.calls[3].request.url, languages_url)
        self.assertTrue('{"message": "Downloading http://files.tmdb.org/p/exports/movie_ids_01_25_2023.json.gz"}', content)
        self.assertTrue('{"message": "0 movies will be persisted"}', content)
        self.assertTrue('{"fetched": 1, "total": 19}', content)
        self.assertTrue('{"fetched": 1, "total": 247}', content)
        self.assertTrue('{"fetched": 1, "total": 187}', content)
        self.assertTrue('{"message": "Done"}', content)
        self.assertEqual(Movie.objects.count(), 0)
        self.assertEqual(ProductionCountries.objects.count(), 247)
        self.assertEqual(SpokenLanguage.objects.count(), 187)
        self.assertEqual(Genre.objects.count(), 19)


class ViewData(SuperClass):
    # Doesn't work yet with sqlite3
    def _ignore_test_fetch_best_movies_per_country(self):
        for country_code in ['US', 'AU', 'GB']:
            country = ProductionCountries.objects.get(iso_3166_1=country_code)
            for ratings in range(0, 20):
                with transaction.atomic():
                    movie = Movie(id=ratings,
                                  original_title="title%s" % ratings,
                                  popularity=36.213,
                                  fetched=True,
                                  vote_average=ratings)
                    movie.save()
                    movie.production_countries.add(country)

        response = self.client.get('/view/best')
        self.assertEqual(response.status_code, 200)
        self.assertEqual(response.content, b'Stuff happening!')


class MapImdbRatingsToWorldinMovies(SuperClass):
    def test_convert_imdb_ratings(self):
        flies = Movie(id=1,
                      original_title="Lord of the Flies",
                      popularity=0.0,
                      fetched=True,
                      imdb_id='tt0100054',
                      vote_average=0)
        flies.save()
        flies.production_countries.add(ProductionCountries.objects.get(iso_3166_1='US'))

        misery = Movie(id=2,
                       original_title="Misery",
                       popularity=0.0,
                       fetched=True,
                       imdb_id='tt0100157',
                       vote_average=0)
        misery.save()
        misery.production_countries.add(ProductionCountries.objects.get(iso_3166_1='AU'))

        files = {
            "file": open('testdata/mini_ratings.csv', 'r', encoding='cp1252')
        }

        response = self.client.post('/ratings', data=files, format='multipart/form-data')
        self.assertEqual(response.status_code, 200)
        self.assertEqual(response.content.decode('utf-8'),
                         '{"found": {"US": [{"title": "Lord of the Flies", "country_code": "US", "year": "1990", "imdb_id": "tt0100054", "personal_rating": "7", "rating": "6.4"}], "AU": [{"title": "Misery", "country_code": "AU", "year": "1990", "imdb_id": "tt0100157", "personal_rating": "8", "rating": "7.8"}]}, "not_found": []}')

    def test_convert_imdb_ratings_not_found(self):
        files = {
            "file": open('testdata/mini_ratings.csv', 'r', encoding='cp1252')
        }

        response = self.client.post('/ratings', data=files, format='multipart/form-data')
        self.assertEqual(response.status_code, 200)
        self.assertEqual(response.content.decode('utf-8'),
                         '{"found": {}, "not_found": [{"title": "Lord of the Flies", "year": "1990", "imdb_id": "tt0100054"}, {"title": "Misery", "year": "1990", "imdb_id": "tt0100157"}]}')

    def _test_convert_imdb_ratings_full_dataset(self):
        files = {
            "file": open('testdata/ratings.csv', 'r', encoding='cp1252')
        }

        response = self.client.post('/ratings', data=files, format='multipart/form-data')
        self.assertEqual(response.status_code, 200)


class ViewBestFromCountry(SuperClass):
    # TODO Something weird happening here
    def __ignore_test_fetch_top_from_country(self):
        i = 0
        for country_code in ['US', 'AU', 'GB']:
            country = ProductionCountries.objects.get(iso_3166_1=country_code)
            for ratings in range(0, 20):
                with transaction.atomic():
                    movie = Movie(id=i,
                                  original_title="titlö%s" % ratings,
                                  popularity=36.213,
                                  fetched=True,
                                  poster_path="/path%s" % ratings,
                                  imdb_id="imdb_id%s" % ratings,
                                  release_date="2019-01-%s" % ratings,
                                  vote_average=ratings + 0.09,
                                  vote_count=20)
                    i = i + 1
                    movie.save()
                    movie.production_countries.add(country)

        response = self.client.get('/view/best/US')
        json_response = response.content.decode('utf-8')
        self.assertEqual(response.status_code, 200)
        # self.assertJSONEqual(json_response[0], {"imdb_id": "imdb_id19", "original_title": "title19", "release_date": "2019-01-19", "poster_path": "/path19", "vote_average": 19})
        # self.assertJSONEqual(json_response[1], {"imdb_id": "imdb_id18", "original_title": "title18", "release_date": "2019-01-18", "poster_path": "/path18", "vote_average": 18})
        # self.assertJSONEqual(json_response[2], {"imdb_id": "imdb_id17", "original_title": "title17", "release_date": "2019-01-17", "poster_path": "/path17", "vote_average": 17})
        # self.assertJSONEqual(json_response[3], {"imdb_id": "imdb_id16", "original_title": "title16", "release_date": "2019-01-16", "poster_path": "/path16", "vote_average": 16})
        # self.assertJSONEqual(json_response[4], {"imdb_id": "imdb_id15", "original_title": "title15", "release_date": "2019-01-15", "poster_path": "/path15", "vote_average": 15})
        # self.assertJSONEqual(json_response[5], {"imdb_id": "imdb_id14", "original_title": "title14", "release_date": "2019-01-14", "poster_path": "/path14", "vote_average": 14})
        # self.assertJSONEqual(json_response[6], {"imdb_id": "imdb_id13", "original_title": "title13", "release_date": "2019-01-13", "poster_path": "/path13", "vote_average": 13})
        # self.assertJSONEqual(json_response[7], {"imdb_id": "imdb_id12", "original_title": "title12", "release_date": "2019-01-12", "poster_path": "/path12", "vote_average": 12})
        # self.assertJSONEqual(json_response[8], {"imdb_id": "imdb_id11", "original_title": "title11", "release_date": "2019-01-11", "poster_path": "/path11", "vote_average": 11})
        # self.assertJSONEqual(json_response[9], {"imdb_id": "imdb_id10", "original_title": "title10", "release_date": "2019-01-10", "poster_path": "/path10", "vote_average": 10})
        self.assertJSONEqual(json_response,
                             [
                                 {"imdb_id": "imdb_id19", "original_title": "titlö19", "release_date": "2019-01-19",
                                  "poster_path": "/path19", "vote_average": 19.1, "vote_count": 20},
                                 {"imdb_id": "imdb_id18", "original_title": "titlö18", "release_date": "2019-01-18",
                                  "poster_path": "/path18", "vote_average": 18.1, "vote_count": 20},
                                 {"imdb_id": "imdb_id17", "original_title": "titlö17", "release_date": "2019-01-17",
                                  "poster_path": "/path17", "vote_average": 17.1, "vote_count": 20},
                                 {"imdb_id": "imdb_id16", "original_title": "titlö16", "release_date": "2019-01-16",
                                  "poster_path": "/path16", "vote_average": 16.1, "vote_count": 20},
                                 {"imdb_id": "imdb_id15", "original_title": "titlö15", "release_date": "2019-01-15",
                                  "poster_path": "/path15", "vote_average": 15.1, "vote_count": 20},
                                 {"imdb_id": "imdb_id14", "original_title": "titlö14", "release_date": "2019-01-14",
                                  "poster_path": "/path14", "vote_average": 14.1, "vote_count": 20},
                                 {"imdb_id": "imdb_id13", "original_title": "titlö13", "release_date": "2019-01-13",
                                  "poster_path": "/path13", "vote_average": 13.1, "vote_count": 20},
                                 {"imdb_id": "imdb_id12", "original_title": "titlö12", "release_date": "2019-01-12",
                                  "poster_path": "/path12", "vote_average": 12.1, "vote_count": 20},
                                 {"imdb_id": "imdb_id11", "original_title": "titlö11", "release_date": "2019-01-11",
                                  "poster_path": "/path11", "vote_average": 11.1, "vote_count": 20},
                                 {"imdb_id": "imdb_id10", "original_title": "titlö10", "release_date": "2019-01-10",
                                  "poster_path": "/path10", "vote_average": 10.1, "vote_count": 20}
                             ])


class ImportImdbData(SuperClass):
    @responses.activate
    def test_import_imdb_data(self):
        Movie(id=19995, original_title='Avatar', popularity=36.213, fetched=True, imdb_id='tt0000001').save()
        Movie(id=1, original_title='1', popularity=36.213, fetched=True, imdb_id='').save()
        Movie(id=1, original_title='1', popularity=36.213, fetched=True, imdb_id=None).save()

        url = "https://datasets.imdbws.com/title.ratings.tsv.gz"
        with open("testdata/mini_ratings.tsv.gz", 'rb') as img1:
            responses.add(responses.GET,
                          url,
                          body=img1.read(), status=200,
                          content_type='binary/octet-stream',
                          stream=True
                          )

        response = self.client.get('/import/imdb/ratings')
        content = response.getvalue()
        self.assertEqual(response.status_code, 200)
        self.assertEqual(len(responses.calls), 1)
        self.assertEqual(responses.calls[0].request.url, url)
        self.assertEqual(content.decode('utf-8'), '{"message": "Downloading file: https://datasets.imdbws.com/title.ratings.tsv.gz"}\n{"fetched": 1, "total": 1}\n')

    @responses.activate
    def test_import_imdb_data_no_match(self):
        url = "https://datasets.imdbws.com/title.ratings.tsv.gz"
        with open("testdata/mini_ratings.tsv.gz", 'rb') as img1:
            responses.add(responses.GET,
                          url,
                          body=img1.read(), status=200,
                          content_type='binary/octet-stream',
                          stream=True
                          )

        response = self.client.get('/import/imdb/ratings')
        content = response.getvalue()
        self.assertEqual(response.status_code, 200)
        self.assertEqual(len(responses.calls), 1)
        self.assertEqual(responses.calls[0].request.url, url)
        self.assertEqual(content.decode('utf-8'), '{"message": "Downloading file: https://datasets.imdbws.com/title.ratings.tsv.gz"}\n')


class CheckTMDBForChanges(SuperClass):
    @responses.activate
    def test_1(self):
        Movie(id=1, original_title='Avatar', popularity=36.213, fetched=True, imdb_id='tt0000001').save()
        Movie(id=2, original_title='Avatar', popularity=36.213, fetched=True, imdb_id='tt0000002').save()

        url = "https://api.themoviedb.org/3/movie/changes?api_key=test&start_date=2019-01-01&end_date=2019-01-02&page=1"
        body = '{"results": [{"id": 1,"adult": false},{"id": 2,"adult": false},{"id": 3,"adult": true}],"page": 1,"total_pages": 1,"total_results": 1}'
        responses.add(responses.GET, url, body=body, status=200, stream=True)

        response = self.client.get('/import/tmdb/changes?start_date=2019-01-01&end_date=2019-01-02')
        self.assertEqual(response.status_code, 200)
        self.assertEqual('{"movie_id": 1}\n{"movie_id": 2}\n', response.getvalue().decode('utf-8'))
        self.assertEqual(len(responses.calls), 1)
        self.assertEqual(responses.calls[0].request.url, url)
        self.assertEqual(False, Movie.objects.get(pk=1).fetched)
        self.assertEqual(False, Movie.objects.get(pk=2).fetched)


class ImportIMDBTitles(SuperClass):
    @responses.activate
    def test_normal(self):
        movie = Movie(id=1, original_title='orig_title', popularity=123.0, fetched=True, imdb_id='tt0000001')
        movie.save()
        url = "https://datasets.imdbws.com/title.akas.tsv.gz"
        string = "titleId	ordering	title	region	language	types	attributes	isOriginalTitle\n"
        string += "tt0000001	1	Carmencita - spanyol tánc	HU	\\N	imdbDisplay	\\N	0\n"
        string += "tt0000001	2	Καρμενσίτα	GR	\\N	\\N	\\N	0\n"
        string += "tt0000001	3	Карменсита	RU	\\N	\\N	\\N	0"

        out = gzip.compress(bytes(string, 'utf-8'))

        responses.add(responses.GET,
                      url,
                      body=out, status=200,
                      content_type='binary/octet-stream',
                      stream=True
                      )

        response = self.client.get('/import/imdb/titles')
        content = response.getvalue()
        self.assertEqual(response.status_code, 200)
        self.assertEqual(len(responses.calls), 1)
        self.assertEqual(responses.calls[0].request.url, url)
        alt_titles = Movie.objects.get(pk=1).alternative_titles.all()
        self.assertEqual(3, len(alt_titles))

    @responses.activate
    def test_filter_on_empty_region(self):
        movie = Movie(id=1, original_title='orig_title', popularity=123.0, fetched=True, imdb_id='tt0000001')
        movie.save()
        url = "https://datasets.imdbws.com/title.akas.tsv.gz"

        string = "titleId	ordering	title	region	language	types	attributes	isOriginalTitle\n"
        string += "tt0000001	1	Carmencita - spanyol tánc	\\N	\\N	imdbDisplay	\\N	0\n"
        string += "tt0000001	2	Καρμενσίτα	GR	\\N	\\N	\\N	0\n"
        string += "tt0000001	3	Карменсита	RU	\\N	\\N	\\N	0"

        out = gzip.compress(bytes(string, 'utf-8'))

        responses.add(responses.GET,
                      url,
                      body=out, status=200,
                      content_type='binary/octet-stream',
                      stream=True
                      )

        response = self.client.get('/import/imdb/titles')
        content = response.getvalue()
        self.assertEqual(response.status_code, 200)
        self.assertEqual(len(responses.calls), 1)
        self.assertEqual(responses.calls[0].request.url, url)
        alt_titles = Movie.objects.get(pk=1).alternative_titles.all()
        self.assertEqual(2, len(alt_titles))

    @responses.activate
    def test_running_multiple_times_should_only_create_uniques(self):
        movie = Movie(id=1, original_title='orig_title', popularity=123.0, fetched=True, imdb_id='tt0000001')
        movie.save()
        url = "https://datasets.imdbws.com/title.akas.tsv.gz"

        string = "titleId	ordering	title	region	language	types	attributes	isOriginalTitle\n"
        string += "tt0000001	2	Καρμενσίτα	GR	\\N	\\N	\\N	0\n"
        string += "tt0000001	3	Карменсита	RU	\\N	\\N	\\N	0"

        out = gzip.compress(bytes(string, 'utf-8'))

        responses.add(responses.GET, url, body=out, status=200, content_type='binary/octet-stream')

        self.client.get('/import/imdb/titles')

        string = "titleId	ordering	title	region	language	types	attributes	isOriginalTitle\n"
        string += "tt0000001	2	Καρμενσίτα	GR	\\N	\\N	\\N	0\n"
        string += "tt0000001	3	Карменсита	RU	\\N	\\N	\\N	0\n"
        string += "tt0000001	4	Carmencita	SE	\\N	\\N	\\N	0"

        out = gzip.compress(bytes(string, 'utf-8'))

        responses.replace(responses.GET, url, body=out, status=200, content_type='binary/octet-stream')

        response = self.client.get('/import/imdb/titles')

        content = response.getvalue()
        self.assertEqual(response.status_code, 200)
        self.assertEqual(len(responses.calls), 1)
        self.assertEqual(responses.calls[0].request.url, url)
        alt_titles = Movie.objects.get(pk=1).alternative_titles.all()
        self.assertEqual(3, len(alt_titles))
        self.assertEqual(3, AlternativeTitle.objects.count())
