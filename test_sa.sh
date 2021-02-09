#!/bin/bash
set -u
set -x
set -e
function revert() {
  [ -n "${original_account}" ] && gcloud config set account "${original_account}"
}

trap "revert" EXIT

original_account=$(gcloud auth list --filter=status:ACTIVE --format="value(account)")
echo "Project $(gcloud config get-value project)"
gcloud auth activate-service-account  --key-file=./service-account.json
#gcloud auth activate-service-account  --key-file=./temp1.json

gsutil list gs://multicloud_pubsub-joshua-playground
RAND=$RANDOM
echo "x${RAND}">x${RAND}
gsutil cp x${RAND} gs://multicloud_pubsub-joshua-playground/
echo "copied to cloud"
gsutil cp gs://multicloud_pubsub-joshua-playground/x$RAND xplus
