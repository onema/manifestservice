package io.onema.manifestservice.domain

import com.fasterxml.jackson.annotation.JsonProperty
import io.onema.manifestservice.extensions.hex

enum class H264Profiles(val value: Int) {
    BASELINE(66),
    EXTENDED(88),
    MAIN(77),
    HIGH(100),
    HIGH_10(110),
    HIGH_422(122),
    HIGH_444_PREDICTIVE(144);

    fun hex(): String = value.hex()
}

data class StreamData(

    @JsonProperty("format")
    var format: Format? = null,

    @JsonProperty("streams")
    var streams: List<Stream?>? = null
) {
    val frameRate: Float
        get() {
            val videoStream = firstVideoStream()
            val arr = videoStream.frameRate?.split('/')
            val num = arr?.get(0)?.toFloat() ?: 0F
            val den = arr?.get(1)?.toFloat() ?: 1F
            return num/den
        }

    val resolution: String
        get() {
            val videoStream = firstVideoStream()
            return "${videoStream.width}x${videoStream.height}"
        }

    val codecs: String
        get() {
            val videoStream = firstVideoStream()
            val audioStream = firstAudioStream()
            val profile = H264Profiles.valueOf(videoStream.profile.toUpperCase())
            val videoPart = "avc1.${profile.hex()}00${videoStream.level.hex()}"
            val audioPart = if (audioStream.profile == "LC") "mp4a.40.2" else "mp4a.40.5"
            return "$videoPart,$audioPart"
        }

    val bandwidth: String
        get() = format?.bitRate ?: ""

    private fun firstVideoStream(): Stream = streams?.first { it?.codecType == "video" } ?: throw RuntimeException("Unable to find video streams")
    private fun firstAudioStream(): Stream = streams?.first { it?.codecType == "audio" && it.codecName == "aac" } ?: throw RuntimeException("Unable to find audio streams")
}

data class Format(

    @JsonProperty("bit_rate")
    var bitRate: String? = null,

    @JsonProperty("duration")
    var duration: String? = null,

    @JsonProperty("filename")
    var filename: String? = null,

    @JsonProperty("format_long_name")
    var formatLongName: String? = null,

    @JsonProperty("format_name")
    var formatName: String? = null,

    @JsonProperty("nb_programs")
    var nbPrograms: Int? = null,

    @JsonProperty("nb_streams")
    var nbStreams: Int? = null,

    @JsonProperty("probe_score")
    var probeScore: Int? = null,

    @JsonProperty("size")
    var size: String? = null,

    @JsonProperty("start_time")
    var startTime: String? = null
)

data class Stream(

    @JsonProperty("avg_frame_rate")
    var avgFrameRate: String? = null,

    @JsonProperty("bit_rate")
    var bitRate: String? = null,

    @JsonProperty("bits_per_raw_sample")
    var bitsPerRawSample: String? = null,

    @JsonProperty("bits_per_sample")
    var bitsPerSample: Int? = null,

    @JsonProperty("channel_layout")
    var channelLayout: String? = null,

    @JsonProperty("channels")
    var channels: Int? = null,

    @JsonProperty("chroma_location")
    var chromaLocation: String? = null,

    @JsonProperty("closed_captions")
    var closedCaptions: Int? = null,

    @JsonProperty("codec_long_name")
    var codecLongName: String? = null,

    @JsonProperty("codec_name")
    var codecName: String? = null,

    @JsonProperty("codec_tag")
    var codecTag: String? = null,

    @JsonProperty("codec_tag_string")
    var codecTagString: String? = null,

    @JsonProperty("codec_time_base")
    var codecTimeBase: String? = null,

    @JsonProperty("codec_type")
    var codecType: String? = null,

    @JsonProperty("coded_height")
    var codedHeight: Int? = null,

    @JsonProperty("coded_width")
    var codedWidth: Int? = null,

    @JsonProperty("display_aspect_ratio")
    var displayAspectRatio: String? = null,

    @JsonProperty("disposition")
    var disposition: Disposition? = null,

    @JsonProperty("duration")
    var duration: String? = null,

    @JsonProperty("duration_ts")
    var durationTs: Int? = null,

    @JsonProperty("field_order")
    var fieldOrder: String? = null,

    @JsonProperty("has_b_frames")
    var hasBFrames: Int? = null,

    @JsonProperty("height")
    var height: Int? = null,

    @JsonProperty("id")
    var id: String? = null,

    @JsonProperty("index")
    var index: Int? = null,

    @JsonProperty("is_avc")
    var isAvc: String? = null,

    @JsonProperty("level")
    var level: Int? = null,

    @JsonProperty("nal_length_size")
    var nalLengthSize: String? = null,

    @JsonProperty("pix_fmt")
    var pixFmt: String? = null,

    @JsonProperty("profile")
    var profile: String = "",

    @JsonProperty("r_frame_rate")
    var frameRate: String? = null,

    @JsonProperty("refs")
    var refs: Int? = null,

    @JsonProperty("sample_aspect_ratio")
    var sampleAspectRatio: String? = null,

    @JsonProperty("sample_fmt")
    var sampleFmt: String? = null,

    @JsonProperty("sample_rate")
    var sampleRate: String? = null,

    @JsonProperty("start_pts")
    var startPts: Int? = null,

    @JsonProperty("start_time")
    var startTime: String? = null,

    @JsonProperty("tags")
    var tags: Tags? = null,

    @JsonProperty("time_base")
    var timeBase: String? = null,

    @JsonProperty("width")
    var width: Int? = null
)


data class Disposition(

    @JsonProperty("attached_pic")
    var attachedPic: Int? = null,

    @JsonProperty("clean_effects")
    var cleanEffects: Int? = null,

    @JsonProperty("comment")
    var comment: Int? = null,

    @JsonProperty("default")
    var default: Int? = null,

    @JsonProperty("dub")
    var dub: Int? = null,

    @JsonProperty("forced")
    var forced: Int? = null,

    @JsonProperty("hearing_impaired")
    var hearingImpaired: Int? = null,

    @JsonProperty("karaoke")
    var karaoke: Int? = null,

    @JsonProperty("lyrics")
    var lyrics: Int? = null,

    @JsonProperty("original")
    var original: Int? = null,

    @JsonProperty("timed_thumbnails")
    var timedThumbnails: Int? = null,

    @JsonProperty("visual_impaired")
    var visualImpaired: Int? = null
)

data class Tags(

    @JsonProperty("language")
    var language: String? = null
)
