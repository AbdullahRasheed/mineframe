package me.abdullahrasheed.mineframe.materials.drops;

import java.io.File;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import me.abdullahrasheed.mineframe.materials.FrameMaterial;

/** Loads, validates, and caches material drop rules from material-drops.yml. */
public final class MaterialDropRegistry {

    private static final String FILE_NAME = "material-drops.yml";

    private final JavaPlugin plugin;
    private Map<Material, List<MaterialDropRule>> rulesByBlock = Map.of();

    public MaterialDropRegistry(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public int reload() {
        File file = new File(plugin.getDataFolder(), FILE_NAME);
        if (!file.exists()) {
            plugin.saveResource(FILE_NAME, false);
        }

        YamlConfiguration configuration = YamlConfiguration.loadConfiguration(file);
        Map<Material, List<MaterialDropRule>> loadedRules = new EnumMap<>(Material.class);
        int ruleCount = 0;

        for (String materialId : configuration.getKeys(false)) {
            FrameMaterial frameMaterial = findFrameMaterial(materialId);
            if (frameMaterial == null) {
                warn("Unknown FrameMaterial '%s'; skipping its drop rules.".formatted(materialId));
                continue;
            }

            ConfigurationSection materialSection = configuration.getConfigurationSection(materialId);
            if (materialSection == null) {
                warn("'%s' must be a configuration section.".formatted(materialId));
                continue;
            }

            for (String blockId : materialSection.getKeys(false)) {
                Material sourceBlock = Material.matchMaterial(blockId);
                String rulePath = materialId + "." + blockId;
                if (sourceBlock == null || !sourceBlock.isBlock()) {
                    warn("%s references unknown block '%s'; skipping it.".formatted(rulePath, blockId));
                    continue;
                }

                ConfigurationSection ruleSection = materialSection.getConfigurationSection(blockId);
                if (ruleSection == null) {
                    warn("%s must be a configuration section.".formatted(rulePath));
                    continue;
                }

                try {
                    double chance = readChance(ruleSection, rulePath);
                    DropRange range = readRange(ruleSection.get("range"), rulePath);
                    BlockConditions conditions = readConditions(ruleSection);
                    MaterialDropRule rule = new MaterialDropRule(
                        frameMaterial,
                        sourceBlock,
                        chance,
                        range,
                        conditions
                    );
                    loadedRules.computeIfAbsent(sourceBlock, ignored -> new ArrayList<>()).add(rule);
                    ruleCount++;
                } catch (IllegalArgumentException exception) {
                    warn("Invalid rule %s: %s. Skipping it.".formatted(rulePath, exception.getMessage()));
                }
            }
        }

        Map<Material, List<MaterialDropRule>> immutableRules = new EnumMap<>(Material.class);
        loadedRules.forEach((material, rules) -> immutableRules.put(material, List.copyOf(rules)));
        rulesByBlock = Map.copyOf(immutableRules);
        plugin.getLogger().info("Cached " + ruleCount + " FrameMaterial drop rule(s)." );
        return ruleCount;
    }

    public List<MaterialDropRule> rulesFor(Material blockType) {
        return rulesByBlock.getOrDefault(blockType, List.of());
    }

    private static double readChance(ConfigurationSection section, String path) {
        if (!section.contains("chance")) {
            throw new IllegalArgumentException("missing chance");
        }

        double chance = section.getDouble("chance", Double.NaN);
        if (!Double.isFinite(chance) || chance < 0.0 || chance > 1.0) {
            throw new IllegalArgumentException("chance must be a number between 0 and 1");
        }
        return chance;
    }

    private static DropRange readRange(Object value, String path) {
        if (value == null) {
            throw new IllegalArgumentException("missing range");
        }

        if (value instanceof Number number) {
            int amount = number.intValue();
            return new DropRange(amount, amount);
        }

        if (value instanceof List<?> list && list.size() == 2) {
            return new DropRange(parsePositiveInt(list.get(0), path), parsePositiveInt(list.get(1), path));
        }

        String[] parts = value.toString().split(",", -1);
        if (parts.length != 2) {
            throw new IllegalArgumentException("range must look like '3,12' or [3, 12]");
        }
        return new DropRange(
            parsePositiveInt(parts[0].trim(), path),
            parsePositiveInt(parts[1].trim(), path)
        );
    }

    private static int parsePositiveInt(Object value, String path) {
        try {
            int parsed = Integer.parseInt(value.toString().trim());
            if (parsed < 1) {
                throw new IllegalArgumentException("range values must be at least 1");
            }
            return parsed;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("range contains a non-integer value at " + path);
        }
    }

    private static BlockConditions readConditions(ConfigurationSection ruleSection) {
        ConfigurationSection conditionsSection = ruleSection.getConfigurationSection("conditions");
        if (conditionsSection == null) {
            return BlockConditions.none();
        }

        Map<String, String> conditions = new LinkedHashMap<>();
        for (String key : conditionsSection.getKeys(false)) {
            Object value = conditionsSection.get(key);
            if (value != null) {
                conditions.put(key, value.toString());
            }
        }
        return new BlockConditions(conditions);
    }

    private static FrameMaterial findFrameMaterial(String id) {
        String normalized = id.toLowerCase(Locale.ROOT);
        for (FrameMaterial material : FrameMaterial.values()) {
            if (material.getModelName().equals(normalized)) {
                return material;
            }
        }
        return null;
    }

    private void warn(String message) {
        Logger logger = plugin.getLogger();
        logger.warning("[" + FILE_NAME + "] " + message);
    }
}
