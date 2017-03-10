# Whipper REST

**NOTE: WIP!!**

This is a REST API for Whipper. It's mainly intended to be deployed on an
application server and serve as a back-end for Whipper web applicatio.
However, you can use REST.

## How to deploy it

There are two options how to deploy Whipper REST:

1. **Build WAR**  
Build a simple WAR archive and deploy it to your
application server. Simply run `mvn package`.
2. **Build executable JAR using WildFly Swarm**  
Create an executable JAR archive using [WildFly Swarm](http://wildfly-swarm.io/).
To build it, run `mvn package -Pbeswarm`

## How to use it

Whipper REST exposes swager.json file with description of the API.
