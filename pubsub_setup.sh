#!/bin/bash


set -e
if [ -z "$TOPIC" ]
then
  echo "must set TOPIC in env"
  exit 1
fi
set -u
SUB=$TOPIC
gcloud pubsub topics create $TOPIC ||true
gcloud pubsub subscriptions create $SUB --topic $TOPIC ||true