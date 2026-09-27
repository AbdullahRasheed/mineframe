package me.abdullahrasheed.mineframe.materials;

import org.bukkit.entity.Player;

import me.abdullahrasheed.mineframe.collectibles.PickupBars;

/** Displays one temporary pickup boss bar for each material type. */
public final class FrameMaterialPickupBars {

    private FrameMaterialPickupBars() {
    }

    public static void show(Player player, FrameMaterial material, int amount) {
        PickupBars.show(player, "material", material, amount);
    }

    public static void clear(Player player) {
        PickupBars.clear(player);
    }

    public static void clearAll() {
        PickupBars.clearAll();
    }
}
