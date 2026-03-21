import com.vanniktech.maven.publish.JavaLibrary
import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.MavenPublishBaseExtension
import com.vanniktech.maven.publish.SonatypeHost
import org.apache.tools.ant.taskdefs.condition.Os
import java.net.HttpURLConnection
import java.net.URI
import java.util.Properties

plugins {
    `java-library`
    alias(libs.plugins.maven.publish.base)
}

base {
    archivesName.set("lavaplayer-natives")
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

mavenPublishing {
    configure(JavaLibrary(JavadocJar.Javadoc()))
    coordinates(group.toString(), base.archivesName.get(), version.toString())

    val mavenCentralUsername = findProperty("mavenCentralUsername") as String?
    val mavenCentralPassword = findProperty("mavenCentralPassword") as String?
    if (!mavenCentralUsername.isNullOrEmpty() && !mavenCentralPassword.isNullOrEmpty()) {
        publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL, false)
        if (version.toString().let { !it.endsWith("-SNAPSHOT") }) {
            signAllPublications()
        }
    } else {
        logger.lifecycle("Not publishing to OSSRH due to missing credentials")
    }

    pom {
        name = "lavaplayer"
        description = "A Lavaplayer fork maintained by Lavalink"
        url = "https://github.com/lavalink-devs/lavaplayer"

        licenses {
            license {
                name = "The Apache License, Version 2.0"
                url = "https://github.com/lavalink-devs/lavaplayer/blob/main/LICENSE"
            }
        }

        developers {
            developer {
                id = "freyacodes"
                name = "Freya Arbjerg"
                url = "https://www.arbjerg.dev"
            }
        }

        scm {
            url = "https://github.com/lavalink-devs/lavaplayer/"
            connection = "scm:git:git://github.com/lavalink-devs/lavaplayer.git"
            developerConnection = "scm:git:ssh://git@github.com/lavalink-devs/lavaplayer.git"
        }
    }
}

publishing {
    val mavenUsername = findProperty("MAVEN_USERNAME") as String?
    val mavenPassword = findProperty("MAVEN_PASSWORD") as String?
    if (!mavenUsername.isNullOrEmpty() && !mavenPassword.isNullOrEmpty()) {
        repositories {
            val snapshots = "https://maven.lavalink.dev/snapshots"
            val releases = "https://maven.lavalink.dev/releases"
            val isRelease = version.toString().let { !it.endsWith("-SNAPSHOT") }

            maven(if (isRelease) releases else snapshots) {
                credentials {
                    username = mavenUsername
                    password = mavenPassword
                }
            }
        }
    } else {
        logger.lifecycle("Not publishing to maven.lavalink.dev because credentials are not set")
    }
}

val versionProps = Properties().apply {
    file("$projectDir/versions.properties").inputStream().use { load(it) }
}

val opusVersion = versionProps["opus"] as String
val mpg123Version = versionProps["mpg123"] as String
val oggVersion = versionProps["ogg"] as String
val vorbisVersion = versionProps["vorbis"] as String
val sampleRateVersion = versionProps["samplerate"] as String
val fdkAacVersion = versionProps["fdkaac"] as String

fun downloadFile(url: String, dest: String) {
    val destFile = file(dest)
    destFile.parentFile.mkdirs()
    val connection = URI(url).toURL().openConnection() as HttpURLConnection
    connection.instanceFollowRedirects = true
    connection.inputStream.use { input ->
        destFile.outputStream().use { output ->
            input.copyTo(output)
        }
    }
}

tasks.register("load") {
    doLast {
        if (!file("$projectDir/samplerate/src").exists()) {
            val downloadPath = "${layout.buildDirectory.get()}/tmp/libsamplerate.tar.xz"
            val unpackPath = "${layout.buildDirectory.get()}/tmp"

            downloadFile(
                "https://github.com/libsndfile/libsamplerate/releases/download/$sampleRateVersion/libsamplerate-$sampleRateVersion.tar.xz",
                downloadPath
            )

            val process = ProcessBuilder("tar", "xf", downloadPath, "-C", unpackPath)
                .inheritIO()
                .start()

            val exitCode = process.waitFor()
            if (exitCode != 0) {
                throw IllegalStateException("Failed to extract libsamplerate.")
            }

            copy {
                from("$unpackPath/libsamplerate-$sampleRateVersion/src")
                into("$projectDir/samplerate/src")
            }

            copy {
                from("$unpackPath/libsamplerate-$sampleRateVersion/include")
                into("$projectDir/samplerate/include")
            }
        }

        if (!file("$projectDir/fdk-aac/libAACdec").exists()) {
            val downloadPath = "${layout.buildDirectory.get()}/tmp/fdk-aac-v$fdkAacVersion.zip"
            val unpackPath = "${layout.buildDirectory.get()}"

            downloadFile(
                "https://github.com/mstorsjo/fdk-aac/archive/v$fdkAacVersion.zip",
                downloadPath
            )

            copy {
                from(zipTree(file(downloadPath)))
                into(unpackPath)
            }

            copy {
                from("$unpackPath/fdk-aac-$fdkAacVersion")
                into("$projectDir/fdk-aac")
                exclude("CMakeLists.txt")
            }
        }

        if (!file("$projectDir/vorbis/libogg").exists()) {
            val downloadPath = "${layout.buildDirectory.get()}/tmp/temp.zip"

            downloadFile(
                "https://downloads.xiph.org/releases/ogg/libogg-$oggVersion.zip",
                downloadPath
            )

            copy {
                from(zipTree(file(downloadPath)))
                into("${layout.buildDirectory.get()}/tmp")
            }

            file("$projectDir/vorbis").mkdirs()
            file("${layout.buildDirectory.get()}/tmp/libogg-$oggVersion")
                .renameTo(file("$projectDir/vorbis/libogg"))
        }

        if (!file("$projectDir/vorbis/libvorbis").exists()) {
            val downloadPath = "${layout.buildDirectory.get()}/tmp/temp.zip"

            downloadFile(
                "https://downloads.xiph.org/releases/vorbis/libvorbis-$vorbisVersion.zip",
                downloadPath
            )

            copy {
                from(zipTree(file(downloadPath)))
                into("${layout.buildDirectory.get()}/tmp")
            }

            file("$projectDir/vorbis").mkdirs()
            file("${layout.buildDirectory.get()}/tmp/libvorbis-$vorbisVersion")
                .renameTo(file("$projectDir/vorbis/libvorbis"))
        }

        if (!file("$projectDir/opus/opus").exists()) {
            val downloadPath = "${layout.buildDirectory.get()}/tmp/temp.tar.gz"

            downloadFile(
                "https://downloads.xiph.org/releases/opus/opus-$opusVersion.tar.gz",
                downloadPath
            )

            copy {
                from(tarTree(file(downloadPath)))
                into("${layout.buildDirectory.get()}/tmp")
            }

            file("$projectDir/opus").mkdirs()
            file("${layout.buildDirectory.get()}/tmp/opus-$opusVersion")
                .renameTo(file("$projectDir/opus/opus"))
        }

        if (!Os.isFamily(Os.FAMILY_WINDOWS) && !file("$projectDir/mp3/mpg123").exists()) {
            val downloadPath = "${layout.buildDirectory.get()}/tmp/temp.tar.bz2"

            downloadFile(
                "https://www.mpg123.de/download/mpg123-$mpg123Version.tar.bz2",
                downloadPath
            )

            copy {
                from(tarTree(file(downloadPath)))
                into("${layout.buildDirectory.get()}/tmp")
            }

            file("$projectDir/mp3").mkdirs()
            file("${layout.buildDirectory.get()}/tmp/mpg123-$mpg123Version")
                .renameTo(file("$projectDir/mp3/mpg123"))
        }
    }
}
