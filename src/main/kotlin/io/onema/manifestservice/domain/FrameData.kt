package io.onema.manifestservice.domain


import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey
import com.fasterxml.jackson.annotation.JsonProperty
import io.onema.manifestservice.extensions.pad
import java.lang.RuntimeException

data class FrameData(@JsonProperty("frames") var frames: List<Frame> = listOf()) {

    fun buildUpSegments(streamData: StreamData, rendition: String): List<Segment> {
        val sortedFrames = frames
            .filter   { frame ->  frame.mediaType == "video" }
            .sortedBy { frame ->  frame.pktPos?.toInt() ?: 0 }

        val pk = streamData.format?.pk ?: throw RuntimeException("PK is not available in stream data")
        val sk = "SEGMENT#$rendition#POSITION#"
        val startSegment = Segment(frameRate = streamData.frameRate, pk = pk, sk = sk + 0.pad())
        return tailrecBuildUpSegments(pk, sk, startSegment, sortedFrames)
    }

    private tailrec fun tailrecBuildUpSegments(pk: String, sk: String, segment: Segment, frames: List<Frame>, segs: List<Segment> = listOf(), lastFramePosition: Int = 0): List<Segment> {
        val frame = frames.firstOrNull()
        val tail = frames.drop(1)

        // We are segmenting on key frames
        return if (frame?.keyFrame == 1) {

            // Skip the first frame of the video
            if (segment.frames == 0) {
                val newSegment = segment.copy(frames = segment.frames + 1)
                tailrecBuildUpSegments(pk, sk, newSegment, tail, segs, lastFramePosition)

                // set the length for the next key frame once we know the first key frame
            } else {

                // Set the length so we know what the next key frame is
                segment.length = frame.packagePosition() - segment.position

                // Start a new segment
                val newSegment = Segment(frameRate = segment.frameRate, position = frame.packagePosition(), frames = 1, pk = pk, sk = sk + frame.packagePosition().pad())

                // Add the completed segment to the list and continue to nest iteration
                tailrecBuildUpSegments(pk, sk, newSegment, tail, segs + segment, frame.packagePosition())
            }
        } else if(frame != null) {

            // Continue iterations keeping track of the frames per segment so we can generate the duration and the last frame segment position
            tailrecBuildUpSegments(pk, sk, segment.copy(frames = segment.frames + 1), tail, segs, frame.packagePosition())
        } else {

            // Handle the last segment
            segment.length = lastFramePosition - segment.position
            segs + segment
        }
    }

    fun iterativeBuildUpSegments(pk: String, sk: String, streamData: StreamData): MutableList<Segment> {
        val frameRate = streamData.frameRate
        var mainSegment = Segment(frameRate = frameRate, pk = pk, sk = sk + 0.pad())
        val segs = mutableListOf<Segment>()
        var lastFramePosition = 0
        frames.forEach { frame ->

            // We're segmenting on key frames.
            if (frame.keyFrame == 1) {

                // We're skipping the first key frame because it is the first frame of the video
                if (mainSegment.frames == 0) {
                    mainSegment.frames++
                    return@forEach
                } else {

                    // Set the length now that we know where the next key frame is.
                    mainSegment.length = frame.packagePosition() - mainSegment.position

                    // Add the completed segment to the list.
                    segs += mainSegment

                    // Start a new segment.
                    mainSegment = Segment(frameRate = frameRate, position = frame.packagePosition(), pk = pk, sk = frame.packagePosition().pad())
                }
            }

            // Keep track of the frames per segment so we can generate the duration later.
            mainSegment.frames ++

            // Keep track of this for the last segment.
            lastFramePosition = frame.packagePosition()
        }

        // Handle the last segment.
        if (mainSegment.position != segs.last().position) {
            mainSegment.length = lastFramePosition - mainSegment.position
            segs += mainSegment
        }
        return segs
    }
}


data class Frame(

    @JsonProperty("best_effort_timestamp")
    var bestEffortTimestamp: Int? = null,

    @JsonProperty("best_effort_timestamp_time")
    var bestEffortTimestampTime: String? = null,

    @JsonProperty("channel_layout")
    var channelLayout: String? = null,

    @JsonProperty("channels")
    var channels: Int? = null,

    @JsonProperty("chroma_location")
    var chromaLocation: String? = null,

    @JsonProperty("coded_picture_number")
    var codedPictureNumber: Int? = null,

    @JsonProperty("display_picture_number")
    var displayPictureNumber: Int? = null,

    @JsonProperty("height")
    var height: Int? = null,

    @JsonProperty("interlaced_frame")
    var interlacedFrame: Int? = null,

    @JsonProperty("key_frame")
    var keyFrame: Int? = null,

    @JsonProperty("media_type")
    var mediaType: String? = null,

    @JsonProperty("nb_samples")
    var nbSamples: Int? = null,

    @JsonProperty("pict_type")
    var pictType: String? = null,

    @JsonProperty("pix_fmt")
    var pixFmt: String? = null,

    @JsonProperty("pkt_dts")
    var pktDts: Int? = null,

    @JsonProperty("pkt_dts_time")
    var pktDtsTime: String? = null,

    @JsonProperty("pkt_duration")
    var pktDuration: Int? = null,

    @JsonProperty("pkt_duration_time")
    var pktDurationTime: String? = null,

    @JsonProperty("pkt_pos")
    var pktPos: String? = null,

    @JsonProperty("pkt_pts")
    var pktPts: Int? = null,

    @JsonProperty("pkt_pts_time")
    var pktPtsTime: String? = null,

    @JsonProperty("pkt_size")
    var pktSize: String? = null,

    @JsonProperty("repeat_pict")
    var repeatPict: Int? = null,

    @JsonProperty("sample_aspect_ratio")
    var sampleAspectRatio: String? = null,

    @JsonProperty("sample_fmt")
    var sampleFmt: String? = null,

    @JsonProperty("stream_index")
    var streamIndex: Int? = null,

    @JsonProperty("top_field_first")
    var topFieldFirst: Int? = null,

    @JsonProperty("width")
    var width: Int? = null
) {
    fun packagePosition(): Int = pktPos?.toInt() ?: 0
}

data class Segment(
    var frameRate: Float = 0F,
    var frames: Int = 0,
    var position: Int = 0,
    var length: Int = 0,

    @DynamoDBHashKey(attributeName = "PK")
    override var pk: String? = null,

    @DynamoDBRangeKey(attributeName = "SK")
    override var sk: String? = null,
) : DynamoDBTable() {
    fun duration(): Float = frames.toFloat() / frameRate
}