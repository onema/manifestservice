/**
 * This file is part of the ONEMA manifestservice Package.
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed
 * with this source code.
 *
 * copyright (c) 2020, Juan Manuel Torres (http://onema.io)
 *
 * @author Juan Manuel Torres <software@onema.io>
 */

package io.onema.streaming.commons.domain

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable

@DynamoDBTable(tableName = "video-metadata")
open class DynamoDBTable(
    @DynamoDBHashKey(attributeName = "PK")
    open var pk: String? = null,

    @DynamoDBRangeKey(attributeName = "SK")
    open var sk: String? = null
) {

    /**
     * Get the rendition value from the SK
     */
    fun rendition(): String {
        return sk?.split("#")?.last() ?: ""
    }
}