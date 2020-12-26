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

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.events.SQSEvent
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.onema.streaming.commons.domain.MetadataInfo
import io.onema.streaming.commons.domain.Segment
import io.onema.streaming.commons.domain.StreamData
import io.onema.streaming.commons.extensions.renditionMetadata
import io.onema.streaming.commons.extensions.renditionSegments
import org.apache.commons.vfs2.FileObject
import org.apache.commons.vfs2.FileSystemManager
import org.apache.commons.vfs2.VFS

class MetadataLoaderFunction : BaseHandler<SQSEvent>() {
    val fsManager: FileSystemManager = VFS.getManager()
    val tableOverride: DynamoDBMapperConfig.TableNameOverride = DynamoDBMapperConfig.TableNameOverride
        .withTableNameReplacement(System.getenv("TABLE_NAME"))
    val dynamoMapper = DynamoDBMapper(
        AmazonDynamoDBClientBuilder.defaultClient(),
        DynamoDBMapperConfig.builder()
            .withTableNameOverride(tableOverride).build()
    )

    override fun handleRequest(event: SQSEvent?, context: Context?) {

        log.info(mapper.writeValueAsString(event))
        event?.records
            ?.map { r -> mapper.readValue<MetadataInfo>(r.body) }
            ?.forEach(this::processMessage)
    }

    fun processMessage(info: MetadataInfo) {
        log.info("BUCKET: ${info.bucket}")
        log.info("KEY: ${info.metadataKey}")
        log.info("KEY: ${info.framesKey}")
        log.info("METADATA ORIGIN ${info.metadataS3Origin()}")
        log.info("FRAMES ORIGIN ${info.frameS3Origin()}")
        log.info("VIDEO ORIGIN ${info.videoS3Origin()}")

        val metadata: FileObject = fsManager.resolveFile(info.metadataS3Origin())
        val frame: FileObject = fsManager.resolveFile(info.frameS3Origin())
        val video: FileObject = fsManager.resolveFile(info.videoS3Origin())

        val files: List<FileObject> = listOf(metadata, frame, video)
        val renditionMetadata: Map<String, StreamData> = files.renditionMetadata(info.videoName)
        val renditionSegments: Map<String, List<Segment>> = files.renditionSegments(renditionMetadata)

        log.info("SAVING STREAM DATA")
        renditionMetadata.values.forEach { streamData ->
            dynamoMapper.save(streamData.format)
            streamData.streams?.forEach {dynamoMapper.save(it)}
        }

        log.info("SAVING SEGMENTS")
        renditionSegments.values.forEach {segments ->
            segments.forEach { dynamoMapper.save(it) }
        }
        log.info("ALL DONE!")
    }
}


//fun main(args: Array<String>) {
//
//    val m = jacksonObjectMapper()
//    val message = SQSEvent.SQSMessage()
//    message.body = m.writeValueAsString(MetadataInfo("one-transcoding-output", "/GOPR9006/hls400k.json", "/GOPR9006/hls400k_frames.json", "run-pepper-run"))
//    val event = SQSEvent()
//    event.records = listOf(message)
//
//    val func = MetadataLoaderFunction()
//    func.handleRequest(event, null)
//
//}