package me.abdullahrasheed.mineframe.gui;

/** Controls the default interaction rules for a {@link Menu}. */
public record MenuOptions(
        boolean editable,
        boolean allowPlayerInventoryEdits,
        boolean closeOnHandledClick
) {

    public static MenuOptions readOnly() {
        return new MenuOptions(false, false, false);
    }

    public static MenuOptions fullyEditable() {
        return new MenuOptions(true, true, false);
    }

    public MenuOptions withCloseOnHandledClick(boolean closeOnHandledClick) {
        return new MenuOptions(editable, allowPlayerInventoryEdits, closeOnHandledClick);
    }
}
