package me.abdullahrasheed.mineframe.collectibles.events;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import me.abdullahrasheed.mineframe.blueprints.drops.BlueprintDropRegistry;
import me.abdullahrasheed.mineframe.blueprints.drops.BlueprintDropRule;
import me.abdullahrasheed.mineframe.materials.drops.MaterialDropRegistry;
import me.abdullahrasheed.mineframe.materials.drops.MaterialDropRule;

/** Evaluates all block-based collectible tables against one captured block state. */
public final class BlockBreakCollectibleDrops implements Listener {

    private final MaterialDropRegistry materialDrops;
    private final BlueprintDropRegistry blueprintDrops;

    public BlockBreakCollectibleDrops(
            MaterialDropRegistry materialDrops,
            BlueprintDropRegistry blueprintDrops
    ) {
        this.materialDrops = materialDrops;
        this.blueprintDrops = blueprintDrops;
    }

    @EventHandler(ignoreCancelled = true)
    public void onBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        BlockData blockData = block.getBlockData();
        Location dropLocation = block.getLocation().add(0.5, 0.5, 0.5);

        for (MaterialDropRule rule : materialDrops.rulesFor(block.getType())) {
            if (rule.shouldDrop(blockData)) {
                int amount = rule.range().randomAmount();
                rule.frameMaterial().dropAt(
                    dropLocation,
                    rule.frameMaterial().getBeamColor(),
                    amount
                );
            }
        }

        for (BlueprintDropRule rule : blueprintDrops.rulesFor(block.getType())) {
            if (rule.shouldDrop(blockData)) {
                rule.blueprint().dropAt(dropLocation);
            }
        }
    }
}
