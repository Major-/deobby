import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import org.jetbrains.kotlin.gradle.plugin.KotlinPluginWrapper
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    base
    `maven-publish`
    id("com.jfrog.bintray") version "1.8.4" apply(false)
    id("com.github.ben-manes.versions") version "0.27.0"

    kotlin("jvm") version "1.3.61" apply false
}

allprojects {
    repositories {
        mavenCentral()
        jcenter()
        maven(url = "https://dl.bintray.com/michaelbull/maven")
    }
}

tasks.withType<DependencyUpdatesTask> {
    rejectVersionIf {
        listOf("alpha", "beta", "rc", "cr", "m", "eap", "pr").any {
            candidate.version.contains(it, ignoreCase = true)
        }
    }
}

subprojects {
    apply(plugin = "maven-publish")
    apply(plugin = "java")
    apply(plugin = "com.jfrog.bintray")

    plugins.withType<JavaPlugin> {
        tasks.withType<Test> {
            failFast = true
            useJUnitPlatform()
        }
    }

    plugins.withType<com.jfrog.bintray.gradle.BintrayPlugin> {
        configure<com.jfrog.bintray.gradle.BintrayExtension> {
            user = System.getenv("BINTRAY_USER")
            key = System.getenv("BINTRAY_KEY")
            publish = true
            setPublications("mavenJava")

            pkg(delegateClosureOf<com.jfrog.bintray.gradle.BintrayExtension.PackageConfig> {
                name = "deobby"
                userOrg = "apollo-rsps"
                desc = description
                repo = property("bintray.repository") as String

                setLicenses("ISC")
            })
        }
    }

    plugins.withType<PublishingPlugin> {
        configure<PublishingExtension >{
            (publications) {
                create<MavenPublication>("mavenJava") {
                    from(components["java"])

                    groupId = project.group as String
                    artifactId = project.name
                    version = project.version as String
                }
            }
        }
    }

    plugins.withType<KotlinPluginWrapper> {
        val implementation by configurations
        val testImplementation by configurations

        dependencies {
            implementation(kotlin("stdlib-jdk8"))
            implementation(enforcedPlatform("org.junit:junit-bom:5.5.2"))
            implementation("com.michael-bull.kotlin-inline-logger:kotlin-inline-logger-jvm:1.0.0")
            implementation("com.michael-bull.kotlin-result:kotlin-result:1.1.3")

            testImplementation(enforcedPlatform("org.junit:junit-bom:5.5.2"))
            testImplementation("org.junit.jupiter:junit-jupiter")
        }

        tasks.withType<KotlinCompile> {
            kotlinOptions {
                jvmTarget = "1.8"
                freeCompilerArgs = listOf("-Xjsr305=strict")
            }
        }
    }
}
