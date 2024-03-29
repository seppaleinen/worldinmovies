import datetime
import json
import threading

from app.importer import download_files, fetch_tmdb_data_concurrently, import_genres, import_countries, \
    import_languages, \
    base_import, check_which_movies_needs_update
from app.models import Movie, Genre, SpokenLanguage, ProductionCountries
from django.http import HttpResponse
from app.kafka import produce


def import_status(request):
    result = Movie.objects().aggregate([
        {
            '$group': {
                '_id': None,
                'Total': {
                    '$sum': 1
                },
                'Fetched': {
                    '$sum': {
                        '$cond': {
                            'if': '$fetched', 'then': 1, 'else': 0
                        }
                    }
                }
            }
        }, {
            '$project': {
                '_id': 0,
                'Total': 1,
                'Fetched': 1,
                'Percentage': {
                    '$multiply': [
                        {'$divide': ['$Fetched', '$Total']},
                        100
                    ]
                }
            }
        }
    ])
    for row in result:
        return HttpResponse(json.dumps({"total": row['Total'],
                                    "fetched": row['Fetched'],
                                    "percentageDone": row['Percentage']}),
                            content_type='application/json')


# Imports

def download_file(request):
    if 'download_files' not in [thread.name for thread in threading.enumerate()]:
        thread = threading.Thread(target=download_files, name='download_files')
        thread.daemon = True
        thread.start()
        return HttpResponse(json.dumps({"Message": "Starting to process TMDB downloads"}))
    else:
        return HttpResponse(json.dumps({"Message": "TMDB downloads process already started"}))


def base_fetch(request):
    if 'base_import' not in [thread.name for thread in threading.enumerate()]:
        thread = threading.Thread(target=base_import, name='base_import')
        thread.daemon = True
        thread.start()
        return HttpResponse(json.dumps({"Message": "Starting to process TMDB base import"}))
    else:
        return HttpResponse(json.dumps({"Message": "TMDB base import process already started"}))


def import_tmdb_data(request):
    if 'import_tmdb_data' not in [thread.name for thread in threading.enumerate()]:
        thread = threading.Thread(target=fetch_tmdb_data_concurrently, name='import_tmdb_data')
        thread.daemon = True
        thread.start()
        return HttpResponse(json.dumps({"Message": "Starting to process TMDB data"}))
    else:
        return HttpResponse(json.dumps({"Message": "TMDB data process already started"}))


def fetch_genres(request):
    if 'import_genres' not in [thread.name for thread in threading.enumerate()]:
        thread = threading.Thread(target=import_genres, name='import_genres')
        thread.daemon = True
        thread.start()
        return HttpResponse(json.dumps({"Message": "Starting to process TMDB genres"}))
    else:
        return HttpResponse(json.dumps({"Message": "TMDB genres process already started"}))


def fetch_countries(request):
    if 'import_countries' not in [thread.name for thread in threading.enumerate()]:
        thread = threading.Thread(target=import_countries, name='import_countries')
        thread.daemon = True
        thread.start()
        return HttpResponse(json.dumps({"Message": "Starting to process TMDB countries"}))
    else:
        return HttpResponse(json.dumps({"Message": "TMDB countries process already started"}))


def fetch_languages(request):
    if 'import_languages' not in [thread.name for thread in threading.enumerate()]:
        thread = threading.Thread(target=import_languages, name='import_languages')
        thread.daemon = True
        thread.start()
        return HttpResponse(json.dumps({"Message": "Starting to process TMDB languages"}))
    else:
        return HttpResponse(json.dumps({"Message": "TMDB languages process already started"}))


def check_tmdb_for_changes(request):
    start_date = request.GET.get('start_date',
                                 (datetime.date.today() - datetime.timedelta(days=1)).strftime("%Y-%m-%d"))
    end_date = request.GET.get('end_date', datetime.date.today().strftime("%Y-%m-%d"))
    if 'check_which_movies_needs_update' not in [thread.name for thread in threading.enumerate()]:
        thread = threading.Thread(target=check_which_movies_needs_update,
                                  args=[start_date, end_date],
                                  name='check_which_movies_needs_update')
        thread.daemon = True
        thread.start()
        return HttpResponse(json.dumps({"Message": "Starting to process TMDB changes"}))
    else:
        return HttpResponse(json.dumps({"Message": "TMDB changes process already started"}))


def fetch_movie_data(request, ids):
    movie_ids = list(map(lambda x: int(x), ids.split(',')))
    data_list = Movie.objects.filter(pk__in=movie_ids).values_list('data')
    return HttpResponse(json.dumps([data for data in data_list]),
                        content_type='application/json')


def dump_genres(request):
    data = [{"id": x.id, "name": x.name} for x in Genre.objects.all()]
    return HttpResponse(json.dumps(data), content_type='application/json')


def dump_langs(request):
    data = [{"iso_639_1": x.iso_639_1, "name": x.name} for x in SpokenLanguage.objects.all()]
    return HttpResponse(json.dumps(data), content_type='application/json')


def dump_countries(request):
    data = [{"iso_3166_1": x.iso_3166_1, "name": x.name} for x in ProductionCountries.objects.all()]
    return HttpResponse(json.dumps(data), content_type='application/json')


def generate_kafka_dump(request):
    def gen():
        for chunk in __chunks(Movie.objects.all().values_list('id'), 1000):
            [produce('NEW', x, topic='data_dump') for x in chunk]

    if 'generate_kafka_dump' not in [thread.name for thread in threading.enumerate()]:
        thread = threading.Thread(target=gen,
                                  name='generate_kafka_dump')
        thread.daemon = True
        thread.start()
        return HttpResponse(json.dumps({"Message": "Starting to generate kafka dump"}))
    else:
        return HttpResponse(json.dumps({"Message": "kafka dump process already started"}))


def __chunks(__list, n):
    """Yield successive n-sized chunks from list."""
    for i in range(0, len(__list), n):
        yield __list[i:i + n]
