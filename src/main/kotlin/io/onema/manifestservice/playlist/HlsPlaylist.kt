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

fun buildMasterPlaylist(videoName: String, renditionMetadata: Map<String, StreamData>): String {
    return master playlist {
        version set 5
        renditionMetadata.forEach { renditionId, md ->
            stream {
                streamInf resolution md.resolution codecs md.codecs bandwidth md.bandwidth frameRate md.frameRate
                info name videoName rendition renditionId
            }
        }
    }
}

fun buildMediaPlaylist(videoName: String, segments: List<Segment>, renditionId: String): String {
    return media playlist {
        version set 5
        type set PlaylistTypeEnum.VOD
        mediaSequence set 0
        targetDuration set 6

        segments.forEach { segment ->
            add segment {
                extInf duration segment.duration()
                byteRange length segment.length position segment.position
                info name videoName rendition renditionId
            }
        }
        method value "NONE"
    }
}

