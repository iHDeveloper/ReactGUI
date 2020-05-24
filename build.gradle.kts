import de.undercouch.gradle.tasks.download.Download

plugins {
    java
    id ("de.undercouch.download") version "4.0.4"
}

val buildTools = BuildTools(
        minecraftVersion = "1.8.8"
)

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
        dependsOn(":run-build-tools")
    }

    /**
     * Download the build tools
     */
    register<Download>("download-build-tools") {
        src("https://hub.spigotmc.org/jenkins/job/BuildTools/lastSuccessfulBuild/artifact/target/BuildTools.jar")
        dest(buildTools.file)
    }

    /**
     * Run build tools to create tools for the workspace
     */
    register("run-build-tools") {
        val temp = buildTools.temp
        if (!temp.exists())
            temp.mkdir()

        if (temp.isFile)
            error("Can't use the folder [.build-tools] because it's a file")

        if (!buildTools.file.exists())
            dependsOn(":download-build-tools")

        javaexec {
            workingDir = buildTools.temp
            main = "-jar"
            args = mutableListOf<String>(
                    buildTools.file.absolutePath,
                    "--rev",
                    buildTools.minecraftVersion
            )
        }
    }

}

class BuildTools (
        val minecraftVersion: String
) {
    val temp = File(".build-tools")
    val file = File(temp, "build-tools.jar")
}
