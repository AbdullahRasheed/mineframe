# Mineframe

A minimal PaperMC plugin project targeting Paper 26.2 and Java 25.

## Requirements

- JDK 25

## Build

```sh
./gradlew build
```

The plugin JAR will be written to `build/libs/Mineframe-0.1.0-SNAPSHOT.jar`.
The matching resource pack will be written to
`build/distributions/mineframe-texturepack.zip`.

## Material pickup bars

Picking up a FrameMaterial shows a centered `+X Material` boss-bar title for
three seconds. Repeated pickups of the same material are aggregated, while
different material types use separate bars and stack vertically. The matching
resource pack makes the reserved white, notched-20 bar background transparent.

The transparent sprites are generated deterministically from
`texturepack-assets/GenerateTexturePackAssets.java` during `./gradlew build`.

## Material drop configuration

On first startup, `plugins/Mineframe/material-drops.yml` is created from the
bundled defaults. Rules are validated and cached by source block when the plugin
starts:

```yaml
selite:
  coal_ore:
    chance: 0.65
    range: "3,12"
  iron_ore:
    chance: 0.7
    range: [4, 18]
```

Optional `conditions` match serialized block-data properties. The special age
value `max` matches any fully grown Bukkit `Ageable` block:

```yaml
future_crop_material:
  potatoes:
    chance: 0.25
    range: "1,3"
    conditions:
      age: max
```

Exact values such as `facing: north`, `waterlogged: false`, or `age: 4` are
also supported. After changing the file, run `/materials reload` (permission:
`mineframe.command.materials.reload`), restart the server, or call
`getMaterialDropRegistry().reload()` from plugin code.

## Test on a server

1. Download a Paper 26.2 server.
2. Build the plugin.
3. Copy the JAR from `build/libs/` into the server's `plugins/` directory.
4. Start the server and accept its EULA when prompted.
5. Run `/materials` in game.

## Project layout

- `src/main/java` — plugin source code
- `src/main/resources/plugin.yml` — Paper plugin metadata and commands
- `build.gradle.kts` — dependencies and build configuration
