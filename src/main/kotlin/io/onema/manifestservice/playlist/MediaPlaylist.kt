package io.onema.manifestservice.playlist

import io.onema.manifestservice.playlist.MediaPlaylist.MediaSegment

class MediaPlaylist {
    val version: Version = Version()
    val type: PlaylistType = PlaylistType()
    val mediaSequence: MediaSequence = MediaSequence()
    val targetDuration: TargetDuration = TargetDuration()
    val method: Key = Key() name "METHOD" value "NONE"

    private val segments = mutableListOf<MediaSegment>()

    val add = this

    infix fun segment(block: MediaSegment.() -> Unit) {
        segments.add(MediaSegment().apply(block))
    }

    override fun toString(): String = buildString {
        appendLine("#EXTM3U")
        appendLine(version)
        appendLine(type)
        appendLine(mediaSequence)
        appendLine(targetDuration)
        appendLine(method)
        segments.forEach { segment ->
            appendLine(segment)
        }
        appendLine(EndList())
    }

    class MediaSegment {
        var extInf = ExtInf()
        val byteRange = ByteRange()
        var info: Path = SegmentPath()

        override fun toString(): String  = buildString {
            appendLine(extInf)
            appendLine(byteRange)
            append(info)
        }
    }
}

object Media {
    infix fun segment(block: MediaSegment.() -> Unit): MediaSegment = MediaSegment().apply(block)
    infix fun playlist(block: MediaPlaylist.() -> Unit): String = MediaPlaylist().apply(block).toString()
}

typealias media = Media
