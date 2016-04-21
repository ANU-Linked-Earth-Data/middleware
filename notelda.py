#!/usr/bin/env python3
from screw_rdflib import ConjunctiveGraph, Literal, Namespace, OWL, RDF, RDFS, XSD, URIRef, BNode, Graph
from app          import app, manager
from flask        import request

query_url = 'http://localhost:3030/landsat/query'
update_url = 'http://localhost:3030/landsat/update'
fuseki = ConjunctiveGraph(store='SPARQLUpdateStore')
fuseki.open((query_url, update_url))

@app.route('/query/landsat/<int:uid>')
def uid(uid):
    return str(uid)

if __name__ == '__main__':
    manager.run()
