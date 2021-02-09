#!/bin/bash

set -x
set -e
if [ -z "$BUCKET" ]
then
  echo "must set BUCKET in env"
  exit 1
fi
set -u
gsutil mb -b on gs://$BUCKET ||true