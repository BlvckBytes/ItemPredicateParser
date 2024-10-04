package me.blvckbytes.item_predicate_parser;

import org.bukkit.entity.Player;

public enum PluginPermission {
  IPP_RELOAD_COMMAND("command.ipp.reload"),
  IPP_TEST_COMMAND("command.ipp.test"),
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

