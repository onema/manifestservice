package io.onema.manifestservice.playlist

import java.lang.StringBuilder

abstract class HlsTag {
    abstract val type: String
    protected val builder = StringBuilder()

    override fun toString(): String {
        builder.insert(0, "#${this.type}:")
        return builder.toString().trimEnd(',')
    }
}

abstract class ExtX : HlsTag() {
    override fun toString(): String {
        builder.insert(0, "#EXT-X-${this.type}:")
        return builder.toString().trimEnd(',')
    }
}

abstract class SingleValueExtX<T> : ExtX() {
    open var value: T? = null

    infix fun set(value: T) {
        this.value = value
    }

    override fun toString(): String {
        builder.append(value)
        return super.toString()
    }
}

class StreamInf : ExtX() {
    override val type = "STREAM-INF"
    infix fun resolution(resolution: String): StreamInf = apply {
        builder.append("RESOLUTION=$resolution,")
    }

    infix fun codecs(codecs: String): StreamInf = apply {
        builder.append("""CODECS="$codecs",""")
    }

    infix fun bandwidth(bandwidth: String): StreamInf = apply {
        builder.append("BANDWIDTH=$bandwidth,")
    }

    infix fun frameRate(frameRate: Float): StreamInf = apply {
        builder.append("FRAME-RATE=$frameRate,")
    }
}

class ByteRange : ExtX() {
    override val type = "BYTERANGE"

    private var length: Int = 0
    private var position: Int = 0

    infix fun length(length: Int): ByteRange = apply {
        this.length = length
    }

    infix fun position(position: Int): ByteRange = apply {
        this.position = position
    }

    override fun toString(): String {
        builder.append("$length@$position")
        return super.toString()
    }
}

class Key : ExtX() {
    override val type = "KEY"
    var name: String = ""
    var value: String = ""

    infix fun value(value: String) = apply {
        this.value = value
    }

    infix fun name(name: String) = apply {
        this.name = name
    }

    override fun toString(): String {
        builder.append("${name.toUpperCase()}=${value.toUpperCase()}")
        return super.toString()
    }
}

class EndList : ExtX() {
    override val type = "ENDLIST"
    override fun toString(): String {
        return super.toString().trimEnd(':')
    }
}

class ExtInf : HlsTag() {
    private var duration: Float = 0F
    private var title: String = ""
    override var type: String = "EXTINF"

    infix fun duration(duration: Float)  = apply {
        this.duration = duration
    }

    infix fun title(title: String) = apply {
        this.title = title
    }

    override fun toString(): String {
        builder.append("$duration,$title".trim(','))
        return super.toString()
    }
}

abstract class Path {
    private var name: String = ""
    private var rendition: String = ""
    abstract val type: String

    infix fun name(name: String): Path = apply {
        this.name = name
    }

    infix fun rendition(rendition: String): Path = apply {
        this.rendition =rendition
    }

    override fun toString(): String  = buildString {
        append("$type/$rendition")
    }
}

class SegmentPath : Path() {
    override val type: String = "segment"
}

class MediaPath : Path() {
    override val type: String = "media"
}

class Version : SingleValueExtX<Int>() {
    override val type = "VERSION"
}

class MediaSequence : SingleValueExtX<Int>() {
    override val type = "MEDIA-SEQUENCE"
}

class TargetDuration(override var value: Int? = 6) : SingleValueExtX<Int>() {
    override val type = "TARGETDURATION"
}

class PlaylistType : SingleValueExtX<PlaylistTypeEnum>() {
    override val type = "PLAYLIST-TYPE"
}

enum class PlaylistTypeEnum {
    EVENT,
    VOD
}
