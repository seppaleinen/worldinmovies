import unittest, sys

if __name__ == '__main__':
    suite = unittest.TestLoader().discover('.', pattern = "test*.py")
    ret = not unittest.TextTestRunner(verbosity=2).run(suite).wasSuccessful()
    sys.exit(ret)
