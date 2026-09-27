package me.abdullahrasheed.mineframe.blueprints.drops;

import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.Material;
import org.bukkit.block.data.BlockData;

import me.abdullahrasheed.mineframe.blueprints.Blueprint;
import me.abdullahrasheed.mineframe.collectibles.drops.BlockConditions;

public record BlueprintDropRule(
        Blueprint blueprint,
        Material sourceBlock,
        double chance,
        BlockConditions conditions
) {

    public BlueprintDropRule {
        if (chance < 0.0 || chance > 1.0) {
            throw new IllegalArgumentException("chance must be between 0 and 1");
        }
    }

    public boolean shouldDrop(BlockData blockData) {
        return conditions.matches(blockData)
            && ThreadLocalRandom.current().nextDouble() < chance;
    }
}
