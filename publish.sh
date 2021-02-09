#!/bin/bash
# Act as client of the system: Publish to the relevant topic
set -u
set -x
set -e
export TOPIC=multicloud_pubsub
# shellcheck disable=SC2155
export BUCKET=multicloud_pubsub-$(gcloud config get-value project)
./bucket_setup.sh

echo "Hello, World!">hello
gsutil cp hello gs://$BUCKET
rm hello
./pubsub_setup.sh
gcloud pubsub topics publish $TOPIC --message "gs://$BUCKET/hello"