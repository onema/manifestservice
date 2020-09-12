import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "2.3.3.RELEASE"
    id("io.spring.dependency-management") version "1.0.10.RELEASE"
    kotlin("jvm") version "1.4.0"
    kotlin("plugin.spring") version "1.4.0"
}

group = "io.onema"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_11

repositories {
    mavenCentral()
    jcenter()
}

extra["springCloudVersion"] = "Hoxton.SR7"

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.springframework.cloud:spring-cloud-function-web")
    implementation("com.github.abashev:vfs-s3:4.3.1")
    implementation("org.springframework.cloud:spring-cloud-aws:2.2.4.RELEASE")

    implementation("org.springframework.cloud:spring-cloud-function-adapter-aws:3.0.10.RELEASE")
    implementation("com.amazonaws:aws-lambda-java-log4j2:1.2.0")
    implementation("com.amazonaws:aws-lambda-java-events:3.2.0")

    implementation("com.amazonaws:aws-lambda-java-core:1.2.1")

    // build plugins?
    implementation("org.springframework.boot.experimental:spring-boot-thin-layout:1.0.25.RELEASE")

    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
    }




    // include for server side
    implementation("org.jetbrains.kotlinx:kotlinx-html-jvm:0.6.10")

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
