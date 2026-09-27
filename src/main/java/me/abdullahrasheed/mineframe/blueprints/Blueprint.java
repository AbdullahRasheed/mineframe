package me.abdullahrasheed.mineframe.blueprints;

import java.util.List;
import java.util.Objects;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.CustomModelData;
import me.abdullahrasheed.mineframe.collectibles.Collectible;
import me.abdullahrasheed.mineframe.collectibles.LootBeamEffects;
import me.abdullahrasheed.mineframe.items.FrameItem;
import me.abdullahrasheed.mineframe.items.ingredients.FrameItemIngredient;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

/** The unique blueprint corresponding to one FrameItem. */
public final class Blueprint implements Collectible {

    public static final NamespacedKey ITEM_TYPE_KEY =
        NamespacedKey.fromString("mineframe:blueprint_type");
    public static final Color BEAM_COLOR = Color.fromRGB(0x55AAFF);

    private static final Material DUMMY_ICON = Material.PAPER;
    private static final String GREYSCALE_MODEL_VARIANT = "greyscale";

    private final FrameItem frameItem;

    public Blueprint(FrameItem frameItem) {
        this.frameItem = Objects.requireNonNull(frameItem, "frameItem");
    }

    public FrameItem getFrameItem() {
        return frameItem;
    }

    @Override
    public String getId() {
        return frameItem.getId();
    }

    @Override
    public String getName() {
        return frameItem.getDisplayName() + " Blueprint";
    }

    @Override
    public Color getBeamColor() {
        return BEAM_COLOR;
    }

    public List<FrameItemIngredient> getIngredients() {
        return frameItem.getIngredients();
    }

    @Override
    public ItemStack createItemStack() {
        ItemStack item = new ItemStack(DUMMY_ICON);
        item.setData(
            DataComponentTypes.ITEM_MODEL,
            NamespacedKey.fromString("mineframe:" + getId())
        );
        item.setData(
            DataComponentTypes.CUSTOM_MODEL_DATA,
            CustomModelData.customModelData().addString(GREYSCALE_MODEL_VARIANT).build()
        );
        item.editMeta(meta -> meta.displayName(
            Component.text(getName(), NamedTextColor.AQUA)
                .decoration(TextDecoration.ITALIC, false)
                .decorate(TextDecoration.BOLD)
        ));
        item.editPersistentDataContainer(pdc -> pdc.set(
            ITEM_TYPE_KEY,
            PersistentDataType.STRING,
            getId()
        ));
        return item;
    }

    public void dropAt(Location location) {
        LootBeamEffects.drop(location, createItemStack(), BEAM_COLOR);
    }
}
