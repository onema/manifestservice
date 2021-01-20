/**
 * This file is part of the ONEMA streaming Package.
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed
 * with this source code.
 *
 * copyright (c) 2021, Juan Manuel Torres (http://onema.io)
 *
 * @author Juan Manuel Torres <software@onema.io>
 */

package io.onema.streaming.transcode.transcoderstarter

import com.amazonaws.services.lambda.runtime.events.S3Event
import io.onema.streaming.transcode.extensions.firstRecord
import io.onema.streaming.transcode.extensions.keyOrigin
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import software.amazon.awssdk.services.mediaconvert.MediaConvertClient
import software.amazon.awssdk.services.mediaconvert.model.*

class TranscodeStarterLogic(
    private val jobTemplateName: String,
    private val outputBucket: String,
    private val mediaConvertRole: String,
    private val mediaConverter: MediaConvertClient) {

    private val log: Logger = LoggerFactory.getLogger(javaClass)

    fun process(event: S3Event) {
        val record = event.firstRecord()
        log.info("Event for Bucket: ${record.s3.bucket} Key: ${record.s3.`object`.key}")
        val output = "s3://$outputBucket/"

        val inputs = Input
            .builder()
            .fileInput(record.keyOrigin())
            .build()
        val hlsGroupSettings = HlsGroupSettings
            .builder()
            .destination(output)
            .build()
        val outputGroupSettings = OutputGroupSettings
            .builder()
            .hlsGroupSettings(hlsGroupSettings)
            .build()
        val outputs = OutputGroup.builder()
            .name("Apple HLS")
            .outputGroupSettings(outputGroupSettings)
            .build()
        val settings = JobSettings.builder()
            .inputs(inputs)
            .outputGroups(outputs)
            .build()
        val createJobRequest = CreateJobRequest.builder()
            .jobTemplate(jobTemplateName)
            .role(mediaConvertRole)
            .settings(settings)
            .build()
        mediaConverter.createJob(createJobRequest)
    }
}
