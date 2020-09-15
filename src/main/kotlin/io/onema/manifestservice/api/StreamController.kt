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
import io.onema.manifestservice.extensions.*
import io.onema.manifestservice.playlist.buildMasterPlaylist
import io.onema.manifestservice.playlist.buildMediaPlaylist
import org.apache.commons.vfs2.FileObject
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Import
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.servlet.config.annotation.EnableWebMvc


@RestController
@EnableWebMvc
@Import(value = [OriginConfig::class, DynamoDBMapperConfig::class])
class StreamController(private val files: List<FileObject>, private val mapper: DynamoDBMapper) {

    private val log = LoggerFactory.getLogger(javaClass)
    private val renditionMetadata: Map<String, StreamData> = files.renditionMetadata()
    private val renditionSegments: Map<String, List<Segment>> = files.renditionSegments(renditionMetadata)
    private val mimeTypeM3U8: String = "application/x-mpegURL"
    private val mimeTypeTS: String = "video/MP2T"

    @GetMapping("/master.m3u8")
    fun masterM3U8(): ResponseEntity<String> {
        log.info("/master.m3u8")

        val body = buildMasterPlaylist(renditionMetadata)
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_TYPE, mimeTypeM3U8).body(body)
    }

    @GetMapping("/media/{mediaName}")
    fun mediaHandler(@PathVariable mediaName: String): ResponseEntity<String> {
        log.info("/media/$mediaName.m3u8")

        val segments = renditionSegments[mediaName]
            ?: throw ResponseStatusException(NOT_FOUND, "Invalid rendition ID $mediaName")
        val body = buildMediaPlaylist(segments, mediaName)
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_TYPE, mimeTypeM3U8).body(body)
    }

    @GetMapping("/segment/{segmentName}")
    fun segmentHandler(@PathVariable segmentName: String, @RequestHeader("range") range: String): ResponseEntity<ByteArray> {
        log.info("/segment/$segmentName.ts")
        val (start, end) = range.splitRange()
        val file = files.findVideoByRenditionId(segmentName)
            ?: throw ResponseStatusException(NOT_FOUND, "Segment $segmentName not found")

        return ResponseEntity.ok().header(HttpHeaders.CONTENT_TYPE, mimeTypeTS).body(file.segment(start, end))
    }
}
