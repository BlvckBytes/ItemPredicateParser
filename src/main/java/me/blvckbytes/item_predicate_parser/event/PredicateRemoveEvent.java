package me.blvckbytes.item_predicate_parser.event;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PredicateRemoveEvent extends PredicateEvent {

  private static final HandlerList handlers = new HandlerList();

  private @Nullable PredicateAndLanguage removedPredicate;

  public PredicateRemoveEvent(Player player, Block block) {
    super(player, block);
  }

  public @Nullable PredicateAndLanguage getRemovedPredicate() {
    return removedPredicate;
  }

  public void setRemovedPredicate(@NotNull PredicateAndLanguage removedPredicate) {
    if (this.removedPredicate != null)
      throw new IllegalStateException("There was already a removed predicate set on this event-instance");

    this.removedPredicate = removedPredicate;
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
