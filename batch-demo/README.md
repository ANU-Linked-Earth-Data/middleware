# Batch-loading middleware demo

This directory contains an offline implementation of our new RDF data cube
architecture for delivering satellite coverage data. Unlike our new dynamic
implementation, which generates RDF triples corresponding to satellite
observations on the fly, the code in this directory produces all possible
triples in one go, and sticks them in a triple store (Fuseki) for later
querying. This was very easy to implement, and is suitable for prototyping, but
will not scale to larger amounts of data.

## Dependencies

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

## Getting some data

Data loading in the middleware relies on the [resampler
script](https://github.com/ANU-Linked-Earth-Data/resampler), which converts
AGDC data in GeoTIFF format to HDF5 data structured as rHEALPix cells. Before
following the steps below, try running that on an AGDC file to get a nicely
formatted HDF5 file (see instructions in the resampler repository).

## Running it

To actually run the data importer, start by running Fueski. If you didn't run
Fuseki in the dependency instructions above, you can run it in the foreground
of the current TTY with:

```shell
$ ./fuseki/run-fuseki
```

Now you can import some data. If `result.h5` is the HDF5 file you generated
with the resampler, then you can just do:

```shell
$ . env/bin/activate
(env)$ ./import_agdc_data.py result.h5
```

That will take a while to complete. Once it's done, you should be able to go to
the Fuseki control panel (usually [localhost:3030](http://localhost:3030/)) and
play around with your data.
