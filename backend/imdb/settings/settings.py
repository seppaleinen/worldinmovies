import os
import sys
import sentry_sdk

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
    'health_check.db',
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
    ('0 1 * * *', 'app.importer.import_imdb_ratings', '>> /tmp/scheduled_job.log'),
    ('0 13 * * *', 'app.importer.import_imdb_alt_titles', '>> /tmp/scheduled_job.log'),
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
# CORS_ORIGIN_WHITELIST = ('http://localhost', 'https://localhost', 'http://localhost:81')
# ALLOWED_HOSTS=['http://localhost:3000', 'http://localhost:81', 'http://webapp:81', 'http://webapp:3000', 'http://localhost:8000', 'localhost:8000']
ALLOWED_HOSTS = ['*']
ROOT_URLCONF = 'settings.urls'
ASGI_APPLICATION = 'settings.asgi.application'
environment = os.getenv('ENVIRONMENT', 'docker')
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

if 'test' in sys.argv:
    PROJECT_DIR = os.path.abspath(os.path.dirname(__file__))
    DATABASES = {
        'default': {
            'ENGINE': 'django.db.backends.sqlite3',
            'NAME': os.path.join(BASE_DIR, 'worldinmovies.db'),
            'CONN_MAX_AGE': 500,
        }
    }
elif environment == 'localhost':
    DATABASES = {
        'default': {
            'ENGINE': 'django.db.backends.postgresql',
            'NAME': 'postgres',
            'USER': 'postgres',
            'PASSWORD': 'postgres',
            'HOST': 'localhost',
            'PORT': 5433,
            'CONN_MAX_AGE': 500,
        }
    }
elif environment == 'docker':
    DATABASES = {
        'default': {
            'ENGINE': 'django.db.backends.postgresql',
            'NAME': 'postgres',
            'USER': 'postgres',
            'PASSWORD': 'postgres',
            'HOST': 'db',
            'PORT': 5432,
            'CONN_MAX_AGE': 500,
        }
    }
else:
    PROJECT_DIR = os.path.abspath(os.path.dirname(__file__))
    DATABASES = {
        'default': {
            'ENGINE': 'django.db.backends.sqlite3',
            'NAME': os.path.join(BASE_DIR, 'worldinmovies.db'),
            'CONN_MAX_AGE': 500,
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

sentryApi = os.getenv('SENTRY_API', '')
if sentryApi:
    sentry_sdk.init(
        dsn=sentryApi,

        # Set traces_sample_rate to 1.0 to capture 100%
        # of transactions for performance monitoring.
        # We recommend adjusting this value in production,
        traces_sample_rate=1.0,
        profiles_sample_rate=1.0,
    )

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
            'level': 'WARN',
            'class': 'logging.StreamHandler',
            'formatter': 'standard'
        },
        'console': {
            'level': 'WARN',
            'class': 'logging.StreamHandler',
            'formatter': 'standard'
        },
    },
    "root": {
        "handlers": ["console"],
        "level": "INFO",
    },
    'loggers': {
        'gunicorn.errors': {
            'handlers': ['gunicorn'],
            'level': 'DEBUG',
            'propagate': True,
        },
        'django': {
            'handlers': ['console'],
            'level': 'WARN',
            'propagate': True,
        },
    }
}
