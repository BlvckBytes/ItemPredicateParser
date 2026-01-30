package me.blvckbytes.item_predicate_parser.display.overview;

import at.blvckbytes.cm_mapper.ConfigKeeper;
import me.blvckbytes.item_predicate_parser.config.MainSection;
import me.blvckbytes.item_predicate_parser.display.DisplayHandler;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.plugin.Plugin;

public class VariablesDisplayHandler extends DisplayHandler<VariablesDisplay, VariablesDisplayData> {

  public VariablesDisplayHandler(
    ConfigKeeper<MainSection> config,
    Plugin plugin
  ) {
    super(config, plugin);
  }

  @Override
  public VariablesDisplay instantiateDisplay(Player player, VariablesDisplayData displayData) {
    return new VariablesDisplay(config, plugin, player, displayData);
  }

  @Override
  protected void handleClick(Player player, VariablesDisplay display, ClickType clickType, int slot) {
    if (clickType == ClickType.LEFT) {
      if (config.rootSection.variablesDisplay.items.previousPage.getDisplaySlots().contains(slot)) {
        display.previousPage();
        return;
      }

      if (config.rootSection.variablesDisplay.items.nextPage.getDisplaySlots().contains(slot)) {
        display.nextPage();
        return;
      }

      return;
    }

    if (clickType == ClickType.RIGHT) {
      if (config.rootSection.variablesDisplay.items.previousPage.getDisplaySlots().contains(slot)) {
        display.firstPage();
        return;
      }

      if (config.rootSection.variablesDisplay.items.nextPage.getDisplaySlots().contains(slot))
        display.lastPage();
    }
  }
}
