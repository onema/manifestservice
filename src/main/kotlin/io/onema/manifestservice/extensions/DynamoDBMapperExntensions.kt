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

package io.onema.manifestservice.extensions

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression
import com.amazonaws.services.dynamodbv2.model.AttributeValue

inline fun <reified T> DynamoDBMapper.getByPrimaryKey(pk: String): T? = load(T::class.java, pk)

inline fun <reified T> DynamoDBMapper.getByPrimaryKeyAndSortKey(pk: String, sk: String): T? = load(T::class.java, pk, sk)

inline fun <reified T> DynamoDBMapper.query(pk: String, sk: String, condition: String): List<T?> {
    val eav = mapOf<String, AttributeValue>(
        ":pk" to AttributeValue().withS(pk),
        ":sk" to AttributeValue().withS(sk)
    )
    val queryExpression = DynamoDBQueryExpression<T>()
        .withKeyConditionExpression("PK = :pk and SK $condition :sk")
        .withExpressionAttributeValues(eav)
    return query(T::class.java, queryExpression)
}
