plugins {
    java
    id("org.springframework.boot") version "3.4.1"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "com.app"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.1")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    // WebSocket
    implementation("org.springframework.boot:spring-boot-starter-websocket")
    // MongoDB
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb")
    //Redis
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    //Jackson Datatype: JSR310
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
    //Querydsl
    implementation("com.querydsl:querydsl-jpa:5.0.0:jakarta")
    //JJWT :: API
    implementation("io.jsonwebtoken:jjwt-api:0.12.6")
    //Redisson/Spring Boot Starter
    implementation("org.redisson:redisson-spring-boot-starter:3.45.0")

    compileOnly("org.projectlombok:lombok")
    developmentOnly("org.springframework.boot:spring-boot-devtools")

    //JJWT :: Impl
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.6")
    //JJWT :: Extensions :: Jackson
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.6")
    runtimeOnly("com.h2database:h2")
    runtimeOnly("com.mysql:mysql-connector-j")

    annotationProcessor("org.projectlombok:lombok")
    //Jakarta Annotations API
    annotationProcessor("jakarta.annotation:jakarta.annotation-api")
    //Jakarta Persistence API
    annotationProcessor("jakarta.persistence:jakarta.persistence-api")
    //Querydsl
    annotationProcessor("com.querydsl:querydsl-apt:5.0.0:jakarta")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.mockito:mockito-inline:5.2.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    //test lombok
    testImplementation("org.projectlombok:lombok")
    testAnnotationProcessor("org.projectlombok:lombok")

    // env 파일 사용 (카카오 보안 키)
    implementation("me.paulschwarz:spring-dotenv:4.0.0")

    // kafka 의존성
    implementation ("org.springframework.kafka:spring-kafka")
}

tasks.withType<Test> {
    useJUnitPlatform {
        val includeTags = project.findProperty("includeTags") as String?
        val excludeTags = project.findProperty("excludeTags") as String?

        includeTags?.split(",")?.forEach { tag -> includeTags(tag.trim()) }
        excludeTags?.split(",")?.forEach { tag -> excludeTags(tag.trim()) }
    }
}

//Querydsl - Start
val generatedDir = file("src/main/generated")

sourceSets {
    main {
        java {
            srcDir(generatedDir)
        }
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.generatedSourceOutputDirectory.set(generatedDir)
}

tasks {
    clean {
        doFirst {
            delete(generatedDir)
        }
    }
}
//Querydsl - End