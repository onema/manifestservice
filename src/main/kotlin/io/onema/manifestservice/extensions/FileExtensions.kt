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

package io.onema.manifestservice.extensions

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.onema.manifestservice.domain.FrameData
import io.onema.manifestservice.domain.Segment
import io.onema.manifestservice.domain.StreamData
import org.apache.commons.vfs2.FileObject
import java.io.File

val mapper = jacksonObjectMapper()

/**
 * De-serializes a json file to the given type parameter.
 *
 * De-serializes a file to the type T and returns a key value pair where the key corresponds to the first part
 * of the file name delimited by underscores *"_"*.
 *
 * On a metadata file, the key should match the rendition name e.g. *"1920x1080"*
 *
 * @param T Type that the file will be de-serialized into
 * @return Pair<String, T>
 */
inline fun <reified T> FileObject.metadataMap(): Pair<String, T> {
    val key = nameWithoutExtension.split('_').first()
    return key to mapper.readValue(this.content.inputStream)
}

fun FileObject.isFrame(): Boolean =
    nameWithoutExtension.contains("frames")

/**
 * @param renditionNames list of all the available rendition names
 * @return true if it is a json file and the name corresponds to a valid rendition
 */
fun FileObject.isRendition(renditionNames: List<String>): Boolean =
    nameWithoutExtension in renditionNames && name.extension == "json"

/**
 * Get a list of all the rendition names such as *"1920x1080"*.
 *
 * @return list of strings representing the rendition names
 */
fun List<FileObject>.renditionNames(): List<String> {
    File("").nameWithoutExtension
    val tsFiles = this.filter { it.name.extension == "ts" }
    return tsFiles.map { it.nameWithoutExtension }
}

/**
 * Filters and loads the rendition metadata from the list of files.
 *
 * Returns a map of rendition names to segment data.
 *
 * @return Map<String, StreamData>
 */
fun List<FileObject>.renditionMetadata(): Map<String, StreamData> {
    val renditionNames = this.renditionNames()
    return this
        .filter { file ->  file.isRendition(renditionNames) }
        .map    { file ->  file.metadataMap<StreamData>() }
        .toMap()
}

/**
 * Filters, loads, and transforms the frame files into a list of segments.
 *
 * Returns a map of rendition names to list of segments.
 *
 * @return Map<String, List<Segment>>
 */
fun List<FileObject>.renditionSegments(metaData: Map<String, StreamData>): Map<String, List<Segment>> {
     return this
        .filter { file ->  file.isFrame() }
        .map    { file ->  file.metadataMap<FrameData>() }.toMap()
        .mapValues { (key, frameData) ->
            val streamData = metaData[key] ?: throw RuntimeException("Metadata for $key doesn't exist")
            frameData.buildUpSegments(streamData)
        }
}

fun List<FileObject>.findVideoByRenditionId(renditionId: String): FileObject? {
    return this
        .find { file -> file.nameWithoutExtension.contains(renditionId) && file.name.extension == "ts" }
}

/**
 * Returns a segment of the file given a start and end position
 *
 * @return ByteArray
 */
fun FileObject.segment(start: Int, end: Int): ByteArray {
    val length = end - start + 1
    val bytes = ByteArray(length)
    this.content.inputStream.use {
        it.skip(start.toLong())
        if (end > 0) {
            it.read(bytes, 0, length)
        } else {
            it.read(bytes)
        }
    }
    return bytes
}

val FileObject.nameWithoutExtension: String
    get() =  name.baseName.substringBeforeLast(".")