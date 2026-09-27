package me.abdullahrasheed.mineframe.materials;

import java.util.Locale;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import io.papermc.paper.datacomponent.DataComponentTypes;
import me.abdullahrasheed.mineframe.collectibles.Collectible;
import me.abdullahrasheed.mineframe.collectibles.LootBeamEffects;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;

public enum FrameMaterial implements Collectible {

    SELITE("Selite", Color.AQUA),
    THORIN("Thorin", Color.ORANGE),
    GLUCON("Glucon", Color.GREEN);

    private final String name;
    private final String modelName;
    private final Color beamColor;

    FrameMaterial(String name, Color beamColor) {
        this.name = name;
        this.modelName = name().toLowerCase(Locale.ROOT);
        this.beamColor = beamColor;
    }

    @Override
    public String getId() {
        return modelName;
    }

    @Override
    public String getName() {
        return name;
    }

    public String getModelName() {
        return modelName;
    }

    @Override
    public Color getBeamColor() {
        return beamColor;
    }

    @Override
    public ItemStack createItemStack() {
        ItemStack item = new ItemStack(Material.PAPER);
        item.editMeta(meta -> meta.displayName(
            Component.text(getName())
                .decoration(TextDecoration.ITALIC, false)
                .decorate(TextDecoration.BOLD)
                .color(TextColor.color(getBeamColor().asRGB()))
        ));
        item.setData(
            DataComponentTypes.ITEM_MODEL,
            NamespacedKey.fromString("mineframe:" + modelName)
        );
        item.editPersistentDataContainer(pdc -> pdc.set(
            NamespacedKey.fromString("mineframe:material_type"),
            PersistentDataType.STRING,
            modelName
        ));
        return item;
    }

    public void dropAt(Location location) {
        dropAt(location, beamColor, 1);
    }

    public void dropAt(Location location, Color color) {
        dropAt(location, color, 1);
    }

    public void dropAt(Location location, Color color, int amount) {
        if (amount < 1) {
            throw new IllegalArgumentException("amount must be at least 1");
        }

        ItemStack item = createItemStack();
        item.editPersistentDataContainer(pdc -> pdc.set(
            NamespacedKey.fromString("mineframe:material_amount"),
            PersistentDataType.INTEGER,
            amount
        ));
        LootBeamEffects.drop(location, item, color);
    }

    public static FrameMaterial getType(ItemStack itemStack) {
        String type = itemStack.getPersistentDataContainer().get(
            NamespacedKey.fromString("mineframe:material_type"),
            PersistentDataType.STRING
        );
        return type == null ? null : fromId(type);
    }

    public static FrameMaterial fromId(String id) {
        for (FrameMaterial material : values()) {
            if (material.modelName.equalsIgnoreCase(id)) {
                return material;
            }
        }
        return null;
    }

    public static int getDropAmount(ItemStack itemStack) {
        Integer amountPerItem = itemStack.getPersistentDataContainer().get(
            NamespacedKey.fromString("mineframe:material_amount"),
            PersistentDataType.INTEGER
        );
        if (amountPerItem == null) {
            return itemStack.getAmount();
        }

        long total = (long) amountPerItem * itemStack.getAmount();
        return (int) Math.min(total, Integer.MAX_VALUE);
    }

    /** @deprecated Use {@link LootBeamEffects#cleanup()} for shared collectible effects. */
    @Deprecated
    public static void cleanupEffects() {
        LootBeamEffects.cleanup();
    }
}
