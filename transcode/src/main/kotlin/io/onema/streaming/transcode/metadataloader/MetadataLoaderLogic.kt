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

package io.onema.streaming.transcode.metadataloader

import arrow.fx.IO
import arrow.fx.extensions.fx
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper
import com.amazonaws.services.lambda.runtime.events.SQSEvent
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.onema.streaming.commons.domain.MetadataInfo
import io.onema.streaming.commons.domain.Segment
import io.onema.streaming.commons.domain.StreamData
import io.onema.streaming.commons.extensions.renditionMetadata
import io.onema.streaming.commons.extensions.renditionSegments
import io.onema.streaming.transcode.extensions.allRecords
import org.apache.commons.vfs2.FileObject
import org.apache.commons.vfs2.FileSystemManager
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class MetadataLoaderLogic(
    private val fsManager: FileSystemManager,
    private val dynamoMapper: DynamoDBMapper,
    private val mapper: ObjectMapper) {

    //--- Fieds ---
    private val log: Logger = LoggerFactory.getLogger(javaClass)

    //--- Methods ---
    fun process(event: SQSEvent) {
        event
            .allRecords()
            .map { r -> mapper.readValue<MetadataInfo>(r.body) }
            .map { sendMessage(it).unsafeRunSync() }
    }

    private fun sendMessage(info: MetadataInfo): IO<Unit> {
        log.info("BUCKET: ${info.bucket}")
        log.info("KEY: ${info.metadataKey}")
        log.info("KEY: ${info.framesKey}")
        log.info("METADATA ORIGIN ${info.metadataS3Origin()}")
        log.info("FRAMES ORIGIN ${info.frameS3Origin()}")
        log.info("VIDEO ORIGIN ${info.videoS3Origin()}")

        return IO.fx {
            val metadata = fsManager.resolveFileIO(info.metadataS3Origin()).bind()
            val frame = fsManager.resolveFileIO(info.frameS3Origin()).bind()
            val video = fsManager.resolveFileIO(info.videoS3Origin()).bind()

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
        }
    }

    fun FileSystemManager.resolveFileIO(uri: String): IO<FileObject> = IO.fx{
        resolveFile(uri)
    }
}
