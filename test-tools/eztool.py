#!/usr/bin/env python3

import time

from eztool.Communicator import Communicator
from eztool.Einkaufszettel import Einkaufszettel


def main():

    com = Communicator("https://ez.nachtsieb.de/")

    start = time.perf_counter()

    ez = Einkaufszettel.random(2)
    print(ez.get_json())
    com.store(ez)

    elapsed = time.perf_counter() - start
    print(f"executed in {elapsed:0.2f} seconds.")


main()
