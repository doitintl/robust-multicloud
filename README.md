# Robust Multicloud 

This is part of an asynchronous architecture for robustness in the face of cloud instability.

It's also a simple illustration of the use of Kotlin with Gradle in the cloud.

## Set up IAM and bucket
* Run `setup_service_account.sh.sh` 
* This script creates a service account, gives it access to the subscription and the bucket
needed for the scenario. It also creates the topic, subscription and bucket.

## Build and run
Use `build.sh` and  `run.sh`

## Try it
* `client.sh` provides a sample client.
* It publishes a message to topic `multicloud_pubsub`, with the message
content being a `gs://` url to a file.

The app will receive the message, read the referenced file, and create a
new file with the same name and suffix `.out` with the same text, upside down.


