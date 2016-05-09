#!/bin/bash

set -e

VERSION=2.3.1
DEST_TGZ="apache-jena-fuseki-${VERSION}.tar.gz"
DEST_TGZ_MD5SUM="b8ee1ecfb47849550f9412450606c606  $DEST"
DEST_DIR="$(pwd)/apache-jena-fuseki-${VERSION}"
APACHE_REPO_PATH="/jena/binaries/apache-jena-fuseki-${VERSION}.tar.gz"
MIRRORS="apache.mirror.serversaustralia.com.au apache.uberglobalmirror.com apache.mirror.digitalpacific.com.au"

if [ ! -d "$DEST_DIR" ]; then
    echo "Calculating checksum for $DEST_TGZ"
    if md5sum -c <(echo $DEST_TGZ_MD5SUM); then
        echo "$DEST_TGZ exists, and checksum is fine, skipping download"
    else
        echo "Re-downloading to $DEST_TGZ"
        for mirror in $MIRRORS; do
            URL="http://$mirror/$APACHE_REPO_PATH"
            echo "Attempting download from $URL"
            wget -O "$DEST_TGZ" "$URL" && break || echo "Download failed"
        done
        if [ ! -f "$DEST_TGZ" ]; then
            echo "Failed to download from any mirror"
            exit 1
        fi
    fi

    tar xf "$DEST_TGZ"
fi

ln -sf "$DEST_DIR/bin/soh" ./soh
