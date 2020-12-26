/**
 * This file is part of the ONEMA transcode Package.
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed
 * with this source code.
 *
 * copyright (c) 2020, Juan Manuel Torres (http://onema.io)
 *
 * @author Juan Manuel Torres <software@onema.io>
 */

package io.onema.streaming.transcode

import com.amazonaws.services.elastictranscoder.AmazonElasticTranscoderClientBuilder
import com.amazonaws.services.elastictranscoder.model.CreateJobOutput
import com.amazonaws.services.elastictranscoder.model.CreateJobRequest
import com.amazonaws.services.elastictranscoder.model.JobInput
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.amazonaws.services.lambda.runtime.events.S3Event
import org.slf4j.LoggerFactory


class TranscodeFunction : RequestHandler<S3Event, Unit> {

    //--- Constants ---
    private val presets = mapOf(
        "hls400k" to "1351620000001-200050",
        "hls600k" to "1351620000001-200040",
        "hls1000k" to "1351620000001-200030",
        "hls1500k" to "1351620000001-200020",
        "hls2000k" to "1351620000001-200010",
    )

    private val PIPELINE_ID = System.getenv("PIPELINE_ID")

    //--- Fields ---
    private val amazonElasticTranscoder = AmazonElasticTranscoderClientBuilder.defaultClient()
    private val log = LoggerFactory.getLogger(javaClass)


    //--- Methods ---
    override fun handleRequest(event: S3Event?, context: Context?) {
        val record = event?.records?.first() ?: throw RuntimeException("S3Event record is null")
        val inputKey = record.s3.`object`.urlDecodedKey
        log.info("Event for Bucket: ${record.s3.bucket} Key: ${record.s3.`object`.key}")

        val input = JobInput().withKey(inputKey)

        val videoName = inputKey.split('.').first() //inputKey.inputKeyToOutputKey()
        val outputs: List<CreateJobOutput> = presets.map { (key, presetId) ->
            CreateJobOutput()
                .withKey("$key.ts")
                .withPresetId(presetId)
        }

        val outputKeyPrefix = "$videoName/"
        val createJobRequest = CreateJobRequest()
            .withPipelineId(PIPELINE_ID)
            .withInput(input)
            .withOutputKeyPrefix(outputKeyPrefix)
            .withOutputs(outputs)
        val job = amazonElasticTranscoder.createJob(createJobRequest).job
        log.info("OUTPUT KEY PREFIX: $outputKeyPrefix")
        log.info("JOB ID: " + job?.id)
    }
}
