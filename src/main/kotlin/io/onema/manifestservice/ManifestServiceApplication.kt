package io.onema.manifestservice

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.function.adapter.aws.SpringBootRequestHandler
import org.springframework.context.annotation.Bean

@SpringBootApplication
class ManifestServiceApplication {

    @Bean
    fun handler(): (APIGatewayProxyRequestEvent) -> APIGatewayProxyResponseEvent = ::apiGateway

    fun apiGateway(request: APIGatewayProxyRequestEvent): APIGatewayProxyResponseEvent {
        val headers = request.headers

        val response = APIGatewayProxyResponseEvent()
        response.body = "Foobar"
        return response
    }
}

class LambdaHandler : SpringBootRequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent>()


fun main(args: Array<String>) {
    runApplication<ManifestServiceApplication>(*args)
}
