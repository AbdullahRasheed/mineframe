package me.abdullahrasheed.mineframe;

import java.util.Objects;

import org.bukkit.event.Listener;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import me.abdullahrasheed.mineframe.gui.MenuListener;
import me.abdullahrasheed.mineframe.materials.FrameMaterial;
import me.abdullahrasheed.mineframe.materials.FrameMaterialPickupBars;
import me.abdullahrasheed.mineframe.materials.MaterialsCommand;
import me.abdullahrasheed.mineframe.materials.drops.MaterialDropRegistry;
import me.abdullahrasheed.mineframe.materials.events.BlockBreakMaterialEvents;
import me.abdullahrasheed.mineframe.materials.events.FrameMaterialPickUp;

public final class MineframePlugin extends JavaPlugin {

    private MaterialDropRegistry materialDropRegistry;

    @Override
    public void onEnable() {
        getLogger().info("Mineframe has been enabled.");

        materialDropRegistry = new MaterialDropRegistry(this);
        materialDropRegistry.reload();

        registerEvent(new BlockBreakMaterialEvents(materialDropRegistry));
        registerEvent(new FrameMaterialPickUp());
        registerEvent(new MenuListener());

        PluginCommand materialsCommand = Objects.requireNonNull(
            getCommand("materials"),
            "materials command is missing from plugin.yml"
        );
        MaterialsCommand materialsExecutor = new MaterialsCommand(materialDropRegistry);
        materialsCommand.setExecutor(materialsExecutor);
        materialsCommand.setTabCompleter(materialsExecutor);
    }

    @Override
    public void onDisable() {
        FrameMaterialPickupBars.clearAll();
        FrameMaterial.cleanupEffects();
        getLogger().info("Mineframe has been disabled.");
    }

    private void registerEvent(Listener listener){
        getServer().getPluginManager().registerEvents(listener, this);
    }

    public MaterialDropRegistry getMaterialDropRegistry() {
        return materialDropRegistry;
    }
}
