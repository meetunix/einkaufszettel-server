#!/usr/bin/env python3

import sys
import time

from eztool.Communicator import Communicator
from eztool.Einkaufszettel import Einkaufszettel


def benchmark(func, *arg):
    print(f"\nBenchmarking --- {func.__name__} ---\n")
    start = time.perf_counter()
    func(*arg)
    elapsed = time.perf_counter() - start
    print(f"executed in {elapsed:0.2f} seconds.")


def main():
    """Main."""
    com = Communicator(sys.argv[1])
    ezl = [Einkaufszettel.random(32) for i in range(10)]
    eids = [ez.eid for ez in ezl]

    benchmark(com.store, ezl)
    benchmark(com.read, eids)

    ezll = [ez.increment_version() for ez in ezl]
    benchmark(com.store, ezll)
    #benchmark(com.delete, eids)


main()
