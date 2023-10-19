import multiprocessing

bind = "0.0.0.0:8000"
workers = 1
threads = multiprocessing.cpu_count() * 2 + 1
timeout = "9000000"
accesslog = "-"
errorlog = "-"
proc_name = "worldinmovies_imdb"
