package me.abdullahrasheed.mineframe.materials.events;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import me.abdullahrasheed.mineframe.materials.drops.MaterialDropRegistry;
import me.abdullahrasheed.mineframe.materials.drops.MaterialDropRule;

public final class BlockBreakMaterialEvents implements Listener {

    private final MaterialDropRegistry dropRegistry;

    public BlockBreakMaterialEvents(MaterialDropRegistry dropRegistry) {
        this.dropRegistry = dropRegistry;
    }

    @EventHandler(ignoreCancelled = true)
    public void onBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        BlockData blockData = block.getBlockData();
        Location dropLocation = block.getLocation().add(0.5, 0.5, 0.5);

        for (MaterialDropRule rule : dropRegistry.rulesFor(block.getType())) {
            if (rule.shouldDrop(blockData)) {
                int amount = rule.range().randomAmount();
                rule.frameMaterial().dropAt(
                    dropLocation,
                    rule.frameMaterial().getBeamColor(),
                    amount
                );
            }
        }
    }
}
