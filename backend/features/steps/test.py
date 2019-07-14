import httpretty, datetime, os

from urllib.request import urlopen
from behave import given, when, then


BASE_DIR = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))


@given(u'TMDB Daily File is mocked')
@httpretty.activate
def mock_tmdb_daily_file(context):
    httpretty.enabled()
    yesterday = datetime.date.today() - datetime.timedelta(days=1)
    yesterday_formatted = yesterday.strftime("%m_%d_%Y")
    daily_export_url = "http://files.tmdb.org/p/exports/movie_ids_%s.json.gz" % yesterday_formatted
    print("Mocking: %s" % daily_export_url)
    path = os.path.join(BASE_DIR, 'movie_ids.json')
    data = open(path, "rt", encoding='utf-8').read()
    httpretty.register_uri(
        httpretty.GET,
        daily_export_url,
        body=data,
        forcing_headers={'content-encoding': 'gzip'},
        status=200
    )


@when(u'I visit "{url}"')
def visit(context, url):
    print("IS ACTIVE: %s" % httpretty.is_enabled())
    page = urlopen(context.base_url + url)
    context.response = str(page.read())


@then(u'I should see "{text}"')
def i_should_see(context, text):
    assert text in context.response, "Should have been: %s, but was: %s" % (text, context.response)
