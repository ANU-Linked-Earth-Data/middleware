# Fuseki install and run scripts

To install and run Fuseki, use `./run-fuseki.sh`. This script will probably only
work on Linux, and requires Java 8 to run (Ubuntu users: `sudo apt-get install
openjdk-8-{jdk,jre}`).

You can query Fuseki by going to [localhost:3030](http://localhost:3030/) and
using the web console, or by using the `soh` binary (if you've run
`./run-fuseki`, you'll have a symlink to it in this directory). For example:

```sh
SERVICE=http://localhost:3030/accidents
QUERY='SELECT ?a ?b ?c WHERE { ?a ?b ?c } LIMIT 10'
./soh put "$SERVICE" default ../data/accidents.rdf
./soh query --output=tsv --service="$SERVICE" "$QUERY"
```

## Using SPARQL

SPARQL queries can be sent using `./soh query` (as above), and SPARQL updates
using `./soh update`. We're just using the default graph for the experimental
data, so it's important that you direct all SPARQL updates at the graph
`urn:x-arq:DefaultGraph` (Fuseki's special name for the default graph). Here's
an example:

```sh
UPDATE_SERVICE=http://localhost:3030/accidents/update
UPDATE="INSERT DATA{GRAPH <urn:x-arq:DefaultGraph>{<urn:foo><urn:bar><urn:baz>.}}"
./soh update --service="$UPDATE_SERVICE" "$UPDATE"
```
