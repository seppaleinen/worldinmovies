from behave import given, when, then
from hamcrest import assert_that, contains, not_none, none, equal_to
from importer import download_daily_file
import os.path

results = []

@given('{text} as watchlist')
def given_watchlist_data(context, text):
    my_path = os.path.abspath(os.path.dirname(__file__))
    path = os.path.join(my_path, "../resources/" + text)

    context.watchlist_path = path


@when('comparing')
def compare(context):
    download_daily_file
    context.result = results


@then('this "{expected}" should be in the result')
def movies_should_be_in_result(context, expected):
    match = expected.upper() in map(str.upper, context.result)
    if not match:
        print("EXPECTED MATCH ON: %s, result: %s" % (expected, context.result))
    assert_that(match, equal_to(True))
