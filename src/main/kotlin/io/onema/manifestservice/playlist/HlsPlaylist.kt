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

package io.onema.manifestservice.playlist

import io.onema.manifestservice.domain.Segment
import io.onema.manifestservice.domain.StreamData

fun buildMasterPlaylist(renditionKeys: List<String>, renditionMetadata: Map<String, StreamData>): String {
    return playlist {
        renditionKeys.forEach { name ->
            val metadata: StreamData = renditionMetadata[name] ?: throw RuntimeException("Unable to find metadata for $name")
            STREAM_INF(metadata.resolution, metadata.codecs, metadata.bandwidth, metadata.frameRate)
            media(name)
        }
    }
}

fun buildMediaPlaylist(segments: List<Segment>, renditionId: String): String {
     return mediaPlaylist {
        segments.forEach { segment ->
            EXTINF(segment.duration())
            BYTERANGE(segment.length, segment.position)
            segment(renditionId)
        }
    }
}

