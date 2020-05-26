import de.undercouch.gradle.tasks.download.Download
import org.yaml.snakeyaml.Yaml
import java.io.ByteArrayInputStream
import java.io.FileInputStream
import java.nio.charset.StandardCharsets

plugins {
    java
    id ("de.undercouch.download") version "4.0.4"
    id ("com.github.johnrengelman.shadow") version "5.2.0"
}

group = "me.ihdeveloper"
version = "0.1"

val buildTools = BuildTools(

        // Server Version
        minecraftVersion = "1.8.8",

        // Spigot = true
        // Craftbukkit = false
        useSpigot = true,

        // The gradle class of the kit
        gradleStart = "com.example.plugin.GradleStart"
)

repositories {
    mavenCentral()
}

val serverJarConfig: Configuration by configurations.creating

dependencies {
    // Include the server jar source
    serverJarConfig(files(buildTools.serverJar.absolutePath))
    compileOnly(serverJarConfig)

    testCompileOnly("junit", "junit", "4.12")
}

buildscript {

    dependencies {
        // For reading the Main class in plugin.yml
        classpath("org.yaml", "snakeyaml", "1.26")
    }

}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}

tasks {

    getByName("clean").doLast {
        // Delete the production server directory
        buildTools.productionServer.delete()

        // Delete the debug sever directory
        buildTools.debugServer.delete()

        // Delete the common server directory
        buildTools.commonServer.delete()
    }

    /**
     * Overwrite the build task to put the compiled jar into the build folder instead of build/libs
     */
    build {
        dependsOn(":shadowJar")

        doLast {
            copy {
                val libsDir = buildTools.libsDir
                from(libsDir)
                into(libsDir.parent)
            }
        }
    }

    /**
     *  Setup the workspace to develop the plugin
     */
    register("setup") {

        // Build the production plugin to be able to test it
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

        buildTools.debugServer.enabled = false

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

    /**
     * Run the production server with the production plugin on it
     */
    register("run") {
        dependsOn(":build-production-plugin")

        doLast {
            val server = buildTools.productionServer

            printIntro()
            logger.lifecycle("> Starting the production server...")
            logger.lifecycle("")

            javaexec {
                standardInput = System.`in`
                workingDir = server.dir
                main = "-jar"
                args = mutableListOf(
                        server.jar.absolutePath
                )
            }
        }
    }

    /**
     * Build the debug server to enable the debugging feature
     */
    register("build-debug-server") {
        dependsOn(":build-common-server")

        val server = buildTools.debugServer

        // Enable debug for overwriting shadowJar
        server.enabled = true

        dependsOn(":shadowJar")

        server.mkdir()

        doLast {

            // Copy the common server contents into debug server
            buildTools.commonServer.copyTo(server)

            // Copy the compiled jar with server jar source + plugin source
            copy {
                from(buildTools.libsDir)
                into(server.dir)
                rename {
                    server.jar.name
                }
            }
        }

    }

    // Overwrite the shadow jar when the debug is enabled for writing server jar with plugin source
    // So, we can intercept it easily
    shadowJar {
        val fileName = "${archiveBaseName.get()}-${archiveVersion.get()}.${archiveExtension.get()}"
        archiveFileName.set(fileName)

        doFirst {
            if (buildTools.debugServer.enabled) {

                // Include the server jar source
                configurations.add(serverJarConfig)

                manifest {

                    // Points the start point to be at gradle start
                    attributes["Main-Class"] = buildTools.gradleStartClass
                }

                // Include everything
                include("**")

                // Exclude the main class
                exclude(buildTools.pluginMainClassFile)
            } else {

                // Exclude gradle start from the production plugin
                exclude("${buildTools.gradleStartClass}.class")
            }
        }
    }

    /**
     * Build a plugin that special for debugging process and it's not useful in any production environment
     */
    register("build-debug-plugin") {
        dependsOn(":build-debug-server")
        dependsOn(":jar")

        // Debug mode is already enabled in :build-debug-server

        doLast {

            // Copy the plugin to the debug plugins folder
            copy {
                from(buildTools.libsDir)
                into(buildTools.debugServer.plugins)
                rename {
                    buildTools.pluginJarName
                }
            }
        }
    }

    // Overwrite jar task so that when debug is enabled we can create the plugin jar for debugging
    jar {
        doFirst {
            if (buildTools.debugServer.enabled) {
                // Include anything else
                include("*.*")

                // Exclude any class file because they exist in the debug server jar
                exclude("*.class")

                // Include the main class only because we need it for the plugin load process
                include(buildTools.pluginMainClassFile)
            }
        }
    }

    /**
     * Run the debug server with debug version of the plugin
     */
    register("debug") {
        dependsOn(":build-debug-plugin")

        doLast {
            val server = buildTools.debugServer

            printIntro()

            javaexec {
                standardInput = System.`in`
                workingDir = server.dir
                main = "-jar"
                args = mutableListOf(
                        server.jar.absolutePath
                )
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

fun printIntro() {
    val intro = arrayOf(
            """=================================""",
            """   _____       _             __  """,
            """  / ___/____  (_)___ _____  / /_ """,
            """  \__ \/ __ \/ / __ `/ __ \/ __/ """,
            """  ___/ / /_/ / / /_/ / /_/ / /_  """,
            """/____/ .___/_/\__, /\____/\__/   """,
            """    /_/      /____/              """,
            """                                 """,
            """    [#] Spigot Starter Kit [#]   """,
            """        By: @iHDeveloper         """,
            """================================="""
    )
    for (line in intro) {
        logger.lifecycle(line)
    }
}

class BuildTools (
        val minecraftVersion: String,
        val useSpigot: Boolean,
        gradleStart: String
) {
    val buildDir = File(".build-tools")
    val file = File(buildDir, "build-tools.jar")

    val libsDir = File("build/libs/")

    private val pluginConfig = File("src/main/resources/plugin.yml")

    private val serversDir = File("server")

    init {
        serversDir.mkdir()
    }

    val commonServer = CommonServer(serversDir)
    val productionServer = ProductionServer(serversDir)
    val debugServer = DebugServer(serversDir)

    val pluginJarName: String
        get() {
            return "${rootProject.name}.jar"
        }

    var gradleStartClass: String

    init {
        gradleStartClass = gradleStart
        while(gradleStartClass.contains(".")) {
            gradleStartClass = gradleStartClass.replace(".", "/")
        }
    }

    val serverJar = if (useSpigot) {
        File(buildDir, "spigot-${minecraftVersion}.jar")
    } else {
        File(buildDir, "craftbukkit-${minecraftVersion}.jar")
    }

    val pluginMainClassFile: String
        get() {
            val configFile = buildTools.pluginConfig
            val yaml = Yaml()
            val data = yaml.load(FileInputStream(configFile)) as Map<String, Any>
            var mainClass = data["main"] as String
            while (mainClass.contains(".")) {
                mainClass = mainClass.replace(".", "/")
            }
            mainClass = "${mainClass}.class"
            return mainClass
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

/**
 * An environment that offers the debugging feature to the plugin developer
 */
class DebugServer (
        parent: File
) : Server(parent, "debug") {

    var enabled = false

}
