/**
 * This file is part of the ONEMA transcode Package.
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed
 * with this source code.
 *
 * copyright (c) 2020, Juan Manuel Torres (http://onema.io)
 *
 * @author Juan Manuel Torres <software@onema.io>
 */

package io.onema.streaming.transcode.metadatageneration

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.events.S3Event
import io.onema.streaming.transcode.BaseHandler
import software.amazon.awssdk.services.s3.S3AsyncClient
import software.amazon.awssdk.services.sqs.SqsAsyncClient

class MetadataGenerationFunction : BaseHandler<S3Event, Unit>() {

    //--- Fields ---
      private val execDir = "/opt/bin"
//    private val execDir = "/usr/local/bin"
    private val queueUrl = System.getenv("QUEUE_URL")
    private val s3Client = S3AsyncClient.builder().build()
    private val sqsClient: SqsAsyncClient = SqsAsyncClient.builder().build()
    private val logic = MetadataGenerationLogic(queueUrl, sqsClient, s3Client, execDir, mapper)

    //--- Methods ---
    override fun handleRequest(event: S3Event, context: Context?) = handle {
        log.info(mapper.writeValueAsString(event))
        logic.process(event)
    }
}
