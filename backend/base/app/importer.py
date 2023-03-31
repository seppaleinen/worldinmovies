import datetime
import csv
import gzip
import json
import requests
import sys

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
        counter = 0
        contents = __unzip_file('title.ratings.tsv.gz')
        reader = csv.reader(contents, delimiter='\t')
        # chunks_of_reader_maybe = __chunks(reader, 50)
        all_imdb_ids = Movie.objects.filter(fetched=True) \
            .exclude(imdb_id__isnull=True)\
            .exclude(imdb_id__exact='')\
            .all()\
            .values_list('imdb_id', flat=True)

        imdb_ids_length = len(all_imdb_ids)
        # Multithread this maybe?
        for row in __log_progress(list(reader), "IMDB Ratings"):
            tconst = row[0]
            if tconst in all_imdb_ids:
                try:
                    movie = Movie.objects.get(imdb_id=tconst)
                    movie.imdb_vote_average = row[1]
                    movie.imdb_vote_count = row[2]
                    movie.save()
                    counter += 1
                    __send_data_to_channel(layer=layer, message=f"fetched {counter} ratings out of {imdb_ids_length}")
                except Movie.DoesNotExist:
                    pass
    else:
        __send_data_to_channel(layer=layer, message=f"Exception: {response.status_code} - {response.content}")


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
        next(reader) # Skip header
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
            AlternativeTitle.objects.bulk_create(alt_titles_chunk)
            i += len(alt_titles_chunk)
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
