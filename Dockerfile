FROM ubuntu:16.04
RUN apt-get update && apt-get install -y \
    maven \
    libjhdf5-java
RUN useradd -rmd /middleware middleware
USER middleware

# Now install the actual application. Eventually this will need to be volume
# mapped for development. For now it's fine like this.
RUN mkdir -p /middleware/dynamic-middleware
ADD ./dynamic-middleware/ /middleware/dynamic-middleware
RUN mvn -f /middleware/pom.xml package
