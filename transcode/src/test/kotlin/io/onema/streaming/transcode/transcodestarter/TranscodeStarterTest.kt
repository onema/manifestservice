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
        val json = """{"records":[{"eventVersion":"2.1","eventSource":"aws:s3","awsRegion":"us-east-1","eventTime":"2021-01-07T06:11:13.266Z","eventName":"ObjectCreated:CompleteMultipartUpload","userIdentity":{"principalId":"AWS:AIDAJ4W5SF5AANMKCKRVW"},"requestParameters":{"sourceIPAddress":"23.241.230.109"},"responseElements":{"x-amz-request-id":"576E6123C0705D3B","x-amz-id-2":"+lQjLSRn6tCLn1zhUx7fPf4S+4ubnVdwckbqPxZLo222c0c1e3VkzTEjvQmCob+djaAoletKmfYBkgToKybx8KdbRaMlLyux"},"s3":{"s3SchemaVersion":"1.0","configurationId":"manifest-service-dev-transcodingJob-68a737133cefb25bff959852b8f04754","bucket":{"name":"manifest-service-dev-inputbucket-1emlcrh6qvali","ownerIdentity":{"principalId":"ARAKUEIRWNFD6"},"arn":"arn:aws:s3:::manifest-service-dev-inputbucket-1emlcrh6qvali"},"object":{"key":"bbb.mp4","size":673223862,"eTag":"c0205b7f5b8f65e9a32dcab27873f16d-81","sequencer":"005FF6A5FBCFFFEEFE"}}}]}"""
        val event = mapper.readValue<S3Event>(json)
        val function = TranscodeStarterFunction()

        // Act
        val result = function.handleRequest(event, null)

        // Assert


    }
}