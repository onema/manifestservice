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

import com.amazonaws.services.elastictranscoder.AmazonElasticTranscoderClientBuilder
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.events.S3Event
import io.onema.streaming.transcode.BaseHandler
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async


class TranscodeStarterFunction : BaseHandler<S3Event, Unit>() {

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
    private val logic = TranscodeStarterLogic(PIPELINE_ID, amazonElasticTranscoder, presets)


    override suspend fun handleRequestAsync(event: S3Event, context: Context?): Deferred<Unit> = GlobalScope.async(Dispatchers.IO) {
        logic.process(event)
    }

}
