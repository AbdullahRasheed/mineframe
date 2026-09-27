package me.abdullahrasheed.mineframe.gui;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import net.kyori.adventure.text.Component;

/**
 * Base class for chest-style menus.
 *
 * <p>Menus are read-only by default. Subclasses can use editable options and
 * override the protected interaction hooks for crafting inputs, result slots,
 * or other stateful interfaces.</p>
 */
public abstract class Menu implements InventoryHolder {

    private final Inventory inventory;
    private final MenuOptions options;
    private final Map<Integer, MenuClick> clickHandlers = new HashMap<>();

    protected Menu(int rows, Component title) {
        this(rows, title, MenuOptions.readOnly());
    }

    protected Menu(int rows, Component title, MenuOptions options) {
        if (rows < 1 || rows > 6) {
            throw new IllegalArgumentException("rows must be between 1 and 6");
        }

        this.options = Objects.requireNonNull(options, "options");
        this.inventory = Bukkit.createInventory(this, rows * 9, title);
    }

    @Override
    public final @NotNull Inventory getInventory() {
        return inventory;
    }

    public final void open(Player player) {
        beforeOpen(player);
        player.openInventory(inventory);
    }

    protected final void setItem(int slot, ItemStack item) {
        validateSlot(slot);
        inventory.setItem(slot, item);
        clickHandlers.remove(slot);
    }

    protected final void setItem(int slot, ItemStack item, MenuClick clickHandler) {
        validateSlot(slot);
        inventory.setItem(slot, item);
        clickHandlers.put(slot, Objects.requireNonNull(clickHandler, "clickHandler"));
    }

    protected final void clearItem(int slot) {
        validateSlot(slot);
        inventory.clear(slot);
        clickHandlers.remove(slot);
    }

    /** Called immediately before this menu is opened for a player. */
    protected void beforeOpen(Player player) {
    }

    /** Called for clicks in the menu's top inventory after default protection is applied. */
    protected void onClick(MenuClickContext context) {
    }

    /** Called after a drag while this menu is open. */
    protected void onDrag(Player player, InventoryDragEvent event) {
    }

    /** Called when a viewer closes this menu. Useful for returning crafting inputs. */
    protected void onClose(Player player, InventoryCloseEvent event) {
    }

    /** Override to lock individual slots in an otherwise editable menu. */
    protected boolean isSlotEditable(int slot, InventoryClickEvent event) {
        return true;
    }

    /** Override to restrict individual slots affected by an inventory drag. */
    protected boolean isSlotEditable(int slot, InventoryDragEvent event) {
        return true;
    }

    /** Override for custom shift-click routing in editable menus. */
    protected boolean allowsShiftClickIntoMenu(InventoryClickEvent event) {
        return options.editable();
    }

    /**
     * Override for complete control over click mutations. The default honors
     * {@link MenuOptions}, per-slot locks, hotbar swaps, and shift-clicks.
     */
    protected boolean allowsModification(InventoryClickEvent event) {
        int rawSlot = event.getRawSlot();
        boolean clickedTop = rawSlot >= 0 && rawSlot < inventory.getSize();

        if (!options.editable()) {
            return false;
        }

        if (clickedTop) {
            if (!isSlotEditable(rawSlot, event)) {
                return false;
            }
            if ((event.getClick() == ClickType.NUMBER_KEY || event.getClick() == ClickType.SWAP_OFFHAND)
                    && !options.allowPlayerInventoryEdits()) {
                return false;
            }
            return true;
        }

        if (!options.allowPlayerInventoryEdits()) {
            return false;
        }
        return event.getAction() != InventoryAction.MOVE_TO_OTHER_INVENTORY
            || allowsShiftClickIntoMenu(event);
    }

    /** Override for complete control over drag mutations. */
    protected boolean allowsDrag(InventoryDragEvent event) {
        if (!options.editable()) {
            return false;
        }

        int topSize = inventory.getSize();
        for (int rawSlot : event.getRawSlots()) {
            if (rawSlot < topSize) {
                if (!isSlotEditable(rawSlot, event)) {
                    return false;
                }
            } else if (!options.allowPlayerInventoryEdits()) {
                return false;
            }
        }
        return true;
    }

    final void handleClick(Player player, InventoryClickEvent event) {
        int topSize = inventory.getSize();
        int rawSlot = event.getRawSlot();
        boolean clickedTop = rawSlot >= 0 && rawSlot < topSize;

        if (!allowsModification(event)) {
            event.setCancelled(true);
        }

        if (!clickedTop) {
            return;
        }

        ItemStack clickedItem = event.getCurrentItem();
        MenuClickContext context = new MenuClickContext(this, player, rawSlot, clickedItem, event);
        MenuClick clickHandler = clickHandlers.get(rawSlot);
        if (clickHandler != null) {
            clickHandler.handle(context);
        }
        onClick(context);

        if (clickHandler != null && options.closeOnHandledClick()) {
            player.closeInventory();
        }
    }

    final void handleDrag(Player player, InventoryDragEvent event) {
        if (!allowsDrag(event)) {
            event.setCancelled(true);
        }
        onDrag(player, event);
    }

    final void handleClose(Player player, InventoryCloseEvent event) {
        onClose(player, event);
    }

    private void validateSlot(int slot) {
        if (slot < 0 || slot >= inventory.getSize()) {
            throw new IndexOutOfBoundsException("slot " + slot + " is outside this menu");
        }
    }
}
