/*
 * Copyright 2018 Google LLC.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.doitintl.multicloud


import com.google.api.gax.rpc.ApiException
import com.google.cloud.ServiceOptions
import com.google.cloud.pubsub.v1.*
import com.google.cloud.storage.Blob
import com.google.cloud.storage.Storage
import com.google.cloud.storage.StorageOptions
import com.google.pubsub.v1.ProjectSubscriptionName
import com.google.pubsub.v1.ProjectTopicName
import com.google.pubsub.v1.PubsubMessage
import com.google.pubsub.v1.PushConfig

import java.nio.charset.StandardCharsets.UTF_8
import java.util.concurrent.LinkedBlockingDeque


private val projectId = ServiceOptions.getDefaultProjectId()!!
val storage: Storage = StorageOptions.getDefaultInstance().service


private fun createTopic(topicId: String) { // expects 1 arg: <topic> to create

    val topic = ProjectTopicName.of(projectId, topicId)

    try {
        TopicAdminClient.create().use { topicAdminClient -> topicAdminClient.createTopic(topic) }
        println("Topic ${topic.project}:${topic.topic} successfully created.")
    } catch (e: ApiException) {
        println("Error in createTopic ${e.statusCode.code}")
        //TODO if (e.statusCode.code==ALREADY_EXISTS){ OK} else{ throw e}
    }
}

private fun subscribeTopic(topicId: String, subscriptionId: String) {


    val topicName = ProjectTopicName.of(projectId, topicId)

    val subscriptionName = ProjectSubscriptionName.of(projectId, subscriptionId)

    try {
        SubscriptionAdminClient.create().use { subscriptionAdminClient ->
            // create a pull subscription with default acknowledgement deadline (= 10 seconds)
            subscriptionAdminClient.createSubscription(subscriptionName, topicName, PushConfig.getDefaultInstance(), 0)
        }
        println("Subscription ${subscriptionName.project}:${subscriptionName.subscription} successfully created.")
    } catch (e: ApiException) {

        println("Error in subscribeTopic ${e.statusCode.code}")
        //TODO if (e.statusCode.code==ALREADY_EXISTS){ OK} else{ throw e}
    }
}


private fun listenToSub(subscriptionId: String) {

    val messages = LinkedBlockingDeque<PubsubMessage>()

    class MessageReceiverExample : MessageReceiver {
        override fun receiveMessage(message: PubsubMessage, consumer: AckReplyConsumer) {
            //Message from PubSub are placed on an in-memory queue
            messages.offer(message)
            consumer.ack()
        }
    }


    val subscriptionName = ProjectSubscriptionName.of(projectId, subscriptionId)
    lateinit var subscriber: Subscriber
    try {
        // create a subscriber bound to the asynchronous message receiver
        subscriber = Subscriber.newBuilder(subscriptionName, MessageReceiverExample()).build()
        subscriber.startAsync().awaitRunning()
        // Continue to listen to messages
        println("Listening to in-memory queue with messages received on $subscriptionId")
        while (true) {
            try {
                val message = messages.take()
                processMessage(message)
            } catch (e: Exception) {
                println("Error in msg loop $e")
            }
        }
    } catch (e: Exception) {
        println("Error in msg loop $e")
    } finally {
        subscriber.stopAsync()
    }
}

private fun processMessage(message: PubsubMessage) {
    val messageS = message.data.toStringUtf8()
    val gsPfx = "gs://"
    assert(messageS.startsWith(gsPfx))
    val noPfx = messageS.substring(gsPfx.length)
    val idx = noPfx.indexOf("/")
    assert(idx > 0)
    val bucket = noPfx.substring(0, idx)
    val file = noPfx.substring(idx + 1)

    println("Processing bucket $bucket, file $file")

    val flipped: String = flip(String(download(bucket, file), UTF_8))

    upload(bucket, flipped.toByteArray(UTF_8), file + ".out")
}


private fun upload(bucketName: String, bytes: ByteArray, blobName: String) {
    val bucket = storage.get(bucketName) ?: error("Bucket $bucketName does not exist")

    bucket.create(blobName, bytes)
    println("$blobName was uploaded to bucket $bucketName.")
}

private fun download(bucketName: String, blobName: String): ByteArray {
    val bucket = storage.get(bucketName) ?: error("Bucket $bucketName does not exist")

    val blob: Blob = bucket.get(blobName) ?: error("Blob $blobName does not exist")

    println("$blobName was downloaded from bucket $bucketName.")
    return blob.getContent()
}


fun main(vararg argsIn: String) {
    val topic = "topic1"
    createTopic(topic)
    val sub = "subscription1"
    subscribeTopic(topic, sub)
    listenToSub(sub)
}