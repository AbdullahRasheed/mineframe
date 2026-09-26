package me.abdullahrasheed.mineframe.materials.events;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import me.abdullahrasheed.mineframe.materials.FrameMaterial;

public class OreMaterialEvents implements Listener {

    @EventHandler
    public void onBreak(BlockBreakEvent event){
        if(event.getBlock().getType() == Material.COAL_ORE){
            if(Math.random() <= 0.30) {
                FrameMaterial.SELITE.dropAt(event.getBlock().getLocation());
            }
        }
    }
}