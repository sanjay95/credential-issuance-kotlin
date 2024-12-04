plugins {
    kotlin("jvm") version "1.9.25"
    kotlin("plugin.spring") version "1.9.25"
    id("org.springframework.boot") version "3.4.0"
    id("io.spring.dependency-management") version "1.1.6"
}

group = "com.example"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-jdbc")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework:spring-webflux:6.2.0")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    runtimeOnly("com.h2database:h2")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
//    runtimeOnly(
//        files(
//            "src/lib/auth-provider/1.29.0/auth-provider-1.29.0.jar",
//            "src/lib/auth-provider/1.29.0/auth-provider-1.29.0.jar"
//        )
//    )
//    runtimeOnly(fileTree("src/main/lib") { include("**/*.jar") })
    runtimeOnly(files("src/main/lib/auth-provider/1.29.0/auth-provider-1.29.0.jar"))
    implementation("io.github.cdimascio:dotenv-kotlin:6.0.0")
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
