import de.undercouch.gradle.tasks.download.Download
import java.io.ByteArrayInputStream
import java.nio.charset.StandardCharsets

plugins {
    java
    kotlin("jvm") version "1.4.21"
    id ("de.undercouch.download") version "4.0.4"
    id ("com.github.johnrengelman.shadow") version "5.2.0"
}

val server = Server(
        /**
         * Directory of the server
         */
        dir = File("${rootProject.projectDir}/server")
)

val buildTools = BuildTools(

        // Server Version
        minecraftVersion = "1.8.8",

        // Spigot = true
        // Craftbukkit = false
        useSpigot = true,

        // Use local cached dependency (default = false)
        useLocalDependency = true
)

allprojects {
    group = "me.ihdeveloper"
    version = "0.1"

    if (project != rootProject) {
        apply(plugin = "java")
        apply(plugin = "kotlin")
        apply(plugin = "de.undercouch.download")
        apply(plugin = "com.github.johnrengelman.shadow")
    }

    repositories {
        mavenCentral()
        mavenLocal()
    }

    dependencies {
        val stdlib = kotlin("stdlib-jdk8")

        if (project == rootProject) {
            compileOnly(stdlib)
        } else {
            implementation(stdlib)
        }

        // Include the server jar source
        if (buildTools.useLocalDependency) {
            compileOnly("org.spigotmc:spigot:1.8.8-R0.1-SNAPSHOT")
        } else {
            if (server.jar.exists()) {
                compileOnly(files(server.jar.absolutePath))
            } else if (buildTools.serverJar.exists()) {
                compileOnly(files(buildTools.serverJar.absolutePath))
            }
        }

        testCompileOnly("junit", "junit", "4.12")
    }

    configure<JavaPluginConvention> {
        sourceCompatibility = JavaVersion.VERSION_1_8
    }

    tasks {
        /**
         * Overwrite the build task to put the compiled jar into the build folder instead of build/libs
         */
        build {
            dependsOn("shadowJar")

            // Copy the compiled plugin jar from build/libs to build/
            doLast {
                copy {
                    val libsDir = File("${project.buildDir}/libs")
                    from(libsDir)
                    into(libsDir.parent)
                }
            }
        }

        shadowJar {
            from("LICENSE")

            val name = "${archiveBaseName.get()}-${archiveVersion.get()}.${archiveExtension.get()}"
            archiveFileName.set(name)
        }

        /**
         * Build the plugin for the server
         */
        register("build-plugin") {
            dependsOn("build")
            if (project == rootProject) {
                subprojects.forEach { dependsOn(":${it.name}:build-plugin") }
            }

            doLast {

                // Copy generated plugin jar into server plugins folder
                copy {
                    from(project.buildDir)
                    into(server.plugins)
                }

                logger.lifecycle("Built! plugins/${shadowJar.get().archiveFileName.get()}")
            }
        }
    }
}

tasks {

    /**
     *  Setup the workspace to develop the plugin
     */
    register("setup") {

        // Build the plugin to be able to test it
        dependsOn("build-plugin")
    }

    /**
     * Download the build tools
     */
    register<Download>("download-build-tools") {
        onlyIf {
            !buildTools.useLocalDependency && !buildTools.file.exists() && !server.jar.exists()
        }

        val temp = buildTools.buildDir

        // Check if the temporary folder doesn't exist
        if (!temp.exists())
            temp.mkdir() // Create the temporary folder

        // Check if the temporary folder is file
        if (temp.isFile)
            error("Can't use the folder [.build-tools] because it's a file")

        // Download the latest successful build of SpigotMC/BuildTools
        src("https://hub.spigotmc.org/jenkins/job/BuildTools/lastSuccessfulBuild/artifact/target/BuildTools.jar")

        // Put it in the .build-tools/
        dest(buildTools.file)
    }

    /**
     * Run build tools to create tools for the workspace
     */
    register("run-build-tools") {
        dependsOn("download-build-tools")

        onlyIf {
            !buildTools.useLocalDependency && !server.jar.exists()
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
     * Build the server to test the plugin on it
     */
    register("build-server") {
        dependsOn("run-build-tools")

        onlyIf {
            !buildTools.useLocalDependency && !server.exists
        }

        server.mkdir()

        doLast {
            // Print the EULA to the user
            printEULA()

            // Wait for 2 seconds to realise the message
            try {
                Thread.sleep(2 * 1000)
            } catch (e: Exception) {}

            // Since the process didn't stop
            // This means the user indicates to agree on the Minecraft EULA
            // And this code automates the indicates process
            val eula = server.eula
            if (eula.exists()) {
                var text = eula.readText()
                text = text.replace("eula=false", "eula=true", true)
                eula.writeText(text)
            } else {
                eula.writeText("eula=true")
            }


            // Copy the selected compiled server jar to the server folder
            copy {
                from(buildTools.serverJar)
                into(server.dir)
                rename {
                    "server.jar"
                }
            }

            // Sends "stop" command to the server to stop after initialising
            val stopCommand = "stop"
            val input = ByteArrayInputStream(stopCommand.toByteArray(StandardCharsets.UTF_8))

            // Run the server to initialise everything and then it executes the command "stop" to stop itself after getting the environment ready
            javaexec {
                standardInput = input
                workingDir = server.dir
                main = "-jar"
                args = mutableListOf<String>(
                        server.jar.absolutePath
                )
            }

            // Close the input after the termination of the server
            input.close()
        }
    }

    /**
     * Run the server with the plugin on it
     */
    register("run") {
        dependsOn("build-plugin")

        doLast {

            printIntro()
            logger.lifecycle("> Starting the server...")
            logger.lifecycle("")

            // Run the server to test the plugin
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
            "| |                       [#] By using Spigot Starter Kit [#]                       | |",
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
            """   _____       _             __  """,
            """  / ___/____  (_)___ _____  / /_ """,
            """  \__ \/ __ \/ / __ `/ __ \/ __/ """,
            """  ___/ / /_/ / / /_/ / /_/ / /_  """,
            """/____/ .___/_/\__, /\____/\__/   """,
            """    /_/      /____/              """,
            """                                 """,
            """    [#] Spigot Starter Kit [#]   """,
            """                                 """
    )
    for (line in intro) {
        logger.lifecycle(line)
    }
}

class BuildTools (
        val minecraftVersion: String,
        val useSpigot: Boolean,
        val useLocalDependency: Boolean
) {
    val buildDir = File(".build-tools")
    val file = File(buildDir, "build-tools.jar")

    val serverJar = if (useSpigot) {
        File(buildDir, "spigot-${minecraftVersion}.jar")
    } else {
        File(buildDir, "craftbukkit-${minecraftVersion}.jar")
    }
}

/**
 * Help making the server and structuring it
 */
class Server(
        val dir: File = File("server")
) {
    /**
     * Plugins of the sever
     */
    val plugins = File(dir, "plugins")

    /**
     * Server jar that manages the server
     */
    val jar = File(dir, "server.jar")

    /**
     * Minecraft's EULA file
     */
    val eula = File(dir, "eula.txt")

    /**
     * Does the server exist in the right way
     */
    val exists: Boolean
        get() {
            return dir.exists() and plugins.exists() and jar.exists() and eula.exists()
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
        // Delete everything including the directory itself
        dir.deleteRecursively()

        // Create an empty directory for better user experience
        dir.mkdir()
    }
}
