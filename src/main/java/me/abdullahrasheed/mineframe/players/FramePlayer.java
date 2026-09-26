package me.abdullahrasheed.mineframe.players;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import me.abdullahrasheed.mineframe.materials.FrameMaterial;

public class FramePlayer {

    public static void addFrameMaterial(Player player, FrameMaterial material){
        PersistentDataContainer pdc = player.getPersistentDataContainer();
        NamespacedKey key = NamespacedKey.fromString("mineframe:" + material.getModelName());

        int current = pdc.getOrDefault(key, PersistentDataType.INTEGER, 0);
        pdc.set(key, PersistentDataType.INTEGER, current + 1);
    }
}