package me.abdullahrasheed.mineframe.materials;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import io.papermc.paper.datacomponent.DataComponentTypes;

public enum FrameMaterial {

    SELITE("Selite");

    private final String name, modelName;
    FrameMaterial(String name){
        this.name = name;
        this.modelName = this.toString().toLowerCase();
    }

    public String getName(){
        return name;
    }

    public String getModelName(){
        return modelName;
    }
    
    public void dropAt(Location location){
        ItemStack item = new ItemStack(Material.ENDER_EYE);
        item.setData(DataComponentTypes.ITEM_MODEL, NamespacedKey.fromString("mineframe:" + modelName));
        item.editPersistentDataContainer(pdc -> {
            pdc.set(NamespacedKey.fromString("mineframe:material_type"), PersistentDataType.STRING, modelName);
        });

        location.getWorld().dropItemNaturally(location, item);
    }

    public static FrameMaterial getType(ItemStack itemStack){
        String type = itemStack.getPersistentDataContainer().getOrDefault(
            NamespacedKey.fromString("mineframe:material_type"), 
            PersistentDataType.STRING, 
            null);

        if(type == null) return null;

        return valueOf(type);
    }
}