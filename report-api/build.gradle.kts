import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    application
    id("org.springframework.boot") version "3.2.2"
    id("io.spring.dependency-management") version "1.1.3"
    id("com.github.ben-manes.versions") version "0.48.0"
    id("io.freefair.lombok") version "8.6"
}


group = "ru.volkov"
version = "1.0-SNAPSHOT"

application { mainClass.set("ru.volkov.report.Application") }

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-logging")

    implementation("jakarta.validation:jakarta.validation-api:3.0.0")
    implementation("org.openapitools:jackson-databind-nullable:0.2.8")
    implementation("org.springframework.boot:spring-boot-devtools")

    implementation("org.mapstruct:mapstruct:1.6.3")
    annotationProcessor("org.mapstruct:mapstruct-processor:1.6.3")

    implementation("net.datafaker:datafaker:2.0.1")

    implementation("com.clickhouse:clickhouse-jdbc:0.6.3")
    implementation("org.springframework.boot:spring-boot-starter-jdbc")

    implementation("org.apache.httpcomponents.core5:httpcore5:5.2.1")
    implementation("org.apache.httpcomponents.client5:httpclient5:5.2.1")

    implementation(platform("software.amazon.awssdk:bom:2.25.60"))
    implementation("software.amazon.awssdk:s3")
    implementation("software.amazon.awssdk:auth")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
}


tasks.test {
    useJUnitPlatform()
    testLogging {
        exceptionFormat = TestExceptionFormat.FULL
        events = mutableSetOf(TestLogEvent.FAILED, TestLogEvent.PASSED, TestLogEvent.SKIPPED)
        showStandardStreams = true
    }
}

