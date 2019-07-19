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
    # genres = models.ManyToManyField('Genre')

    class Meta:
        indexes = [models.Index(fields=['id'], name='movie_pk_index')]

    def add_fetched_info(self, fetched_movie):
        self.fetched = True
        self.raw_response = fetched_movie
        self.budget = fetched_movie['budget']
        self.imdb_id = fetched_movie['imdb_id']
        self.original_language = fetched_movie['original_language']
        self.overview = fetched_movie['overview']
        self.poster_path = fetched_movie['poster_path']
        self.release_date = fetched_movie['release_date']
        self.revenue = fetched_movie['revenue']
        self.runtime = fetched_movie['runtime']
        self.vote_average = fetched_movie['vote_average']
        self.vote_count = fetched_movie['vote_count']


    def __str__(self):
        return "id: %s, original_title: %s, fetched: %s" % (self.id, self.original_title, self.fetched)



class Genre(models.Model):
    movie = models.ForeignKey(Movie, related_name='genres', on_delete=models.CASCADE, db_index=True)
    # id = models.IntegerField(primary_key=True)
    name = models.TextField()


class AlternativeTitle(models.Model):
    movie = models.ForeignKey(Movie, related_name='alternative_titles', on_delete=models.CASCADE, db_index=True)
    iso_3166_1 = models.CharField(max_length=20)
    title = models.CharField(max_length=500)
    type = models.CharField(max_length=100, blank=True, null=True)

    def __str__(self):
        return "iso:{iso}, title:{title}".format(iso=self.iso_3166_1, title=self.title)


class SpokenLanguage(models.Model):
    movie = models.ForeignKey(Movie, related_name='spoken_languages', on_delete=models.CASCADE, db_index=True)
    iso_639_1 = models.CharField(max_length=4)
    name = models.CharField(max_length=50)

    def __str__(self):
        return "iso:{iso}, name:{name}".format(iso=self.iso_639_1, name=self.name)


class ProductionCountries(models.Model):
    movie = models.ForeignKey(Movie, related_name='production_countries', on_delete=models.CASCADE, db_index=True)
    iso_3166_1 = models.CharField(max_length=4)
    name = models.CharField(max_length=50)

    def __str__(self):
        return "iso:{iso}, name:{name}".format(iso=self.iso_3166_1, name=self.name)