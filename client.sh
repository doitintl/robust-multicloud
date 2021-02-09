#!/bin/bash
# Act as client of the system: Publish to the relevant topic, then read the result
set -u
set -e
export TOPIC=multicloud_pubsub
# shellcheck disable=SC2155
export BUCKET=multicloud_pubsub-$(gcloud config get-value project)
./bucket_setup.sh

RAND=$RANDOM
STR="x${RAND}"
echo $STR > $STR
echo "Hello, World!" > $STR
gsutil cp $STR gs://$BUCKET
rm $STR
./pubsub_setup.sh
gcloud pubsub topics publish $TOPIC --message "gs://$BUCKET/$STR"
echo "Sleeping 10 s"
sleep 10

gsutil cp gs://$BUCKET/$STR.out  .

cat $STR.out
OUTPUT=$(cat $STR.out| tr -d '\n')
printf "\n"
rm $STR.out
if [[ "${OUTPUT}" != "¡pןɹoM 'oןןǝH" ]]; then
  echo "Did not receive desired output. Was: \"$OUTPUT\""
  exit 1
else
  echo "Success"
  exit 0
fi