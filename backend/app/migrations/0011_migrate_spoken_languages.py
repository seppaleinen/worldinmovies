# Generated by Django 2.2.3 on 2019-07-20 11:07

from django.db import models, migrations, connection


def migrate_spoken_languages(apps, schema_editor):
    Movie = apps.get_model('app', 'Movie')
    SpokenLanguage = apps.get_model('app', 'SpokenLanguage')

    for movie in Movie.objects.filter(fetched=True).all():
        for lang in movie.spoken_languages.all():
            try:
                spoken_lang = SpokenLanguage.objects.filter(iso_639_1=lang.iso_639_1).first()
            except Exception as exc:
                spoken_lang = lang
                spoken_lang.save()
            movie.spoken_languages2.add(spoken_lang)
    # Remove all unused spoken languages
    with connection.cursor() as cursor:
        cursor.execute("delete from app_spokenlanguage where id not in (select spokenlanguage_id from app_spokenlanguage_movies)")


class Migration(migrations.Migration):

    dependencies = [
        ('app', '0011_migrate_production_countries'),
    ]

    operations = [
        migrations.RunPython(migrate_spoken_languages),
    ]
