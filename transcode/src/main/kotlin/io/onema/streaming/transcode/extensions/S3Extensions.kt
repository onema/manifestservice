/**
 * This file is part of the ONEMA streaming Package.
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed
 * with this source code.
 *
 * copyright (c) 2020, Juan Manuel Torres (http://onema.io)
 *
 * @author Juan Manuel Torres <software@onema.io>
 */

package io.onema.streaming.transcode.extensions

import com.amazonaws.services.lambda.runtime.events.S3Event
import com.amazonaws.services.lambda.runtime.events.models.s3.S3EventNotification.S3EventNotificationRecord
import java.io.File


fun S3EventNotificationRecord.origin(): String = "s3://${bucket()}.s3.amazonaws.com${path()}"
fun S3EventNotificationRecord.bucket(): String = s3.bucket.name
fun S3EventNotificationRecord.key(): String = s3.`object`.urlDecodedKey
fun S3EventNotificationRecord.path(): String = File(s3.`object`.urlDecodedKey).parent
fun S3EventNotificationRecord.name(): String = File(s3.`object`.urlDecodedKey).name
fun S3EventNotificationRecord.nameWithoutExtension(): String = File(s3.`object`.urlDecodedKey).nameWithoutExtension
fun S3EventNotificationRecord.directoryName(): String = File(s3.`object`.urlDecodedKey).parentFile.name

fun S3EventNotificationRecord.metadataName(): String = "${nameWithoutExtension()}.json"
fun S3EventNotificationRecord.framesName(): String = "${nameWithoutExtension()}_frames.json"
fun S3Event.firstRecord(): S3EventNotificationRecord? = this.records?.first()
