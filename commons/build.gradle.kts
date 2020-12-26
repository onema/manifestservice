import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
}

group = "io.onema.streaming.commons"
repositories {
    mavenCentral()
}

dependencies {
    val awsSdkVersion = "1.11.923"
    implementation("com.amazonaws:aws-java-sdk-dynamodb:$awsSdkVersion")

    // HLS DSL
    implementation("io.onema:playlist-dsl:0.1.0")

    implementation("com.github.abashev:vfs-s3:4.3.2")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.12.0")
    implementation("com.amazonaws:aws-lambda-java-events:3.2.0")

}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "11"
    }
}
val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = "11"
}
val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    jvmTarget = "11"
}