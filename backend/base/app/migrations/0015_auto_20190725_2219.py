# Generated by Django 2.2.3 on 2019-07-25 20:19

from django.db import migrations, models


class Migration(migrations.Migration):

    dependencies = [
        ('app', '0014_auto_20190724_2148'),
    ]

    operations = [
        migrations.AddField(
            model_name='movie',
            name='imdb_vote_average',
            field=models.DecimalField(blank=True, decimal_places=1, max_digits=10, null=True),
        ),
        migrations.AddField(
            model_name='movie',
            name='imdb_vote_count',
            field=models.IntegerField(blank=True, null=True),
        ),
    ]