#!/bin/bash

set -e

VERSION=2.3.1
DEST_TGZ="apache-jena-fuseki-${VERSION}.tar.gz"
DEST_TGZ_MD5SUM="fd3ffd63c381b4080ab7810e8b721421  $DEST_TGZ"
DEST_DIR="$(pwd)/apache-jena-fuseki-${VERSION}"
APACHE_REPO_PATH="/jena/binaries/apache-jena-fuseki-${VERSION}.tar.gz"
MIRRORS="apache.mirror.serversaustralia.com.au apache.uberglobalmirror.com apache.mirror.digitalpacific.com.au"

JTS_VERSION=1.14
JTS_DEST_ZIP="$(pwd)/jts-${JTS_VERSION}.zip"
JTS_DEST_JAR="$(pwd)/jts-${JTS_VERSION}.jar"
JTS_JAR_ZIP_PATH="lib/jts-${JTS_VERSION}.jar"
JTS_TMP_DIR="$(pwd)/jts-junk"
JTS_URL="http://downloads.sourceforge.net/project/jts-topo-suite/jts/${JTS_VERSION}/jts-${JTS_VERSION}.zip"

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

if [ ! -f "$JTS_DEST_JAR" ]; then
    if [ ! -f "$JTS_DEST_ZIP" ]; then
        wget -O "$JTS_DEST_ZIP" "$JTS_URL"
    fi

    mkdir -p "$JTS_TMP_DIR"
    unzip "$JTS_DEST_ZIP" "$JTS_JAR_ZIP_PATH" -d "$JTS_TMP_DIR"
    mv "$JTS_TMP_DIR/$JTS_JAR_ZIP_PATH" "$JTS_DEST_JAR"
    rm -r "$JTS_TMP_DIR"
fi

# Now replace fuseki-server script with one which makes sense
sed -i.bak -e "s/^exec \$JAVA.*\$/exec \$JAVA \$JVM_ARGS -cp \"\$JAR:\$EXTRA_JARS\" org.apache.jena.fuseki.cmd.FusekiCmd \"\$@\"/" \
    "$DEST_DIR/fuseki-server"

ln -sf "$DEST_DIR/bin/soh" ./soh
