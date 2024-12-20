plugins {
    kotlin("jvm") version "1.9.25"
    kotlin("plugin.spring") version "1.9.25"
    kotlin("plugin.serialization") version "1.8.0"
    id("org.springframework.boot") version "3.4.0"
    id("io.spring.dependency-management") version "1.1.6"
    application
}

group = "com.example"
version = "0.0.1"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.0")
    implementation("org.springframework.boot:spring-boot-devtools")
    implementation("org.springframework.boot:spring-boot-starter-data-jdbc")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework:spring-webflux:6.2.0")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    runtimeOnly("com.h2database:h2")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    implementation(files("src/main/lib/auth-provider/1.29.0/auth-provider-1.29.0.jar"))
    implementation("software.amazon.jsii:jsii-runtime:1.68.0")
    implementation("io.github.cdimascio:dotenv-kotlin:6.4.1")
    implementation("io.projectreactor:reactor-core:3.7.0")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

application {
    mainClass.set("credentialissuance.CredentialIssuanceApplicationKt")
}