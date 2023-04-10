import mongoengine
import os
import mongomock

# Build paths inside the project like this: os.path.join(BASE_DIR, ...)
BASE_DIR = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))

# Quick-start development settings - unsuitable for production
# See https://docs.djangoproject.com/en/1.11/howto/deployment/checklist/

# SECURITY WARNING: keep the secret key used in production secret!
SECRET_KEY = '!xr(&l&-)*&!$kfj_&!ku#@%z8+ox4kb$y(k$nh8ur8b5wjshj'

# SECURITY WARNING: don't run with debug turned on in production!
DEBUG = False

# Application definition

INSTALLED_APPS = [
    'daphne',
    'channels',
    'health_check',
    'health_check.cache',
    'health_check.storage',
    'health_check.contrib.migrations',
    'django.contrib.auth',
    'django.contrib.contenttypes',
    'django.contrib.sessions',
    'django.contrib.messages',
    'app',
    'corsheaders',
    'django_crontab',
]

CRONJOBS = [
    ('0 9 * * *', 'app.importer.cron_endpoint_for_checking_updateable_movies', '>> /tmp/scheduled_job.log'),
    ('0 10 * * *', 'app.importer.base_import', '>> /tmp/scheduled_job.log'),
    ('0 */2 * * *', 'app.importer.fetch_tmdb_data_concurrently', '>> /tmp/scheduled_job.log')
]

MIDDLEWARE = [
    'django.middleware.security.SecurityMiddleware',
    'django.contrib.sessions.middleware.SessionMiddleware',
    'corsheaders.middleware.CorsMiddleware',
    'django.middleware.common.CommonMiddleware',
    'django.middleware.csrf.CsrfViewMiddleware',
    'django.contrib.auth.middleware.AuthenticationMiddleware',
    'django.contrib.messages.middleware.MessageMiddleware',
    'django.middleware.clickjacking.XFrameOptionsMiddleware',
]

CORS_ORIGIN_ALLOW_ALL = True
ALLOWED_HOSTS = ['*']
ROOT_URLCONF = 'settings.urls'
ASGI_APPLICATION = 'settings.asgi.application'
environment = os.environ.get('ENVIRONMENT', 'docker')
redis_url = 'redis' if environment == 'docker' else 'localhost'
CHANNEL_LAYERS = {
    'default': {
        'BACKEND': "channels_redis.core.RedisChannelLayer",
        'CONFIG': {
            'hosts': [(redis_url, 6379)],
        }
    }
}

# Database
# https://docs.djangoproject.com/en/1.11/ref/settings/#databases

PROJECT_DIR = os.path.abspath(os.path.dirname(__file__))

if environment == 'docker':
    mongo_url = 'mongo:27017'
    mongoengine.connect(db='tmdb', host=mongo_url, username='', password='')
elif environment == 'localhost':
    mongo_url = 'localhost:27017'
    mongoengine.connect(db='tmdb', host=mongo_url, username='', password='')
else:
    mongo_url = 'localhost:27018'
    mongoengine.connect(db='tmdb', host='mongodb://localhost', mongo_client_class=mongomock.MongoClient, username='', password='')

DATABASES = {
    'default': {
        'ENGINE': 'django.db.backends.sqlite3',
    }
}

# Password validation
# https://docs.djangoproject.com/en/1.11/ref/settings/#auth-password-validators

AUTH_PASSWORD_VALIDATORS = [
    {
        'NAME': 'django.contrib.auth.password_validation.UserAttributeSimilarityValidator',
    },
    {
        'NAME': 'django.contrib.auth.password_validation.MinimumLengthValidator',
    },
    {
        'NAME': 'django.contrib.auth.password_validation.CommonPasswordValidator',
    },
    {
        'NAME': 'django.contrib.auth.password_validation.NumericPasswordValidator',
    },
]

# Internationalization
# https://docs.djangoproject.com/en/1.11/topics/i18n/

LANGUAGE_CODE = 'en-us'
TIME_ZONE = 'Europe/Stockholm'
USE_I18N = True
USE_L10N = True
USE_TZ = True

LOGGING = {
    'version': 1,
    'disable_existing_loggers': False,
    'formatters': {
        'standard': {
            'format': '%(asctime)s %(levelname)s [%(name)s:%(lineno)s] %(module)s %(process)d %(thread)d %(message)s'
        }
    },
    'handlers': {
        'gunicorn': {
            'level': 'INFO',
            'class': 'logging.StreamHandler',
            'formatter': 'standard'
        },
        'console': {
            'level': 'INFO',
            'class': 'logging.StreamHandler',
            'formatter': 'standard'
        },
    },
    "root": {
        "handlers": ["console"],
        "level": "INFO",
    },
    'loggers': {
        'gunicorn.error': {
            'handlers': ['console'],
            'level': 'INFO',
            'propagate': True,
        },
        'django': {
            'handlers': ['console'],
            'level': 'INFO',
            'propagate': True,
        },
    }
}
