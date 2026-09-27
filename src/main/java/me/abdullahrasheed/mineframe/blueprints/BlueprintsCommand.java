package me.abdullahrasheed.mineframe.blueprints;

import java.util.List;
import java.util.Locale;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import me.abdullahrasheed.mineframe.blueprints.drops.BlueprintDropRegistry;
import me.abdullahrasheed.mineframe.items.FrameItemRegistry;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public final class BlueprintsCommand implements TabExecutor {

    private static final String RELOAD_PERMISSION = "mineframe.command.blueprints.reload";

    private final FrameItemRegistry itemRegistry;
    private final BlueprintDropRegistry dropRegistry;

    public BlueprintsCommand(
            FrameItemRegistry itemRegistry,
            BlueprintDropRegistry dropRegistry
    ) {
        this.itemRegistry = itemRegistry;
        this.dropRegistry = dropRegistry;
    }

    @Override
    public boolean onCommand(
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String label,
            @NotNull String[] args
    ) {
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission(RELOAD_PERMISSION)) {
                sender.sendMessage(Component.text(
                    "You do not have permission to reload blueprints.",
                    NamedTextColor.RED
                ));
                return true;
            }

            int itemCount = itemRegistry.reload();
            int ruleCount = dropRegistry.reload();
            sender.sendMessage(Component.text("Reloaded ", NamedTextColor.GREEN)
                .append(Component.text(itemCount, NamedTextColor.WHITE))
                .append(Component.text(" FrameItem(s) and ", NamedTextColor.GREEN))
                .append(Component.text(ruleCount, NamedTextColor.WHITE))
                .append(Component.text(" block drop rule(s).", NamedTextColor.GREEN)));
            return true;
        }

        if (args.length != 0) {
            sender.sendMessage(Component.text("Usage: /" + label + " [reload]", NamedTextColor.RED));
            return true;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text(
                "Only players can open the blueprints menu.",
                NamedTextColor.RED
            ));
            return true;
        }

        new BlueprintsMenu(player, itemRegistry).open(player);
        return true;
    }

    @Override
    public @NotNull List<String> onTabComplete(
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String alias,
            @NotNull String[] args
    ) {
        if (args.length == 1
                && sender.hasPermission(RELOAD_PERMISSION)
                && "reload".startsWith(args[0].toLowerCase(Locale.ROOT))) {
            return List.of("reload");
        }
        return List.of();
    }
}
