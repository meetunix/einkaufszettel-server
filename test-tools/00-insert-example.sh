#!/usr/bin/env bash


FILE="./example.json"
URL="$1/r0/ez/76f2c9e4-ea57-4df6-bdbf-cc7a5301df80"

curl -v -H "Content-Type: application/json" -XPUT -s $URL --data @$FILE

curl -v -H "Accept: application/json" -XGET -s $URL
