#!/usr/bin/env python3

import argparse
import sys
from pathlib import Path

from eztool.Einkaufszettel import Einkaufszettel

ezids = [
    ("18aede70-a704-449d-a23f-ece30998ab34", 2),
    ("6d72f704-ffc4-44a8-aaf3-aac99b90174d", 17),
    ("aeb6c57f-49a1-4b46-88ee-ca45c0053bbc", 32),
    ("053a00c4-42f8-4632-8dd4-40179a66ea0a", 3),
    ("fe6cc8c7-a948-42df-be7f-5c761fbdf78e", 5),
    ("c9df0d84-7c43-4329-b0e7-878e04d0b5a5", 127),
    ("39296a8d-bb49-4908-9296-bf00f712afb0", 16),
    ("d4b70003-2050-4fc1-8c9a-000529806dad", 8),
    ("1861b947-3092-4cae-a0a3-40a6fa6904b3", 64),
    ("bafbadd4-c583-4981-a223-1463c3d59b10", 31)
]


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--export-path", type=Path, required=True, help="directory where the ezs are written to")
    args = parser.parse_args()

    path: Path = args.export_path
    if not path.exists() or not path.is_dir():
        sys.stdout.write(f"{path} is not a directory")

    idx = 0
    for eid, items in ezids:
        ez = Einkaufszettel.random(items)
        ez.eid = eid
        file_path = path / Path(f"ez-{idx:02}.json")
        file_path.write_text(ez.get_json())
        idx += 1


if __name__ == "__main__":
    main()
