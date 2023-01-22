"""settings URL Configuration

The `urlpatterns` list routes URLs to views. For more information please see:
    https://docs.djangoproject.com/en/1.11/topics/http/urls/
Examples:
Function views
    1. Add an import:  from my_app import views
    2. Add a URL to urlpatterns:  url(r'^$', views.home, name='home')
Class-based views
    1. Add an import:  from other_app.views import Home
    2. Add a URL to urlpatterns:  url(r'^$', Home.as_view(), name='home')
Including another URLconf
    1. Import the include() function: from django.conf.urls import url, include
    2. Add a URL to urlpatterns:  url(r'^blog/', include('blog.urls'))
"""
from django.urls import path, re_path
from django.contrib import admin

from app import views

urlpatterns = [
    re_path(r'^admin/',                         admin.site.urls),
    # Imports a daily file with the data of what movies are available to download
    path('import/tmdb/daily',               views.download_file),
    # Starts to fetch info from tmdb with the keys from daily
    path('import/tmdb/data',                views.fetch_movie),
    # Runs /daily, /genres, /countries, /languages
    path('import/base',                     views.base_fetch),
    path('import/tmdb/genres',              views.fetch_genres),
    path('import/tmdb/countries',           views.fetch_countries),
    path('import/tmdb/languages',           views.fetch_languages),
    path('import/imdb/ratings',             views.fetch_imdb_ratings),
    path('import/imdb/titles',              views.fetch_imdb_titles),
    path('import/tmdb/changes',             views.check_tmdb_for_changes),
    path('stream_response_test',            views.stream_response_test),
    re_path(r'^status$',                        views.import_status, name='import_status'),
    re_path(r'^ratings$',                       views.ratings, name='ratings'),
    path('view/best',                       views.get_best_movies_from_all_countries),
    path('view/best/<str:country_code>',    views.get_best_movies_from_country),
    path('', views.index, name='index')
]
