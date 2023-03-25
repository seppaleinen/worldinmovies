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
from app import views
from django.urls import path, re_path, include

urlpatterns = [
    # Imports a daily file with the data of what movies are available to download
    path('import/tmdb/daily',               views.download_file),
    # Starts to fetch info from tmdb with the keys from daily
    path('import/tmdb/data',                views.fetch_movie),
    # Runs /daily, /genres, /countries, /languages
    path('import/base',                     views.base_fetch),
    path('import/tmdb/genres',              views.fetch_genres),
    path('import/tmdb/countries',           views.fetch_countries),
    path('import/tmdb/languages',           views.fetch_languages),
    path('import/tmdb/changes',             views.check_tmdb_for_changes),
    path('movie/<str:ids>',                 views.fetch_movie_data),
    re_path(r'^status$',                    views.import_status),
    re_path(r'^health/',                    include('health_check.urls'))
]
