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

package io.onema.manifestservice.config

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig.TableNameOverride
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class DynamoDBMapperConfig {

    @Value("\${TABLE_NAME}")
    lateinit var tableName: String

    @Bean
    fun dynamoDBMapper(): DynamoDBMapper {
        val tableOverride = TableNameOverride.withTableNameReplacement(tableName)
        return DynamoDBMapper(
            AmazonDynamoDBClientBuilder.defaultClient(),
            DynamoDBMapperConfig.builder()
                .withTableNameOverride(tableOverride).build()
        )
    }
}