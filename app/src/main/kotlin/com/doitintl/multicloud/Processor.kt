package com.doitintl.multicloud


import com.google.cloud.ServiceOptions
import com.google.cloud.pubsub.v1.AckReplyConsumer
import com.google.cloud.pubsub.v1.MessageReceiver
import com.google.cloud.pubsub.v1.Subscriber
import com.google.cloud.storage.*
import com.google.pubsub.v1.ProjectSubscriptionName
import com.google.pubsub.v1.PubsubMessage
import java.nio.charset.StandardCharsets.UTF_8
import java.nio.file.Files
import java.nio.file.Paths
import java.util.concurrent.LinkedBlockingDeque


private val projectId = ServiceOptions.getDefaultProjectId()!!


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
        // Not listening directly to sub, but rather to in-memory queue that gets msgs from sub
        while (true) {
            try {
                val message = messages.take()
                processMessage(message)
            } catch (e: Exception) {
                println("Error in msg loop: $e")
            }
        }
    } catch (e: Exception) {
        println("Error outside msg loop: $e")
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

    val flipped: String = flip(String(download(bucket, file), UTF_8))

    upload(bucket, flipped.toByteArray(UTF_8), file + ".out")
}


private fun upload(bucketName: String, bytes: ByteArray, blobName: String) {

    val storage: Storage = StorageOptions.getDefaultInstance().service

    val blobId = BlobId.of(bucketName, blobName)
    val blobInfo = BlobInfo.newBuilder(blobId).build()
    storage.create(blobInfo, bytes)

    println("Upload gs://$bucketName/$blobName")
}

private fun download(bucketName: String, blobName: String): ByteArray {
    val storage: Storage = StorageOptions.getDefaultInstance().service


    val blob: Blob = storage.get(BlobId.of(bucketName, blobName))
    val path = Paths.get(blobName)
    blob.downloadTo(path)

    val bytes = Files.readAllBytes(path)
    Files.delete(path)
    println("Get gs://$bucketName/$blobName")
    return bytes
}


fun main(vararg argsIn: String) {
    val sub = "multicloud_pubsub"
    println("Project $projectId, subscription $sub")
    listenToSub(sub)
}