/**
 * This file is part of the ONEMA manifestservice Package.
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed
 * with this source code.
 *
 * copyright (c) 2020, Juan Manuel Torres (http://onema.io)
 *
 * @author Juan Manuel Torres <software@onema.io>
 */

package io.onema.manifestservice.api

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper
import io.onema.manifestservice.config.DynamoDBMapperConfig
import io.onema.manifestservice.config.OriginConfig
import io.onema.manifestservice.domain.Segment
import io.onema.manifestservice.domain.StreamData
import io.onema.manifestservice.extensions.nameWithoutExtension
import io.onema.manifestservice.extensions.renditionMetadata
import io.onema.manifestservice.extensions.renditionSegments
import org.apache.commons.vfs2.FileObject
import org.springframework.context.annotation.Import
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.config.annotation.EnableWebMvc

@RestController
@EnableWebMvc
@Import(value = [OriginConfig::class, DynamoDBMapperConfig::class])
class IngestController(private val dir: List<FileObject>, private val mapper: DynamoDBMapper) {

    @PostMapping("/video")
    fun loadVideo(@RequestBody video: VideoInfo): ResponseEntity<String> {

        val files: List<FileObject> = dir.first { it.nameWithoutExtension == video.name }.children.toList()
        val renditionMetadata: Map<String, StreamData> = files.renditionMetadata(video.name)
        val renditionSegments: Map<String, List<Segment>> = files.renditionSegments(renditionMetadata)

        renditionMetadata.values.forEach { streamData ->
            mapper.save(streamData.format)
            streamData.streams?.forEach {mapper.save(it)}
        }

        renditionSegments.values.forEach {segments ->
            segments.forEach { mapper.save(it) }
        }

        return ResponseEntity.ok("OK")
    }
}

data class VideoInfo(val name: String)