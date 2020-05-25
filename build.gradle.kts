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
        // Delete the production server directory
        buildTools.productionServer.delete()

        // Delete the common server directory
        buildTools.commonServer.delete()
    }

    /**
     *  Setup the workspace to develop the plugin
     */
    register("setup") {
        dependsOn(":build-production-plugin")
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
        dependsOn(":run-build-tools")

        val server = buildTools.commonServer

        onlyIf {
            !server.exists
        }

        server.mkdir()

        doLast {
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

            server.build()
        }
    }

    /**
     * Build the production server for testing the plugin on it
     */
    register("build-production-server") {
        dependsOn(":build-common-server")

        val server = buildTools.productionServer

        onlyIf {
            !server.exists
        }

        server.mkdir()

        doLast {
            copy {
                from(buildTools.serverJar)
                into(server.dir)
                rename {
                    server.jar.name
                }
            }

            buildTools.commonServer.copyTo(server)
        }
    }

    /**
     * Build the production plugin for the production server
     */
    register("build-production-plugin") {
        dependsOn("build-production-server")
        dependsOn(":shadowJar")

        doLast {
            copy {
                from(buildTools.libsDir)
                into(buildTools.productionServer.plugins)
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
    val eulaInfo = mutableListOf(
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

    private val serversDir = File("server")

    init {
        serversDir.mkdir()
    }

    val commonServer = CommonServer(serversDir)
    val productionServer = ProductionServer(serversDir)

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
        parent: File,
        name: String
) {
    /**
     * Directory of the server
     */
    val dir = File(parent, name)

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
    open fun delete() {
        // Delete everything including the directory itself
        dir.deleteRecursively()

        // Create an empty directory for better user experience
        dir.mkdir()
    }
}

/**
 * A common server environment that its contents are replicated to the other servers
 *
 * Any change should be on this environment instead of the other environments ( aka servers )
 */
class CommonServer (
        parent: File
) : Server(parent, "common") {

    /**
     * The Minecraft's EULA file
     */
    val eula = File(dir, "eula.txt")

    /**
     * A file to detect if we are ready to copy or not
     */
    private val cache = File(dir.parent, ".build-cache")

    override val exists: Boolean
        get() {
            return dir.exists() and plugins.exists() and eula.exists() and cache.exists()
        }

    /**
     * Build cache file to know that we built before
     *
     * If the file doesn't exist then we will build the common server again
     */
    fun build() {
        cache.createNewFile()
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

    override fun delete() {
        super.delete()

        // Delete the cache file because the contents no longer exists
        cache.delete()
    }
}

/**
 * A similar server environment for testing the plugin
 */
class ProductionServer (
        parent: File
) : Server(parent, "prod")
