import sys
import os
import argparse
import secrets
import pathlib
import tempfile
import zipfile
import base64
import requests
import concurrent.futures

parser = argparse.ArgumentParser(prog="Client")

parser.add_argument(
    "--host",
    dest="host",
    type=str,
    default="localhost",
)

parser.add_argument(
    "--port",
    dest="port",
    type=int,
    default=8000,
)

parser.add_argument(
    "-d", "--dir",
    dest="dir",
    type=pathlib.Path,
    default=pathlib.Path.cwd().joinpath("files")
)

def upload(fp: pathlib.Path, host:str, port:int) -> None:
    fname = f'tmp-{secrets.token_hex(4)}.zip'

    try:
        with zipfile.ZipFile(fname, mode="w", compression=zipfile.ZIP_DEFLATED) as zf:
            zf.write(fp, arcname=fp.name)

        with (
            tempfile.TemporaryFile() as tf,
            open(fname, "rb") as f
        ):
            base64.encode(f, tf)  # Encryption
            tf.seek(0)

            response = requests.post(f"http://{host}:{port}/upload", files=dict(file=tf))
    finally:
        os.unlink(fname)

    print(response.status_code)
    if not (200 <= response.status_code < 300):
        return -1

    print(response.content.decode())

def main() -> int:
    args = parser.parse_args()

    dir: pathlib.Path = args.dir

    with concurrent.futures.ThreadPoolExecutor() as executor:
        for fp in dir.iterdir():
            print(f'Uploading {fp.name}')
            executor.submit(upload, fp, host=args.host, port=args.port)

    return 0


if __name__ == "__main__":
    sys.exit(main())
