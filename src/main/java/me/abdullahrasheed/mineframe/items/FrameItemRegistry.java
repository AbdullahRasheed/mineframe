package me.abdullahrasheed.mineframe.items;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import me.abdullahrasheed.mineframe.config.MergedYamlConfig;
import me.abdullahrasheed.mineframe.items.ingredients.FrameItemIngredient;
import me.abdullahrasheed.mineframe.items.ingredients.FrameMaterialIngredient;
import me.abdullahrasheed.mineframe.items.ingredients.VanillaItemIngredient;
import me.abdullahrasheed.mineframe.materials.FrameMaterial;

/** Loads FrameItems and their one-to-one blueprints from items.yml. */
public final class FrameItemRegistry {

    private static final String FILE_NAME = "items.yml";
    private static final Pattern VALID_ID = Pattern.compile("[a-z0-9._-]+");

    private final JavaPlugin plugin;
    private Map<String, FrameItem> itemsById = Map.of();

    public FrameItemRegistry(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public int reload() {
        YamlConfiguration configuration = MergedYamlConfig.load(plugin, FILE_NAME);
        Map<String, FrameItem> loaded = new LinkedHashMap<>();

        for (String configuredId : configuration.getKeys(false)) {
            String id = configuredId.toLowerCase(Locale.ROOT);
            if (!VALID_ID.matcher(id).matches()) {
                warn("Invalid item id '%s'; use lowercase letters, numbers, '.', '_' or '-'."
                    .formatted(configuredId));
                continue;
            }

            ConfigurationSection section = configuration.getConfigurationSection(configuredId);
            if (section == null) {
                warn("'%s' must be a configuration section.".formatted(configuredId));
                continue;
            }

            try {
                loaded.put(id, loadItem(id, section));
            } catch (IllegalArgumentException exception) {
                warn("Invalid item '%s': %s. Skipping it."
                    .formatted(configuredId, exception.getMessage()));
            }
        }

        itemsById = Collections.unmodifiableMap(new LinkedHashMap<>(loaded));
        plugin.getLogger().info("Cached " + loaded.size() + " FrameItem definition(s).");
        return loaded.size();
    }

    public FrameItem get(String id) {
        return id == null ? null : itemsById.get(id.toLowerCase(Locale.ROOT));
    }

    public List<FrameItem> all() {
        return List.copyOf(itemsById.values());
    }

    private static FrameItem loadItem(String id, ConfigurationSection section) {
        String displayName = section.getString("display-name", "").trim();
        if (displayName.isEmpty()) {
            throw new IllegalArgumentException("display-name must not be empty");
        }
        return new FrameItem(id, displayName, readIngredients(section));
    }

    private static List<FrameItemIngredient> readIngredients(ConfigurationSection item) {
        ConfigurationSection ingredients = item.getConfigurationSection("ingredients");
        if (ingredients == null) {
            return List.of();
        }

        List<FrameItemIngredient> loaded = new ArrayList<>();
        ConfigurationSection frameMaterials = ingredients.getConfigurationSection("frame-materials");
        if (frameMaterials != null) {
            for (String materialId : frameMaterials.getKeys(false)) {
                FrameMaterial material = FrameMaterial.fromId(materialId);
                if (material == null) {
                    throw new IllegalArgumentException("unknown FrameMaterial ingredient '" + materialId + "'");
                }
                loaded.add(new FrameMaterialIngredient(
                    material,
                    readPositiveAmount(frameMaterials, materialId)
                ));
            }
        }

        ConfigurationSection minecraftItems = ingredients.getConfigurationSection("minecraft-items");
        if (minecraftItems != null) {
            for (String itemId : minecraftItems.getKeys(false)) {
                Material material = Material.matchMaterial(itemId);
                if (material == null || !material.isItem()) {
                    throw new IllegalArgumentException("unknown Minecraft item ingredient '" + itemId + "'");
                }
                loaded.add(new VanillaItemIngredient(
                    material,
                    readPositiveAmount(minecraftItems, itemId)
                ));
            }
        }
        return loaded;
    }

    private static int readPositiveAmount(ConfigurationSection section, String key) {
        int amount = section.getInt(key, -1);
        if (amount < 1) {
            throw new IllegalArgumentException("ingredient '" + key + "' must have an amount of at least 1");
        }
        return amount;
    }

    private void warn(String message) {
        plugin.getLogger().warning("[" + FILE_NAME + "] " + message);
    }
}
