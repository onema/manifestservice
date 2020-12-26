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

package io.onema.streaming.transcode

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.events.S3Event
import com.amazonaws.services.lambda.runtime.events.models.s3.S3EventNotification.*
import io.onema.streaming.commons.domain.MetadataInfo
import io.onema.streaming.transcode.extensions.*
import kotlinx.coroutines.future.await
import kotlinx.coroutines.runBlocking
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

class MetadataGenerationFunction : BaseHandler<S3Event>() {
    //--- Constants ---
//    val execDir = "/usr/local/bin"
    private val execDir = "/opt/bin"
    private val queue = System.getenv("QUEUE_ARN").split(':').last()

    //--- Methods ---
    override fun handleRequest(event: S3Event?, context: Context?) {
        val record = event?.firstRecord() ?: throw RuntimeException("S3Event record is null")
        log.info(mapper.writeValueAsString(event))
        log.info("BUCKET: ${record.bucket()}")
        log.info("KEY: ${record.key()}")
        log.info("NAME: ${record.name()}")
        log.info("NAME W/O EXTENSION: ${record.nameWithoutExtension()}")

        log.info("DOWNLOADING VIDEO")
        val id = java.util.UUID.randomUUID().toString()
        val videoFile = record.download(id)
        val metadataFile = File("/tmp/$id${record.metadataName()}")
        log.info("METADATA: ${record.metadataName()}")
        val framesFile = File("/tmp/$id${record.framesName()}")
        log.info("FRAMES: ${record.framesName()}")

        runCommands(videoFile, metadataFile, framesFile)

        log.info("SAVING METADATA TO S3")
        val info = upload(record, metadataFile, framesFile)

        log.info("SENDING SQS MESSAGE")
        sqsClient.sendMessage(queue, mapper.writeValueAsString(info))

        log.info("CLEANING UP!")
        metadataFile.delete()
        framesFile.delete()
        videoFile.delete()

        log.info("ALL DONE!")
    }

    private fun runCommands(videoFile: File, metadataFile: File, framesFile: File) {
        log.info("GENERATING METADATA")
        val cmd1 = "$execDir/ffprobe -hide_banner -show_streams -show_format -print_format json ${videoFile.path}"
        val cmd2 = "$execDir/ffprobe -hide_banner -show_frames -print_format json ${videoFile.path}"
        cmd1.runCommand(metadataFile)
        cmd2.runCommand(framesFile)
    }

    private fun upload(record: S3EventNotificationRecord, metadataFile: File, framesFile: File): MetadataInfo = runBlocking {
        val info = MetadataInfo(
            bucket = record.bucket(),
            metadataKey = "${record.path()}/${record.metadataName()}",
            framesKey = "${record.path()}/${record.framesName()}", record.directoryName())
        val metadataFuture = s3Client.putObject(
            PutObjectRequest.builder().bucket(record.bucket()).key(info.metadataKey).build(),
            metadataFile.toPath()
        )
        metadataFuture.whenComplete { _, _ ->  log.info("METADATA UPLOADED")}

        val framesFuture = s3Client.putObject(
            PutObjectRequest.builder().bucket(record.bucket()).key(info.framesKey).build(),
            framesFile.toPath()
        )
        framesFuture.whenComplete { _, _ ->  log.info("FRAMES UPLOADED")}
        metadataFuture.await()
        framesFuture.await()
        info
    }

    fun String.runCommand(output: File) {
        try {
            val parts = this.split("\\s".toRegex())
            val proc = ProcessBuilder(*parts.toTypedArray())
                .redirectOutput(output)
                .redirectError(ProcessBuilder.Redirect.PIPE)
                .start()

            proc.waitFor(300, TimeUnit.SECONDS)
        } catch(e: IOException) {
            e.printStackTrace()
        }
    }
}
