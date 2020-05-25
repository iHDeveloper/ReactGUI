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
        buildTools.runServer.delete()

        // Delete the common server directory
        buildTools.commonServer.delete()
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
        val server = buildTools.commonServer
        onlyIf {
            !server.exists
        }

        server.mkdir()

        // Print the EULA to the user
        printEULA()

        // Wait for 10 seconds to realise the message
        try {
            Thread.sleep(10 * 1000)
        } catch (e: Exception) {}

        // Since the process didn't stop
        // This means the user indicates to agree on the EULA
        // And this code automates the indicates process
        val eula = server.eula
        if (eula.exists()) {
            var text = eula.readText()
            text = text.replace("eula=false", "eula=true", true)
            eula.writeText(text)
        } else {
            eula.writeText("eula=true")
        }


        copy {
            from(buildTools.serverJar)
            into(server.dir)
            rename {
                "server.jar"
            }
        }

        // Sends "stop" command to the common server to stop after initialising
        val stopCommand = "stop"
        val input = ByteArrayInputStream(stopCommand.toByteArray(StandardCharsets.UTF_8))

        javaexec {
            standardInput = input
            workingDir = server.dir
            main = "-jar"
            args = mutableListOf<String>(
                    server.jar.absolutePath
            )
        }

        input.close()

        server.jar.delete()
    }

    /**
     * Build the run server for testing the plugin on it
     */
    register("build-run-server") {
        val server = buildTools.runServer
        onlyIf {
            !server.exists
        }

        server.mkdir()

        copy {
            from(buildTools.serverJar)
            into(server.dir)
            rename {
                server.jar.name
            }
        }

        buildTools.commonServer.copyTo(server)
    }

    /**
     * Build the plugin for the run server
     */
    register("build-run-plugin") {
        dependsOn(":shadowJar")

        doLast {
            copy {
                from(buildTools.libsDir)
                into(buildTools.runServer.plugins)
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
    logger.lifecycle("")
}

class BuildTools (
        val minecraftVersion: String,
        val useSpigot: Boolean
) {
    val buildDir = File(".build-tools")
    val file = File(buildDir, "build-tools.jar")

    val libsDir = File("build/libs/")

    val commonServer = CommonServer()
    val runServer = RunServer()

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

/**
 * Help making the server and structuring it
 */
open class Server(
    private val name: String
) {
    /**
     * Directory of the server
     */
    val dir = File(name)

    /**
     * Plugins of the sever
     */
    val plugins = File(dir, "plugins")

    /**
     * Server jar that manages the server
     */
    val jar = File(dir, "server.jar")

    /**
     * Does the server exist in the right way
     */
    open val exists: Boolean
        get() {
            return dir.exists() and plugins.exists() and jar.exists()
        }

    /**
     * Make the directories required for the server
     */
    fun mkdir() {
        dir.mkdir()
        plugins.mkdir()
    }

    /**
     * Delete the server
     */
    fun delete() {
        dir.deleteRecursively()
    }
}

/**
 * A common server environment that its contents are replicated to the other servers
 *
 * Any change should be on this environment instead of the other environments ( aka servers )
 */
class CommonServer : Server("common_Server") {

    /**
     * The Minecraft's EULA file
     */
    val eula = File(dir, "eula.txt")

    override val exists: Boolean
        get() {
            return eula.exists() and dir.exists() and plugins.exists()
        }

    /**
     * Copy the server contents into a target server
     *
     * It overwrites the contents of the target server
     */
    fun copyTo(target: Server) {
        dir.copyRecursively(
                target = target.dir,
                overwrite = true
        )
    }

}

/**
 * A similar server environment for testing the plugin
 */
class RunServer : Server("run_server") {
}
