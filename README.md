# Spigot Starter Kit
A kit for spigot starter to start developing plugins.

### Table of Contents
- [Requirements](#requirements)
- [Goal](#goal)
- [Getting Started](#getting-started)
- [Rename the plugin](#rename-the-plugin)
- [Build the plugin](#build-the-plugin)
- [Test the plugin](#test-the-plugin)
- [Use Craftbukkit](#use-craftbukkit)
- [Change Minecraft Version](#change-server-version)

# Requirements
**Reference:** [[SpigotMC] Build Tools - Prerequisties](https://www.spigotmc.org/wiki/buildtools/#prerequisites)

### Windows
- [Git](https://gitforwindows.org/)
- [Java](https://www.oracle.com/sa/java/technologies/javase-downloads.html)
> We prefer you to download [Java 8](https://www.oracle.com/java/technologies/javase-jre8-downloads.html)

### Mac
- [Git](http://sourceforge.net/projects/git-osx-installer/files/)
- [Java](https://gist.github.com/johan/10590467)
> Java may need to be updated from the Apple distributed version, and even if previously updated,
> may need to be linked for shell use.

### Linux
**Both git and Java, as well as util commands, can be installed using a single command via your package manager.**

Distribution Name | Command
----------------- | -------
Debian / Ubuntu | `sudo apt-get install git openjdk-8-jre-headless`
CentOS / RHEL | `sudo yum install git java-1.8.0-openjdk-devel`
Arch | `sudo pacman -S jdk8-openjdk git`

## Goal
The goal of this starter kit is to help make spigot plugin easy in development and a simple point to start.

## Getting Started
1. Clone the repository
2. Setup the workspace by `./gradlew setup`
3. Execute `./gradlew run` to run the server to test your plugin
---
**You can create an issue to report a bug or suggest improvements to the kit.**

## Rename the plugin
1. Change the project name the `settings.gradle.kts`
    ```kotlin
    rootProject.name = "your-plugin-name"
    ```
2. Change the group in `build.gradle.kts`
    ```kotlin
    group = "yourdomain.yourname.pluginName"
    // Example
    group = "me.ihdeveloper.example"
    ```
   > Your group name should follow [Oracle(Java Docs): Naming a package](https://docs.oracle.com/javase/tutorial/java/package/namingpkgs.html).
3. Rename the main package to your group name
    1. Go to `src/main/java`
    2. You will find `com.example.plugin`
    3. Rename the package using your favourite IDE to the same group name you change above
    > Your package name should follow [Oracle(Java Docs): Naming a package](https://docs.oracle.com/javase/tutorial/java/package/namingpkgs.html).
4. Change the main class path in plugin configuration
    1. Go to `src/main/resources`
    2. Edit `plugin.yml`
    3. Change it from
        ```yaml
        name: example-plugin
        main: com.example.plugin.Main
        ```
        to
        ```yaml
        name: your-plugin-name
        author: your-name
        main: yourdomain.yourname.pluginName.Main
        ```
5. You are ready to go!

## Build the plugin
```shell script
./gradlew build
```
Builds the plugin and put it in `build/{name}.jar`.

## Test the plugin
```shell script
./gradlew run
```
Run a server to test the plugin on it.

## Use Craftbukkit
If you want to use `Craftbukkit` without `Spigot`
- Edit `build.gradle.kts`
    ```kotlin
    val buildTools = BuildTools(
            /* ... */
    
            // Change it to false to use Craftbukkit instead
            useSpigot = false
    )
    ```
- Delete `.build-tools` directory by executing `rm -rf .build-tools/`
    > If `.build-tools/` exists in your kit workspace
- Run `./gradlew setup`

## Change Server Version
If you want to change the Minecraft server version.
- Edit `build.gradle.kts`
    ```kotlin
    val buildTools = BuildTools(
    
            // Minecraft Server Version (Ex: 1.8.8)
            minecraftVersion = "1.8.8",
    
            /* ... */
    )
    ```
- Delete `.build-tools` directory by executing `rm -rf .build-tools/`
    > If `.build-tools/` exists in your kit workspace
- Run `./gradlew setup`
