package me.abdullahrasheed.mineframe;

import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import me.abdullahrasheed.mineframe.materials.events.OreMaterialEvents;

public final class MineframePlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        getLogger().info("Mineframe has been enabled.");

        registerEvent(new OreMaterialEvents());
    }

    @Override
    public void onDisable() {
        getLogger().info("Mineframe has been disabled.");
    }

    private void registerEvent(Listener listener){
        getServer().getPluginManager().registerEvents(listener, this);
    }
}

