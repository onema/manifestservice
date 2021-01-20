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

package io.onema.streaming.transcode.transcoderstarter

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.events.S3Event
import io.onema.streaming.transcode.BaseHandler
import software.amazon.awssdk.services.mediaconvert.MediaConvertClient
import software.amazon.awssdk.services.mediaconvert.model.DescribeEndpointsRequest
import java.net.URI


class TranscodeStarterFunction : BaseHandler<S3Event, Unit>() {

    //--- Constants ---
    private val JOB_TEMPLATE_NAME = System.getenv("JOB_TEMPLATE_NAME")
    private val OUTPUT_BUCKET_NAME = System.getenv("OUTPUT_BUCKET_NAME")
    private val MEDIA_CONVERT_ROLE = System.getenv("MEDIA_CONVERT_ROLE")

    //--- Fields ---
    // Get the account API endpoint
    val endpoint = MediaConvertClient
        .builder()
        .build()
        .describeEndpoints(DescribeEndpointsRequest.builder().build())
        .endpoints()[0].url()
    private val mediaConverter = MediaConvertClient
        .builder()
        .endpointOverride(URI.create(endpoint))
        .build()
    private val logic = TranscodeStarterLogic(JOB_TEMPLATE_NAME, OUTPUT_BUCKET_NAME, MEDIA_CONVERT_ROLE, mediaConverter)

    //--- Methods ---
    override fun handleRequest(event: S3Event, context: Context?) = handle {
        log.info(mapper.writeValueAsString(event))
        logic.process(event)
    }
}
