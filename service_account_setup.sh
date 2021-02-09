#!/bin/bash
set -u
set -x
set -e
PROJECT_ID=$(gcloud config get-value project)
SERVICE_ACCOUNT_NAME=multicloud-pubsub
SA_FULLID=${SERVICE_ACCOUNT_NAME}@${PROJECT_ID}.iam.gserviceaccount.com

# shellcheck disable=SC2155
export BUCKET=multicloud_pubsub-$(gcloud config get-value project)
./bucket_setup.sh

gcloud iam service-accounts create $SERVICE_ACCOUNT_NAME \
    --description="For this example https://github.com/doitintl/robust-multicloud" \
    ||true

#TODO limit just to the one topic
gcloud projects add-iam-policy-binding $PROJECT_ID --member=serviceAccount:$SA_FULLID \
    --role="roles/pubsub.subscriber"

gsutil iam ch serviceAccount:$SA_FULLID:roles/storage.objectCreator gs://$BUCKET
gsutil iam ch serviceAccount:$SA_FULLID:roles/storage.objectViewer gs://$BUCKET

gcloud iam service-accounts keys create ./service-account.json  --iam-account  $SA_FULLID