package me.abdullahrasheed.mineframe.materials;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import me.abdullahrasheed.mineframe.materials.drops.MaterialDropRegistry;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public final class MaterialsCommand implements TabExecutor {

    private static final String RELOAD_PERMISSION = "mineframe.command.materials.reload";
    private final MaterialDropRegistry dropRegistry;

    public MaterialsCommand(MaterialDropRegistry dropRegistry) {
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
                    "You do not have permission to reload material drops.",
                    NamedTextColor.RED
                ));
                return true;
            }

            int ruleCount = dropRegistry.reload();
            sender.sendMessage(Component.text("Reloaded material-drops.yml and cached ", NamedTextColor.GREEN)
                .append(Component.text(ruleCount, NamedTextColor.WHITE))
                .append(Component.text(" drop rule(s).", NamedTextColor.GREEN)));
            return true;
        }

        if (args.length != 0) {
            sender.sendMessage(Component.text("Usage: /" + label + " [reload]", NamedTextColor.RED));
            return true;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Only players can open the materials menu.", NamedTextColor.RED));
            return true;
        }

        new MaterialsMenu(player).open(player);
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
                && "reload".startsWith(args[0].toLowerCase())) {
            return List.of("reload");
        }
        return List.of();
    }
}
