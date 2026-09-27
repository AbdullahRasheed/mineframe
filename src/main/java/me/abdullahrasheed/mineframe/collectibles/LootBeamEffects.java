package me.abdullahrasheed.mineframe.collectibles;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Levelled;
import org.bukkit.entity.Display;
import org.bukkit.entity.Item;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.joml.Vector3f;

import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.DyedItemColor;

/** Spawns and maintains the shared beam and light effect for collectible drops. */
public final class LootBeamEffects {

    private static final Map<Block, LightSource> ACTIVE_LIGHTS = new HashMap<>();
    private static final Set<BeamEffect> ACTIVE_BEAMS = new HashSet<>();

    private LootBeamEffects() {
    }

    public static Item drop(Location location, ItemStack itemStack, Color color) {
        ItemStack beamItem = new ItemStack(Material.PAPER);
        beamItem.setData(
            DataComponentTypes.ITEM_MODEL,
            NamespacedKey.fromString("mineframe:loot_beam")
        );
        beamItem.setData(DataComponentTypes.DYED_COLOR, DyedItemColor.dyedItemColor(color));

        Item droppedItem = location.getWorld().dropItemNaturally(location, itemStack);
        ItemDisplay beamDisplay = location.getWorld().spawn(
            droppedItem.getLocation(),
            ItemDisplay.class,
            display -> {
                display.setItemStack(beamItem);
                display.setItemDisplayTransform(ItemDisplay.ItemDisplayTransform.NONE);
                display.setBrightness(new Display.Brightness(15, 15));

                var transformation = display.getTransformation();
                transformation.getTranslation().set(new Vector3f(0.0f, 1.75f, 0.0f));
                transformation.getScale().set(new Vector3f(1.0f, 4.0f, 1.0f));
                display.setTransformation(transformation);
            }
        );

        if (!droppedItem.addPassenger(beamDisplay)) {
            beamDisplay.remove();
            plugin().getLogger().warning("Could not attach a loot beam to its dropped item.");
            return droppedItem;
        }

        BeamEffect beamEffect = new BeamEffect(droppedItem, beamDisplay);
        ACTIVE_BEAMS.add(beamEffect);
        beamEffect.runTaskTimer(plugin(), 0L, 1L);
        return droppedItem;
    }

    public static void cleanup() {
        for (BeamEffect beam : Set.copyOf(ACTIVE_BEAMS)) {
            beam.stop();
        }
    }

    private static JavaPlugin plugin() {
        return JavaPlugin.getProvidingPlugin(LootBeamEffects.class);
    }

    private static boolean acquireLight(Block block) {
        LightSource activeLight = ACTIVE_LIGHTS.get(block);
        if (activeLight != null) {
            activeLight.references++;
            return true;
        }

        if (!block.getType().isAir()) {
            return false;
        }

        BlockData originalData = block.getBlockData();
        Levelled lightData = (Levelled) Bukkit.createBlockData(Material.LIGHT);
        lightData.setLevel(7);
        block.setBlockData(lightData, false);
        ACTIVE_LIGHTS.put(block, new LightSource(originalData));
        return true;
    }

    private static void releaseLight(Block block) {
        LightSource activeLight = ACTIVE_LIGHTS.get(block);
        if (activeLight == null || --activeLight.references > 0) {
            return;
        }

        ACTIVE_LIGHTS.remove(block);
        if (block.getType() == Material.LIGHT) {
            block.setBlockData(activeLight.originalData, false);
        }
    }

    private static final class BeamEffect extends BukkitRunnable {
        private final Item droppedItem;
        private final ItemDisplay beamDisplay;
        private Block lightBlock;
        private boolean stopped;

        private BeamEffect(Item droppedItem, ItemDisplay beamDisplay) {
            this.droppedItem = droppedItem;
            this.beamDisplay = beamDisplay;
        }

        @Override
        public void run() {
            if (!droppedItem.isValid()
                    || droppedItem.isDead()
                    || !beamDisplay.isValid()
                    || beamDisplay.getVehicle() != droppedItem) {
                stop();
                return;
            }

            moveLight(droppedItem.getLocation().getBlock());
        }

        private void moveLight(Block nextBlock) {
            if (nextBlock.equals(lightBlock)) {
                return;
            }

            Block previousBlock = lightBlock;
            lightBlock = acquireLight(nextBlock) ? nextBlock : null;
            if (previousBlock != null) {
                releaseLight(previousBlock);
            }
        }

        private void stop() {
            if (stopped) {
                return;
            }

            stopped = true;
            cancel();
            beamDisplay.remove();
            if (lightBlock != null) {
                releaseLight(lightBlock);
                lightBlock = null;
            }
            ACTIVE_BEAMS.remove(this);
        }
    }

    private static final class LightSource {
        private final BlockData originalData;
        private int references = 1;

        private LightSource(BlockData originalData) {
            this.originalData = originalData;
        }
    }
}
