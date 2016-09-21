import unittest, sys, os

if __name__ == '__main__':
    current_dir = os.path.dirname(__file__)
    suite = unittest.TestLoader().discover(current_dir, pattern = "test*.py")
    ret = not unittest.TextTestRunner(verbosity=2).run(suite).wasSuccessful()
    sys.exit(ret)
