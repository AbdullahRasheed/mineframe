package me.abdullahrasheed.mineframe.collectibles;

import org.bukkit.Color;
import org.bukkit.inventory.ItemStack;

/** Common presentation contract for persistent, non-inventory collectibles. */
public interface Collectible {

    String getId();

    String getName();

    Color getBeamColor();

    ItemStack createItemStack();
}
