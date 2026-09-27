package me.abdullahrasheed.mineframe.blueprints.drops;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import me.abdullahrasheed.mineframe.blueprints.Blueprint;
import me.abdullahrasheed.mineframe.collectibles.drops.BlockConditions;
import me.abdullahrasheed.mineframe.config.MergedYamlConfig;
import me.abdullahrasheed.mineframe.items.FrameItem;
import me.abdullahrasheed.mineframe.items.FrameItemRegistry;

/** Loads block-only blueprint drops. Mob sources can be added separately later. */
public final class BlueprintDropRegistry {

    private static final String FILE_NAME = "blueprint-drops.yml";

    private final JavaPlugin plugin;
    private final FrameItemRegistry itemRegistry;
    private Map<Material, List<BlueprintDropRule>> rulesByBlock = Map.of();

    public BlueprintDropRegistry(JavaPlugin plugin, FrameItemRegistry itemRegistry) {
        this.plugin = plugin;
        this.itemRegistry = itemRegistry;
    }

    public int reload() {
        YamlConfiguration configuration = MergedYamlConfig.load(plugin, FILE_NAME);
        Map<Material, List<BlueprintDropRule>> loadedRules = new EnumMap<>(Material.class);
        int ruleCount = 0;

        for (String itemId : configuration.getKeys(false)) {
            FrameItem frameItem = itemRegistry.get(itemId);
            if (frameItem == null) {
                warn("Unknown FrameItem '%s'; skipping its blueprint drop rules.".formatted(itemId));
                continue;
            }
            Blueprint blueprint = frameItem.getBlueprint();

            ConfigurationSection blueprintSection = configuration.getConfigurationSection(itemId);
            if (blueprintSection == null) {
                warn("'%s' must be a configuration section.".formatted(itemId));
                continue;
            }

            for (String blockId : blueprintSection.getKeys(false)) {
                Material sourceBlock = Material.matchMaterial(blockId);
                String rulePath = itemId + "." + blockId;
                if (sourceBlock == null || !sourceBlock.isBlock()) {
                    warn("%s references unknown block '%s'; skipping it."
                        .formatted(rulePath, blockId));
                    continue;
                }

                ConfigurationSection ruleSection = blueprintSection.getConfigurationSection(blockId);
                if (ruleSection == null) {
                    warn("%s must be a configuration section.".formatted(rulePath));
                    continue;
                }

                try {
                    double chance = readChance(ruleSection);
                    BlueprintDropRule rule = new BlueprintDropRule(
                        blueprint,
                        sourceBlock,
                        chance,
                        BlockConditions.fromRule(ruleSection)
                    );
                    loadedRules.computeIfAbsent(sourceBlock, ignored -> new ArrayList<>()).add(rule);
                    ruleCount++;
                } catch (IllegalArgumentException exception) {
                    warn("Invalid rule %s: %s. Skipping it."
                        .formatted(rulePath, exception.getMessage()));
                }
            }
        }

        Map<Material, List<BlueprintDropRule>> immutableRules = new EnumMap<>(Material.class);
        loadedRules.forEach((material, rules) -> immutableRules.put(material, List.copyOf(rules)));
        rulesByBlock = Map.copyOf(immutableRules);
        plugin.getLogger().info("Cached " + ruleCount + " blueprint block drop rule(s).");
        return ruleCount;
    }

    public List<BlueprintDropRule> rulesFor(Material blockType) {
        return rulesByBlock.getOrDefault(blockType, List.of());
    }

    private static double readChance(ConfigurationSection section) {
        if (!section.contains("chance")) {
            throw new IllegalArgumentException("missing chance");
        }
        double chance = section.getDouble("chance", Double.NaN);
        if (!Double.isFinite(chance) || chance < 0.0 || chance > 1.0) {
            throw new IllegalArgumentException("chance must be a number between 0 and 1");
        }
        return chance;
    }

    private void warn(String message) {
        plugin.getLogger().warning("[" + FILE_NAME + "] " + message);
    }
}
