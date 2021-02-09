#!/bin/bash
# Act as client of the system: Publish to the relevant topic, then read the result
set -u
set -e
export TOPIC=multicloud_pubsub
# shellcheck disable=SC2155
export BUCKET=multicloud_pubsub-$(gcloud config get-value project)
./bucket_setup.sh

RAND=$RANDOM
FILENAME="x${RAND}"
echo "Hello, World!" > $FILENAME
gsutil cp $FILENAME gs://$BUCKET
rm $FILENAME
./pubsub_setup.sh
gcloud pubsub topics publish $TOPIC --message "gs://$BUCKET/$FILENAME"
echo "Sleeping 10 s"
sleep 10

gsutil cp gs://$BUCKET/$FILENAME.out  .

cat $FILENAME.out
OUTPUT=$(cat $FILENAME.out| tr -d '\n')
printf "\n"
rm $FILENAME.out
if [[ "${OUTPUT}" != "¡pןɹoM 'oןןǝH" ]]; then
  echo "Did not receive desired output. Was: \"$OUTPUT\""
  exit 1
else
  echo "Success"
  exit 0
fi