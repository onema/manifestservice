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

package io.onema.streaming.manifestservice.api

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper
import io.onema.streaming.manifestservice.config.DynamoDBMapperConfig
import io.onema.streaming.manifestservice.config.OriginConfig
import io.onema.streaming.commons.extensions.*
import io.onema.streaming.manifestservice.playlist.buildMasterPlaylist
import io.onema.streaming.manifestservice.playlist.buildMediaPlaylist
import io.onema.streaming.manifestservice.service.FileService
import org.apache.commons.vfs2.FileObject
import org.apache.commons.vfs2.FileSystemManager
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Import
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.servlet.config.annotation.EnableWebMvc
import java.net.URI


@RestController
@EnableWebMvc
@Import(value = [OriginConfig::class, DynamoDBMapperConfig::class])
class StreamController(
    private val origin: URI,
    private val fsManager: FileSystemManager,
    private val fileService: FileService,
    private val mapper: DynamoDBMapper) {

    //--- Fields ---
    private val log = LoggerFactory.getLogger(javaClass)
    private val mimeTypeM3U8: String = "application/x-mpegURL"
    private val mimeTypeTS: String = "video/MP2T"

    //--- Methods ---
    @GetMapping("/video/{videoName}/master.m3u8")
    fun masterM3U8(@PathVariable videoName: String): ResponseEntity<String> {

        log.info("/video/$videoName/master")
        val renditionMetadata = mapper.renditionMetadata(videoName)

        return if (renditionMetadata.isEmpty()) {
            log.info("VIDEO NOT FOUND")
            ResponseEntity.notFound().build()
        } else {
            val body = buildMasterPlaylist(videoName, renditionMetadata)
            log.info("VIDEO BODY: \n $body")
            ResponseEntity.ok().header(HttpHeaders.CONTENT_TYPE, mimeTypeM3U8).body(body)
        }
    }

    @GetMapping("/video/{videoName}/media/{mediaName}")
    fun mediaHandler(@PathVariable videoName: String, @PathVariable mediaName: String): ResponseEntity<String> {

        log.info("/video/$videoName/media/$mediaName")
        val renditionSegments = mapper.renditionSegments(videoName, mediaName)
        val body = buildMediaPlaylist(videoName, renditionSegments, mediaName)
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_TYPE, mimeTypeM3U8).body(body)
    }

    @GetMapping("/video/{videoName}/media/segment/{segmentName}")
    fun segmentHandler(
        @PathVariable videoName: String,
        @PathVariable segmentName: String,
        @RequestHeader("range") range: String
    ): ResponseEntity<ByteArray> {

        log.info("/video/$videoName/media/segment/$segmentName $range")
        val dir = fsManager.resolveFile(origin).children.toList()
        val (start, end) = range.splitRange()
        val files: List<FileObject> = dir.first { it.nameWithoutExtension == videoName }.children.toList()
        val file = files.findVideoByRenditionId(segmentName)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Segment $segmentName not found")
        val segment = fileService.readBytes(file, start, end)

        log.info("Sending back ${segment.size} bytes")
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_TYPE, mimeTypeTS)
            .header("Keep-Alive", "timeout=60")
            .body(segment)
    }
}
