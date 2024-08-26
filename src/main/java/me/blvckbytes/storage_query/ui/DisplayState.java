package me.blvckbytes.storage_query.ui;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;

public class DisplayState {

  private int currentPage = 1;
  private Inventory inventory;
  private boolean selectedItemsShown;

  private final Player player;
  private final List<ChestItem> items;
  private final Set<Integer> slots;
  private final Consumer<Inventory> inventoryPreparer;
  private final int numberOfPages;
  private final int pageSize;
  private final int nRows;

  private final Set<ChestItem> selectedItems;
  private final boolean[] blinkingSlots;

  private final Map<Integer, ChestItem> slotMap;

  public DisplayState(
    Player player,
    List<ChestItem> items,
    int nRows,
    Set<Integer> slots,
    Consumer<Inventory> inventoryPreparer
  ) {
    this.player = player;
    this.inventoryPreparer = inventoryPreparer;
    this.selectedItems = new HashSet<>();
    this.blinkingSlots = new boolean[nRows * 9];
    this.slotMap = new HashMap<>();
    this.items = items;

    this.pageSize = slots.size();
    this.numberOfPages = (int) Math.ceil(items.size() / (double) pageSize);

    this.nRows = nRows;
    this.slots = slots;
  }

  public boolean isInventory(Inventory other) {
    return other == inventory;
  }

  public void show() {
    // Avoid the case of the client not accepting opening the new inventory
    // and then being able to take items out of there. This way, we're safe.
    this.clearItems();

    inventory = makeInventory();
    player.openInventory(inventory);
    renderItems();
  }

  public void nextPage() {
    if (currentPage == numberOfPages)
      return;

    ++currentPage;
    show();
  }

  public void previousPage() {
    if (currentPage == 1)
      return;

    --currentPage;
    show();
  }

  public @Nullable ChestItem getItemCorrespondingToSlot(int slot) {
    return slotMap.get(slot);
  }

  private void renderItems() {
    var itemsIndex = (currentPage - 1) * pageSize;
    var numberOfItems = items.size();

    for (var slot : slots) {
      var currentSlot = itemsIndex++;

      if (currentSlot >= numberOfItems) {
        blinkingSlots[slot] = false;
        continue;
      }

      var chestItem = items.get(currentSlot);
      blinkingSlots[slot] = selectedItems.contains(chestItem);
      inventory.setItem(slot, chestItem.access());
      slotMap.put(slot, chestItem);
    }
  }

  private Inventory makeInventory() {
    var result = Bukkit.createInventory(null, 9 * nRows, "§8StorageQuery §5" + currentPage + "§8/§5" + numberOfPages);
    inventoryPreparer.accept(result);
    return result;
  }

  public boolean isSlotSelected(int slot) {
    return blinkingSlots[slot];
  }

  public void toggleSlotSelection(int slot) {
    if (!this.slotMap.containsKey(slot))
      return;

    var chestItem = getItemCorrespondingToSlot(slot);

    if (chestItem == null)
      return;

    if (blinkingSlots[slot]) {
      this.selectedItems.remove(chestItem);

      if (this.inventory != null)
        this.inventory.setItem(slot, chestItem.access());

      blinkingSlots[slot] = false;
      return;
    }

    this.selectedItems.add(chestItem);
    blinkingSlots[slot] = true;
  }

  public void forEachSelection(Consumer<ChestItem> handler) {
    for (var selectedItem : selectedItems)
      handler.accept(selectedItem);
  }

  public void tickSelectionBlink() {
    this.selectedItemsShown ^= true;

    if (inventory == null)
      return;

    for (var slot = 0; slot < inventory.getSize(); ++slot) {
      if (!blinkingSlots[slot])
        continue;

      if (!selectedItemsShown) {
        inventory.setItem(slot, null);
        continue;
      }

      var chestItem = slotMap.get(slot);

      if (chestItem != null)
        inventory.setItem(slot, chestItem.access());
    }
  }

  public void clearItems() {
    if (this.inventory != null)
      this.inventory.clear();
    this.slotMap.clear();
  }
}
