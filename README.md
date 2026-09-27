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

## Collectible pickup bars

Picking up a FrameMaterial or blueprint shows a centered `+X Name` boss-bar
title for three seconds. Repeated pickups of the same collectible are
aggregated, while different types use separate bars and stack vertically. The
matching resource pack makes the reserved white, notched-20 bar background
transparent.

The transparent sprites are generated deterministically from
`texturepack-assets/GenerateTexturePackAssets.java` during `./gradlew build`.

## Material drop configuration

On first startup, `plugins/Mineframe/material-drops.yml` is created from the
bundled defaults. On later startups and `/materials reload`, any settings that
are missing from the server copy are added from the bundled file without
overwriting customized values. Rules are then validated and cached by source
block:

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

## FrameItems and blueprints

`items.yml` is the source of truth for FrameItems. A FrameItem may later be a
weapon, tool, armor piece, utility, or another category, and every FrameItem
automatically owns exactly one blueprint definition. The bundled file starts
with a commented template so the plugin does not invent gameplay content:

```yaml
starter_sword:
  display-name: "Starter Sword"
  ingredients:
    frame-materials:
      selite: 200
    minecraft-items:
      rotten_flesh: 20
```

Both representations use a dummy paper stack and the item model
`mineframe:<item-id>`. In this example the item and blueprint both use
`mineframe:starter_sword`; the blueprint additionally carries the custom model
data string `greyscale`. Resource-pack item-model rules can use that string to
select the greyscale model. Blueprint beam color is shared and is not configured
per item. Finished FrameItems are never dropped.

Blueprint copies are counted in the player's PersistentDataContainer instead of
entering the physical inventory. Run `/blueprints` to view only blueprints the
player owns; the menu automatically paginates beyond 45 types.

Block drops live separately in `plugins/Mineframe/blueprint-drops.yml`:

```yaml
starter_sword:
  diamond_ore:
    chance: 0.05
  potatoes:
    chance: 0.01
    conditions:
      age: max
```

Blueprint drop rules intentionally have no `range`: each successful roll drops
one blueprint entity and pickup adds one owned copy. Conditions work exactly as
they do for material drops. Mob drops are intentionally left for a separate
future source system.

Run `/blueprints reload` after editing `items.yml` or `blueprint-drops.yml`. It requires
`mineframe.command.blueprints.reload` (operator by default). Like the material
drop config, missing values from future bundled versions are merged without
overwriting server customizations.

For future Foundry code, `FrameItem#getIngredients()` exposes immutable typed
requirements (`FrameMaterialIngredient` or `VanillaItemIngredient`).
`FramePlayer` provides add/get/remove operations for stored materials,
blueprints, and claimed FrameItems, each in a separate PDC namespace.

The former `blueprints.yml` is no longer read. When upgrading a server that had
active definitions there, copy those definitions into `items.yml`, rename
`name` to `display-name`, and remove the old `icon` and `beam-color` fields.
Existing player blueprint counts remain compatible because ownership continues
to use the same item/blueprint ID.

## Test on a server

1. Download a Paper 26.2 server.
2. Build the plugin.
3. Copy the JAR from `build/libs/` into the server's `plugins/` directory.
4. Start the server and accept its EULA when prompted.
5. Run `/materials` or `/blueprints` in game.

## Project layout

- `src/main/java` — plugin source code
- `src/main/resources/plugin.yml` — Paper plugin metadata and commands
- `build.gradle.kts` — dependencies and build configuration
