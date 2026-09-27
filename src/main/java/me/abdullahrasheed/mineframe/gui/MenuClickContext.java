package me.abdullahrasheed.mineframe.gui;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public record MenuClickContext(
        Menu menu,
        Player player,
        int slot,
        ItemStack clickedItem,
        InventoryClickEvent event
) {
}

