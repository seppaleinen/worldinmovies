from application import app
from flask import Flask
import requests
from flask import request, render_template
from flask import Response
import json

@app.route("/")
def home():
    return render_template('index.html')

@app.route("/findCountries", methods=['GET'])
def findCountries():
    response = requests.get('http://backend:10080/map/findCountries').json()
    return Response(json.dumps(response),  mimetype='application/json')


@app.route('/uploadFile', methods=['POST'])
def uploadFile():
    file = request.files['file']
    if file:
        print(file.readable())
        file_ = {'file': ('file', file)}
        response = requests.post('http://backend:10080/imdb/userRatings', files=file_)
        return json.dumps(response.content.decode("utf-8"))
    else:
        return '''
            <!doctype html>
            <p>SOMETIN WONG</p>
            '''

@app.route('/post_id/<int:post_id>')
def show_post(post_id):
    return 'Post %d' % post_id