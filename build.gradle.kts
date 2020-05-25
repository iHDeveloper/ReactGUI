import de.undercouch.gradle.tasks.download.Download

import java.io.ByteArrayInputStream
import java.nio.charset.StandardCharsets

plugins {
    java
    id ("de.undercouch.download") version "4.0.4"
    id ("com.github.johnrengelman.shadow") version "5.2.0"
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

    testCompileOnly("junit", "junit", "4.12")
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}

tasks {

    getByName("clean").doLast {
        // Delete the run server directory
        buildTools.runServerDir.deleteRecursively()

        // Delete the common server directory
        buildTools.commonServerDir.deleteRecursively()
    }

    /**
     *  Setup the workspace to develop the plugin
     */
    register("setup") {
        dependsOn(":run-build-tools")
        dependsOn(":build-common-server")
        dependsOn(":build-run-server")
    }

    /**
     * Download the build tools
     */
    register<Download>("download-build-tools") {
        onlyIf {
            !buildTools.file.exists()
        }

        val temp = buildTools.buildDir

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
                workingDir = buildTools.buildDir
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
     * Build the common server to be copied from later
     */
    register("build-common-server") {
        onlyIf {
            !buildTools.commonServerDir.exists()
            !buildTools.commonServerPlugins.exists()
            !buildTools.commonServerEULA.exists()
        }

        buildTools.commonServerDir.mkdir()
        buildTools.commonServerPlugins.mkdir()

        // Print the EULA to the user
        printEULA()

        // Wait for 10 seconds to realise the message
        try {
            Thread.sleep(10 * 1000)
        } catch (e: Exception) {}

        // Since the process didn't stop
        // This means the user indicates to agree on the EULA
        // And this code automates the indicates process
        if (buildTools.commonServerEULA.exists()) {
            var text = buildTools.commonServerEULA.readText()
            text = text.replace("eula=false", "eula=true", true)
            buildTools.commonServerEULA.writeText(text)
        } else {
            buildTools.commonServerEULA.writeText("eula=true")
        }


        copy {
            from(buildTools.serverJar)
            into(buildTools.commonServerDir)
            rename {
                "server.jar"
            }
        }

        // Sends "stop" command to the common server to stop after initialising
        val stopCommand = "stop"
        val input = ByteArrayInputStream(stopCommand.toByteArray(StandardCharsets.UTF_8))

        javaexec {
            standardInput = input
            workingDir = buildTools.commonServerDir
            main = "-jar"
            args = mutableListOf<String>(
                    buildTools.commonServerJar.absolutePath
            )
        }

        input.close()

        buildTools.commonServerJar.delete()
    }

    /**
     * Build the run server for testing the plugin on it
     */
    register("build-run-server") {
        onlyIf {
            !buildTools.runServerDir.exists()
            !buildTools.runServerPlugins.exists()
        }

        buildTools.runServerDir.mkdir()
        buildTools.runServerPlugins.mkdir()

        copy {
            from(buildTools.serverJar)
            into(buildTools.runServerJar.parent)
            rename {
                "server.jar"
            }
        }
    }

    /**
     * Build the plugin for the run server
     */
    register("build-run-plugin") {
        dependsOn(":shadowJar")

        doLast {
            copy {
                from(buildTools.libsDir)
                into(buildTools.runServerPlugins)
                rename {
                    buildTools.pluginJarName
                }
            }
        }
    }

}

/**
 * Print to the user that using the kit indicates that his/her agreement to Minecraft's EULA
 */
fun printEULA() {
    val eulaInfo = mutableListOf<String>(
            " _____________________________________________________________________________________",
            "|  _________________________________________________________________________________  |",
            "| |                                                                                 | |",
            "| |                        ███████╗██╗   ██╗██╗      █████╗                         | |",
            "| |                        ██╔════╝██║   ██║██║     ██╔══██╗                        | |",
            "| |                        █████╗  ██║   ██║██║     ███████║                        | |",
            "| |                        ██╔══╝  ██║   ██║██║     ██╔══██║                        | |",
            "| |                        ███████╗╚██████╔╝███████╗██║  ██║                        | |",
            "| |                                                                                 | |",
            "| |                                                                                 | |",
            "| |                [#] By using @iHDeveloper/spigot-starter-kit [#]                 | |",
            "| |                                                                                 | |",
            "| |              You are indicating your agreement to Minecraft's EULA              | |",
            "| |               https://account.mojang.com/documents/minecraft_eula               | |",
            "| |_________________________________________________________________________________| |",
            "|_____________________________________________________________________________________|"
    )

    // Separate the EULA for more attention
    for (i in 1..3) {
        logger.lifecycle("")
    }

    for (i in eulaInfo) {
        logger.lifecycle(i)
    }

    // Separate the EULA for more attention
    for (i in 1..3) {
        logger.lifecycle("")
    }
}

class BuildTools (
        val minecraftVersion: String,
        val useSpigot: Boolean
) {
    val buildDir = File(".build-tools")
    val file = File(buildDir, "build-tools.jar")

    val libsDir = File("build/libs/")

    val commonServerDir = File("common_server")
    val commonServerPlugins = File(commonServerDir, "plugins")
    val commonServerJar = File(commonServerDir, "server.jar")
    val commonServerEULA = File(commonServerDir, "eula.txt")

    val runServerDir = File("run_server")
    val runServerJar = File(runServerDir, "server.jar")
    val runServerPlugins = File(runServerDir, "plugins")

    val pluginJarName: String
        get() {
            return "${rootProject.name}.jar"
        }

    val serverJar = if (useSpigot) {
        File(buildDir, "spigot-${minecraftVersion}.jar")
    } else {
        File(buildDir, "craftbukkit-${minecraftVersion}.jar")
    }
}
