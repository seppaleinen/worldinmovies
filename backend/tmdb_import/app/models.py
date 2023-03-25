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
        return "id: %s, fetched: %s, fetched_at: %s" % (self.id, self.fetched, self.fetched_date)


class Genre(Document):
    id = fields.IntField(primary_key=True)
    name = fields.StringField()

    def __str__(self):
        return "id:{id}, name:{name}".format(id=self.id, name=self.name)


class SpokenLanguage(Document):
    iso_639_1 = fields.StringField(max_length=4)
    name = fields.StringField(max_length=50)

    def __str__(self):
        return "iso:{iso}, name:{name}".format(iso=self.iso_639_1, name=self.name)
