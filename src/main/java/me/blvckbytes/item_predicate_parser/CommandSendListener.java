package me.blvckbytes.item_predicate_parser;

import at.blvckbytes.cm_mapper.ConfigKeeper;
import me.blvckbytes.item_predicate_parser.config.MainSection;
import org.bukkit.command.Command;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandSendEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public class CommandSendListener implements Listener {

  private final String lowerPluginName;
  private final Command ippCommand;

  public CommandSendListener(JavaPlugin plugin, ConfigKeeper<MainSection> config) {
    this.lowerPluginName = plugin.getName().toLowerCase();
    this.ippCommand = Objects.requireNonNull(plugin.getCommand(config.rootSection.commands.itemPredicateParser.evaluatedName));
  }

  @EventHandler
  public void onCommandSend(PlayerCommandSendEvent event) {
    var player = event.getPlayer();

    if (PluginPermission.IPP_TEST_COMMAND.has(player) || PluginPermission.IPP_RELOAD_COMMAND.has(player))
      return;

    event.getCommands().remove(ippCommand.getName());
    event.getCommands().remove(lowerPluginName + ":" + ippCommand.getName());

    for (var alias : ippCommand.getAliases()) {
      event.getCommands().remove(alias);
      event.getCommands().remove(lowerPluginName + ":" + alias);
    }
  }
}
