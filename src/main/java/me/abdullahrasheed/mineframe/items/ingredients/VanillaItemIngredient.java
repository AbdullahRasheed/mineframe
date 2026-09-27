package me.abdullahrasheed.mineframe.items.ingredients;

import java.util.Locale;
import java.util.Objects;

import org.bukkit.Material;

public record VanillaItemIngredient(Material material, int amount)
        implements FrameItemIngredient {

    public VanillaItemIngredient {
        Objects.requireNonNull(material, "material");
        if (!material.isItem()) {
            throw new IllegalArgumentException(material + " is not an item");
        }
        if (amount < 1) {
            throw new IllegalArgumentException("ingredient amount must be at least 1");
        }
    }

    @Override
    public String displayName() {
        String[] words = material.name().toLowerCase(Locale.ROOT).split("_");
        StringBuilder displayName = new StringBuilder();
        for (String word : words) {
            if (!displayName.isEmpty()) {
                displayName.append(' ');
            }
            displayName.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return displayName.toString();
    }
}
