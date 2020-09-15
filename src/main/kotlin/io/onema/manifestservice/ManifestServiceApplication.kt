package io.onema.manifestservice

import io.onema.manifestservice.api.StreamController
import io.onema.manifestservice.config.OriginConfig
import org.springframework.beans.factory.annotation.Value
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
@Import(value = [StreamController::class, OriginConfig::class])
class ManifestServiceApplication : SpringBootServletInitializer() {

    // silence console logging
    @Value("\${logging.level.root:OFF}")
    lateinit var message: String

    @Bean
    fun handlerMapping(): HandlerMapping = RequestMappingHandlerMapping()

    @Bean
    fun handlerAdapter(): HandlerAdapter = RequestMappingHandlerAdapter()
}

fun main(args: Array<String>) {
    runApplication<ManifestServiceApplication>(*args)
}
