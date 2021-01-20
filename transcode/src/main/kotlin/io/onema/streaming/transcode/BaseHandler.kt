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
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.fasterxml.jackson.datatype.joda.JodaModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import kotlinx.coroutines.runBlocking
import org.slf4j.Logger
import org.slf4j.LoggerFactory


abstract class BaseHandler<TEvent, TOut> : RequestHandler<TEvent, TOut> {

    //--- Fields ---
    protected val mapper = jacksonObjectMapper()
    protected val log: Logger = LoggerFactory.getLogger(javaClass)

    //--- Constructors ---
    init {
        mapper.registerModule(JodaModule())
    }

    fun handle(lambdaFunction: () -> TOut): TOut = runBlocking {
        when(val result = Either.catch { lambdaFunction() }) {
            is Either.Right -> {
                log.info("ALL DONE!")
                result.b
            }
            is Either.Left -> throw result.a
        }
    }
}
