from django.db import migrations, models, transaction
import json, ast


def update_raw_response(apps, schema_editor):
    Movie = apps.get_model("app", "Movie")

    all_ids = Movie.objects \
        .filter(raw_response__isnull=False) \
        .all() \
        .values_list('id', flat=True)

    expected = len(all_ids) / 100
    index = 0
    print("Processing")
    for ids in __chunks(all_ids, 100):
        movies = Movie.objects.filter(pk__in=ids).all()
        for movie in movies:
            try:
                try:
                    json.loads(movie.raw_response)
                    pass
                except Exception:
                    new_raw = json.dumps(ast.literal_eval(movie.raw_response))
                    movie.raw_response = new_raw
            except Exception as e:
                print("Could not handle" + e)
                pass
        with transaction.atomic():
            for movie in movies:
                movie.save()
        index = index + 1
        print("Processing " + str(index) + " out of " + str(expected))



def __chunks(__list, n):
    """Yield successive n-sized chunks from list."""
    for i in range(0, len(__list), n):
        yield __list[i:i + n]


class Migration(migrations.Migration):

    dependencies = [
        ('app', '0025_alter_alternativetitle_id_and_more'),
    ]

    operations = [
        migrations.RunPython(update_raw_response, migrations.RunPython.noop)
    ]
