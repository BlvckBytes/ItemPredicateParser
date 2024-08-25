package me.blvckbytes.storage_query;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;

public class DisplayState {

  private int currentPage = 1;
  private Inventory inventory;

  private final Player player;
  private final List<ChestItem> items;
  private final Set<Integer> slots;
  private final Consumer<Inventory> inventoryPreparer;
  private final int numberOfPages;
  private final int pageSize;
  private final int nRows;

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
    inventory = makeInventory();
    player.openInventory(inventory);
    renderItems();
  }

  public void nextPage() {
    if (currentPage == numberOfPages)
      return;

    ++currentPage;
    onPageChange();
  }

  public void previousPage() {
    if (currentPage == 1)
      return;

    --currentPage;
    onPageChange();
  }

  public @Nullable ChestItem getItemCorrespondingToSlot(int slot) {
    return slotMap.get(slot);
  }

  private void onPageChange() {
    show();
    renderItems();
  }

  private void renderItems() {
    slotMap.clear();

    var itemsIndex = (currentPage - 1) * pageSize;
    var numberOfItems = items.size();

    for (var slot : slots) {
      var currentSlot = itemsIndex++;

      if (currentSlot >= numberOfItems)
        break;

      var chestItem = items.get(currentSlot);
      inventory.setItem(slot, chestItem.access());
      slotMap.put(slot, chestItem);
    }
  }

  private Inventory makeInventory() {
    var result = Bukkit.createInventory(null, 9 * nRows, "§8StorageQuery " + currentPage + "/" + numberOfPages);
    inventoryPreparer.accept(result);
    return result;
  }
}
