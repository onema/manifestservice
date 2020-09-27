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

import java.lang.StringBuilder

class HlsDsl {
    private val builder = StringBuilder()
    init {
        with(builder) {
            appendLine("#EXTM3U")
        }
    }

    fun VERSION(version: Int) {
        builder.appendLine("#EXT-X-VERSION:$version")
    }

    fun PLAYLIST_TYPE(type: String) {
        builder.appendLine("#EXT-X-PLAYLIST-TYPE:$type")
    }

    fun MEDIA_SEQUENCE(sequence: Int) {
        builder.appendLine("#EXT-X-MEDIA-SEQUENCE:$sequence")
    }

    fun TARGETDURATION(duration: Int) {
        builder.appendLine("#EXT-X-TARGETDURATION:$duration")
    }

    fun KEY(key: String, value: String) {
        builder.appendLine("#EXT-X-KEY:${key.toUpperCase()}=${value.toUpperCase()}")
    }

    fun STREAM_INF(resolution: String, codecs: String, bandwidth: String, frameRate: Float) {
        builder.appendLine("""#EXT-X-STREAM-INF:RESOLUTION=$resolution,CODECS="$codecs",BANDWIDTH=$bandwidth,FRAME-RATE=$frameRate""")
    }

    fun media(videoName: String, name: String) {
        builder.appendLine("/video/$videoName/media/$name")
    }

    fun EXTINF(duration: Float) {
       builder.appendLine("#EXTINF:${duration}")
    }

    fun BYTERANGE(length: Int, position: Int) {
       builder.appendLine("#EXT-X-BYTERANGE:${length}@${position}")
    }

    fun segment(videoName: String, renditionId: String) {
       builder.appendLine("/video/$videoName/segment/${renditionId}")
    }

    fun ENDLIST() {
        builder.appendLine("#EXT-X-ENDLIST")
    }

    override fun toString(): String = builder.toString()
}

fun playlist(action: HlsDsl.() -> Unit ): String {
    return HlsDsl().apply(action).toString()
}

fun mediaPlaylist(type: String = "VOD", sequence: Int = 0, duration: Int = 6, action: HlsDsl.() -> Unit): String {
    return playlist {
        PLAYLIST_TYPE(type)
        MEDIA_SEQUENCE(sequence)
        TARGETDURATION(duration)
        KEY("METHOD", "NONE")
        action()
        ENDLIST()
    }
}