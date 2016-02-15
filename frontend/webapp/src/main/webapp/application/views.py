from application import app
from flask import Flask
import requests
from flask import request, render_template
from flask import Response
import json

BACKEND = 'http://api:10080'

@app.route("/")
def home():
    return render_template('index.html')

@app.route('/uploadFile', methods=['POST'])
def uploadFile():
    file = request.files['file']
    if file and '.csv' in file.filename:
        print(file.readable())
        file_ = {'file': ('file', file)}
        response = requests.post(BACKEND + '/imdb/userRatings', files=file_)
        data = json.dumps(response.content.decode("utf-8"))
        return render_template('index.html', data=data)
    else:
        return render_template('index.html')

@app.route("/findCountries", methods=['GET'])
def findCountries():
    response = requests.get(BACKEND + '/map/findCountries').json()
    return Response(json.dumps(response),  mimetype='application/json')

@app.route('/findMoviesByCountry/<country>', methods=['GET'])
def findMoviesByCountry(country):
    response = requests.get(BACKEND + '/imdb/movies/country', params={'country': country.upper()})
    return Response(json.dumps(response.json()), mimetype='application/json')
