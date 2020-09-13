///**
// * This file is part of the ONEMA manifestservice Package.
// * For the full copyright and license information,
// * please view the LICENSE file that was distributed
// * with this source code.
// *
// * copyright (c) 2020, Juan Manuel Torres (http://onema.io)
// *
// * @author Juan Manuel Torres <software@onema.io>
// */
//
//package io.onema.manifestservice.api
//
//import io.onema.manifestservice.domain.Segment
//import io.onema.manifestservice.domain.StreamData
//import io.onema.manifestservice.extensions.*
//import io.onema.manifestservice.playlist.buildMasterPlaylist
//import io.onema.manifestservice.playlist.buildMediaPlaylist
//import org.apache.commons.vfs2.FileObject
//import org.slf4j.LoggerFactory
//import org.springframework.http.HttpHeaders
//import org.springframework.http.HttpStatus.NOT_FOUND
//import org.springframework.http.ResponseEntity
//import org.springframework.web.bind.annotation.GetMapping
//import org.springframework.web.bind.annotation.PathVariable
//import org.springframework.web.bind.annotation.RequestHeader
//import org.springframework.web.bind.annotation.RestController
//import org.springframework.web.server.ResponseStatusException
//
//@RestController
//class StreamController(final val files: List<FileObject>) {
//
//    private val log = LoggerFactory.getLogger(javaClass)
//    private val renditionMetadata: Map<String, StreamData>
//    private val renditionSegments: Map<String, List<Segment>>
//    private val renditionKeys: List<String>
//    private val mimeTypeM3U8: String = "application/x-mpegURL"
//    private val mimeTypeTS: String = "video/MP2T"
//
//    init {
//        renditionKeys = files.renditionNames()
//        renditionMetadata = files.renditionMetadata()
//        renditionSegments= files.renditionSegments(renditionMetadata)
//        log.info("All done initializing controller")
//    }
//
//    @GetMapping("/master.m3u8")
//    fun masterM3U8(): ResponseEntity<String> {
//
//        log.info("/master.m3u8")
//        val body = buildMasterPlaylist(renditionKeys, renditionMetadata)
//
//        return ResponseEntity.ok().header(HttpHeaders.CONTENT_TYPE, mimeTypeM3U8).body(body)
//    }
//
//    @GetMapping("/media/{renditionId}.m3u8")
//    fun mediaHandler(@PathVariable renditionId: String): ResponseEntity<String> {
//
//        log.info("/media/$renditionId.m3u8")
//        val segments = renditionSegments[renditionId]
//            ?: throw ResponseStatusException(NOT_FOUND, "Invalid rendition ID $renditionId")
//
//        val body = buildMediaPlaylist(segments, renditionId)
//        return ResponseEntity.ok().header(HttpHeaders.CONTENT_TYPE, mimeTypeM3U8).body(body)
//    }
//
//    @GetMapping("/segment/{renditionId}.ts")
//    fun segmentHandler(@PathVariable renditionId: String, @RequestHeader("range") range: String): ResponseEntity<ByteArray> {
//
//        log.info("/segment/$renditionId.ts")
//        val (start, end) = range.splitRange()
//        val file = files.findVideoByRenditionId(renditionId)
//            ?: throw ResponseStatusException(NOT_FOUND, "Segment $renditionId not found")
//
//        return ResponseEntity.ok().header(HttpHeaders.CONTENT_TYPE, mimeTypeTS).body(file.segment(start, end))
//    }
//}
