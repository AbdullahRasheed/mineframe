package me.abdullahrasheed.mineframe.materials;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.abdullahrasheed.mineframe.gui.Menu;
import me.abdullahrasheed.mineframe.players.FramePlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

public final class MaterialsMenu extends Menu {

    private static final NumberFormat AMOUNT_FORMAT = NumberFormat.getIntegerInstance(Locale.US);

    public MaterialsMenu(Player player) {
        super(requiredRows(), title());

        FrameMaterial[] materials = FrameMaterial.values();
        for (int slot = 0; slot < materials.length; slot++) {
            FrameMaterial material = materials[slot];
            setItem(slot, displayItem(player, material));
        }
    }

    private static int requiredRows() {
        return Math.max(1, (FrameMaterial.values().length + 8) / 9);
    }

    private static Component title() {
        return Component.text("✦ ", NamedTextColor.AQUA)
            .append(Component.text("Frame Materials", NamedTextColor.DARK_AQUA))
            .decoration(TextDecoration.BOLD, true);
    }

    private static ItemStack displayItem(Player player, FrameMaterial material) {
        ItemStack item = material.createItemStack();
        int amount = FramePlayer.getFrameMaterial(player, material);
        String formattedAmount = AMOUNT_FORMAT.format(amount);

        item.editMeta(meta -> meta.lore(List.of(
            Component.empty(),
            plain("  YOUR COLLECTION", NamedTextColor.DARK_GRAY)
                .decoration(TextDecoration.BOLD, true),
            plain("  ✦ ", NamedTextColor.AQUA)
                .append(plain(formattedAmount, NamedTextColor.WHITE)
                    .decoration(TextDecoration.BOLD, true)),
            plain("  total materials owned", NamedTextColor.GRAY),
            Component.empty()
        )));
        return item;
    }

    private static Component plain(String text, NamedTextColor color) {
        return Component.text(text, color).decoration(TextDecoration.ITALIC, false);
    }
}
