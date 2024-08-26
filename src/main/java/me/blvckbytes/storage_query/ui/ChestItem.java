package me.blvckbytes.storage_query.ui;

import org.bukkit.block.Chest;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public record ChestItem (Chest chest, int slot) {
  public @Nullable ItemStack access() {
    return chest.getInventory().getItem(slot);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof ChestItem otherItem)) return false;
    if (slot != otherItem.slot) return false;
    return chest.getLocation().equals(otherItem.chest.getLocation());
  }

  @Override
  public int hashCode() {
    return Objects.hash(chest.getLocation(), slot);
  }
}
