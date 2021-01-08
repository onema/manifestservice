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

import com.amazonaws.services.elastictranscoder.AmazonElasticTranscoder
import com.amazonaws.services.elastictranscoder.model.CreateJobOutput
import com.amazonaws.services.elastictranscoder.model.CreateJobRequest
import com.amazonaws.services.elastictranscoder.model.JobInput
import com.amazonaws.services.lambda.runtime.events.S3Event
import io.onema.streaming.transcode.extensions.firstRecord
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class TranscodeStarterLogic(
    private val pipelineId: String,
    private val amazonElasticTranscoder: AmazonElasticTranscoder,
    private val presets: Map<String, String>) {

    private val log: Logger = LoggerFactory.getLogger(javaClass)

    fun process(event: S3Event) {
        val record = event.firstRecord()
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
            .withPipelineId(pipelineId)
            .withInput(input)
            .withOutputKeyPrefix(outputKeyPrefix)
            .withOutputs(outputs)
        val job = amazonElasticTranscoder.createJob(createJobRequest).job
        log.info("OUTPUT KEY PREFIX: $outputKeyPrefix")
        log.info("JOB ID: " + job?.id)
    }
}