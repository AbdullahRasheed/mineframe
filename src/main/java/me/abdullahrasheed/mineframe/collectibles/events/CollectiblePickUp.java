package me.abdullahrasheed.mineframe.collectibles.events;

import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import me.abdullahrasheed.mineframe.blueprints.Blueprint;
import me.abdullahrasheed.mineframe.collectibles.PickupBars;
import me.abdullahrasheed.mineframe.items.FrameItem;
import me.abdullahrasheed.mineframe.items.FrameItemRegistry;
import me.abdullahrasheed.mineframe.materials.FrameMaterial;
import me.abdullahrasheed.mineframe.players.FramePlayer;

/** Moves collectible item entities into player PDC counts instead of inventories. */
public final class CollectiblePickUp implements Listener {

    private static final String MATERIAL_PICKUP_SOUND = "mineframe:world.pickup";
    private static final float MATERIAL_PICKUP_VOLUME = 1.0f;
    private static final float MIN_PICKUP_PITCH = 0.94f;
    private static final float PICKUP_PITCH_VARIATION = 0.12f;

    private final FrameItemRegistry itemRegistry;

    public CollectiblePickUp(FrameItemRegistry itemRegistry) {
        this.itemRegistry = itemRegistry;
    }

    @EventHandler
    public void onPickUp(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        ItemStack itemStack = event.getItem().getItemStack();
        FrameMaterial material = FrameMaterial.getType(itemStack);
        if (material != null) {
            int amount = FrameMaterial.getDropAmount(itemStack);
            collect(event);
            FramePlayer.addFrameMaterial(player, material, amount);
            PickupBars.show(player, "material", material, amount);
            playMaterialPickupSound(player);
            return;
        }

        String blueprintId = itemStack.getPersistentDataContainer().get(
            Blueprint.ITEM_TYPE_KEY,
            PersistentDataType.STRING
        );
        FrameItem frameItem = itemRegistry.get(blueprintId);
        if (frameItem == null) {
            return;
        }
        Blueprint blueprint = frameItem.getBlueprint();

        int amount = itemStack.getAmount();
        collect(event);
        FramePlayer.addBlueprint(player, blueprint, amount);
        PickupBars.show(player, "blueprint", blueprint, amount);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        PickupBars.clear(event.getPlayer());
    }

    private static void collect(EntityPickupItemEvent event) {
        event.setCancelled(true);
        event.getItem().remove();
    }

    private static void playMaterialPickupSound(Player player) {
        float pitch = MIN_PICKUP_PITCH
            + ThreadLocalRandom.current().nextFloat() * PICKUP_PITCH_VARIATION;
        player.playSound(
            player.getLocation(),
            MATERIAL_PICKUP_SOUND,
            SoundCategory.PLAYERS,
            MATERIAL_PICKUP_VOLUME,
            pitch
        );
    }
}
