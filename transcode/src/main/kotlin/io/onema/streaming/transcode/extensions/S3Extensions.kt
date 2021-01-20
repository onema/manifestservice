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

package io.onema.streaming.transcode.extensions

import arrow.fx.IO
import arrow.fx.extensions.fx
import com.amazonaws.services.lambda.runtime.events.S3Event
import com.amazonaws.services.lambda.runtime.events.models.s3.S3EventNotification.S3EventNotificationRecord
import kotlinx.coroutines.runBlocking
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import software.amazon.awssdk.services.s3.S3AsyncClient
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import java.io.File

val log: Logger = LoggerFactory.getLogger(S3EventNotificationRecord::class.java)

fun S3EventNotificationRecord.origin(): String = "s3://${bucket()}${path()}"
fun S3EventNotificationRecord.keyOrigin(): String = "s3://${bucket()}/${key()}"
fun S3EventNotificationRecord.bucket(): String = s3.bucket.name
fun S3EventNotificationRecord.key(): String = s3.`object`.urlDecodedKey
fun S3EventNotificationRecord.path(): String = File(s3.`object`.urlDecodedKey).parent
fun S3EventNotificationRecord.name(): String = File(s3.`object`.urlDecodedKey).name
fun S3EventNotificationRecord.nameWithoutExtension(): String = File(s3.`object`.urlDecodedKey).nameWithoutExtension
fun S3EventNotificationRecord.directoryName(): String = File(s3.`object`.urlDecodedKey).parentFile.name
fun S3EventNotificationRecord.metadataName(): String = "${nameWithoutExtension()}.json"
fun S3EventNotificationRecord.framesName(): String = "${nameWithoutExtension()}_frames.json"
fun S3Event.firstRecord(): S3EventNotificationRecord {
    val record = this.records?.first() ?: throw RuntimeException("The S3 event notification record cannot be empty")
    log.info("BUCKET: ${record.bucket()}")
    log.info("KEY: ${record.key()}")
    log.info("NAME: ${record.name()}")
    log.info("NAME W/O EXTENSION: ${record.nameWithoutExtension()}")
    return record
}

fun S3AsyncClient.download(id: String, record: S3EventNotificationRecord): IO<File> = runBlocking {
    val bucket = record.s3.bucket.name
    log.info("DOWNLOADING BUCKET: $bucket KEY: ${record.key()} TO /tmp/${record.name()}")
    val request: GetObjectRequest = GetObjectRequest.builder()
        .bucket(bucket)
        .key(record.key())
        .build()
    val file = File("/tmp/$id${record.name()}")
    IO.fx {
        getObject(request, file.toPath()).get()
        file
    }
}
