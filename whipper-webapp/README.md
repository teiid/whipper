# Whipper Web-app

**NOTE: WIP!!**

This is a Whipper web-application - front-end of Whipper project.

## How to deploy it
There are three options how to deploy this web-application:

1. **Build WAR**  
Build a WAR archive which you can deploy yo your application server.
Simply run `mvn package`.
2. **Build executable JAR using WildFly Swarm**  
Create an executable JAR archive using [WildFly Swarm](http://wildfly-swarm.io/).
To build it, run `mvn package -Pfeswarm`.
3. **Build standalone distribution**
You can build a standalone distribution of the application and deploy it
to your HTTP server. To build it, run `mvn process-resources -Pstandalone`.
This will create a *zip* and *tar.gz* archives in the `target` directory.
You can use whichever you want.
