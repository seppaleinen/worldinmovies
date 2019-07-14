import os

from behave import given, when, then
from app.models import Movie, Genre, AlternativeTitle, SpokenLanguage, ProductionCountries


BASE_DIR = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))


@when(u'I save a movie with basic info')
def visit(context):
    movie = Movie(id=1,
                  original_title="original_title",
                  popularity=1,
                  fetched=False)
    movie.save()

@when(u'I save a movie with all info')
def visit(context):
    movie = Movie(id=2,
                  original_title="original_title2",
                  popularity=2,
                  fetched=True,
                  raw_response="RAWDATA",
                  budget=0,
                  imdb_id='1asdpj13',
                  original_language='swahili',
                  overview='Filmdescription',
                  poster_path='/path.jpg',
                  release_date='2019-01-01',
                  revenue=123,
                  runtime='123',
                  vote_average=123.0,
                  vote_count=123
                  )
    movie.save()
    alt_titles = [AlternativeTitle(movie_id=movie.id, iso_3166_1='SE', title='superduper', type='Svensk titel')]
    for alt_title in alt_titles:
        alt_title.save()
    movie.alternative_titles.set(alt_titles)
    spoken_langs = [SpokenLanguage(movie_id=movie.id, iso_639_1='sv', name='Svenska')]
    for spoken_lang in spoken_langs:
        spoken_lang.save()
    movie.spoken_languages.set(spoken_langs)
    prod_countries = [ProductionCountries(movie_id=movie.id, iso_3166_1='SE', name='Swärje')]
    for prod_country in prod_countries:
        prod_country.save()
    movie.production_countries.set(prod_countries)
    movie.save()


@then(u'I should be able to find it')
def i_should_see(context):
    movie = Movie.objects.get(pk=1)
    assert movie is not None, "There should be an object"
    assert movie.original_title == "original_title", "title was %s" % movie.original_title


@then(u'I should be able to find it with all info')
def i_should_see(context):
    movie = Movie.objects.get(pk=2)
    assert movie is not None, "There should be an object"
    assert movie.original_title == "original_title2", "title was %s" % movie.original_title
    assert movie.overview == "Filmdescription", "title was %s" % movie.original_title
    assert movie.alternative_titles.all()[0].title == "superduper", "title was %s" % movie.alternative_titles.all()[0].title
    assert movie.spoken_languages.all()[0].name == "Svenska", "name was %s" % movie.spoken_languages.all()[0].name
    assert movie.production_countries.all()[0].name == "Swärje", "name was %s" % movie.production_countries.all()[0].name
