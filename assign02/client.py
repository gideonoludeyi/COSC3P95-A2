import sys
import os
import argparse
import secrets
import pathlib
import tempfile
import zipfile
import base64

import requests

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

def main() -> int:
    args = parser.parse_args()

    dir: pathlib.Path = args.dir

    fname = f'tmp-{secrets.token_hex(4)}.zip'

    try:
        with zipfile.ZipFile(fname, mode="w", # Compression
                    compression=zipfile.ZIP_DEFLATED) as zf:
            for fp in dir.iterdir():
                zf.write(fp, arcname=fp.name)

        with (
            tempfile.TemporaryFile() as tf,
            open(fname, "rb") as f
        ):
            base64.encode(f, tf) # Encryption
            tf.seek(0)
            response = requests.post(f"http://{args.host}:{args.port}/upload",
                                files=dict(file=tf))
    finally:
        os.unlink(fname)

    print(response.status_code)
    if not (200 <= response.status_code < 300):
        return -1

    print(response.content.decode())

    return 0


if __name__ == "__main__":
    sys.exit(main())
