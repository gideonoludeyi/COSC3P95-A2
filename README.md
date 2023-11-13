# A2 - COSC 3P95

## Usage
1. Download [`opentelemetry-javaagent.jar`](https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/latest/download/opentelemetry-javaagent.jar) into `lib/` directory
2. Compile `Server.java`
```sh
$ javac Server.java
```
3. Build jar file
```sh
$ jar cfe server.jar Server Server.class
```
4. Run server with auto-instrumentation
```sh
$ java -javaagent:lib/opentelemetry-javaagent.jar \
    -Dotel.service.name=server \
    -Dotel.traces.exporter=zipkin \
    -Dotel.javaagent.debug=true \
    -jar server.jar
```
