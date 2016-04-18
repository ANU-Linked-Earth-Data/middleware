#!/usr/bin/env python3
from flask        import Flask
from flask_script import Manager

try:
    import secrets
except ImportError as e:
    import os
    py = open('secrets.py', 'w')
    py.write('SECRET_KEY            = \'{}\'\n'.format(os.urandom(24).hex()))
    py.close()
    import secrets

app = Flask(__name__)
app.config.update(
    SECRET_KEY = secrets.SECRET_KEY,
)

class deriving_Show():
    def __str__(self):
        """
        A default __str__ method that just reads off attributes not starting
        with _.
        """
        name = self.__class__.__name__
        space = ' '*len(name)
        args = (space + ', ').join(
            '{} = {}\n'.format(k, repr(v))
            for k, v in sorted(self.__dict__.items(), key=lambda k:k[0])
            if not k.startswith('_')
        )
        return '\n{}( {}{})'.format(name, args, space)

    def __repr__(self):
        return self.__str__()

manager = Manager(app)
