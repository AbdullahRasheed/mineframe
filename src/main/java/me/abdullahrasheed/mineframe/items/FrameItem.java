package me.abdullahrasheed.mineframe.items;

import java.util.List;
import java.util.Objects;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import io.papermc.paper.datacomponent.DataComponentTypes;
import me.abdullahrasheed.mineframe.blueprints.Blueprint;
import me.abdullahrasheed.mineframe.items.ingredients.FrameItemIngredient;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

/** A persistent Mineframe-owned weapon, tool, armor piece, utility, or future item type. */
public final class FrameItem {

    public static final NamespacedKey ITEM_TYPE_KEY =
        NamespacedKey.fromString("mineframe:frame_item_type");
    private static final Material DUMMY_ICON = Material.PAPER;

    private final String id;
    private final String displayName;
    private final List<FrameItemIngredient> ingredients;
    private final Blueprint blueprint;

    public FrameItem(String id, String displayName, List<FrameItemIngredient> ingredients) {
        this.id = Objects.requireNonNull(id, "id");
        this.displayName = Objects.requireNonNull(displayName, "displayName");
        this.ingredients = List.copyOf(ingredients);
        this.blueprint = new Blueprint(this);
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public List<FrameItemIngredient> getIngredients() {
        return ingredients;
    }

    public Blueprint getBlueprint() {
        return blueprint;
    }

    /** Creates a visual representation; claimed items remain stored in player PDC. */
    public ItemStack createItemStack() {
        ItemStack item = new ItemStack(DUMMY_ICON);
        item.setData(
            DataComponentTypes.ITEM_MODEL,
            NamespacedKey.fromString("mineframe:" + id)
        );
        item.editMeta(meta -> meta.displayName(
            Component.text(displayName, NamedTextColor.WHITE)
                .decoration(TextDecoration.ITALIC, false)
                .decorate(TextDecoration.BOLD)
        ));
        item.editPersistentDataContainer(pdc -> pdc.set(
            ITEM_TYPE_KEY,
            PersistentDataType.STRING,
            id
        ));
        return item;
    }
}
