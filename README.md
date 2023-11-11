# A2 - COSC 3P95

## Usage
```sh
$ javac Server.java
$ jar cfe server.jar Server Server.class
$ java -javaagent:lib/opentelemetry-javaagent.jar \
    -Dotel.service.name=server \
    -Dotel.traces.exporter=zipkin \
    -Dotel.javaagent.debug=true \
    -jar server.jar
```
