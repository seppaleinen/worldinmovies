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
from django.urls import path, re_path, include

from app import views

urlpatterns = [
    path('import/imdb/ratings',             views.fetch_imdb_ratings),
    path('import/imdb/titles',              views.fetch_imdb_titles),
    path('ratings',                         views.ratings, name='ratings'),
    path('votes/<str:ids>',                 views.get_imdb_votes),
    path('status',                          views.import_status, name='import_status'),
    path('view/best/<str:country_code>',    views.get_best_movies_from_country),
    path('generate/generate_datadump',      views.generate_datadump),
    re_path(r'^health/',                    include('health_check.urls'))
]
