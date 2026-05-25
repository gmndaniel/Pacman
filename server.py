#!/usr/bin/env python3
"""Serve the Pacman browser build with Python's standard library."""

from __future__ import annotations

import argparse
from functools import partial
from http.server import SimpleHTTPRequestHandler, ThreadingHTTPServer
from pathlib import Path


def main() -> None:
    parser = argparse.ArgumentParser(description="Serve the Pacman web build.")
    parser.add_argument("--host", default="0.0.0.0", help="Bind address. Use 0.0.0.0 on a VM.")
    parser.add_argument("--port", default=8080, type=int, help="HTTP port.")
    parser.add_argument(
        "--directory",
        default=str(Path(__file__).resolve().parent / "web"),
        help="Directory to serve.",
    )
    args = parser.parse_args()

    directory = Path(args.directory).resolve()
    handler = partial(SimpleHTTPRequestHandler, directory=str(directory))
    server = ThreadingHTTPServer((args.host, args.port), handler)

    print(f"Serving {directory}")
    print(f"Open http://{args.host}:{args.port}/")

    try:
        server.serve_forever()
    except KeyboardInterrupt:
        print("\nStopping server.")
    finally:
        server.server_close()


if __name__ == "__main__":
    main()
