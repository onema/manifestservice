package io.onema.streaming.manifestservice

import com.amazonaws.serverless.proxy.model.AwsProxyRequest
import com.amazonaws.serverless.proxy.model.AwsProxyResponse
import com.amazonaws.serverless.proxy.spring.SpringBootLambdaContainerHandler
import com.amazonaws.serverless.proxy.spring.SpringBootProxyHandlerBuilder
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestStreamHandler
import org.slf4j.LoggerFactory
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream


class StreamLambdaHandler : RequestStreamHandler {
    companion object {
        private val log = LoggerFactory.getLogger(StreamLambdaHandler::class.java)

        private lateinit var handler: SpringBootLambdaContainerHandler<AwsProxyRequest, AwsProxyResponse>

        init {
            val result = runCatching {
//                handler = SpringBootLambdaContainerHandler.getAwsProxyHandler(ManifestServiceApplication::class.java)
                handler = SpringBootProxyHandlerBuilder<AwsProxyRequest>()
                    .defaultProxy()
                    .asyncInit()
                    .springBootApplication(ManifestServiceApplication::class.java)
                    .buildAndInitialize()
            }

            if (result.isFailure) {
                // if we fail here. We re-throw the exception to force another cold start
                result.exceptionOrNull()?.printStackTrace()
                throw RuntimeException("Could not initialize Spring Boot application", result.exceptionOrNull())
            }
        }
    }

    @Throws(IOException::class)
    override fun handleRequest(inputStream: InputStream?, outputStream: OutputStream?, context: Context?) {
        log.info("XXXXXXXXXXXX REQUEST: " + inputStream?.bufferedReader().use { it?.readText() })
        inputStream?.reset()
        handler.proxyStream(inputStream, outputStream, context)
    }
}