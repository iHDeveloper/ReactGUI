# Spigot Starter Kit
A kit for spigot starter to start developing plugins.

### Table of Contents
- [Goal](#goal)
- [Getting Started](#getting-started)
- [Rename the plugin](#rename-the-plugin)
- [Build the plugin](#build-the-plugin)
- [Test the plugin](#test-the-plugin)
- [Use Craftbukkit](#use-craftbukkit)

## Goal
The goal of this starter kit is to help make spigot plugin easy in development and a simple point to start.

## Getting Started
1. Clone the repository
2. Setup the workspace by `./gradlew setup`
3. Execute `./gradlew run` to run the server to test your plugin

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
