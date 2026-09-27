package me.abdullahrasheed.mineframe.materials.drops;

import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.Material;
import org.bukkit.block.data.BlockData;

import me.abdullahrasheed.mineframe.materials.FrameMaterial;

public record MaterialDropRule(
        FrameMaterial frameMaterial,
        Material sourceBlock,
        double chance,
        DropRange range,
        BlockConditions conditions
) {

    public MaterialDropRule {
        if (chance < 0.0 || chance > 1.0) {
            throw new IllegalArgumentException("chance must be between 0 and 1");
        }
    }

    public boolean shouldDrop(BlockData blockData) {
        return conditions.matches(blockData)
            && ThreadLocalRandom.current().nextDouble() < chance;
    }
}

