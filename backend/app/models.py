from django.db import models


class Movie(models.Model):
    id = models.IntegerField(db_index=True, primary_key=True)
    original_title = models.CharField(max_length=500)
    popularity = models.DecimalField(decimal_places=3, max_digits=10)
    fetched = models.BooleanField(default=False)
    budget = models.BigIntegerField(null=True, blank=True)
    imdb_id = models.CharField(max_length=30, null=True, blank=True)
    original_language = models.CharField(max_length=30, null=True, blank=True)
    overview = models.TextField(null=True, blank=True)
    poster_path = models.CharField(max_length=40, null=True, blank=True)
    release_date = models.CharField(max_length=10, null=True, blank=True)
    revenue = models.BigIntegerField(null=True, blank=True)
    runtime = models.IntegerField(null=True, blank=True)
    vote_average = models.DecimalField(decimal_places=1, max_digits=10, null=True, blank=True)
    vote_count = models.IntegerField(null=True, blank=True)
    raw_response = models.TextField(null=True, blank=True)

    class Meta:
        indexes = [models.Index(fields=['id'], name='movie_pk_index')]

    def __str__(self):
        return "id: %s, original_title: %s, original_language: %s" % (self.id, self.original_title, self.original_language)

    # genres = models.ManyToManyField('Genre')


class Genre(models.Model):
    movie = models.ForeignKey(Movie, related_name='genres', on_delete=models.CASCADE, db_index=True)
    # id = models.IntegerField(primary_key=True)
    name = models.TextField()


class AlternativeTitle(models.Model):
    movie = models.ForeignKey(Movie, related_name='alternative_titles', on_delete=models.CASCADE, db_index=True)
    iso_3166_1 = models.CharField(max_length=20)
    title = models.CharField(max_length=500)
    type = models.CharField(max_length=100, blank=True, null=True)


class SpokenLanguage(models.Model):
    movie = models.ForeignKey(Movie, related_name='spoken_languages', on_delete=models.CASCADE, db_index=True)
    iso_639_1 = models.CharField(max_length=4)
    name = models.CharField(max_length=50)


class ProductionCountries(models.Model):
    movie = models.ForeignKey(Movie, related_name='production_countries', on_delete=models.CASCADE, db_index=True)
    iso_3166_1 = models.CharField(max_length=4)
    name = models.CharField(max_length=50)