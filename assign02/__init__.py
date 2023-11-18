import zipfile
import gzip
import pathlib
import logging
import tempfile
import base64


from fastapi import FastAPI, UploadFile
import opentelemetry.baggage as baggage
import opentelemetry.metrics as metrics
from opentelemetry.sdk.trace import TracerProvider
from opentelemetry.sdk.trace.export import (
    BatchSpanProcessor,
    ConsoleSpanExporter,
)
from opentelemetry.instrumentation.fastapi import FastAPIInstrumentor

provider = TracerProvider(sampler=None)
provider.add_span_processor(
    BatchSpanProcessor(ConsoleSpanExporter()))

tracer = provider.get_tracer("server_tracer")

app = FastAPI()


@app.post("/upload", status_code=204)
@tracer.start_as_current_span("upload_files")
def upload(file: UploadFile):
    with tempfile.TemporaryFile() as tf: 
        base64.decode(file.file, tf) # Dencryption
        tf.seek(0)
        with zipfile.ZipFile(tf, mode="r", compression=zipfile.ZIP_DEFLATED) as zf: # Decompression
            zf.extractall(pathlib.Path.cwd().joinpath("tmp"))



FastAPIInstrumentor.instrument_app(app)
