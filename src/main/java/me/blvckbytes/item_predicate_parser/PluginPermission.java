package me.blvckbytes.item_predicate_parser;

import org.bukkit.entity.Player;

public enum PluginPermission {
  IPP_RELOAD_COMMAND("command.ipp.reload"),
  IPP_VARIABLES_COMMAND("command.ipp.variables"),
  IPP_TEST_COMMAND("command.ipp.test"),
  IPP_LANGUAGE_COMMAND("command.ipp.language"),
  IPP_GET_COMMAND("command.ipp.get"),
  IPP_SET_COMMAND("command.ipp.set"),
  IPP_REMOVE_COMMAND("command.ipp.remove"),
  ;

  private static final String PREFIX = "itempredicateparser";
  private final String node;

  PluginPermission(String node) {
    this.node = PREFIX + "." + node;
  }

  public boolean has(Player player) {
    return player.hasPermission(node);
  }
}

