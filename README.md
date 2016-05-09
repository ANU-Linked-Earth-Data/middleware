# middleware

[![Build Status](https://travis-ci.org/ANU-Linked-Earth-Data/middleware.svg?branch=master)](https://travis-ci.org/ANU-Linked-Earth-Data/middleware)

Parts of the [linked Earth data](https://github.com/ANU-Linked-Earth-Data/main-repo) demonstrator which sit between the [DGGS](https://github.com/ANU-Linked-Earth-Data/dggs) and the [user-facing app](https://github.com/ANU-Linked-Earth-Data/anu-linked-earth-data.github.io).

Guide to this repo:

- `pom.xml` and `.travis.yml` are used for continuous integration; the `pom.xml`
  really just wraps `dynamic-store/pom.xml` to make Travis easier to use with
  this repo.
- `batch-demo/` has an old prototype of the stack which we are working on at the
  moment, which only did offline RDF materialisation for Landsat data
- `dynamic-store/` contains the new implementation we're working on, which can
  generate RDF triples from Landsat data on the fly, and doesn't have to make
  those triples persist (huge space saving!).
- `scripts/` is for development utilities which don't belong anywhere else.
