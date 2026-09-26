# Mineframe

A minimal PaperMC plugin project targeting Paper 26.2 and Java 25.

## Requirements

- JDK 25

## Build

```sh
./gradlew build
```

The plugin JAR will be written to `build/libs/Mineframe-0.1.0-SNAPSHOT.jar`.

## Test on a server

1. Download a Paper 26.2 server.
2. Build the plugin.
3. Copy the JAR from `build/libs/` into the server's `plugins/` directory.
4. Start the server and accept its EULA when prompted.
5. Run `/mineframe` in game or from the server console.

## Project layout

- `src/main/java` — plugin source code
- `src/main/resources/plugin.yml` — Paper plugin metadata and commands
- `build.gradle.kts` — dependencies and build configuration

