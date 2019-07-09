from django.db import models


class Movie(models.Model):
    id = models.IntegerField(primary_key=True)
    original_title = models.CharField(max_length=200)
    popularity = models.DecimalField(decimal_places=3, max_digits=10)
