#!/usr/bin/env bash

# This script generates a file with some of the same curl command and
# grnu-parallel invokes them in parallel.

ITERATIONS=200

FILE="./delete_example.json"
URL="https://$1/r0/ez/76f2c9e4-ea57-4df6-bdbf-cc7a5301df80"

COMMAND="curl -s -H \"Content-Type: application/json\" -XPUT -s $URL --data-binary @$FILE > /dev/null"

for i in `seq 1 $ITERATIONS` ; do echo $COMMAND ; done > jobs_file

echo -e "\n--- parallel execution ---"
time parallel --jobs 4 < jobs_file

echo -e "\n--- serial execution ---"

time for i in `seq 1 $ITERATIONS`
do
    eval $COMMAND
done

rm jobs_file
