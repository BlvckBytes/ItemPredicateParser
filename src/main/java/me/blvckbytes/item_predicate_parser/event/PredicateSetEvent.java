package me.blvckbytes.item_predicate_parser.event;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class PredicateSetEvent extends PredicateEvent {

  private static final HandlerList handlers = new HandlerList();

  private final @NotNull PredicateAndLanguage value;

  public PredicateSetEvent(Player player, Block block, @NotNull PredicateAndLanguage value) {
    super(player, block);

    this.value = value;
  }

  public @NotNull PredicateAndLanguage getValue() {
    return value;
  }

  @Override
  @NotNull
  public HandlerList getHandlers() {
    return handlers;
  }

  @NotNull
  public static HandlerList getHandlerList() {
    return handlers;
  }
}
