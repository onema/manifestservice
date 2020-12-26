import com.github.jengelman.gradle.plugins.shadow.transformers.Log4j2PluginsCacheFileTransformer

plugins {
    kotlin("jvm")
    id("com.github.johnrengelman.shadow")
}

group = "io.onema.streaming.transcode"

repositories {
    mavenCentral()
}

dependencies {
    val awsSdk1Version = "1.11.923"
    val awsSdk2Version = "2.15.53"

    implementation(kotlin("stdlib"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.4.2")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-joda:2.11.4")
    implementation("com.github.abashev:vfs-s3:4.3.2")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.12.0")

    // AWS
    implementation("com.amazonaws:aws-lambda-java-events:3.2.0")
    implementation("com.amazonaws:aws-lambda-java-core:1.2.1")
    implementation("com.amazonaws:aws-java-sdk-elastictranscoder:$awsSdk1Version")
    implementation("com.amazonaws:aws-java-sdk-sqs:$awsSdk1Version")
    implementation("com.amazonaws:aws-java-sdk-dynamodb:$awsSdk1Version")
    implementation("software.amazon.awssdk:s3:$awsSdk2Version")

    // Logging
    implementation("io.symphonia:lambda-logging:1.0.3")

    // Project commons
    implementation(project(":commons"))
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "11"
    }
}

shadow {
    applicationDistribution.from("src/dist")
}
tasks.shadowJar{
    transform(Log4j2PluginsCacheFileTransformer::class.java)
}
