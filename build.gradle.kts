import de.undercouch.gradle.tasks.download.Download

plugins {
    java
    id ("de.undercouch.download") version "4.0.4"
}

val buildTools = BuildTools(

        // Server Version
        minecraftVersion = "1.8.8",

        // Spigot = true
        // Craftbukkit = false
        useSpigot = true
)

group = "me.ihdeveloper"
version = "0.1"

repositories {
    mavenCentral()
}

dependencies {
    compileOnly(files(buildTools.serverJar.absolutePath))

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
        dependsOn(":build-run-server")
    }

    /**
     * Download the build tools
     */
    register<Download>("download-build-tools") {
        onlyIf {
            !buildTools.file.exists()
        }

        val temp = buildTools.temp

        // Check if the temporary folder doesn't exist
        if (!temp.exists())
            temp.mkdir() // Create the temporary folder

        // Check if the temporary folder is file
        if (temp.isFile)
            error("Can't use the folder [.build-tools] because it's a file")

        src("https://hub.spigotmc.org/jenkins/job/BuildTools/lastSuccessfulBuild/artifact/target/BuildTools.jar")
        dest(buildTools.file)
    }

    /**
     * Run build tools to create tools for the workspace
     */
    register("run-build-tools") {
        dependsOn(":download-build-tools")

        onlyIf {
            !buildTools.serverJar.exists()
        }

        doLast {
            // Run the build tools to generate the server
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

    /**
     * Build the run server for testing the plugin on it
     */
    register("build-run-server") {
        onlyIf {
            !buildTools.runServer.exists()
        }

        buildTools.runServer.mkdir()

        copy {
            from(buildTools.serverJar)
            into(buildTools.runServerJar.parent)
            rename {
                "server.jar"
            }
        }
    }

}

class BuildTools (
        val minecraftVersion: String,
        val useSpigot: Boolean
) {
    val temp = File(".build-tools")
    val file = File(temp, "build-tools.jar")

    val runServer = File("run_server")
    val runServerJar = File(runServer, "server.jar")

    val serverJar = if (useSpigot) {
        File(temp, "spigot-${minecraftVersion}.jar")
    } else {
        File(temp, "craftbukkit-${minecraftVersion}.jar")
    }
}
