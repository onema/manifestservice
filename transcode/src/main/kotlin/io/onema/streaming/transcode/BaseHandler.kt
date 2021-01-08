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

package io.onema.streaming.transcode

import arrow.core.Either
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.fasterxml.jackson.datatype.joda.JodaModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import kotlinx.coroutines.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory


abstract class BaseHandler<TEvent, TOut> : RequestHandler<TEvent, TOut> {

    //--- Fields ---
    protected val mapper = jacksonObjectMapper()
    private val log: Logger = LoggerFactory.getLogger(javaClass)

    //--- Constructors ---
    init {
        mapper.registerModule(JodaModule())
    }

    override fun handleRequest(event: TEvent, context: Context?): TOut = runBlocking {
        log.info(mapper.writeValueAsString(event))
        val result = Either.catch {
            handleRequestAsync(event,context).await()
        }
        when(result) {
            is Either.Right -> {
                log.info("ALL DONE!")
                result.b
            }
            is Either.Left -> throw result.a
        }
    }

    abstract suspend fun handleRequestAsync(event: TEvent, context: Context?): Deferred<TOut>

}
