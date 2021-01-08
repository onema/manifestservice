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
package io.onema.streaming.transcode

import com.amazonaws.services.lambda.runtime.events.S3Event
import com.amazonaws.services.lambda.runtime.events.models.s3.S3EventNotification
import io.onema.streaming.transcode.metadatageneration.MetadataGenerationFunction
import org.junit.jupiter.api.Test

class MetadataGenerationTest {

    @Test
    fun basicCallTest() {
        val events = listOf(
            S3EventNotification.S3EventNotificationRecord(
                "us-west-2",
                "ObjectCreated:Put",
                "aws:s3",
                null,
                "2.1",
                S3EventNotification.RequestParametersEntity("23.241.230.109"),
                null,
                S3EventNotification.S3Entity(
                    "transcoding-dev-MetadataGeneration-dcc0201c9649d770169a0b13f261d68f",
                    S3EventNotification.S3BucketEntity(
                        "manifest-output",
                        S3EventNotification.UserIdentityEntity("ARAKUEIRWNFD6"),
                        "arn:aws:s3:::one-transcoding-output"
                    ),
                    S3EventNotification.S3ObjectEntity(
                        "GOPR2531/hls400k.ts",
                        1852928L,
                        "e712a18c515fb7ab65bfea6289bbbdee",
                        "",
                        "005FE04F17EBF04140"
                    ),
                    "1.0"
                ),
                S3EventNotification.UserIdentityEntity("AWS:AIDAIOIDTOV3ONC5FFFEU")
            )
        )
        val event = S3Event(events)

        val func = MetadataGenerationFunction()
        func.handleRequest(event, null)
    }
}