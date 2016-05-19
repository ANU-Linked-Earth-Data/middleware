#!/usr/bin/env bash

set -e

VERSION="1.11.2"
GDAL_URL="http://download.osgeo.org/gdal/${VERSION}/gdal-${VERSION}.tar.gz"
DEST_TGZ="$(basename "$GDAL_URL")"
DEST_DIR="$(basename "$DEST_TGZ" .tar.gz)"

if [ ! -d "$DEST_DIR" ]; then
    if [ ! -f "$DEST_TGZ" ]; then
        echo "Downloading $GDAL_URL"
        curl -o "$DEST_TGZ" "$GDAL_URL"
    fi
    echo "Extracting $DEST_TGZ"
    tar xf "$DEST_TGZ"
fi

NCPUS="$(grep '^cpu MHz' < /proc/cpuinfo | wc -l)"
PREFIX="$HOME/.local"
echo "Building GDAL with $NCPUS CPUs (will install in $PREFIX)"
mkdir -p "$PREFIX"
cd "$DEST_DIR"
# Check it: https://trac.osgeo.org/gdal/wiki/BuildingOnUnixWithMinimizedDrivers
# I took out --without-libtool (libtool is important if you don't want to spend
# all your time chasing down POSIX-related incompatabilities) and added
# --with-java.
./configure --prefix="$PREFIX" \
            --with-threads \
            --with-ogr \
            --with-geos \
            --with-libz=internal \
            --with-libtiff=internal \
            --with-geotiff=internal \
            --with-java \
            --without-gif \
            --without-pg \
            --without-grass \
            --without-libgrass \
            --without-cfitsio \
            --without-pcraster \
            --without-netcdf \
            --without-png \
            --without-jpeg \
            --without-gif \
            --without-ogdi \
            --without-fme \
            --without-hdf4 \
            --without-hdf5 \
            --without-jasper \
            --without-ecw \
            --without-kakadu \
            --without-mrsid \
            --without-jp2mrsid \
            --without-bsb \
            --without-grib \
            --without-mysql \
            --without-ingres \
            --without-xerces \
            --without-expat \
            --without-odbc \
            --without-curl \
            --without-sqlite3 \
            --without-dwgdirect \
            --without-panorama \
            --without-idb \
            --without-sde \
            --without-perl \
            --without-php \
            --without-ruby \
            --without-python \
            --without-ogpython \
            --with-hide-internal-symbols
make -j$NCPUS
make install
echo "Building Java wrapper"
cd swig/java
make -j$NCPUS
make install
