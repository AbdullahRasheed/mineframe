package me.abdullahrasheed.mineframe.materials;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;

/** Displays one temporary pickup boss bar for each material type. */
public final class FrameMaterialPickupBars {

    private static final long DISPLAY_TICKS = 60L;
    private static final Map<UUID, Map<FrameMaterial, PickupBar>> ACTIVE = new HashMap<>();

    private FrameMaterialPickupBars() {
    }

    public static void show(Player player, FrameMaterial material, int amount) {
        if (amount <= 0) {
            return;
        }

        Map<FrameMaterial, PickupBar> playerBars = ACTIVE.computeIfAbsent(
            player.getUniqueId(), ignored -> new EnumMap<>(FrameMaterial.class)
        );
        PickupBar pickupBar = playerBars.get(material);

        if (pickupBar == null) {
            BossBar bossBar = BossBar.bossBar(
                pickupText(material, amount),
                0.0f,
                BossBar.Color.WHITE,
                BossBar.Overlay.NOTCHED_20
            );
            pickupBar = new PickupBar(player, material, bossBar, amount);
            playerBars.put(material, pickupBar);
            player.showBossBar(bossBar);
        } else {
            pickupBar.amount += amount;
            pickupBar.bossBar.name(pickupText(material, pickupBar.amount));
            pickupBar.removalTask.cancel();
        }

        PickupBar scheduledBar = pickupBar;
        JavaPlugin plugin = JavaPlugin.getProvidingPlugin(FrameMaterialPickupBars.class);
        pickupBar.removalTask = Bukkit.getScheduler().runTaskLater(
            plugin,
            () -> removeIfCurrent(scheduledBar),
            DISPLAY_TICKS
        );
    }

    public static void clear(Player player) {
        Map<FrameMaterial, PickupBar> playerBars = ACTIVE.remove(player.getUniqueId());
        if (playerBars == null) {
            return;
        }

        for (PickupBar pickupBar : playerBars.values()) {
            if (pickupBar.removalTask != null) {
                pickupBar.removalTask.cancel();
            }
            player.hideBossBar(pickupBar.bossBar);
        }
    }

    public static void clearAll() {
        for (UUID playerId : ACTIVE.keySet().toArray(UUID[]::new)) {
            Map<FrameMaterial, PickupBar> playerBars = ACTIVE.get(playerId);
            if (playerBars != null && !playerBars.isEmpty()) {
                clear(playerBars.values().iterator().next().player);
            }
        }
    }

    private static void removeIfCurrent(PickupBar pickupBar) {
        Map<FrameMaterial, PickupBar> playerBars = ACTIVE.get(pickupBar.player.getUniqueId());
        if (playerBars == null || playerBars.get(pickupBar.material) != pickupBar) {
            return;
        }

        playerBars.remove(pickupBar.material);
        pickupBar.player.hideBossBar(pickupBar.bossBar);
        if (playerBars.isEmpty()) {
            ACTIVE.remove(pickupBar.player.getUniqueId());
        }
    }

    private static Component pickupText(FrameMaterial material, int amount) {
        return Component.text("+" + amount + " ", NamedTextColor.WHITE)
            .append(Component.text(material.getName(), TextColor.color(material.getBeamColor().asRGB())));
    }

    private static final class PickupBar {
        private final Player player;
        private final FrameMaterial material;
        private final BossBar bossBar;
        private int amount;
        private BukkitTask removalTask;

        private PickupBar(Player player, FrameMaterial material, BossBar bossBar, int amount) {
            this.player = player;
            this.material = material;
            this.bossBar = bossBar;
            this.amount = amount;
        }
    }
}

