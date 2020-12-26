/**
 * This file is part of the ONEMA streaming Package.
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed
 * with this source code.
 *
 * copyright (c) 2020, Juan Manuel Torres (http://onema.io)
 *
 * @author Juan Manuel Torres <software@onema.io>
 */

package io.onema.streaming.transcode

import com.amazonaws.services.lambda.runtime.RequestHandler
import com.amazonaws.services.lambda.runtime.events.models.s3.S3EventNotification
import com.amazonaws.services.sqs.AmazonSQS
import com.amazonaws.services.sqs.AmazonSQSClientBuilder
import com.fasterxml.jackson.datatype.joda.JodaModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.onema.streaming.transcode.extensions.key
import io.onema.streaming.transcode.extensions.name
import kotlinx.coroutines.future.await
import kotlinx.coroutines.runBlocking
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import software.amazon.awssdk.services.s3.S3AsyncClient
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import software.amazon.awssdk.services.s3.model.GetObjectResponse
import java.io.File
import java.nio.file.Path
import java.util.*
import java.util.concurrent.CompletableFuture

abstract class BaseHandler<TEvent> : RequestHandler<TEvent, Unit> {
    //--- Fields ---
    protected val s3Client = S3AsyncClient.builder().build()
    protected val sqsClient: AmazonSQS = AmazonSQSClientBuilder.defaultClient()
    protected val log: Logger = LoggerFactory.getLogger(javaClass)
    protected val mapper = jacksonObjectMapper()

    //--- Constructors ---
    init {
        mapper.registerModule(JodaModule())
    }

    //--- Methods ---
    fun S3EventNotification.S3EventNotificationRecord.download(id: String): File = runBlocking {
        val bucket = s3.bucket.name
        log.info("DOWNLOADING BUCKET: $bucket KEY: ${key()} TO /tmp/${name()}")
        val request: GetObjectRequest = GetObjectRequest.builder()
            .bucket(bucket)
            .key(key())
            .build()
        val file = File("/tmp/$id${name()}")
        val response =  s3Client
            .getObject(request, file.toPath())
            .await()
        file
    }
}
