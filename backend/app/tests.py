import httpretty, datetime, os, requests_mock, requests

from django.test import TestCase

BASE_DIR = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))


# Create your tests here.
class SecondPageTests(TestCase):
    def test1(self):
        response = self.client.get('/')
        self.assertEqual(response.status_code, 200)
        self.assertContains(response, 'movie')

    @httpretty.activate
    def test2(self):
        yesterday = datetime.date.today() - datetime.timedelta(days=1)
        yesterday_formatted = yesterday.strftime("%m_%d_%Y")
        daily_export_url = "http://files.tmdb.org/p/exports/movie_ids_%s.json.gz" % yesterday_formatted
        path = os.path.join(BASE_DIR, 'features/movie_ids.json.gz')
        data = open(path, "rb").read()
        httpretty.register_uri(
           httpretty.GET,
           daily_export_url,
           body=data,
           forcing_headers={'content-encoding': 'gzip'},
           status=200
        )

        response = self.client.get('/movies')
        self.assertEqual(response.status_code, 200)
        self.assertContains(response, 'Amount of movies imported: 3')
