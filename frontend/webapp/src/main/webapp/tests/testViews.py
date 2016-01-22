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
        assert 'Hello World!' in result.data

    def test_home_post_not_allowed(self):
        result = self.app.post(self.url)
        assert STATUS_405 in result.status

    def test_home_put_not_allowed(self):
        result = self.app.put(self.url)
        assert STATUS_405 in result.status

    def test_home_delete_not_allowed(self):
        result = self.app.delete(self.url)
        assert STATUS_405 in result.status


class showUserTestCase(unittest.TestCase):
    def setUp(self):
        views.app.config['TESTING'] = True
        self.app = views.app.test_client()
        with views.app.test_request_context():
            self.url = url_for('show_user', user='David')

    def tearDown(self):
        self.app = None

    def test_show_user_get(self):
        result = self.app.get(self.url)
        assert STATUS_200 in result.status
        assert 'User David' in result.data

    def test_show_user_post(self):
        result = self.app.post(self.url)
        assert STATUS_405 in result.status

    def test_show_user_put(self):
        result = self.app.put(self.url)
        assert STATUS_405 in result.status

    def test_show_user_delete(self):
        result = self.app.delete(self.url)
        assert STATUS_405 in result.status


class showPostTestCase(unittest.TestCase):
    def setUp(self):
        views.app.config['TESTING'] = True
        self.app = views.app.test_client()
        with views.app.test_request_context():
            self.url = url_for('show_post', post_id=123)

    def tearDown(self):
        self.app = None

    def test_show_post_get_integer(self):
        result = self.app.get(self.url)
        assert STATUS_200 in result.status
        assert 'Post 123' in result.data

    def test_show_post_get_str(self):
        result = self.app.get('/post_id/asd')
        assert STATUS_404 in result.status  

    def test_show_post_post(self):
        result = self.app.post(self.url)
        assert STATUS_405 in result.status

    def test_show_post_put(self):
        result = self.app.put(self.url)
        assert STATUS_405 in result.status

    def test_show_post_delete(self):
        result = self.app.delete(self.url)
        assert STATUS_405 in result.status


class postTestCase(unittest.TestCase):
    def setUp(self):
        views.app.config['TESTING'] = True
        self.app = views.app.test_client()
        with views.app.test_request_context():
            self.url = url_for('post')

    def tearDown(self):
        self.app = None

    def test_post_success(self):
        result = self.app.post(self.url)
        assert STATUS_200 in result.status
        assert 'POST' in result.data

    def test_post_get_failure(self):
        result = self.app.get(self.url)
        assert STATUS_405 in result.status

    def test_post_put_failure(self):
        result = self.app.put(self.url)
        assert STATUS_405 in result.status

    def test_post_delete_failure(self):
        result = self.app.delete(self.url)
        assert STATUS_405 in result.status