from django.db import models


class Movie(models.Model):
    id = models.IntegerField(primary_key=True)
    original_title = models.CharField(max_length=200)
    popularity = models.DecimalField(decimal_places=3, max_digits=10)
    fetched = models.BooleanField(default=False)
    budget = models.IntegerField(null=True, blank=True)
    # genres = models.ManyToManyField('Genre')


class Genre(models.Model):
    id = models.IntegerField(primary_key=True)
    name = models.TextField()
