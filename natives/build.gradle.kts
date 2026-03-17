import com.vanniktech.maven.publish.JavaLibrary
import com.vanniktech.maven.publish.JavadocJar
import org.apache.tools.ant.taskdefs.condition.Os
import java.util.Properties

plugins {
    `java-library`
    id("de.undercouch.download") version "5.4.0"
    alias(libs.plugins.maven.publish.base)
}

base {
    archivesName.set("lavaplayer-natives")
}

mavenPublishing {
    configure(JavaLibrary(JavadocJar.Javadoc()))
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

tasks.register("load") {
    doLast {
        if (!file("$projectDir/samplerate/src").exists()) {
            val downloadPath = "${layout.buildDirectory.get()}/tmp/libsamplerate.tar.xz"
            val unpackPath = "${layout.buildDirectory.get()}/tmp"

            extensions.getByType(de.undercouch.gradle.tasks.download.DownloadExtension::class).run {
                src("https://github.com/libsndfile/libsamplerate/releases/download/$sampleRateVersion/libsamplerate-$sampleRateVersion.tar.xz")
                dest(downloadPath)
            }

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

            extensions.getByType(de.undercouch.gradle.tasks.download.DownloadExtension::class).run {
                src("https://github.com/mstorsjo/fdk-aac/archive/v$fdkAacVersion.zip")
                dest(downloadPath)
            }

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

            extensions.getByType(de.undercouch.gradle.tasks.download.DownloadExtension::class).run {
                src("https://downloads.xiph.org/releases/ogg/libogg-$oggVersion.zip")
                dest(downloadPath)
            }

            copy {
                from(zipTree(file(downloadPath)))
                into("${layout.buildDirectory.get()}/tmp")
            }

            file("${layout.buildDirectory.get()}/tmp/libogg-$oggVersion")
                .renameTo(file("$projectDir/vorbis/libogg"))
        }

        if (!file("$projectDir/vorbis/libvorbis").exists()) {
            val downloadPath = "${layout.buildDirectory.get()}/tmp/temp.zip"

            extensions.getByType(de.undercouch.gradle.tasks.download.DownloadExtension::class).run {
                src("https://downloads.xiph.org/releases/vorbis/libvorbis-$vorbisVersion.zip")
                dest(downloadPath)
            }

            copy {
                from(zipTree(file(downloadPath)))
                into("${layout.buildDirectory.get()}/tmp")
            }

            file("${layout.buildDirectory.get()}/tmp/libvorbis-$vorbisVersion")
                .renameTo(file("$projectDir/vorbis/libvorbis"))
        }

        if (!file("$projectDir/opus/opus").exists()) {
            val downloadPath = "${layout.buildDirectory.get()}/tmp/temp.tar.gz"

            extensions.getByType(de.undercouch.gradle.tasks.download.DownloadExtension::class).run {
                src("https://downloads.xiph.org/releases/opus/opus-$opusVersion.tar.gz")
                dest(downloadPath)
            }

            copy {
                from(tarTree(file(downloadPath)))
                into("${layout.buildDirectory.get()}/tmp")
            }

            file("${layout.buildDirectory.get()}/tmp/opus-$opusVersion")
                .renameTo(file("$projectDir/opus/opus"))
        }

        if (!Os.isFamily(Os.FAMILY_WINDOWS) && !file("$projectDir/mp3/mpg123").exists()) {
            val downloadPath = "${layout.buildDirectory.get()}/tmp/temp.tar.bz2"

            extensions.getByType(de.undercouch.gradle.tasks.download.DownloadExtension::class).run {
                src("https://www.mpg123.de/download/mpg123-$mpg123Version.tar.bz2")
                dest(downloadPath)
            }

            copy {
                from(tarTree(file(downloadPath)))
                into("${layout.buildDirectory.get()}/tmp")
            }

            file("${layout.buildDirectory.get()}/tmp/mpg123-$mpg123Version")
                .renameTo(file("$projectDir/mp3/mpg123"))
        }
    }
}
