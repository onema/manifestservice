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

package io.onema.streaming.transcode.metadataloader

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.events.SQSEvent
import io.onema.streaming.transcode.BaseHandler
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import org.apache.commons.vfs2.FileSystemManager
import org.apache.commons.vfs2.VFS

class MetadataLoaderFunction : BaseHandler<SQSEvent, Unit>() {

    //--- Fields ---
    private val fsManager: FileSystemManager = VFS.getManager()
    private val tableName = System.getenv("TABLE_NAME")
    private val tableOverride: DynamoDBMapperConfig.TableNameOverride = DynamoDBMapperConfig.TableNameOverride
        .withTableNameReplacement(tableName)
    private val dynamoMapper = DynamoDBMapper(
        AmazonDynamoDBClientBuilder.defaultClient(),
        DynamoDBMapperConfig.builder()
            .withTableNameOverride(tableOverride).build()
    )
    private val logic = MetadataLoaderLogic(fsManager, dynamoMapper, mapper)

    override suspend fun handleRequestAsync(event: SQSEvent, context: Context?): Deferred<Unit> = GlobalScope.async(Dispatchers.IO) {
        logic.process(event)
    }
}
