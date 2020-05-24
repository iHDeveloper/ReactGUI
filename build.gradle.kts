import de.undercouch.gradle.tasks.download.Download

plugins {
    java
    id ("de.undercouch.download") version "4.0.4"
}

val buildTools = BuildTools()

group = "me.ihdeveloper"
version = "0.1"

repositories {
    mavenCentral()
}

dependencies {
    testCompile("junit", "junit", "4.12")
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}

tasks {

    /**
     *  Setup the workspace to develop the plugin
     */
    register("setup") {
        dependsOn(":download-build-tools")
    }

    /**
     * Download the build tools
     */
    register<Download>("download-build-tools") {
        val temp = buildTools.temp
        if (!temp.exists())
            temp.mkdir()

        if (temp.isFile)
            error("Can't use the folder [.build-tools] because it's a file")

        src("https://hub.spigotmc.org/jenkins/job/BuildTools/lastSuccessfulBuild/artifact/target/BuildTools.jar")
        dest(buildTools.file)
    }

}

class BuildTools {
    val temp = File(".build-tools")
    val file = File(temp, "build-tools.jar")
}
