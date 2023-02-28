import java.util.Properties;
import java.io.FileInputStream;

plugins {
    id("org.jetbrains.kotlin.jvm")
    id("maven-publish")
    kotlin("plugin.serialization")
}

group = "com.qawaz.jvm"
version = "1.0.1"

repositories {
    mavenCentral()
}

dependencies {

    // Kotlin
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:1.5.2")

    // Ktor
    val ktorVersion = "2.2.3"
    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-cio:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")

    // Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.0")

    // Testing
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.2")

}

afterEvaluate {
    publishing {
        publications {
            register<MavenPublication>("release") {
                from(components["kotlin"])
            }
        }
        repositories {
            maven("https://maven.pkg.github.com/Qawaz/jvm-updater") {
                name = "GithubPackages"
                try {
                    credentials {
                        username = (System.getenv("GPR_USER")).toString()
                        password = (System.getenv("GPR_API_KEY")).toString()
                    }
                } catch(e : Throwable){
                    e.printStackTrace()
                }
            }
        }
    }
}