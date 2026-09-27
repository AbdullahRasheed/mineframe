package me.abdullahrasheed.mineframe.materials;

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
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.joml.Vector3f;

import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.DyedItemColor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;

public enum FrameMaterial {

    SELITE("Selite", Color.AQUA),
    THORIN("Thorin", Color.ORANGE);

    private static final Map<Block, LightSource> ACTIVE_LIGHTS = new HashMap<>();
    private static final Set<BeamEffect> ACTIVE_BEAMS = new HashSet<>();

    private final String name, modelName;
    private final Color beamColor;

    FrameMaterial(String name, Color beamColor){
        this.name = name;
        this.modelName = this.toString().toLowerCase();
        this.beamColor = beamColor;
    }

    public String getName(){
        return name;
    }

    public String getModelName(){
        return modelName;
    }

    public Color getBeamColor() {
        return beamColor;
    }

    public ItemStack createItemStack(){
        ItemStack item = new ItemStack(Material.PAPER);
        item.editMeta(meta -> {
            Component displayName = Component.text(getName())
                .decoration(TextDecoration.ITALIC, false)
                .color(TextColor.color(getBeamColor().asRGB()));
            meta.displayName(displayName);
        });
        item.setData(DataComponentTypes.ITEM_MODEL, NamespacedKey.fromString("mineframe:" + modelName));
        item.editPersistentDataContainer(pdc -> {
            pdc.set(NamespacedKey.fromString("mineframe:material_type"), PersistentDataType.STRING, modelName);
        });

        return item;
    }
    
    public void dropAt(Location location){
        location.getWorld().dropItemNaturally(location, createItemStack());
    }

    public void dropAt(Location location, Color color){
        dropAt(location, color, 1);
    }

    public void dropAt(Location location, Color color, int amount){
        if (amount < 1) {
            throw new IllegalArgumentException("amount must be at least 1");
        }

        ItemStack item = createItemStack();
        item.editPersistentDataContainer(pdc -> pdc.set(
            NamespacedKey.fromString("mineframe:material_amount"),
            PersistentDataType.INTEGER,
            amount
        ));

        ItemStack beamItem = new ItemStack(Material.PAPER);
        beamItem.setData(DataComponentTypes.ITEM_MODEL, NamespacedKey.fromString("mineframe:loot_beam"));
        DyedItemColor dyedData = DyedItemColor.dyedItemColor(color);
        beamItem.setData(DataComponentTypes.DYED_COLOR, dyedData);

        Item droppedItem = location.getWorld().dropItemNaturally(location, item);
        ItemDisplay beamDisplay = location.getWorld().spawn(droppedItem.getLocation(), ItemDisplay.class, display -> {
            display.setItemStack(beamItem);
            display.setItemDisplayTransform(ItemDisplay.ItemDisplayTransform.NONE);

            display.setBrightness(new Display.Brightness(15, 15));

            // Scale it via transformation vectors if it needs to be taller/wider
            var transformation = display.getTransformation();
            transformation.getTranslation().set(new Vector3f(0.0f, 1.75f, 0.0f));
            transformation.getScale().set(new Vector3f(1.0f, 4.0f, 1.0f)); // Extends it 4x taller
            display.setTransformation(transformation);
        });

        if (!droppedItem.addPassenger(beamDisplay)) {
            beamDisplay.remove();
            JavaPlugin.getProvidingPlugin(FrameMaterial.class).getLogger()
                .warning("Could not attach a loot beam to its dropped item.");
            return;
        }

        BeamEffect beamEffect = new BeamEffect(droppedItem, beamDisplay);
        ACTIVE_BEAMS.add(beamEffect);
        beamEffect.runTaskTimer(JavaPlugin.getProvidingPlugin(FrameMaterial.class), 0L, 1L);
    }

    /** Removes transient displays and light blocks, primarily during plugin shutdown. */
    public static void cleanupEffects() {
        for (BeamEffect beam : Set.copyOf(ACTIVE_BEAMS)) {
            beam.stop();
        }
    }

    public static FrameMaterial getType(ItemStack itemStack){
        String type = itemStack.getPersistentDataContainer().getOrDefault(
            NamespacedKey.fromString("mineframe:material_type"), 
            PersistentDataType.STRING, 
            null);

        if(type == null) return null;

        for (FrameMaterial material : values()) {
            if (material.modelName.equals(type)) {
                return material;
            }
        }
        return null;
    }

    public static int getDropAmount(ItemStack itemStack) {
        Integer amountPerItem = itemStack.getPersistentDataContainer().get(
            NamespacedKey.fromString("mineframe:material_amount"),
            PersistentDataType.INTEGER
        );
        if (amountPerItem == null) {
            return itemStack.getAmount();
        }

        long total = (long) amountPerItem * itemStack.getAmount();
        return (int) Math.min(total, Integer.MAX_VALUE);
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

            Location itemLocation = droppedItem.getLocation();
            moveLight(itemLocation.getBlock());
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
