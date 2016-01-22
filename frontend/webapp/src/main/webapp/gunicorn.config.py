import multiprocessing

workers = multiprocessing.cpu_count() * 2 + 1
timeout = "15"
#check_config = True
bind = "0.0.0.0:8000"
accesslog = "-"
errorlog = "-"
#loglevel = "debug"
proc_name = "FlaskApplication"