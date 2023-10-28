import os, responses, io, gzip, time
import sys

from django.test import TransactionTestCase
from django.db import transaction
from app.models import Movie, SpokenLanguage, ProductionCountries, AlternativeTitle

BASE_DIR = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))


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

    def tearDown(self):
        os.environ.clear()
        os.environ.update(self._environ)


def __gzip_string(string):
    out = io.StringIO()
    with gzip.GzipFile(fileobj=out, mode="w") as f:
        f.write(string)
    return out.getvalue()


class StatusTests(SuperClass):
    def test_status_0_fetched_out_of_3(self):
        Movie(id=1, original_title="title1", popularity=36.213, fetched=False).save()
        Movie(id=2, original_title="title2", popularity=36.213, fetched=False).save()
        Movie(id=3, original_title="title3", popularity=36.213, fetched=False).save()

        response = self.client.get('/status', {}, True)
        self.assertEqual(response.status_code, 200)
        self.assertEqual(response.content, b'{"fetched": 0, "total": 3, "percentageDone": 0}')

    def test_status_1_fetched_out_of_3(self):
        Movie(id=1, original_title="title1", popularity=36.213, fetched=True).save()
        Movie(id=2, original_title="title2", popularity=36.213, fetched=False).save()
        Movie(id=3, original_title="title3", popularity=36.213, fetched=False).save()

        response = self.client.get('/status', {}, True)
        self.assertEqual(response.status_code, 200)
        self.assertEqual(response.content, b'{"fetched": 1, "total": 3, "percentageDone": 33}')

    def test_status_3_fetched_out_of_3(self):
        Movie(id=1, original_title="title1", popularity=36.213, fetched=True).save()
        Movie(id=2, original_title="title2", popularity=36.213, fetched=True).save()
        Movie(id=3, original_title="title3", popularity=36.213, fetched=True).save()

        response = self.client.get('/status', {}, True)
        self.assertEqual(response.status_code, 200)
        self.assertEqual(response.content, b'{"fetched": 3, "total": 3, "percentageDone": 100}')

    def test_status_0_fetched_out_of_0(self):
        response = self.client.get('/status', {}, True)
        self.assertEqual(response.status_code, 200)
        self.assertEqual(response.content, b'{"fetched": 0, "total": 0, "percentageDone": 0}')


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

        response = self.client.post('/ratings', data=files, format='multipart/form-data', follow=True)
        self.assertEqual(response.status_code, 200, response.content)
        json = response.json()
        self.assertEqual(json['found']['US'][0]['original_title'], 'Lord of the Flies')
        self.assertEqual(json['found']['US'][0]['country_code'], 'US')
        self.assertEqual(json['found']['AU'][0]['original_title'], 'Misery')
        self.assertEqual(json['found']['AU'][0]['country_code'], 'AU')

    def test_convert_imdb_ratings_not_found(self):
        files = {
            "file": open('testdata/mini_ratings.csv', 'r', encoding='cp1252')
        }

        response = self.client.post('/ratings', data=files, format='multipart/form-data', follow=True)
        self.assertEqual(response.status_code, 200)
        self.assertEqual(response.content.decode('utf-8'),
                         '{"found": {}, "not_found": [{"title": "Lord of the Flies", "year": "1990", "imdb_id": "tt0100054"}, {"title": "Misery", "year": "1990", "imdb_id": "tt0100157"}]}')


class ViewBestFromCountry(SuperClass):
    def test_fetch_top_from_country(self):
        i = 0
        for country_code in ['US', 'AU', 'GB']:
            country = ProductionCountries.objects.get(iso_3166_1=country_code)
            for ratings in range(0, 10):
                with transaction.atomic():
                    movie = Movie(id=i,
                                  original_title="titlö%s" % ratings,
                                  popularity=36.213,
                                  fetched=True,
                                  poster_path="/path%s" % ratings,
                                  imdb_id="imdb_id%s-%s" % (country_code, ratings),
                                  original_language='en',
                                  release_date="2019-01-0%s" % ratings,
                                  vote_average=ratings + 0.5,
                                  imdb_vote_average=0,
                                  vote_count=201,
                                  imdb_vote_count=0)
                    i = i + 1
                    movie.save()
                    movie.production_countries.add(country)

        response = self.client.get('/view/best/US')
        json = response.json()
        self.assertEqual(response.status_code, 200)
        self.assertEqual(len(json['result']), 10)
        for i in range(0, 9):
            self.assertEqual(json['result'][i], {"en_title": None, "id": i, "imdb_id": f"imdb_idUS-{i}", "original_title": f"titlö{i}", "release_date": f"2019-01-0{i}",
                                             "poster_path": f"/path{i}", "vote_average": i + 0.5, "vote_count": 201})


class ImportImdbRatings(SuperClass):
    url = "https://datasets.imdbws.com/title.ratings.tsv.gz"

    @responses.activate
    def test_import_imdb_ratings(self):
        Movie(id=19995, original_title='Avatar', popularity=36.213, fetched=True, imdb_id='tt0000001').save()
        Movie(id=1, original_title='1', popularity=36.213, fetched=True, imdb_id='').save()
        Movie(id=1, original_title='1', popularity=36.213, fetched=True, imdb_id=None).save()

        mock_response(self.url, "testdata/mini_ratings.tsv.gz")

        response = self.client.get('/import/imdb/ratings', {}, True)
        self.assertEqual(response.status_code, 200)
        wait_until(20)
        self.assertEqual(len(responses.calls), 1)
        self.assertEqual(responses.calls[0].request.url, self.url)

    @responses.activate
    def test_import_imdb_ratings_no_match(self):
        mock_response(self.url, "testdata/mini_ratings.tsv.gz")

        response = self.client.get('/import/imdb/ratings', {}, True)
        self.assertEqual(response.status_code, 200)
        wait_until(20)
        self.assertEqual(len(responses.calls), 1)
        self.assertEqual(responses.calls[0].request.url, self.url)


class ImportIMDBTitles(SuperClass):
    url = "https://datasets.imdbws.com/title.akas.tsv.gz"

    @responses.activate
    def test_normal(self):
        movie = Movie(id=1, original_title='orig_title', popularity=123.0, fetched=True, imdb_id='tt0000001')
        movie.save()
        string = "titleId	ordering	title	region	language	types	attributes	isOriginalTitle\n"
        string += "tt0000001	1	Carmencita - spanyol tánc	HU	\\N	imdbDisplay	\\N	0\n"
        string += "tt0000001	2	Καρμενσίτα	GR	\\N	\\N	\\N	0\n"
        string += "tt0000001	3	Карменсита	RU	\\N	\\N	\\N	0"

        mock_response(self.url, string=string)

        captured_output = io.StringIO()
        sys.stdout = captured_output
        response = self.client.get('/import/imdb/titles', {}, True)
        self.assertEqual(response.status_code, 200)
        self.assertTrue(wait_until2(3, "Done", captured_output))
        sys.stdout = sys.__stdout__
        print(captured_output.getvalue())

        self.assertEqual(len(responses.calls), 1)
        self.assertEqual(responses.calls[0].request.url, self.url)
        alt_titles = Movie.objects.get(pk=1).alternative_titles.all()
        self.assertEqual(3, len(alt_titles))

    @responses.activate
    def skip_test_large(self):
        movies = []
        for x in range(6600):
            imdb_id = "tt%s" % str(x).zfill(7)
            movies.append(Movie(id=x, original_title='orig_title', popularity=123.0, fetched=True, imdb_id=imdb_id))
        for i in range(0, len(movies), 10):
            Movie.objects.bulk_create(movies[i:i + 10])

        mock_response(self.url, "testdata/large_sample_alt_titles.tsv.gz")

        captured_output = io.StringIO()
        sys.stdout = captured_output
        response = self.client.get('/import/imdb/titles', {}, True)

        self.assertEqual(response.status_code, 200)
        wait_until(2)

        wait_until2(20, "Done", captured_output)
        sys.stdout = sys.__stdout__
        print(captured_output.getvalue())

        self.assertEqual(len(responses.calls), 1)
        self.assertEqual(responses.calls[0].request.url, self.url)
        alt_titles = Movie.objects.get(pk=1).alternative_titles.all()
        self.assertEqual(7, len(alt_titles))
        self.assertEqual(13030, AlternativeTitle.objects.count())


    @responses.activate
    def test_filter_on_empty_region(self):
        movie = Movie(id=1, original_title='orig_title', popularity=123.0, fetched=True, imdb_id='tt0000001')
        movie.save()

        string = "titleId	ordering	title	region	language	types	attributes	isOriginalTitle\n"
        string += "tt0000001	1	Carmencita - spanyol tánc	\\N	\\N	imdbDisplay	\\N	0\n"
        string += "tt0000001	2	Καρμενσίτα	GR	\\N	\\N	\\N	0\n"
        string += "tt0000001	3	Карменсита	RU	\\N	\\N	\\N	0"

        mock_response(self.url, string=string)

        response = self.client.get('/import/imdb/titles', {}, True)
        self.assertEqual(response.status_code, 200)
        wait_until(20)
        self.assertEqual(len(responses.calls), 1)
        self.assertEqual(responses.calls[0].request.url, self.url)
        alt_titles = Movie.objects.get(pk=1).alternative_titles.all()
        self.assertEqual(2, len(alt_titles))

    @responses.activate
    def test_running_multiple_times_should_only_create_uniques(self):
        movie = Movie(id=1, original_title='orig_title', popularity=123.0, fetched=True, imdb_id='tt0000001')
        movie.save()

        string = "titleId	ordering	title	region	language	types	attributes	isOriginalTitle\n"
        string += "tt0000001	2	Καρμενσίτα	GR	\\N	\\N	\\N	0\n"
        string += "tt0000001	3	Карменсита	RU	\\N	\\N	\\N	0"

        mock_response(self.url, string=string)

        self.client.get('/import/imdb/titles', {}, True)

        string = "titleId	ordering	title	region	language	types	attributes	isOriginalTitle\n"
        string += "tt0000001	2	Καρμενσίτα	GR	\\N	\\N	\\N	0\n"
        string += "tt0000001	3	Карменсита	RU	\\N	\\N	\\N	0\n"
        string += "tt0000001	4	Carmencita	SE	\\N	\\N	\\N	0"

        out = gzip.compress(bytes(string, 'utf-8'))

        responses.replace(responses.GET, self.url, body=out, status=200, content_type='binary/octet-stream')

        response = self.client.get('/import/imdb/titles', {}, True)

        self.assertEqual(response.status_code, 200)
        wait_until(3)
        self.assertEqual(len(responses.calls), 1)
        self.assertEqual(responses.calls[0].request.url, self.url)
        alt_titles = Movie.objects.get(pk=1).alternative_titles.all()
        self.assertEqual(3, len(alt_titles))
        self.assertEqual(3, AlternativeTitle.objects.count())


def wait_until(timeout, period=0.25):
    mustend = time.time() + timeout
    while time.time() < mustend:
        if len(responses.calls) == 1:
            return True
        time.sleep(period)
    return False


def wait_until2(timeout, expected_text, captured_output, period=0.25):
    mustend = time.time() + timeout
    while time.time() < mustend:
        if expected_text in captured_output.getvalue():
            return True
        time.sleep(period)
    return False


def mock_response(url, path=None, string=None):
    if path is not None:
        with open(path, 'rb') as file:
            data = file.read()
    else:
        data = gzip.compress(bytes(string, 'utf-8'))
    responses.add(responses.GET,
                  url,
                  body=data, status=200,
                  content_type='binary/octet-stream',
                  stream=True
                  )
