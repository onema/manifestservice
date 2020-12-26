package io.onema.streaming.manifestservice

import io.onema.streaming.manifestservice.api.StreamController
import io.onema.streaming.manifestservice.config.OriginConfig
import io.onema.streaming.commons.domain.DynamoDBTable
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.web.servlet.HandlerAdapter
import org.springframework.web.servlet.HandlerMapping
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping

@SpringBootApplication
@Import(value = [StreamController::class, OriginConfig::class, DynamoDBTable::class])
class ManifestServiceApplication : SpringBootServletInitializer() {

    @Bean
    fun handlerMapping(): HandlerMapping = RequestMappingHandlerMapping()

    @Bean
    fun handlerAdapter(): HandlerAdapter = RequestMappingHandlerAdapter()
}

fun main(args: Array<String>) {
    runApplication<ManifestServiceApplication>(*args)
}
