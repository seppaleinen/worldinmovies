import sys
from os import path
sys.path.append( path.dirname( path.dirname( path.abspath(__file__) ) ) )
from application import views
from tests import unittest, STATUS_405, STATUS_200, STATUS_404
from flask import url_for

class homeTestCase(unittest.TestCase):
    def setUp(self):
        views.app.config['TESTING'] = True
        self.app = views.app.test_client()
        with views.app.test_request_context():
            self.url = url_for('home')

    def tearDown(self):
        self.app = None

    def test_home_get(self):
        result = self.app.get(self.url)
        assert STATUS_200 in result.status
        assert 'Welcome to the world in movies' in result.data

    def test_home_post_not_allowed(self):
        result = self.app.post(self.url)
        assert STATUS_405 in result.status

    def test_home_put_not_allowed(self):
        result = self.app.put(self.url)
        assert STATUS_405 in result.status

    def test_home_delete_not_allowed(self):
        result = self.app.delete(self.url)
        assert STATUS_405 in result.status
