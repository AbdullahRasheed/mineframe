package me.abdullahrasheed.mineframe.config;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

/** Loads a custom YAML file and fills missing values from its bundled copy. */
public final class MergedYamlConfig {

    private MergedYamlConfig() {
    }

    public static YamlConfiguration load(JavaPlugin plugin, String fileName) {
        File file = new File(plugin.getDataFolder(), fileName);
        if (!file.exists()) {
            plugin.saveResource(fileName, false);
        }

        YamlConfiguration configuration = YamlConfiguration.loadConfiguration(file);
        mergeMissingDefaults(plugin, fileName, file, configuration);
        return configuration;
    }

    private static void mergeMissingDefaults(
            JavaPlugin plugin,
            String fileName,
            File file,
            YamlConfiguration configuration
    ) {
        YamlConfiguration defaults;
        try (InputStream input = plugin.getResource(fileName)) {
            if (input == null) {
                warn(plugin, fileName, "The bundled defaults could not be found.");
                return;
            }

            defaults = YamlConfiguration.loadConfiguration(
                new InputStreamReader(input, StandardCharsets.UTF_8)
            );
        } catch (IOException exception) {
            warn(plugin, fileName, "The bundled defaults could not be read: " + exception.getMessage());
            return;
        }

        int addedSettings = 0;
        for (Map.Entry<String, Object> entry : defaults.getValues(true).entrySet()) {
            String path = entry.getKey();
            Object defaultValue = entry.getValue();
            if (defaultValue instanceof ConfigurationSection || configuration.contains(path)) {
                continue;
            }

            if (!canAddPath(configuration, path)) {
                warn(plugin, fileName,
                    "Cannot add default '%s' because one of its parent paths is not a section."
                        .formatted(path));
                continue;
            }

            configuration.set(path, defaultValue);
            addedSettings++;
        }

        if (addedSettings == 0) {
            return;
        }

        try {
            configuration.save(file);
            plugin.getLogger().info(
                "Added " + addedSettings + " missing default setting(s) to " + fileName + "."
            );
        } catch (IOException exception) {
            warn(plugin, fileName, "Could not save merged defaults: " + exception.getMessage());
        }
    }

    private static boolean canAddPath(ConfigurationSection configuration, String path) {
        int separator = path.indexOf('.');
        while (separator >= 0) {
            String parentPath = path.substring(0, separator);
            if (configuration.contains(parentPath)
                    && !configuration.isConfigurationSection(parentPath)) {
                return false;
            }
            separator = path.indexOf('.', separator + 1);
        }
        return true;
    }

    private static void warn(JavaPlugin plugin, String fileName, String message) {
        plugin.getLogger().warning("[" + fileName + "] " + message);
    }
}
