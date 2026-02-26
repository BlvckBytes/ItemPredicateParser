package me.blvckbytes.item_predicate_parser.command;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.function.Consumer;

public class PredicateInteractionSession {

  public final Player player;
  public final Consumer<Block> interactionHandler;
  public boolean allowMultiUse;
  private long lastUse;

  public PredicateInteractionSession(Player player, Consumer<Block> interactionHandler) {
    this.player = player;
    this.interactionHandler = interactionHandler;

    touchExpiry();
  }

  public void touchExpiry() {
    lastUse = System.currentTimeMillis();
  }

  public boolean isExpired(int expirySeconds) {
    return System.currentTimeMillis() - lastUse >= expirySeconds * 1000L;
  }
}
