import multiprocessing

#bind = "127.0.0.1:8000"
workers = multiprocessing.cpu_count() * 2 + 1
timeout = "15"
#check_config = True
accesslog = "-"
errorlog = "-"
#loglevel = "debug"
proc_name = "worldinmovies_backend"