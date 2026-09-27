package me.abdullahrasheed.mineframe.items.ingredients;

/** One immutable crafting requirement for a FrameItem's blueprint. */
public sealed interface FrameItemIngredient
        permits FrameMaterialIngredient, VanillaItemIngredient {

    int amount();

    String displayName();
}
