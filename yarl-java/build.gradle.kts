plugins {
  `java-library`
}

repositories {
  mavenCentral()
}

dependencies {
  implementation(project(":yarl-core"))

  testImplementation("junit:junit:4.13.1")
  testImplementation("org.assertj:assertj-core:3.19.0")
  testImplementation("ch.qos.logback:logback-classic:1.2.3")
}

description = "yarl-java"

tasks.withType<JavaCompile>()
  .configureEach {
    sourceCompatibility = JavaVersion.VERSION_11.toString()
    targetCompatibility = JavaVersion.VERSION_11.toString()
  }
