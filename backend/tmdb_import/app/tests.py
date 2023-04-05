import datetime
import gzip
import io
import os
import responses
import time
from django.test import TransactionTestCase
from app.models import Movie, Genre, SpokenLanguage, ProductionCountries
BASE_DIR = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))


def wait_until(timeout=5, period=0.25, expected_calls=1):
    mustend = time.time() + timeout
    while time.time() < mustend:
        if len(responses.calls) == expected_calls:
            return True
        time.sleep(period)
    return False


def mock_response(url, path=None):
    if path is not None:
        with open(path, 'rb') as img1:
            data = img1.read()
    responses.add(responses.GET,
                  url,
                  body=data, status=200,
                  content_type='application/javascript',
                  stream=True
                  )


class SuperClass(TransactionTestCase):
    def setUp(self):
        self.maxDiff = None
        self._environ = dict(os.environ)
        os.environ['TMDB_API'] = 'test'
        SpokenLanguage(iso_639_1='en', name='English').save()
        SpokenLanguage(iso_639_1='es', name='Spanish').save()
        ProductionCountries(iso_3166_1='US', name='United States of america').save()
        ProductionCountries(iso_3166_1='AU', name='Australia').save()
        ProductionCountries(iso_3166_1='GB', name='Great Britain').save()
        Movie.objects.all().delete()

    def tearDown(self):
        os.environ.clear()
        os.environ.update(self._environ)


def __gzip_string(string):
    out = io.StringIO()
    with gzip.GzipFile(fileobj=out, mode="w") as f:
        f.write(string)
    return out.getvalue()


class ImportTests(SuperClass):
    @responses.activate
    def test_daily_file_import(self):
        self.maxDiff = 99999999
        yesterday = datetime.date.today() - datetime.timedelta(days=1)
        yesterday_formatted = yesterday.strftime("%m_%d_%Y")

        daily_export_url = f"http://files.tmdb.org/p/exports/movie_ids_{yesterday_formatted}.json.gz"
        mock_response(daily_export_url, 'testdata/movie_ids.json.gz')

        self.assertEqual(Movie.objects.all().count(), 0)
        response = self.client.get('/import/tmdb/daily')
        self.assertEqual(response.status_code, 200)
        wait_until(timeout=5)
        self.assertEqual(len(responses.calls), 1)
        self.assertEqual(Movie.objects.all().count(), 3)

    @responses.activate
    def test_daily_file_import_delete(self):
        Movie(id=604, fetched=False).save()
        Movie(id=603, fetched=True).save()

        yesterday = datetime.date.today() - datetime.timedelta(days=1)
        yesterday_formatted = yesterday.strftime("%m_%d_%Y")
        daily_export_url = f"http://files.tmdb.org/p/exports/movie_ids_{yesterday_formatted}.json.gz"
        mock_response(daily_export_url, 'testdata/movie_ids.json.gz')

        self.assertEqual(Movie.objects.filter(pk=604).count(), 1)
        response = self.client.get('/import/tmdb/daily')
        self.assertEqual(response.status_code, 200)
        wait_until()
        self.assertEqual(Movie.objects.filter(pk=604).count(), 0)

    @responses.activate
    def test_fetch_3_unfetched_out_of_4(self):
        Movie(id=601, fetched=False).save()
        Movie(id=602, fetched=False).save()
        Movie(id=603, fetched=False).save()
        Movie(id=604, fetched=True).save()

        for i in [601, 602, 603]:
            url = "https://api.themoviedb.org/3/movie/{movie_id}?" \
                  "api_key={api_key}&" \
                  "language=en-US&" \
                  "append_to_response=alternative_titles,credits,external_ids,images,account_states".format(
                api_key='test', movie_id=i)
            mock_response(url, f"testdata/{i}.json")

        response = self.client.get('/import/tmdb/data')
        self.assertEqual(response.status_code, 200)
        wait_until()
        self.assertEqual(Movie.objects.filter(fetched=False).count(), 0)

    @responses.activate
    def test_fetch_of_failing_movie_avatar(self):
        movie = Movie(id=19995, fetched=False)
        movie.save()

        url = "https://api.themoviedb.org/3/movie/{movie_id}?" \
              "api_key={api_key}&" \
              "language=en-US&" \
              "append_to_response=alternative_titles,credits,external_ids,images,account_states".format(api_key='test',
                                                                                                        movie_id=movie.id)
        mock_response(url, "testdata/failing_movie.json")

        response = self.client.get('/import/tmdb/data')
        self.assertEqual(response.status_code, 200)
        wait_until()
        self.assertEqual(Movie.objects.filter(fetched=True).count(), 1)

    @responses.activate
    def test_fetch_id_thats_removed_from_tmdb(self):
        movie = Movie(id=123, fetched=False)
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

        self.assertEqual(Movie.objects.filter(pk=123).count(), 1)
        response = self.client.get('/import/tmdb/data')
        self.assertEqual(response.status_code, 200)
        wait_until()
        self.assertEqual(Movie.objects.filter(pk=123).count(), 0)

    @responses.activate
    def test_fetch_only_movies_marked_as_fetched_false(self):
        to_be_fetched = Movie(id=601, fetched=False)
        to_be_fetched.save()
        already_fetched = Movie(id=602, fetched=True)
        already_fetched.save()

        url = "https://api.themoviedb.org/3/movie/{movie_id}?" \
              "api_key={api_key}&" \
              "language=en-US&" \
              "append_to_response=alternative_titles,credits,external_ids,images,account_states".format(api_key='test',
                                                                                                        movie_id=to_be_fetched.id)
        mock_response(url, "testdata/601.json")

        response = self.client.get('/import/tmdb/data')
        wait_until()
        self.assertEqual(len(responses.calls), 1)
        self.assertEqual(responses.calls[0].request.url, url)
        self.assertEqual(response.status_code, 200)
        self.assertTrue(Movie.objects.get(pk=to_be_fetched.id).fetched)


class StatusTests(SuperClass):
    def test_status_0_fetched_out_of_3(self):
        Movie(id=1, data={}, fetched=False).save()
        Movie(id=2, data={}, fetched=False).save()
        Movie(id=3, data={}, fetched=False).save()

        response = self.client.get('/status')
        self.assertEqual(response.status_code, 200)
        self.assertEqual(response.content, b'{"total": 3, "fetched": 0, "percentageDone": 0.0}')

    def test_status_1_fetched_out_of_3(self):
        Movie(id=1, data={}, fetched=True).save()
        Movie(id=2, data={}, fetched=False).save()
        Movie(id=3, data={}, fetched=False).save()

        response = self.client.get('/status')
        self.assertEqual(response.status_code, 200)
        self.assertEqual(response.content, b'{"total": 3, "fetched": 1, "percentageDone": 33.33333333333333}')

    def test_status_3_fetched_out_of_3(self):
        Movie(id=1, data={}, fetched=True).save()
        Movie(id=2, data={}, fetched=True).save()
        Movie(id=3, data={}, fetched=True).save()

        response = self.client.get('/status')
        self.assertEqual(response.status_code, 200)
        self.assertEqual(response.content, b'{"total": 3, "fetched": 3, "percentageDone": 100.0}')


class FetchBaseData(SuperClass):
    @responses.activate
    def test_fetch_countries(self):
        url = "https://api.themoviedb.org/3/configuration/countries?api_key=test"
        mock_response(url, "testdata/countries.json")

        response = self.client.get('/import/tmdb/countries')
        self.assertEqual(response.status_code, 200)
        wait_until()
        self.assertEqual(len(responses.calls), 1)
        self.assertEqual(responses.calls[0].request.url, url)
        self.assertEqual(ProductionCountries.objects.count(), 247)

    @responses.activate
    def test_fetch_languages(self):
        url = "https://api.themoviedb.org/3/configuration/languages?api_key=test"
        mock_response(url, "testdata/languages.json")

        response = self.client.get('/import/tmdb/languages')
        self.maxDiff = None
        self.assertEqual(response.status_code, 200)
        wait_until()
        self.assertEqual(len(responses.calls), 1)
        self.assertEqual(responses.calls[0].request.url, url)
        self.assertEqual(SpokenLanguage.objects.count(), 187)

    @responses.activate
    def test_fetch_genres(self):
        url = "https://api.themoviedb.org/3/genre/movie/list?api_key=test&language=en-US"
        mock_response(url, "testdata/genres.json")

        response = self.client.get('/import/tmdb/genres')
        self.assertEqual(response.status_code, 200)
        wait_until()
        self.assertEqual(len(responses.calls), 1)
        self.assertEqual(responses.calls[0].request.url, url)
        self.assertEqual(Genre.objects.count(), 19)

    @responses.activate
    def test_base_fetch(self):
        genres_url = "https://api.themoviedb.org/3/genre/movie/list?api_key=test&language=en-US"
        languages_url = "https://api.themoviedb.org/3/configuration/languages?api_key=test"
        countries_url = "https://api.themoviedb.org/3/configuration/countries?api_key=test"
        mock_response(genres_url, "testdata/genres.json")
        mock_response(languages_url, "testdata/languages.json")
        mock_response(countries_url, "testdata/countries.json")

        yesterday = datetime.date.today() - datetime.timedelta(days=1)
        yesterday_formatted = yesterday.strftime("%m_%d_%Y")
        daily_url = "http://files.tmdb.org/p/exports/movie_ids_%s.json.gz" % yesterday_formatted
        mock_response(daily_url, 'testdata/movie_ids.json.gz')

        response = self.client.get('/import/base')
        self.maxDiff = None
        self.assertEqual(response.status_code, 200)
        wait_until(expected_calls=4)
        self.assertEqual(len(responses.calls), 4)
        self.assertEqual(responses.calls[0].request.url, daily_url)
        self.assertEqual(responses.calls[1].request.url, genres_url)
        self.assertEqual(responses.calls[2].request.url, countries_url)
        self.assertEqual(responses.calls[3].request.url, languages_url)
        self.assertEqual(Movie.objects.count(), 3)
        self.assertEqual(ProductionCountries.objects.count(), 247)
        self.assertEqual(SpokenLanguage.objects.count(), 187)
        self.assertEqual(Genre.objects.count(), 19)


class CheckTMDBForChanges(SuperClass):
    @responses.activate
    def test_1(self):
        Movie(id=1, data={}, fetched=True, fetched_date=datetime.date.fromisoformat("2018-01-01")).save()
        Movie(id=2, data={}, fetched=True, fetched_date=datetime.date.fromisoformat("2018-01-01")).save()

        url = "https://api.themoviedb.org/3/movie/changes?api_key=test&start_date=2019-01-01&end_date=2019-01-02&page=1"
        body = '{"results": [{"id": 1,"adult": false},{"id": 2,"adult": false},{"id": 3,"adult": true}],"page": 1,"total_pages": 1,"total_results": 1}'
        responses.add(responses.GET, url, body=body, status=200, stream=True)

        response = self.client.get('/import/tmdb/changes?start_date=2019-01-01&end_date=2019-01-02')
        self.assertEqual(response.status_code, 200)
        wait_until()
        self.assertEqual(len(responses.calls), 1)
        self.assertEqual(responses.calls[0].request.url, url)
        self.assertEqual(False, Movie.objects.get(pk=1).fetched)
        self.assertEqual(False, Movie.objects.get(pk=2).fetched)
