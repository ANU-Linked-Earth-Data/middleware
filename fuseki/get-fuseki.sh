#!/bin/bash

set -e

VERSION=2.3.1
DEST_TGZ="apache-jena-fuseki-${VERSION}.tar.gz"
DEST_TGZ_MD5SUM="b8ee1ecfb47849550f9412450606c606  $DEST"
DEST_DIR="$(pwd)/apache-jena-fuseki-${VERSION}"
URL="http://apache.mirror.serversaustralia.com.au/jena/binaries/apache-jena-fuseki-${VERSION}.tar.gz"

if [ ! -d "$DEST_DIR" ]; then
    echo "Calculating checksum for $DEST_TGZ"
    if echo "$DEST_TGZ_MD5SUM" | md5sum -c -; then
        echo "$DEST_TGZ exists, and checksum is fine, skipping download"
    else
        echo "Re-downloading to $DEST_TGZ"
        wget -O "$DEST_TGZ" "$URL"
    fi

    tar xf "$DEST_TGZ"
fi

ln -sf "$DEST_DIR/bin/soh" ./soh
