package me.abdullahrasheed.mineframe.materials.drops;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;

/** Cached block-state requirements such as {@code age: max}. */
public final class BlockConditions {

    private static final BlockConditions NONE = new BlockConditions(Map.of());
    private final Map<String, String> expectedProperties;

    public BlockConditions(Map<String, String> expectedProperties) {
        Map<String, String> normalized = new LinkedHashMap<>();
        expectedProperties.forEach((key, value) -> normalized.put(
            key.toLowerCase(Locale.ROOT),
            value.toLowerCase(Locale.ROOT)
        ));
        this.expectedProperties = Map.copyOf(normalized);
    }

    public static BlockConditions none() {
        return NONE;
    }

    public boolean matches(BlockData blockData) {
        if (expectedProperties.isEmpty()) {
            return true;
        }

        Map<String, String> actualProperties = parseProperties(blockData.getAsString(false));
        for (Map.Entry<String, String> condition : expectedProperties.entrySet()) {
            String key = condition.getKey();
            String expected = condition.getValue();

            if (key.equals("age") && isMaximumAlias(expected)) {
                if (!(blockData instanceof Ageable ageable)
                        || ageable.getAge() != ageable.getMaximumAge()) {
                    return false;
                }
                continue;
            }

            if (!expected.equals(actualProperties.get(key))) {
                return false;
            }
        }
        return true;
    }

    private static boolean isMaximumAlias(String value) {
        return value.equals("max") || value.equals("maximum") || value.equals("fully_grown");
    }

    private static Map<String, String> parseProperties(String serializedBlockData) {
        int openingBracket = serializedBlockData.indexOf('[');
        int closingBracket = serializedBlockData.lastIndexOf(']');
        if (openingBracket < 0 || closingBracket <= openingBracket) {
            return Map.of();
        }

        Map<String, String> properties = new LinkedHashMap<>();
        String body = serializedBlockData.substring(openingBracket + 1, closingBracket);
        for (String entry : body.split(",")) {
            String[] pair = entry.split("=", 2);
            if (pair.length == 2) {
                properties.put(pair[0].trim(), pair[1].trim());
            }
        }
        return properties;
    }
}

