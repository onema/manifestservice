package io.onema.manifestservice

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class ManifestServiceApplication

fun main(args: Array<String>) {
    runApplication<ManifestServiceApplication>(*args)
}
