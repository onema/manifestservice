/**
 * This file is part of the ONEMA streaming Package.
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed
 * with this source code.
 *
 * copyright (c) 2021, Juan Manuel Torres (http://onema.io)
 *
 * @author Juan Manuel Torres <software@onema.io>
 */

package io.onema.streaming.transcode.metadatageneration

import arrow.fx.IO
import arrow.fx.extensions.fx
import com.amazonaws.services.lambda.runtime.events.S3Event
import com.amazonaws.services.lambda.runtime.events.models.s3.S3EventNotification.S3EventNotificationRecord
import com.fasterxml.jackson.databind.ObjectMapper
import io.onema.streaming.commons.domain.MetadataInfo
import io.onema.streaming.transcode.extensions.*
import software.amazon.awssdk.services.s3.S3AsyncClient
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import software.amazon.awssdk.services.sqs.SqsAsyncClient
import software.amazon.awssdk.services.sqs.model.SendMessageRequest
import software.amazon.awssdk.services.sqs.model.SendMessageResponse
import java.io.File
import java.util.*

class MetadataGenerationLogic(
    private val queueUrl: String,
    private val sqsClient: SqsAsyncClient,
    private val s3Client: S3AsyncClient,
    private val execDir: String,
    private val mapper: ObjectMapper
) {

    //--- Methods ---
    fun process(event: S3Event): Unit = IO.fx {
        val record =  event.firstRecord()
        val id = UUID.randomUUID().toString()
        val (metadataFile, framesFile) = files(record, id)
        val videoFile = s3Client.download(id, record).bind()
        runCommands(videoFile, metadataFile, framesFile).bind()
        val info = upload(record, metadataFile, framesFile).bind()
        sendMessage(info).bind()
        cleanup(listOf(videoFile, metadataFile, framesFile)).bind()
    }.unsafeRunSync()

    private fun files(record: S3EventNotificationRecord, id: String): Pair<File, File> =
        File("/tmp/$id${record.metadataName()}") to File("/tmp/$id${record.framesName()}")

    private fun cleanup(files: List<File>): IO<Unit> {
        log.info("CLEANING UP!")
        return IO.fx {
            files.map(File::delete).all { it }
        }
    }

    private fun runCommands(videoFile: File, metadataFile: File, framesFile: File): IO<Unit> {
        log.info("GENERATING METADATA")
        log.info("METADATA: ${metadataFile.name}")
        log.info("FRAMES: ${framesFile.name}")
        val cmd1 = "$execDir/ffprobe -hide_banner -show_streams -show_format -print_format json ${videoFile.path}"
        val cmd2 = "$execDir/ffprobe -hide_banner -show_frames -print_format json ${videoFile.path}"
        return IO.fx {
            cmd1.runCommand(metadataFile)
            cmd2.runCommand(framesFile)
        }
    }

    private fun upload(record: S3EventNotificationRecord, metadataFile: File, framesFile: File): IO<MetadataInfo> {
        log.info("SAVING METADATA TO S3")
        val info = MetadataInfo(
            bucket = record.bucket(),
            metadataKey = "${record.path()}/${record.metadataName()}",
            framesKey = "${record.path()}/${record.framesName()}", record.directoryName())
        return IO.fx {
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
            metadataFuture.get()
            framesFuture.get()
            info
        }
    }

    private fun sendMessage(info: MetadataInfo): IO<SendMessageResponse> = IO.fx {
        log.info("SENDING SQS MESSAGE")
        val request = SendMessageRequest.builder()
            .queueUrl(queueUrl)
            .messageBody(mapper.writeValueAsString(info))
            .build()
        sqsClient.sendMessage(request).get()
    }

    private fun String.runCommand(output: File) {
        val parts = this.split("\\s".toRegex())
        val proc = ProcessBuilder(*parts.toTypedArray())
            .redirectOutput(output)
            .redirectError(ProcessBuilder.Redirect.PIPE)
            .start()
        proc.waitFor()
    }
}