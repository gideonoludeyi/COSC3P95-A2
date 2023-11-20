# A2 - COSC3P95

## Requirements

- [Docker](https://www.docker.com/products/docker-desktop/)
- [Python Poetry](https://python-poetry.org/docs/#installing-with-the-official-installer)

## Server

1. Install dependencies and virtual environment

```sh
$ poetry install
```

2. Bootstrap opentelemetry auto-instrumentation libraries

```sh
$ poetry run opentelemetry-bootstrap -a install
```

3. Start Jaeger container

```sh
$ docker run -d --name jaeger-0 \
    -e COLLECTOR_ZIPKIN_HTTP_PORT=9411 \
    -p 5775:5775/udp \
    -p 6831:6831/udp \
    -p 6832:6832/udp \
    -p 5778:5778 \
    -p 16686:16686 \
    -p 14268:14268 \
    -p 9411:9411 \
    jaegertracing/all-in-one:latest
```

4. Run the server with auto-instrumentation

```sh
$ poetry run opentelemetry-instrument uvicorn assign02:app
```

#### Cleanup

5. Stop Jaeger container

```sh
$ docker rm -f jaeger-0
```

## Client

1. Run client program

```sh
$ poetry run client
```

## OpenTelemetry Exporter Data

The `otel_datafiles` directory contains the OpenTelemetry data exported to Jaeger for each combination of advanced features.
