import httpretty, datetime, os, requests_mock, requests, responses

from django.test import TestCase

BASE_DIR = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))


# Create your tests here.
class ImportTests(TestCase):
    def test1(self):
        response = self.client.get('/')
        self.assertEqual(response.status_code, 200)
        self.assertContains(response, 'No movies fetched yet')

    @responses.activate
    def test2(self):
        yesterday = datetime.date.today() - datetime.timedelta(days=1)
        yesterday_formatted = yesterday.strftime("%m_%d_%Y")
        daily_export_url = "http://files.tmdb.org/p/exports/movie_ids_%s.json.gz" % yesterday_formatted
        path = os.path.join(BASE_DIR, 'testdata/movie_ids.json.gz')
        data = open(path, "rb").read()
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
    def test3(self):
        yesterday = datetime.date.today() - datetime.timedelta(days=1)
        yesterday_formatted = yesterday.strftime("%m_%d_%Y")
        daily_export_url = "http://files.tmdb.org/p/exports/movie_ids_%s.json.gz" % yesterday_formatted
        path = os.path.join(BASE_DIR, 'testdata/movie_ids.json.gz')
        data = open(path, "rb").read()
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

        for i in [601, 602, 603]:
            url = "https://api.themoviedb.org/3/movie/{movie_id}?" \
                        "api_key={api_key}&" \
                        "language=en-US&" \
                        "append_to_response=alternative_titles,credits,external_ids,images,account_states".format(api_key='test', movie_id=i)
            path = os.path.join(BASE_DIR, 'large_movie_example_response.json')
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
