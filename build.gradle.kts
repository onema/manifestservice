import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    base
    id("java")
    kotlin("jvm") version "1.4.21"
    id("com.github.johnrengelman.shadow") version "6.1.0"
}

allprojects {
    group = "io.onema.streaming"
    version = "0.0.1-SNAPSHOT"
    repositories {
        mavenCentral()
        jcenter()
    }

    tasks.withType<JavaCompile> {
        java.sourceCompatibility = JavaVersion.VERSION_11
        java.targetCompatibility = JavaVersion.VERSION_11
    }
    tasks.withType<KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs = listOf("-Xjsr305=strict")
            jvmTarget = "11"
        }
    }
}

dependencies {
    subprojects.forEach {
        archives(it)
    }
}

subprojects {
    repositories {
        mavenCentral()
    }
}
repositories {
    mavenCentral()
}

task("deploy", Exec::class) {
    dependsOn("shadowJar")
    commandLine("serverless", "deploy")
}

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    languageVersion = "1.4"
}