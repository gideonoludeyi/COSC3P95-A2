import zipfile
import pathlib
import tempfile
import base64
import secrets
import time

from fastapi import FastAPI, UploadFile
from opentelemetry.sdk.trace import TracerProvider
from opentelemetry.sdk.trace.export import (
    BatchSpanProcessor,
    ConsoleSpanExporter,
)
from opentelemetry.exporter.jaeger.thrift import JaegerExporter
from opentelemetry.instrumentation.fastapi import FastAPIInstrumentor

provider = TracerProvider(sampler=None)
provider.add_span_processor(
    BatchSpanProcessor(JaegerExporter(
        agent_host_name='localhost',
        agent_port=6831
    )))
provider.add_span_processor(
    BatchSpanProcessor(ConsoleSpanExporter()))

tracer = provider.get_tracer("server_tracer")

app = FastAPI()


@app.post("/upload", status_code=204)
@tracer.start_as_current_span("upload_files")
def upload(file: UploadFile):
    # Deliberate delay in 30% of cases
    if secrets.randbelow(10) < 3:  # 30% chance to trigger the delay
        time.sleep(3)  # Simulate a delay of 3 seconds

    with tempfile.TemporaryFile() as tf:
        base64.decode(file.file, tf)  # Decryption
        tf.seek(0)

        with zipfile.ZipFile(tf, mode="r", compression=zipfile.ZIP_DEFLATED) as zf:  # Decompression
            zf.extractall(pathlib.Path.cwd().joinpath("tmp"))


FastAPIInstrumentor.instrument_app(app)
