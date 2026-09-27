package me.abdullahrasheed.mineframe.players;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

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
        pdc.set(key, PersistentDataType.INTEGER, current + amount);
    }

    public static int getFrameMaterial(Player player, FrameMaterial material){
        PersistentDataContainer pdc = player.getPersistentDataContainer();
        NamespacedKey key = NamespacedKey.fromString("mineframe:" + material.getModelName());

        return pdc.getOrDefault(key, PersistentDataType.INTEGER, 0);
    }
}
