package me.abdullahrasheed.mineframe.materials.events;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;

import me.abdullahrasheed.mineframe.materials.FrameMaterial;
import me.abdullahrasheed.mineframe.players.FramePlayer;

public class FrameMaterialPickUp implements Listener {

    @EventHandler
    public void onPickUp(EntityPickupItemEvent event){
        if(event.getEntity() instanceof Player player){
            FrameMaterial material = FrameMaterial.getType(event.getItem().getItemStack());
            if (material == null) return;

            event.setCancelled(true);
            event.getItem().remove();
            FramePlayer.addFrameMaterial(player, material);
        }
    }
}