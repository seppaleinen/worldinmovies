import multiprocessing

bind = 'unix:/run/gunicorn.sock'
bind = "0.0.0.0:8000"
workers = multiprocessing.cpu_count() * 2 + 1
timeout = "9000000"
#check_config = True
accesslog = "-"
errorlog = "-"
#loglevel = "debug"
proc_name = "worldinmovies_backend"