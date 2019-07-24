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
from django.conf.urls import url
from django.urls import path
from django.contrib import admin

from app import views

urlpatterns = [
    url(r'^admin/', admin.site.urls),
    url(r'^movies$', views.download_file, name='download_file'),
    url(r'^test$', views.fetch_movie, name='fetch_movie'),
    url(r'^base_fetch$', views.base_fetch, name='base_fetch'),
    url(r'^fetch_genres$', views.fetch_genres, name='fetch_genres'),
    url(r'^fetch_countries$', views.fetch_countries, name='fetch_countries'),
    url(r'^fetch_languages$', views.fetch_languages, name='fetch_languages'),
    url(r'^status$', views.import_status, name='import_status'),
    url(r'^view_best_movies$', views.get_best_movies_by_country, name='get_best_movies_by_country'),
    url(r'^ratings$', views.ratings, name='ratings'),
    path('view/best/<str:country_code>', views.get_best_movies_from_country),
    path('', views.index, name='index')
]
