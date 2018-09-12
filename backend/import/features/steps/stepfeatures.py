import os.path
import os
from importer import download_daily_file
from behave import given, \
    when, \
    then
from hamcrest import assert_that, \
    contains, \
    not_none, \
    none, \
    equal_to
from wiremock.constants import Config
from wiremock.client import Mapping, \
    MappingRequest, \
    MappingResponse, \
    HttpMethods, \
    Mappings, \
    CommonHeaders
from wiremock.server import WireMockServer

results = []


def before_all(context):
    wm = context.wiremock_server = WireMockServer()
    wm.start()
    Config.base_url = 'http://localhost:{}/__admin'.format(wm.port)
    os.environ["tmdb_url"] = "http://localhost:9999"


def after_all(context):
    context.wiremock_server.stop()


@given('{text} is in daily file response')
def given_watchlist_data(context, text):
    my_path = os.path.abspath(os.path.dirname(__file__))
    path = os.path.join(my_path, "../resources/" + text)

    data = open(path, 'rb').read()
    responseHeaders = {
        "Content-Length": 123,
        "Accept-Ranges": "bytes"
    }

    mapping = Mapping(
        priority=100,
        request=MappingRequest(
            method=HttpMethods.GET,
            url_path_pattern='/p/exports/movie_ids_.*.json.gz'
            #url='/p/exports/movie_ids_09_12_2018.json.gz'
        ),
        response=MappingResponse(
            status=200,
            base64Body=data,
            headers=responseHeaders
        ),
        persistent=False,
    )
    mapping = Mappings.create_mapping(mapping=mapping)

    all_mappings = Mappings.retrieve_all_mappings()

    context.watchlist_path = path


@when('starting import')
def compare(context):
    download_daily_file()
    context.result = results


@then('this "{expected}" should be in the result')
def movies_should_be_in_result(context, expected):
    match = expected.upper() in map(str.upper, context.result)
    if not match:
        print("EXPECTED MATCH ON: %s, result: %s" % (expected, context.result))
    assert_that(match, equal_to(True))
