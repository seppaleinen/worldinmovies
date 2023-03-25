import simplejson, datetime, json

from django.http import HttpResponse, StreamingHttpResponse
from app.models import Movie, Genre
from app.importer import download_files, fetch_tmdb_data_concurrently, import_genres, import_countries, import_languages, \
     base_import, check_which_movies_needs_update


def import_status(request):
    return HttpResponse(simplejson.dumps({"fetched": 0, "total": 0, "percentage_done": 0}), content_type='application/json')


# Imports

def download_file(request):
    return StreamingHttpResponse(download_files())


def base_fetch(request):
    return StreamingHttpResponse(base_import())


def fetch_movie(request):
    return StreamingHttpResponse(fetch_tmdb_data_concurrently())


def fetch_genres(request):
    return StreamingHttpResponse(import_genres())


def fetch_countries(request):
    return StreamingHttpResponse(import_countries())


def fetch_languages(request):
    return StreamingHttpResponse(import_languages())


def check_tmdb_for_changes(request):
    start_date = request.GET.get('start_date',
                                 (datetime.date.today() - datetime.timedelta(days=1)).strftime("%Y-%m-%d"))
    end_date = request.GET.get('end_date', datetime.date.today().strftime("%Y-%m-%d"))
    return StreamingHttpResponse(check_which_movies_needs_update(start_date, end_date))


def fetch_movie_data(request, ids):
    movie_ids = ids.split(',')
    data_list = Movie.objects.filter(pk__in=movie_ids).values_list('data')
    response = json.dumps([data for data in data_list])
    return HttpResponse(response)
