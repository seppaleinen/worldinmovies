import decimal
import math

from django.db import models


class Movie(models.Model):
    id = models.IntegerField(db_index=True, primary_key=True)
    original_title = models.CharField(max_length=1000)
    popularity = models.DecimalField(decimal_places=3, max_digits=10)
    fetched = models.BooleanField(default=False)
    budget = models.BigIntegerField(null=True, blank=True)
    imdb_id = models.CharField(max_length=30, null=True, db_index=True, unique=True)
    original_language = models.CharField(max_length=30, null=True, blank=True)
    overview = models.TextField(null=True, blank=True)
    poster_path = models.CharField(max_length=40, null=True, blank=True)
    release_date = models.CharField(max_length=10, null=True, blank=True)
    revenue = models.BigIntegerField(null=True, blank=True)
    runtime = models.IntegerField(null=True, blank=True)
    vote_average = models.DecimalField(decimal_places=1, max_digits=5, null=False, blank=False, default=0)
    vote_count = models.IntegerField(null=False, blank=False, default=0)
    imdb_vote_average = models.DecimalField(decimal_places=1, max_digits=5, null=False, blank=False, default=0)
    imdb_vote_count = models.IntegerField(null=False, blank=False, default=0)
    weighted_rating = models.DecimalField(decimal_places=1, max_digits=10, default=0)

    class Meta:
        indexes = [models.Index(fields=['id'], name='movie_pk_index')]

    def add_fetched_info(self, fetched_movie):
        self.fetched = True
        self.budget = fetched_movie['budget']
        self.imdb_id = fetched_movie['imdb_id'].strip() if fetched_movie['imdb_id'] and fetched_movie[
            'imdb_id'].strip() else None
        self.original_language = fetched_movie['original_language']
        self.overview = fetched_movie['overview']
        self.poster_path = fetched_movie['poster_path']
        self.release_date = fetched_movie['release_date']
        self.revenue = fetched_movie['revenue']
        self.runtime = fetched_movie['runtime']
        self.vote_average = fetched_movie['vote_average']
        self.vote_count = fetched_movie['vote_count']
        self.popularity = fetched_movie['popularity']
        self.weighted_rating = self.calculate_weighted_rating_log()

    def calculate_weighted_rating_bayes(self):
        """
        The formula for calculating the Top Rated 250 Titles gives a true Bayesian estimate:
        weighted rating (WR) = (v ÷ (v+m)) × R + (m ÷ (v+m)) × C where:

        R = average for the movie (mean) = (Rating)
        v = number of votes for the movie = (votes)
        m = minimum votes required to be listed in the Top 250 (currently 25000)
        C = the mean vote across the whole report (currently 7.0)
        """
        v = decimal.Decimal(self.vote_count) + \
            decimal.Decimal(self.imdb_vote_count)
        m = decimal.Decimal(200)
        r = decimal.Decimal(self.vote_average) + \
            decimal.Decimal(self.imdb_vote_average)
        c = decimal.Decimal(4)
        return (v / (v + m)) * r + (m / (v + m)) * c

    def calculate_weighted_rating_log(self):
        """
        New formula that should include minor movies with fewer votes
        and in the beginning exponentially increase weight on votes, and then decreasingly
        as at a certain point the amount of votes should not affect the result as much
        f(x)= log2(x)
        """
        vote_count = self.vote_count + self.imdb_vote_count
        vote_average = (self.vote_average + self.imdb_vote_average) / 2
        return math.log2(vote_count) * vote_average

    def __str__(self):
        return f"id: {self.id}, original_title: {self.original_title}, fetched: {self.fetched}"


class Genre(models.Model):
    movies = models.ManyToManyField(Movie, related_name='genres')
    id = models.IntegerField(primary_key=True)
    name = models.TextField()

    def __str__(self):
        return f"id:{self.id}, name:{self.name}"


class AlternativeTitle(models.Model):
    id = models.AutoField(primary_key=True)
    movie = models.ForeignKey(Movie, related_name='alternative_titles', on_delete=models.CASCADE, db_index=True)
    iso_3166_1 = models.CharField(max_length=50)
    title = models.CharField()
    type = models.CharField(blank=True, null=True)

    def __str__(self):
        return f"iso:{self.iso_3166_1}, title:{self.title}"


class SpokenLanguage(models.Model):
    id = models.AutoField(primary_key=True)
    movies = models.ManyToManyField(Movie, related_name='spoken_languages')
    iso_639_1 = models.CharField(max_length=4, unique=True)
    name = models.CharField(max_length=50)

    def __str__(self):
        return f"iso:{self.iso_639_1}, name:{self.name}"


class ProductionCountries(models.Model):
    id = models.AutoField(primary_key=True)
    movies = models.ManyToManyField(Movie, related_name='production_countries')
    iso_3166_1 = models.CharField(max_length=4, unique=True)
    name = models.CharField(max_length=50)

    def __str__(self):
        return f"iso:{self.iso_3166_1}, name:{self.name}"
