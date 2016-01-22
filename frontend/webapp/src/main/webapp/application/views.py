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


@app.route('/user/<user>')
def show_user(user):
    return 'User %s' % user

@app.route('/post_id/<int:post_id>')
def show_post(post_id):
    return 'Post %d' % post_id

@app.route('/post', methods=['POST'])
def post():
    return 'POST'