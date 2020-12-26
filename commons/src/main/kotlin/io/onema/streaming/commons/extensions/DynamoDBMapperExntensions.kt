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

package io.onema.streaming.commons.extensions

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression
import com.amazonaws.services.dynamodbv2.model.AttributeValue
import io.onema.streaming.commons.domain.*
import io.onema.streaming.commons.extensions.ConditionExpression.*

enum class ConditionExpression {
    equals,
    lessThan,
    lessThanOrEqualTo,
    greaterThan,
    greaterThanOrEqualTo,
    beginsWith
}

inline fun <reified T> DynamoDBMapper.getByPrimaryKey(pk: String): T? = load(T::class.java, pk)

inline fun <reified T> DynamoDBMapper.getByPrimaryKeyAndSortKey(pk: String, sk: String): T? = load(T::class.java, pk, sk)

inline fun <reified T> DynamoDBMapper.query(pk: String, sk: String, condition: ConditionExpression): List<T> {
    val eav = mapOf<String, AttributeValue>(
        ":pk" to AttributeValue().withS(pk),
        ":sk" to AttributeValue().withS(sk)
    )
    val queryExpression = DynamoDBQueryExpression<T>()
        .withKeyConditionExpression(conditionExpression(condition))
        .withExpressionAttributeValues(eav)
    return query(T::class.java, queryExpression)
}

fun DynamoDBMapper.renditionMetadata(videoName: String): Map<String, StreamData> {
    val pk = "VIDEO#$videoName"
    println("PRIMARY KEY: $pk")
    val streams = query<Stream>(pk, "METADATA#STREAM", beginsWith)
        .groupBy { it.rendition() }
        .filterKeys { it.isNotEmpty() }
    println("DONE QUERING STREAMS")
    val formats = query<Format>(pk, "METADATA#FORMAT#", beginsWith)
        .groupBy { it.rendition() }
        .filterKeys { it.isNotEmpty() }
    println("DONE QUERING FORMAT")
    return formats.mapValues { (key, value) ->
        StreamData(streams[key], value.first())
    }
}

fun DynamoDBMapper.renditionSegments(videoName: String, rendition: String): List<Segment> {
    val pk = "VIDEO#$videoName"
    return query<Segment>(pk, "SEGMENT#$rendition#", beginsWith)
        .sortedBy { it.position }
}

fun conditionExpression(condition: ConditionExpression): String {
    return when(condition) {
        equals -> "PK = :pk and SK = :sk"
        lessThan -> "PK = :pk and SK < :sk"
        lessThanOrEqualTo -> "PK = :pk and SK <= :sk"
        greaterThan -> "PK = :pk and SK > :sk"
        greaterThanOrEqualTo -> "PK = :pk and SK >= :sk"
        beginsWith -> "PK = :pk and begins_with(SK, :sk)"
    }
}

