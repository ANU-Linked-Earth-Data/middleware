This directory contains an attempted implementation of our new architecture (the
one which dynamically generates RDF data cube observations) using Jena. Building
and running is simple:

```
$ mvn exec:java -Dexec.mainClass="anuled.dynamicstore.App
```

There are approximately 3 ↑↑↑ 3 ways of building and running Maven apps, though,
so you'll probably want to figure out a better way than using that magic
invocation each time you need to run the app (especially if you don't want to
try to build it first!).
