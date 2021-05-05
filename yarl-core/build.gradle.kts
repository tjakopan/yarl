plugins {
  kotlin("jvm") version "1.5.0"
  `java-library`
}

repositories {
  mavenCentral()
}

dependencies {
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.0-RC")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.5.0-RC")

  testImplementation(kotlin("test-junit"))
  testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.5.0-RC")
  testImplementation("org.assertj:assertj-core:3.19.0")
  testImplementation("ch.qos.logback:logback-classic:1.2.3")
}

description = "yarl-core"

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>()
  .configureEach {
    kotlinOptions {
      jvmTarget = JavaVersion.VERSION_11.toString()
      freeCompilerArgs = listOf("-Xjvm-default=all")
    }
  }
