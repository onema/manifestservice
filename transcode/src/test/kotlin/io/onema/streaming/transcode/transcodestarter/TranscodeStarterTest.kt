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

package io.onema.streaming.transcode.transcodestarter

import com.amazonaws.services.lambda.runtime.events.S3Event
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.onema.streaming.transcode.transcoderstarter.TranscodeStarterFunction
import org.junit.jupiter.api.Test

class TranscodeStarterTest {

    private val mapper = jacksonObjectMapper()

    @Test
    fun basicTest() {

        // Arrange

        // Act

        // Assert

    }
}