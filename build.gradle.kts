plugins {
	java
	id("org.springframework.boot") version "4.0.0"
	id("io.spring.dependency-management") version "1.1.7"
	// id("org.hibernate.orm") version "7.1.8.Final"
	// id("org.graalvm.buildtools.native") version "0.11.3"
}

group = "com.jusconnect"
version = "0.0.1-SNAPSHOT"
description = "Backend do sistema JusConnect"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
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
    // Implementations principais
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.auth0:java-jwt:4.4.0")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.7.0")

    // Lombok
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    // Somente em desenvolvimento
    developmentOnly("org.springframework.boot:spring-boot-devtools")

    // Runtime
    runtimeOnly("org.postgresql:postgresql")
    runtimeOnly("com.h2database:h2")

    // Testes
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("com.h2database:h2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

// hibernate {
// 	enhancement {
// 		enableAssociationManagement = true
// 	}
// }

tasks.withType<Test> {
	useJUnitPlatform()
}
