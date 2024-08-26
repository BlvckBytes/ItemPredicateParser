package me.blvckbytes.storage_query;

import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import oshi.util.tuples.Pair;

import java.util.ArrayList;
import java.util.List;

/*
  Author: BlvckBytes <blvckbytes@gmail.com>
  Created On: 07/23/2022

  Various utilities in regard to handling inventory related tasks.
*/
public class InventoryUtil {

  /**
   * Tries to add the given item-stack to the player's inventory by firstly seeking to
   * stack onto as many similar slots as possible, and only then makes use of vacant slots.
   * @param allOrNothing If true, transaction-like behavior is carried out
   * @return Number of items that didn't fit
   */
  public static int addToInventory(Inventory target, ItemStack item, boolean allOrNothing) {
    // This number will be decremented as space is found along the way
    int remaining = item.getAmount();
    int stackSize = item.getType().getMaxStackSize();

    // At first, only store planned partitions using the format <Slot, Amount>
    // and execute them all at once at the end, to have a transaction-like behavior
    List<Pair<Integer, Integer>> partitions = new ArrayList<>();
    List<Integer> vacant = new ArrayList<>();

    for (int i = 0; i < target.getSize(); ++i) {
      // Done, no more items remaining
      if (remaining < 0)
        break;

      ItemStack stack = target.getItem(i);

      // Completely vacant slot
      if (stack == null || stack.getType() == Material.AIR) {
        vacant.add(i);
        continue;
      }

      // Incompatible stacks, ignore
      if (!stack.isSimilar(item))
        continue;

      // Compatible stack but no more room left
      int usable = Math.max(0, stackSize - stack.getAmount());
      if (usable == 0)
        continue;

      // Add the last few remaining items, done
      if (usable >= remaining) {
        partitions.add(new Pair<>(i, stack.getAmount() + remaining));
        remaining = 0;
        break;
      }

      // Set to a full stack and subtract the delta from remaining
      partitions.add(new Pair<>(i, stackSize));
      remaining -= usable;
    }

    // If there are still items remaining, start using vacant slots
    if (remaining > 0 && !vacant.isEmpty()) {
      for (int v : vacant) {
        if (remaining <= 0)
          break;

        // Set as many items as possible or as many as remain
        int num = Math.min(remaining, stackSize);
        partitions.add(new Pair<>(v, num));
        remaining -= num;
      }
    }

    // Requested all or nothing, didn't fit completely
    if (allOrNothing && remaining > 0)
      return item.getAmount();

    // Apply partitions to inventory
    for (Pair<Integer, Integer> partition : partitions) {
      ItemStack curr = target.getItem(partition.getA());

      // Slot empty, create new item
      if (curr == null) {
        curr = item.clone();
        curr.setAmount(partition.getB());
        target.setItem(partition.getA(), curr);
        continue;
      }

      // Update existing slot
      curr.setAmount(partition.getB());
    }

    return remaining;
  }
}