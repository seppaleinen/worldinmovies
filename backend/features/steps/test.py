try:
    from urllib.request import urlopen
except ImportError:  # Python 2
    from urllib2 import urlopen

import httpretty, datetime, os, gzip
from behave import given, when, then


BASE_DIR = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))


@httpretty.activate
@given(u'TMDB Daily File is mocked')
def mockTmdbDailyFile(context):
    httpretty.enable()
    yesterday = datetime.date.today() - datetime.timedelta(days=1)
    yesterday_formatted = yesterday.strftime("%m_%d_%Y")
    daily_export_url = "http://files.tmdb.org/p/exports/movie_ids_%s.json.gz" % yesterday_formatted
    path = os.path.join(BASE_DIR, 'movie_ids.json')
    data = open(path, "rt", encoding='utf-8').read()
    httpretty.register_uri(
        httpretty.GET,
        daily_export_url,
        body=data,
        forcing_headers={'content-encoding': 'gzip'}
    )

@when(u'I visit "{url}"')
def visit(context, url):
    page = urlopen(context.base_url + url)
    context.response = str(page.read())


@then(u'I should see "{text}"')
def i_should_see(context, text):
    assert text in context.response
