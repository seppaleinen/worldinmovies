import datetime
from mongoengine import Document, fields


class Movie(Document):
    id = fields.IntField(primary_key=True)
    data = fields.DynamicField()
    fetched = fields.BooleanField(required=True, default=False)
    fetched_date = fields.DateTimeField()

    def add_fetched_info(self, fetched_movie):
        self.data = fetched_movie
        self.fetched = True
        self.fetched_date = datetime.datetime.today()

    def __str__(self):
        return f"id: {self.id}, fetched: {self.fetched}, fetched_at: {self.fetched_date.isoformat()}"


class Genre(Document):
    id = fields.IntField(primary_key=True)
    name = fields.StringField()

    def __str__(self):
        return f"id:{self.id}, name:{self.name}"


class SpokenLanguage(Document):
    iso_639_1 = fields.StringField(max_length=4)
    name = fields.StringField(max_length=50)

    def __str__(self):
        return f"iso:{self.iso_639_1}, name:{self.name}"


class ProductionCountries(Document):
    iso_3166_1 = fields.StringField(primary_key=True, max_length=4)
    name = fields.StringField(max_length=50)

    def __str__(self):
        return f"iso:{self.iso_3166_1}, name:{self.name}"
