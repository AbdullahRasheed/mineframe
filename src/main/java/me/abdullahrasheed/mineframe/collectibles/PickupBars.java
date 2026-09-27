package me.abdullahrasheed.mineframe.collectibles;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;

/** Displays and aggregates temporary pickup notifications for all collectibles. */
public final class PickupBars {

    private static final long DISPLAY_TICKS = 60L;
    private static final Map<UUID, Map<String, PickupBar>> ACTIVE = new HashMap<>();

    private PickupBars() {
    }

    public static void show(Player player, String category, Collectible collectible, int amount) {
        show(
            player,
            category + ":" + collectible.getId(),
            collectible.getName(),
            collectible.getBeamColor(),
            amount
        );
    }

    public static void show(
            Player player,
            String pickupKey,
            String displayName,
            Color color,
            int amount
    ) {
        if (amount <= 0) {
            return;
        }

        Map<String, PickupBar> playerBars = ACTIVE.computeIfAbsent(
            player.getUniqueId(), ignored -> new HashMap<>()
        );
        PickupBar pickupBar = playerBars.get(pickupKey);

        if (pickupBar == null) {
            BossBar bossBar = BossBar.bossBar(
                pickupText(displayName, color, amount),
                0.0f,
                BossBar.Color.WHITE,
                BossBar.Overlay.NOTCHED_20
            );
            pickupBar = new PickupBar(player, pickupKey, bossBar, amount);
            playerBars.put(pickupKey, pickupBar);
            player.showBossBar(bossBar);
        } else {
            pickupBar.amount += amount;
            pickupBar.bossBar.name(pickupText(displayName, color, pickupBar.amount));
            pickupBar.removalTask.cancel();
        }

        PickupBar scheduledBar = pickupBar;
        pickupBar.removalTask = Bukkit.getScheduler().runTaskLater(
            JavaPlugin.getProvidingPlugin(PickupBars.class),
            () -> removeIfCurrent(scheduledBar),
            DISPLAY_TICKS
        );
    }

    public static void clear(Player player) {
        Map<String, PickupBar> playerBars = ACTIVE.remove(player.getUniqueId());
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
            Map<String, PickupBar> playerBars = ACTIVE.get(playerId);
            if (playerBars != null && !playerBars.isEmpty()) {
                clear(playerBars.values().iterator().next().player);
            }
        }
    }

    private static void removeIfCurrent(PickupBar pickupBar) {
        Map<String, PickupBar> playerBars = ACTIVE.get(pickupBar.player.getUniqueId());
        if (playerBars == null || playerBars.get(pickupBar.pickupKey) != pickupBar) {
            return;
        }

        playerBars.remove(pickupBar.pickupKey);
        pickupBar.player.hideBossBar(pickupBar.bossBar);
        if (playerBars.isEmpty()) {
            ACTIVE.remove(pickupBar.player.getUniqueId());
        }
    }

    private static Component pickupText(String displayName, Color color, int amount) {
        return Component.text("+" + amount + " ", NamedTextColor.WHITE)
            .append(Component.text(displayName, TextColor.color(color.asRGB())));
    }

    private static final class PickupBar {
        private final Player player;
        private final String pickupKey;
        private final BossBar bossBar;
        private int amount;
        private BukkitTask removalTask;

        private PickupBar(Player player, String pickupKey, BossBar bossBar, int amount) {
            this.player = player;
            this.pickupKey = pickupKey;
            this.bossBar = bossBar;
            this.amount = amount;
        }
    }
}
