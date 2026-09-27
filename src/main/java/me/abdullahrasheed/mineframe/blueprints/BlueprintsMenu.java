package me.abdullahrasheed.mineframe.blueprints;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.abdullahrasheed.mineframe.gui.Menu;
import me.abdullahrasheed.mineframe.items.FrameItemRegistry;
import me.abdullahrasheed.mineframe.items.ingredients.FrameItemIngredient;
import me.abdullahrasheed.mineframe.items.ingredients.FrameMaterialIngredient;
import me.abdullahrasheed.mineframe.players.FramePlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;

/** Paginated, read-only view of the blueprint copies a player owns. */
public final class BlueprintsMenu extends Menu {

    private static final int PAGE_SIZE = 45;
    private static final NumberFormat AMOUNT_FORMAT = NumberFormat.getIntegerInstance(Locale.US);

    public BlueprintsMenu(Player player, FrameItemRegistry registry) {
        this(player, registry, 0);
    }

    private BlueprintsMenu(Player player, FrameItemRegistry registry, int requestedPage) {
        this(player, registry, ownedBlueprints(player, registry), requestedPage);
    }

    private BlueprintsMenu(
            Player player,
            FrameItemRegistry registry,
            List<Blueprint> owned,
            int requestedPage
    ) {
        super(requiredRows(owned.size()), title(owned.size(), requestedPage));

        int pageCount = Math.max(1, (owned.size() + PAGE_SIZE - 1) / PAGE_SIZE);
        int page = Math.max(0, Math.min(requestedPage, pageCount - 1));

        if (owned.isEmpty()) {
            setItem(4, emptyItem());
            return;
        }

        boolean paginated = owned.size() > PAGE_SIZE;
        int pageSize = paginated ? PAGE_SIZE : getInventory().getSize();
        int start = page * PAGE_SIZE;
        int end = Math.min(start + pageSize, owned.size());
        for (int index = start; index < end; index++) {
            Blueprint blueprint = owned.get(index);
            setItem(index - start, displayItem(player, blueprint));
        }

        if (paginated && page > 0) {
            setItem(45, navigationItem(Material.ARROW, "Previous Page"),
                context -> new BlueprintsMenu(player, registry, page - 1).open(player));
        }
        if (paginated && page + 1 < pageCount) {
            setItem(53, navigationItem(Material.ARROW, "Next Page"),
                context -> new BlueprintsMenu(player, registry, page + 1).open(player));
        }
    }

    private static List<Blueprint> ownedBlueprints(Player player, FrameItemRegistry registry) {
        return registry.all().stream()
            .map(frameItem -> frameItem.getBlueprint())
            .filter(blueprint -> FramePlayer.getBlueprint(player, blueprint) > 0)
            .sorted(Comparator.comparing(Blueprint::getName, String.CASE_INSENSITIVE_ORDER))
            .toList();
    }

    private static int requiredRows(int ownedCount) {
        if (ownedCount > PAGE_SIZE) {
            return 6;
        }
        return Math.max(1, (ownedCount + 8) / 9);
    }

    private static Component title(int ownedCount, int requestedPage) {
        Component title = Component.text("✦ ", NamedTextColor.AQUA)
            .append(Component.text("Blueprints", NamedTextColor.BLUE))
            .decoration(TextDecoration.BOLD, true);
        if (ownedCount > PAGE_SIZE) {
            int pageCount = (ownedCount + PAGE_SIZE - 1) / PAGE_SIZE;
            int page = Math.max(0, Math.min(requestedPage, pageCount - 1));
            title = title.append(Component.text(
                "  " + (page + 1) + "/" + pageCount,
                NamedTextColor.DARK_GRAY
            ));
        }
        return title;
    }

    private static ItemStack displayItem(Player player, Blueprint blueprint) {
        ItemStack item = blueprint.createItemStack();
        List<Component> lore = new ArrayList<>();
        lore.add(Component.empty());
        lore.add(plain("  OWNED", NamedTextColor.DARK_GRAY).decorate(TextDecoration.BOLD));
        lore.add(plain(
            "  ✦ " + AMOUNT_FORMAT.format(FramePlayer.getBlueprint(player, blueprint)) + " copies",
            NamedTextColor.AQUA
        ));
        lore.add(Component.empty());
        lore.add(plain("  FOUNDRY REQUIREMENTS", NamedTextColor.DARK_GRAY)
            .decorate(TextDecoration.BOLD));

        if (blueprint.getIngredients().isEmpty()) {
            lore.add(plain("  No ingredients configured", NamedTextColor.GRAY));
        } else {
            for (FrameItemIngredient ingredient : blueprint.getIngredients()) {
                lore.add(plain(
                    "  • " + AMOUNT_FORMAT.format(ingredient.amount())
                        + " " + ingredient.displayName(),
                    ingredient instanceof FrameMaterialIngredient material
                        ? NamedTextColor.nearestTo(TextColor.color(material.material().getBeamColor().asRGB()))
                        : NamedTextColor.WHITE
                ));
            }
        }
        lore.add(Component.empty());
        item.editMeta(meta -> meta.lore(lore));
        return item;
    }

    private static ItemStack emptyItem() {
        ItemStack item = new ItemStack(Material.MAP);
        item.editMeta(meta -> {
            meta.displayName(plain("No Blueprints Found", NamedTextColor.GRAY)
                .decorate(TextDecoration.BOLD));
            meta.lore(List.of(
                Component.empty(),
                plain("Blueprints you collect will appear here.", NamedTextColor.DARK_GRAY),
                Component.empty()
            ));
        });
        return item;
    }

    private static ItemStack navigationItem(Material material, String name) {
        ItemStack item = new ItemStack(material);
        item.editMeta(meta -> meta.displayName(
            plain(name, NamedTextColor.AQUA).decorate(TextDecoration.BOLD)
        ));
        return item;
    }

    private static Component plain(String text, NamedTextColor color) {
        return Component.text(text, color).decoration(TextDecoration.ITALIC, false);
    }
}
