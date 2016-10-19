from application import app
import json, requests, threading, os
from flask import request, render_template, Response, session, Flask, make_response

BACKEND = os.getenv('BACKEND_URL', 'http://localhost:10080')

@app.route("/")
def home():
    return render_template('index.html')


@app.route('/chart', methods=['GET'])
def chart():
    return render_template('chart.html')


@app.route('/map', methods=['GET'])
def map():
    return render_template('map.html')

@app.route('/map2', methods=['GET'])
def map2():
    return render_template('map2.html')

@app.route('/uploadFile', methods=['POST'])
def uploadFile():
    path = request.args.get('path')
    path = 'chart' if 'chart' in path else 'map'

    file = request.files['file']
    if file and '.csv' in file.filename:
        print(file.readable())
        file_ = {'file': ('file', file)}
        response = requests.post(BACKEND + '/imdb/userRatings', files=file_)
        data = response.content.decode("utf-8")
        #session[request.environ['REMOTE_ADDR']] = data
        return render_template(path + '.html', data=json.dumps(data))
    else:
        return render_template(path + '.html')


@app.route("/findCountries", methods=['GET'])
def findCountries():
    response = requests.get(BACKEND + '/map/findCountries').json()
    return Response(json.dumps(response),  mimetype='application/json')


@app.route('/findMoviesByCountry/<country>', methods=['GET'])
def findMoviesByCountry(country):
    response = requests.get(BACKEND + '/imdb/movies/country', params={'country': country.upper()})
    return Response(json.dumps(response.json()), mimetype='application/json')


@app.route('/admin/startImdbImport', methods=['GET'])
def start_imdb_import():
    async_task = AsyncImdbTask()
    async_task.start()
    return render_template('index.html')


@app.route('/admin/startCountriesImport', methods=['GET'])
def start_countries_import():
    async_task = AsyncCountriesTask()
    async_task.start()
    return render_template('index.html')


class AsyncImdbTask(threading.Thread):
    def run(self):
        requests.post(BACKEND + '/import/startImdbImport')


class AsyncCountriesTask(threading.Thread):
    def run(self):
        requests.post(BACKEND + '/import/startCountriesImport')
