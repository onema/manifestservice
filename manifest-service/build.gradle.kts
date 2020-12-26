import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import com.github.jengelman.gradle.plugins.shadow.transformers.Log4j2PluginsCacheFileTransformer


plugins {
    id("org.springframework.boot") version "2.4.1"
    id("io.spring.dependency-management") version "1.0.10.RELEASE"
    id("com.github.johnrengelman.shadow")
    kotlin("jvm")
    kotlin("plugin.spring") version "1.4.21"

}

group = "io.onema.streaming.manifestservice"
repositories {
    mavenCentral()
    jcenter()
    maven("https://kotlin.bintray.com/kotlinx")
}

extra["springCloudVersion"] = "Hoxton.SR7"

dependencies {
    val awsSdkVersion = "1.11.925"
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    implementation("com.github.abashev:vfs-s3:4.3.2")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.12.0")

    implementation("com.amazonaws:aws-lambda-java-events:3.7.0")
    implementation("com.amazonaws:aws-lambda-java-core:1.2.1")
    implementation("com.amazonaws:aws-java-sdk-core:$awsSdkVersion")
    implementation("com.amazonaws:aws-java-sdk-dynamodb:$awsSdkVersion")
    implementation("com.amazonaws:aws-java-sdk-elastictranscoder:$awsSdkVersion")

    implementation("io.symphonia:lambda-logging:1.0.3")
    implementation("com.amazonaws.serverless:aws-serverless-java-container-springboot2:1.5.2")

    // HLS DSL
    implementation("io.onema:playlist-dsl:0.1.0")

    // Project commons
    implementation(project(":commons"))


    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
    }
    implementation(kotlin("script-runtime"))
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("springCloudVersion")}")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
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