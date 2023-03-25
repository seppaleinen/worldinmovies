import multiprocessing

bind = "0.0.0.0:8020"
workers = round(multiprocessing.cpu_count() / 2)
timeout = "9000000"
accesslog = "-"
errorlog = "-"
proc_name = "tmdb"