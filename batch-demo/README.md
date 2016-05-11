# Batch-loading middleware demo

This directory contains an offline implementation of our new RDF data cube architecture for delivering satellite coverage data. Unlike our new dynamic implementation, which generates RDF triples corresponding to satellite observations on the fly, the code in this directory produces all possible triples in one go, and sticks them in a triple store (Fuseki) for later querying. This was very easy to implement, and is suitable for prototyping, but will not scale to larger amounts of data.

The following steps should get this running on your own machine:

```shell
$ pwd
/path/to/middleware-repo/batch-demo/
$ cd fuseki/
$ ./run-fuseki.sh 2>&1 > fuseki.log &
$ # Use tail -f fuseki.log in another terminal to see what Fuseki is doing
$ cd ../
$ # Install build deps
$ sudo apt-get install -qq virtualenv python3 python3-dev libgdal-dev python-gdal build-essential
$ # Make a virtual environment to isolate the Python packages we need
$ virtualenv --system-site-packages -p "$(which python3)" env/
... some output ...
$ . env/bin/activate
(env)$ # (env) means that you're in the virtual environment; use 'deactivate' to leave it
(env)$ pip install -r requirements.txt
... some output ...
(env)$ ./virtualenv python3 python3-dev libgdal-dev python-gdal build-essential
... more output ...
```

If the `pip install` line fails on GDAL, then you may need to install it
manually using a command like the following:

```shell
$ . env/bin/activate # Activate the virtualenv if you haven't already
(env)$ pip install --global-option=build_ext --global-option="-I/usr/include/gdal" GDAL==1.11.2
```

After running that, you should be able to comment out the line beginning with
`GDAL` in `requirements.txt` and try running `pip install -r requirements.txt`
again. Remember that you can substitute 1.11.2 for whatever version of GDAL you
have installed on your system.

GDAL is *always* a pain to install (regardless of what language you use it
from), but unfortunately it has one of the few extant implementations of
GeoTIFF.
