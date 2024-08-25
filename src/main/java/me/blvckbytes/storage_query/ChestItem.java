package me.blvckbytes.storage_query;

import org.bukkit.block.Chest;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

public record ChestItem (Chest chest, int slot) {
  public @Nullable ItemStack access() {
    return chest.getInventory().getItem(slot);
  }
}
