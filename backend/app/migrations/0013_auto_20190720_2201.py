# Generated by Django 2.2.3 on 2019-07-20 20:01

from django.db import migrations, models


class Migration(migrations.Migration):

    dependencies = [
        ('app', '0012_auto_20190720_2159'),
    ]

    operations = [
        migrations.AlterField(
            model_name='productioncountries',
            name='iso_3166_1',
            field=models.CharField(max_length=4, unique=True),
        ),
        migrations.AlterField(
            model_name='spokenlanguage',
            name='iso_639_1',
            field=models.CharField(max_length=4, unique=True),
        ),
    ]
