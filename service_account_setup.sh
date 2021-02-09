#!/bin/bash
set -u
set -e
PROJECT_ID=$(gcloud config get-value project)
SERVICE_ACCOUNT_NAME=multicloud-pubsub-sa

export  SA_FULLID
SA_FULLID=${SERVICE_ACCOUNT_NAME}@${PROJECT_ID}.iam.gserviceaccount.com

export TOPIC
TOPIC=multicloud_pubsub
SUB=$TOPIC

./pubsub_setup.sh

# shellcheck disable=SC2155
export BUCKET=multicloud_pubsub-$(gcloud config get-value project)
./bucket_setup.sh

gcloud iam service-accounts create $SERVICE_ACCOUNT_NAME \
    --description="For this example https://github.com/doitintl/robust-multicloud" \
    ||true


export ETAG
ETAG=$(gcloud pubsub subscriptions get-iam-policy projects/${PROJECT}/subscriptions/${SUB} --format json \
        |jq  --raw-output ".etag" )


gsutil iam ch serviceAccount:${SA_FULLID}:roles/storage.objectAdmin gs://${BUCKET}

function rm_generated_file() {
   rm subscription_policy.json
}

trap "rm_generated_file" EXIT

envsubst < "subscription_policy.json.base" > "subscription_policy.json"

gcloud pubsub subscriptions set-iam-policy \
  projects/${PROJECT}/subscriptions/${SUB} \
  subscription_policy.json

gcloud iam service-accounts keys create ./service-account.json  --iam-account  ${SA_FULLID}