package me.abdullahrasheed.mineframe.items.ingredients;

import java.util.Objects;

import me.abdullahrasheed.mineframe.materials.FrameMaterial;

public record FrameMaterialIngredient(FrameMaterial material, int amount)
        implements FrameItemIngredient {

    public FrameMaterialIngredient {
        Objects.requireNonNull(material, "material");
        if (amount < 1) {
            throw new IllegalArgumentException("ingredient amount must be at least 1");
        }
    }

    @Override
    public String displayName() {
        return material.getName();
    }
}
