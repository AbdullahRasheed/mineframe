package me.abdullahrasheed.mineframe.players;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import me.abdullahrasheed.mineframe.blueprints.Blueprint;
import me.abdullahrasheed.mineframe.items.FrameItem;
import me.abdullahrasheed.mineframe.materials.FrameMaterial;

public class FramePlayer {

    public static void addFrameMaterial(Player player, FrameMaterial material){
        addFrameMaterial(player, material, 1);
    }

    public static void addFrameMaterial(Player player, FrameMaterial material, int amount){
        if (amount <= 0) return;

        PersistentDataContainer pdc = player.getPersistentDataContainer();
        NamespacedKey key = NamespacedKey.fromString("mineframe:" + material.getModelName());

        int current = pdc.getOrDefault(key, PersistentDataType.INTEGER, 0);
        pdc.set(key, PersistentDataType.INTEGER, safeAdd(current, amount));
    }

    public static int getFrameMaterial(Player player, FrameMaterial material){
        PersistentDataContainer pdc = player.getPersistentDataContainer();
        NamespacedKey key = NamespacedKey.fromString("mineframe:" + material.getModelName());

        return pdc.getOrDefault(key, PersistentDataType.INTEGER, 0);
    }

    public static boolean removeFrameMaterial(Player player, FrameMaterial material, int amount) {
        if (amount < 1) {
            return false;
        }
        int current = getFrameMaterial(player, material);
        if (current < amount) {
            return false;
        }

        NamespacedKey key = NamespacedKey.fromString("mineframe:" + material.getModelName());
        player.getPersistentDataContainer().set(
            key,
            PersistentDataType.INTEGER,
            current - amount
        );
        return true;
    }

    public static void addBlueprint(Player player, Blueprint blueprint) {
        addBlueprint(player, blueprint, 1);
    }

    public static void addBlueprint(Player player, Blueprint blueprint, int amount) {
        if (amount <= 0) {
            return;
        }

        PersistentDataContainer pdc = player.getPersistentDataContainer();
        NamespacedKey key = blueprintKey(blueprint.getId());
        int current = pdc.getOrDefault(key, PersistentDataType.INTEGER, 0);
        pdc.set(key, PersistentDataType.INTEGER, safeAdd(current, amount));
    }

    public static int getBlueprint(Player player, Blueprint blueprint) {
        return player.getPersistentDataContainer().getOrDefault(
            blueprintKey(blueprint.getId()),
            PersistentDataType.INTEGER,
            0
        );
    }

    public static boolean removeBlueprint(Player player, Blueprint blueprint, int amount) {
        if (amount < 1) {
            return false;
        }
        int current = getBlueprint(player, blueprint);
        if (current < amount) {
            return false;
        }

        player.getPersistentDataContainer().set(
            blueprintKey(blueprint.getId()),
            PersistentDataType.INTEGER,
            current - amount
        );
        return true;
    }

    public static void addFrameItem(Player player, FrameItem item) {
        addFrameItem(player, item, 1);
    }

    public static void addFrameItem(Player player, FrameItem item, int amount) {
        if (amount <= 0) {
            return;
        }

        PersistentDataContainer pdc = player.getPersistentDataContainer();
        NamespacedKey key = frameItemKey(item.getId());
        int current = pdc.getOrDefault(key, PersistentDataType.INTEGER, 0);
        pdc.set(key, PersistentDataType.INTEGER, safeAdd(current, amount));
    }

    public static int getFrameItem(Player player, FrameItem item) {
        return player.getPersistentDataContainer().getOrDefault(
            frameItemKey(item.getId()),
            PersistentDataType.INTEGER,
            0
        );
    }

    public static boolean removeFrameItem(Player player, FrameItem item, int amount) {
        if (amount < 1) {
            return false;
        }
        int current = getFrameItem(player, item);
        if (current < amount) {
            return false;
        }

        player.getPersistentDataContainer().set(
            frameItemKey(item.getId()),
            PersistentDataType.INTEGER,
            current - amount
        );
        return true;
    }

    private static NamespacedKey blueprintKey(String blueprintId) {
        return NamespacedKey.fromString("mineframe:blueprint/" + blueprintId);
    }

    private static NamespacedKey frameItemKey(String itemId) {
        return NamespacedKey.fromString("mineframe:frame_item/" + itemId);
    }

    private static int safeAdd(int current, int amount) {
        return (int) Math.min((long) current + amount, Integer.MAX_VALUE);
    }
}
