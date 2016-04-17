"""Just like RDFLib, but gets rid of stupid RDFLib import logging in
interactive mode."""

import __main__
_unset_file = False
if not hasattr(__main__, '__file__'):
    __main__.__file__ = None
    _unset_file = True
from rdflib import *  # flake8: noqa
if _unset_file:
    delattr(__main__, '__file__')
