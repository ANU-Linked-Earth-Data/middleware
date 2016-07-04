#!/bin/bash

set -e

VERSION=2.3.1
WORK_DIR="$(pwd)/fuseki"
FUSEKI_TGZ="${WORK_DIR}/apache-jena-fuseki-${VERSION}.tar.gz"
FUSEKI_TGZ_MD5SUM="fd3ffd63c381b4080ab7810e8b721421  $DEST_TGZ"
FUSEKI_DIR="${WORK_DIR}/apache-jena-fuseki-${VERSION}"
APACHE_REPO_PATH="jena/binaries/apache-jena-fuseki-${VERSION}.tar.gz"
MIRRORS="apache.mirror.serversaustralia.com.au apache.uberglobalmirror.com apache.mirror.digitalpacific.com.au archive.apache.org/dist"

mkdir -p "$WORK_DIR"
if [ ! -d "$FUSEKI_DIR" ]; then
    echo "Calculating checksum for $FUSEKI_TGZ"
    if md5sum -c <(echo $FUSEKI_TGZ_MD5SUM); then
        echo "$FUSEKI_TGZ exists, and checksum is fine, skipping download"
    else
        echo "Re-downloading to $FUSEKI_TGZ"
        for mirror in $MIRRORS; do
            URL="http://$mirror/$APACHE_REPO_PATH"
            echo "Attempting download from $URL"
            wget -O "$FUSEKI_TGZ" "$URL" && break || echo "Download failed"
        done
        if [ ! -f "$FUSEKI_TGZ" ]; then
            echo "Failed to download from any mirror"
            exit 1
        fi
    fi

    tar xf "$FUSEKI_TGZ" -C "$WORK_DIR"
fi

# Now replace fuseki-server script with one which makes sense
sed -i.bak -e "s/^exec \$JAVA.*\$/exec \$JAVA \$JVM_ARGS -cp \"\$JAR:\$EXTRA_JARS\" org.apache.jena.fuseki.cmd.FusekiCmd \"\$@\"/" \
    "$FUSEKI_DIR/fuseki-server"
