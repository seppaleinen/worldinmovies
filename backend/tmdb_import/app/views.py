import datetime
import json
import threading

from app.importer import download_files, fetch_tmdb_data_concurrently, import_genres, import_countries, \
    import_languages, \
    base_import, check_which_movies_needs_update
from app.models import Movie
from django.http import HttpResponse, StreamingHttpResponse


def import_status(request):
    result = Movie.objects().aggregate([
        {
            '$facet': {
                'Total': [
                    {
                        '$match': {
                            '_id': {
                                '$exists': True
                            }
                        }
                    }, {
                        '$count': 'count'
                    }
                ],
                'Fetched': [
                    {
                        '$match': {
                            'fetched': {
                                '$eq': True
                            }
                        }
                    }, {
                        '$count': 'count'
                    }
                ]
            }
        }, {
            '$unwind': {
                'path': '$Total'
            }
        }, {
            '$unwind': {
                'path': '$Fetched'
            }
        }, {
            '$project': {
                'total': '$Total.count',
                'fetched': '$Fetched.count',
                'percentage_done': {
                    '$multiply': [
                        {
                            '$divide': [
                                '$Fetched.count', '$Total.count'
                            ]
                        }, 100
                    ]
                }
            }
        }
    ])
    return HttpResponse(result, content_type='application/json')


# Imports

def download_file(request):
    return StreamingHttpResponse(download_files())


def base_fetch(request):
    return StreamingHttpResponse(base_import())


def import_tmdb_data(request):
    if 'import_tmdb_data' not in [thread.name for thread in threading.enumerate()]:
        thread = threading.Thread(target=fetch_tmdb_data_concurrently, name='import_tmdb_data')
        thread.setDaemon(True)
        thread.start()
        return HttpResponse(json.dumps({"Message": "Starting to process TMDB data"}))
    else:
        return HttpResponse(json.dumps({"Message": "TMDB data process already started"}))


def fetch_genres(request):
    return StreamingHttpResponse(import_genres())


def fetch_countries(request):
    return StreamingHttpResponse(import_countries())


def fetch_languages(request):
    if 'import_languages' not in [thread.name for thread in threading.enumerate()]:
        thread = threading.Thread(target=import_languages, name='import_languages')
        thread.setDaemon(True)
        thread.start()
        return HttpResponse(json.dumps({"Message": "Starting to process TMDB languages"}))
    else:
        return HttpResponse(json.dumps({"Message": "TMDB languages process already started"}))


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
