#!/bin/bash

set -e
cd "$(dirname $0)"
. get-fuseki.sh
export FUSEKI_BASE="$(pwd)/fuseki-base"
export FUSEKI_HOME="$DEST_DIR"
mkdir -p "$FUSEKI_BASE"
exec "$DEST_DIR/fuseki-server" $@

