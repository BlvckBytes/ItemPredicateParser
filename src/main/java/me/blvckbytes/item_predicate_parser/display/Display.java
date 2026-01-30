package me.blvckbytes.item_predicate_parser.display;

import at.blvckbytes.cm_mapper.ConfigKeeper;
import me.blvckbytes.item_predicate_parser.config.MainSection;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.Plugin;

public abstract class Display<DisplayDataType> {

  protected final Player player;
  protected final ConfigKeeper<MainSection> config;
  protected final Plugin plugin;
  public final DisplayDataType displayData;
  protected Inventory inventory;

  protected Display(
    Player player,
    DisplayDataType displayData,
    ConfigKeeper<MainSection> config,
    Plugin plugin
  ) {
    this.player = player;
    this.displayData = displayData;
    this.config = config;
    this.plugin = plugin;
  }

  public void show() {
    var priorInventory = inventory;
    inventory = makeInventory();

    renderItems();

    Bukkit.getScheduler().runTask(plugin, () -> {
      // Make sure to open the newly rendered inventory first as to avoid flicker
      player.openInventory(inventory);

      // Avoid the case of the client not accepting opening the new inventory
      // and then being able to take items out of there. This way, we're safe.
      if (priorInventory != null)
        priorInventory.clear();
    });
  }

  protected abstract void renderItems();

  protected abstract Inventory makeInventory();

  public abstract void onConfigReload();

  public void onInventoryClose() {
    if (this.inventory != null)
      this.inventory.clear();
  }

  public void onShutdown() {
    if (inventory != null)
      inventory.clear();

    if (player.getOpenInventory().getTopInventory() == inventory)
      player.closeInventory();
  }

  public boolean isInventory(Inventory inventory) {
    return this.inventory == inventory;
  }
}
