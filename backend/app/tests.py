import httpretty, datetime, os, requests_mock, requests, responses, sys

from django.test import TestCase
from app.models import Movie

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
    def test_main_page(self):
        response = self.client.get('/')
        self.assertEqual(response.status_code, 200)
        self.assertContains(response, 'No movies fetched yet')

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
        self.assertContains(response, 'Amount of movies imported: 3')

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
        self.assertContains(response, 'Fetched and saved: 3 movies')

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
        self.assertContains(response, 'Fetched and saved: 1 movies')

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
        self.assertContains(response, 'Fetched and saved: 1 movies')
        self.assertFalse(Movie.objects.filter(pk=123)[0].fetched)


class StatusTests(SuperClass):
    def test_status_0_fetched_out_of_3(self):
        Movie(id=1, original_title="title1", popularity=36.213, fetched=False).save()
        Movie(id=2, original_title="title2", popularity=36.213, fetched=False).save()
        Movie(id=3, original_title="title3", popularity=36.213, fetched=False).save()

        response = self.client.get('/status')
        self.assertEqual(response.status_code, 200)
        self.assertContains(response, 'There are 0 fetched movies out of 3, which is about 0%')

    def test_status_1_fetched_out_of_3(self):
        Movie(id=1, original_title="title1", popularity=36.213, fetched=True).save()
        Movie(id=2, original_title="title2", popularity=36.213, fetched=False).save()
        Movie(id=3, original_title="title3", popularity=36.213, fetched=False).save()

        response = self.client.get('/status')
        self.assertEqual(response.status_code, 200)
        self.assertContains(response, 'There are 1 fetched movies out of 3, which is about 33%')

    def test_status_3_fetched_out_of_3(self):
        Movie(id=1, original_title="title1", popularity=36.213, fetched=True).save()
        Movie(id=2, original_title="title2", popularity=36.213, fetched=True).save()
        Movie(id=3, original_title="title3", popularity=36.213, fetched=True).save()

        response = self.client.get('/status')
        self.assertEqual(response.status_code, 200)
        self.assertContains(response, 'There are 3 fetched movies out of 3, which is about 100%')

    def test_status_0_fetched_out_of_0(self):
        response = self.client.get('/status')
        self.assertEqual(response.status_code, 200)
        self.assertContains(response, 'Daily file export have not been imported yet. No movies to be fetched')
