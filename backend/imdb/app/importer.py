import datetime
import csv
import gzip
import json
import requests
import sys
import sentry_sdk

from sentry_sdk.crons import monitor
from django.db import transaction
from app.models import Movie, AlternativeTitle
from asgiref.sync import async_to_sync
from channels.layers import get_channel_layer


def __unzip_file(file_name):
    f = gzip.open(file_name, 'rt', encoding='utf-8')
    file_content = f.read()
    f.close()
    return file_content.splitlines()


def __chunks(__list, n):
    """Yield successive n-sized chunks from list."""
    for i in range(0, len(__list), n):
        yield __list[i:i + n]


@monitor(monitor_slug='import_imdb_ratings')
def import_imdb_ratings():
    """Data-dump of imdbs ratings of all films
       TSV Headers are: tconst, averageRating, numVotes
       and file is about 1 million rows, which takes awhile to process...
       While we only have around 450k rows in our database.
    """
    url = 'https://datasets.imdbws.com/title.ratings.tsv.gz'
    response = requests.get(url)
    layer = get_channel_layer()
    __send_data_to_channel(layer=layer, message=f"Downloading file: {url}")
    with open('title.ratings.tsv.gz', 'wb') as f:
        f.write(response.content)
    if response.status_code == 200:
        contents = __unzip_file('title.ratings.tsv.gz')
        reader = csv.reader(contents, delimiter='\t')
        all_imdb_ids = Movie.objects.filter(fetched=True) \
            .exclude(imdb_id__isnull=True)\
            .exclude(imdb_id__exact='')\
            .all()\
            .values_list('imdb_id', flat=True)

        imdb_ids_length = len(all_imdb_ids)
        count = 0
        for chunk in __chunks(list(reader), 100):
            movies = dict()
            for movie in chunk:
                if movie[0] in all_imdb_ids:
                    movies[movie[0]] = movie
            data = Movie.objects.filter(imdb_id__in=movies.keys())
            with transaction.atomic():
                for db_row in data:
                    data = movies[db_row.imdb_id]
                    db_row.imdb_vote_average = data[1]
                    db_row.imdb_vote_count = data[2]
                    db_row.weighted_rating = db_row.calculate_weighted_rating()
                    db_row.save()
                count += len(movies.keys())
                __send_data_to_channel(layer=layer, message=f"Processed {len(movies.keys())} ratings out of {count}/{imdb_ids_length}")
    else:
        __send_data_to_channel(layer=layer, message=f"Exception: {response.status_code} - {response.content}")


@monitor(monitor_slug='import_imdb_alt_titles')
def import_imdb_alt_titles():
    """titleId ordering title region language types attributes isOriginalTitle
    columns of interest: titleId, title, region
    """
    print("Dowloading title.akas.tsv.gz")
    url = 'https://datasets.imdbws.com/title.akas.tsv.gz'
    layer = get_channel_layer()
    __send_data_to_channel(layer=layer, message=f"Downloading file: {url}")
    response = requests.get(url)
    with open('title.akas.tsv.gz', 'wb') as f:
        f.write(response.content)
    if response.status_code == 200:
        contents = __unzip_file('title.akas.tsv.gz')
        count = len(contents)
        csv.field_size_limit(sys.maxsize)
        all_imdb_ids = Movie.objects.filter(fetched=True) \
            .exclude(imdb_id__isnull=True) \
            .exclude(imdb_id__exact='') \
            .all() \
            .values_list('imdb_id', flat=True)

        reader = csv.reader(contents, delimiter='\t', quoting=csv.QUOTE_NONE)
        print("Processing IMDB Titles")
        next(reader)  # Skip header

        """
        matches = dict()
        for chunk in __chunks(list(reader), 100):
            # matches = dict(map(lambda x: (x[0], x), filter(lambda y: y[0] in all_imdb_ids, chunk)))
            for movie in list(filter(lambda x: x[0] in all_imdb_ids, chunk)):
                matches.setdefault(movie[0], []).append(movie)
        for id_chunks in __chunks(matches, 100):
            a = dict()
            titles = list(map(lambda x: x[2], id_chunks.values()))
            titles_already_in_db = Movie.objects.filter(imdb_id__in=id_chunks.keys(), title__in=titles)
            for title in titles_already_in_db.iterator():
                a.setdefault(title.imdb_id, title.title)
        """
        alt_titles = []
        counter = 0
        for row in __log_progress(reader, "Processing IMDB Titles", count):
            tconst = row[0]
            if tconst in all_imdb_ids:
                try:
                    movie = Movie.objects.get(imdb_id=tconst)
                    title = row[2]
                    if row[3] != r'\N' and not movie.alternative_titles.filter(title=title).exists():
                        alt_title = AlternativeTitle(movie_id=movie.id,
                                                     iso_3166_1=row[3],
                                                     title=title,
                                                     type='IMDB')
                        alt_titles.append(alt_title)
                        __send_data_to_channel(layer=layer, message=f"created {counter} alternative titles out of {len(all_imdb_ids)}")
                except Movie.DoesNotExist:
                    pass
        print("Persisting IMDB Titles")
        i = 0
        alt_titles_len = len(alt_titles)
        for alt_titles_chunk in __chunks(alt_titles, 50):
            with transaction.atomic():
                for title in alt_titles_chunk:
                    title.save()
                    i += 1
            __send_data_to_channel(layer=layer, message=f"Persisted {i} out of {alt_titles_len} titles")
    else:
        __send_data_to_channel(layer=layer, message=f"Exception: {response.status_code} - {response.content}")


def __log_progress(iterable, message, length=None):
    datetime_format = "%Y-%m-%d %H:%M:%S"
    count = 1
    percentage = 0
    total_count = length if length else len(iterable)
    layer = get_channel_layer()
    for i in iterable:
        temp_perc = int(100 * count / total_count)
        if percentage != temp_perc:
            percentage = temp_perc
            __send_data_to_channel(layer=layer, message=f"{message} data handling in progress - {percentage}%")
            print(f"{datetime.datetime.now().strftime(datetime_format)} - {message} data handling in progress - {percentage}%")
        count += 1
        yield i


def __send_data_to_channel(message, layer=get_channel_layer()):
    async_to_sync(layer.group_send)('group', {"type": "events", "message": json.dumps(message)})
