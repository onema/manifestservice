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

package io.onema.streaming.transcode.extensions

import com.amazonaws.services.lambda.runtime.events.SQSEvent

fun SQSEvent.allRecords(): List<SQSEvent.SQSMessage> =
    records ?: throw RuntimeException("SQS Event recrds should not be empty")
