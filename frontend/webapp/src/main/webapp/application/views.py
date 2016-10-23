from application import app
import json, requests, threading, os
from flask import request, render_template, Response, session, Flask, make_response
from application.models import SignupForm, LoginForm

BACKEND = os.getenv('BACKEND_URL', 'http://localhost:10080')

@app.route("/")
def home():
    return render_template('index.html', **Helper().forms())


@app.route('/chart', methods=['GET'])
def chart():
    return render_template('chart.html', **Helper().forms())


@app.route('/map', methods=['GET'])
def map():
    return render_template('map.html', **Helper().forms())

@app.route('/map2', methods=['GET'])
def map2():
    return render_template('map2.html', **Helper().forms())

@app.route('/uploadFile', methods=['POST'])
def uploadFile():
    path = request.args.get('path')
    path = 'chart' if 'chart' in path else 'map'

    file = request.files['file']
    if file and '.csv' in file.filename:
        print(file.readable())
        response = requests.post(BACKEND + '/imdb/userRatings',
                                 files={'file': ('file', file)},
                                 data={'username': 'seppa'})
        print(response.content.decode("utf-8"))
        data = response.content.decode("utf-8")
        #session[request.environ['REMOTE_ADDR']] = data
        return render_template(path + '.html', data=json.dumps(data), **Helper().forms())
    else:
        return render_template(path + '.html', **Helper().forms())


@app.route("/findCountries", methods=['GET'])
def findCountries():
    response = requests.get(BACKEND + '/map/findCountries').json()
    return Response(json.dumps(response),  mimetype='application/json')


@app.route('/findMoviesByCountry/<country>', methods=['GET'])
def findMoviesByCountry(country):
    response = requests.get(BACKEND + '/imdb/movies/country', params={'country': country.upper()})
    return Response(json.dumps(response.json()), mimetype='application/json')


@app.route('/signup', methods=['POST'])
def signup():
    form = SignupForm(request.form)
    if form.validate():
        response = requests.post(BACKEND + '/user/signup', json=form.data)
        if response.status_code == '201':
            print("User created!")
        else:
            print("Failed to create user")
    return render_template('index.html', **Helper().forms({'signupForm': form}))


@app.route('/login', methods=['POST'])
def login():
    form = LoginForm(request.form)
    print(form.data)
    if form.validate():
        response = requests.post(BACKEND + '/user/login', json=form.data)
        if response.status_code == '202':
            print("Login successful")
        else:
            print("Login unsuccessful")
    return render_template('index.html', **Helper().forms({'loginForm': form}))


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


class Helper(object):
    @staticmethod
    def forms(kwargs={}):
        forms = {'signupForm': SignupForm(), 'loginForm': LoginForm()}
        forms.update(kwargs)
        return forms
