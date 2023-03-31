import decimal

from django.db import migrations, models, transaction
import json, ast


def update_weighted_rating(apps, schema_editor):
    Movie = apps.get_model("app", "Movie")

    all_ids = Movie.objects \
        .filter(fetched=True) \
        .all() \
        .values_list('id', flat=True)

    expected = len(all_ids) / 100
    index = 0
    print("Processing")
    for ids in __chunks(all_ids, 100):
        movies = Movie.objects.filter(pk__in=ids).all()
        for movie in movies:
            try:
                movie.weighted_rating = calculate_weighted_rating(movie.vote_count, movie.vote_average)
            except Exception as e:
                print(f"Could not handle {e}")
        with transaction.atomic():
            for movie in movies:
                movie.save()
        index = index + 1
        print(f"Processing {index} out of {expected}")


def calculate_weighted_rating(vote_count, vote_average):
    """
    The formula for calculating the Top Rated 250 Titles gives a true Bayesian estimate:
    weighted rating (WR) = (v ÷ (v+m)) × R + (m ÷ (v+m)) × C where:

    R = average for the movie (mean) = (Rating)
    v = number of votes for the movie = (votes)
    m = minimum votes required to be listed in the Top 250 (currently 25000)
    C = the mean vote across the whole report (currently 7.0)
    """
    v = decimal.Decimal(vote_count)
    m = decimal.Decimal(200)
    r = decimal.Decimal(vote_average)
    c = decimal.Decimal(4)
    return (v / (v + m)) * r + (m / (v + m)) * c


def __chunks(__list, n):
    """Yield successive n-sized chunks from list."""
    for i in range(0, len(__list), n):
        yield __list[i:i + n]


class Migration(migrations.Migration):
    dependencies = [
        ('app', '0028_movie_weighted_rating'),
    ]

    operations = [
        migrations.RunPython(update_weighted_rating, migrations.RunPython.noop)
    ]
