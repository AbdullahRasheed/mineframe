package me.abdullahrasheed.mineframe;

import java.util.Objects;

import org.bukkit.event.Listener;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import me.abdullahrasheed.mineframe.blueprints.BlueprintsCommand;
import me.abdullahrasheed.mineframe.blueprints.drops.BlueprintDropRegistry;
import me.abdullahrasheed.mineframe.collectibles.LootBeamEffects;
import me.abdullahrasheed.mineframe.collectibles.PickupBars;
import me.abdullahrasheed.mineframe.collectibles.events.BlockBreakCollectibleDrops;
import me.abdullahrasheed.mineframe.collectibles.events.CollectiblePickUp;
import me.abdullahrasheed.mineframe.gui.MenuListener;
import me.abdullahrasheed.mineframe.items.FrameItemRegistry;
import me.abdullahrasheed.mineframe.materials.MaterialsCommand;
import me.abdullahrasheed.mineframe.materials.drops.MaterialDropRegistry;

public final class MineframePlugin extends JavaPlugin {

    private MaterialDropRegistry materialDropRegistry;
    private FrameItemRegistry frameItemRegistry;
    private BlueprintDropRegistry blueprintDropRegistry;

    @Override
    public void onEnable() {
        getLogger().info("Mineframe has been enabled.");

        materialDropRegistry = new MaterialDropRegistry(this);
        materialDropRegistry.reload();
        frameItemRegistry = new FrameItemRegistry(this);
        frameItemRegistry.reload();
        blueprintDropRegistry = new BlueprintDropRegistry(this, frameItemRegistry);
        blueprintDropRegistry.reload();

        registerEvent(new BlockBreakCollectibleDrops(materialDropRegistry, blueprintDropRegistry));
        registerEvent(new CollectiblePickUp(frameItemRegistry));
        registerEvent(new MenuListener());

        PluginCommand materialsCommand = Objects.requireNonNull(
            getCommand("materials"),
            "materials command is missing from plugin.yml"
        );
        MaterialsCommand materialsExecutor = new MaterialsCommand(materialDropRegistry);
        materialsCommand.setExecutor(materialsExecutor);
        materialsCommand.setTabCompleter(materialsExecutor);

        PluginCommand blueprintsCommand = Objects.requireNonNull(
            getCommand("blueprints"),
            "blueprints command is missing from plugin.yml"
        );
        BlueprintsCommand blueprintsExecutor = new BlueprintsCommand(
            frameItemRegistry,
            blueprintDropRegistry
        );
        blueprintsCommand.setExecutor(blueprintsExecutor);
        blueprintsCommand.setTabCompleter(blueprintsExecutor);
    }

    @Override
    public void onDisable() {
        PickupBars.clearAll();
        LootBeamEffects.cleanup();
        getLogger().info("Mineframe has been disabled.");
    }

    private void registerEvent(Listener listener){
        getServer().getPluginManager().registerEvents(listener, this);
    }

    public MaterialDropRegistry getMaterialDropRegistry() {
        return materialDropRegistry;
    }

    public FrameItemRegistry getFrameItemRegistry() {
        return frameItemRegistry;
    }

    public BlueprintDropRegistry getBlueprintDropRegistry() {
        return blueprintDropRegistry;
    }
}
