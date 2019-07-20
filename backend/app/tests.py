import datetime, os, responses

from django.test import TestCase
from app.models import Movie, Language, Country, Genre

BASE_DIR = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))


class SuperClass(TestCase):
    def setUp(self):
        self._environ = dict(os.environ)
        os.environ['TMDB_API'] = 'test'

    def tearDown(self):
        os.environ.clear()
        os.environ.update(self._environ)


# Create your tests here.
class ImportTests(SuperClass):
    def test_main_page_without_movies(self):
        response = self.client.get('/')
        self.assertEqual(response.status_code, 200)
        self.assertEqual(response.content, b'No movies fetched yet')

    def test_main_page_with_movies(self):
        Movie(id=2, original_title="title1", popularity=36.213, fetched=False).save()

        response = self.client.get('/')
        self.assertEqual(response.status_code, 200)
        self.assertEqual(response.content, b'Amount of movies in DB: 1, first is: 2')

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

        response = self.client.get('/movies')
        self.assertEqual(response.status_code, 200)
        self.assertEqual(response.content, b'Amount of movies imported: 3')

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
                        "append_to_response=alternative_titles,credits,external_ids,images,account_states".format(api_key='test', movie_id=i)
            with open("testdata/%s.json" % i, 'rt') as img1:
                responses.add(responses.GET,
 			        url,
			        body=img1.read(), status=200,
			        content_type='application/javascript',
			        stream=True
		        )

        response = self.client.get('/test')
        self.assertEqual(response.status_code, 200)
        self.assertEqual(response.content, b'Fetched and saved: 3 movies')

    @responses.activate
    def test_fetch_of_failing_movie_avatar(self):
        movie = Movie(id=19995, original_title='Avatar', popularity=36.213, fetched=False)
        movie.save()

        url = "https://api.themoviedb.org/3/movie/{movie_id}?" \
              "api_key={api_key}&" \
              "language=en-US&" \
              "append_to_response=alternative_titles,credits,external_ids,images,account_states".format(api_key='test', movie_id=movie.id)
        with open("testdata/failing_movie.json", 'rt') as img1:
            responses.add(responses.GET,
                          url,
                          body=img1.read(), status=200,
                          content_type='application/javascript',
                          stream=True
                          )

        response = self.client.get('/test')
        self.assertEqual(response.status_code, 200)
        self.assertEqual(response.content, b'Fetched and saved: 1 movies')

    @responses.activate
    def test_fetch_id_thats_removed_from_tmdb(self):
        movie = Movie(id=123, original_title='removed_movie', popularity=36.213, fetched=False)
        movie.save()

        url = "https://api.themoviedb.org/3/movie/{movie_id}?" \
              "api_key={api_key}&" \
              "language=en-US&" \
              "append_to_response=alternative_titles,credits,external_ids,images,account_states".format(api_key='test', movie_id=movie.id)
        responses.add(responses.GET,
                        url,
                        json={"status_code":34,"status_message":"The resource you requested could not be found."},
                        status=404,
                        content_type='application/javascript',
                        stream=True
                        )

        response = self.client.get('/test')
        self.assertEqual(response.status_code, 200)
        self.assertEqual(response.content, b'Fetched and saved: 1 movies')
        self.assertFalse(Movie.objects.filter(pk=123)[0].fetched)

    @responses.activate
    def test_fetch_only_movies_marked_as_fetched_false(self):
        to_be_fetched = Movie(id=601, original_title='to_be_fetched', popularity=36.213, fetched=False)
        to_be_fetched.save()
        already_fetched = Movie(id=602, original_title='already_fetched', popularity=36.213, fetched=True)
        already_fetched.save()

        url = "https://api.themoviedb.org/3/movie/{movie_id}?" \
              "api_key={api_key}&" \
              "language=en-US&" \
              "append_to_response=alternative_titles,credits,external_ids,images,account_states".format(api_key='test', movie_id=to_be_fetched.id)
        with open("testdata/601.json", 'rt') as img1:
            responses.add(responses.GET,
                          url,
                          body=img1.read(), status=200,
                          content_type='application/javascript',
                          stream=True
                          )

        response = self.client.get('/test')
        self.assertEqual(len(responses.calls), 1)
        self.assertEqual(responses.calls[0].request.url, url)
        self.assertEqual(response.status_code, 200)
        self.assertEqual(response.content, b'Fetched and saved: 1 movies')
        self.assertTrue(Movie.objects.get(pk=to_be_fetched.id).fetched)


class StatusTests(SuperClass):
    def test_status_0_fetched_out_of_3(self):
        Movie(id=1, original_title="title1", popularity=36.213, fetched=False).save()
        Movie(id=2, original_title="title2", popularity=36.213, fetched=False).save()
        Movie(id=3, original_title="title3", popularity=36.213, fetched=False).save()

        response = self.client.get('/status')
        self.assertEqual(response.status_code, 200)
        self.assertEqual(response.content, b'There are 0 fetched movies out of 3, which is about 0%')

    def test_status_1_fetched_out_of_3(self):
        Movie(id=1, original_title="title1", popularity=36.213, fetched=True).save()
        Movie(id=2, original_title="title2", popularity=36.213, fetched=False).save()
        Movie(id=3, original_title="title3", popularity=36.213, fetched=False).save()

        response = self.client.get('/status')
        self.assertEqual(response.status_code, 200)
        self.assertEqual(response.content, b'There are 1 fetched movies out of 3, which is about 33%')

    def test_status_3_fetched_out_of_3(self):
        Movie(id=1, original_title="title1", popularity=36.213, fetched=True).save()
        Movie(id=2, original_title="title2", popularity=36.213, fetched=True).save()
        Movie(id=3, original_title="title3", popularity=36.213, fetched=True).save()

        response = self.client.get('/status')
        self.assertEqual(response.status_code, 200)
        self.assertEqual(response.content, b'There are 3 fetched movies out of 3, which is about 100%')

    def test_status_0_fetched_out_of_0(self):
        response = self.client.get('/status')
        self.assertEqual(response.status_code, 200)
        self.assertEqual(response.content, b'Daily file export have not been imported yet. No movies to be fetched')


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

        response = self.client.get('/fetch_countries')
        self.assertEqual(response.status_code, 200)
        self.assertEqual(len(responses.calls), 1)
        self.assertEqual(responses.calls[0].request.url, url)
        self.assertEqual(response.content, b'Imported: 247 countries')
        self.assertEqual(Country.objects.count(), 247)

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

        response = self.client.get('/fetch_languages')
        self.assertEqual(response.status_code, 200)
        self.assertEqual(len(responses.calls), 1)
        self.assertEqual(responses.calls[0].request.url, url)
        self.assertEqual(response.content, b'Imported: 187 languages')
        self.assertEqual(Language.objects.count(), 187)

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

        response = self.client.get('/fetch_genres')
        self.assertEqual(response.status_code, 200)
        self.assertEqual(len(responses.calls), 1)
        self.assertEqual(responses.calls[0].request.url, url)
        self.assertEqual(response.content, b'Imported: 19 genres')
        self.assertEqual(Genre.objects.count(), 19)