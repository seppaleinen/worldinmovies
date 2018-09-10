import requests
import datetime
import gzip
import json
from clint.textui import progress


todays_date = datetime.datetime.now().strftime("%m_%d_%Y")
filename = "movie_ids_%s.json.gz" % todays_date


def download_daily_file():
    url = "http://files.tmdb.org/p/exports/%s" % filename
    print("URL %s" % url)
    response = requests.get(url, stream=True)
    if response.status_code == 200:
        with open("superduper", 'wb') as f:
            total_length = int(response.headers.get('content-length'))
            for chunk in progress.bar(response.iter_content(chunk_size=1024), expected_size=(total_length/1024) + 1):
                if chunk:
                    f.write(chunk)
                    f.flush()

        contents = unzip_file()
        for i in contents:
            try:
                data = json.loads(i)
                adult = data['adult']
                id = data['id']
                original_title = data['original_title']
                video = data['video']
                popularity = data['popularity']
                if adult is False and video is False:
                    print("ID: %s, TITLE: %s, POPULARITY: %s" % (id, original_title, popularity))
            except Exception:
                print("This line fucked up: %s" % i)


def unzip_file():
    f = gzip.open('superduper', 'rt', encoding='utf-8')
    file_content = f.read()
    f.close()
    return file_content.splitlines()


download_daily_file()
