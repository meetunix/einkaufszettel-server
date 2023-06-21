#!/usr/bin/env bash

HOST="$1"

if [[ -z $HOST ]] ; then
  HOST="http://127.0.0.1:18080"
fi

for file_name in `ls -1 examples/`
do

  file="examples/$file_name"
  URL="$HOST/r0/ez/$(cat $file | jq '.eid' | tr -d '"')"
  echo $URL

  curl -H "Content-Type: application/json" -XDELETE -s $URL

  curl -v -H "Content-Type: application/json" -XPUT -s $URL --data @$file

  curl -v -H "Accept: application/json" -XGET -s $URL
  echo

done
