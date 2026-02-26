package me.blvckbytes.item_predicate_parser.event;

import me.blvckbytes.item_predicate_parser.parse.ItemPredicateParseException;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PredicateGetEvent extends PredicateEvent {

  private static final HandlerList handlers = new HandlerList();

  private @Nullable PredicateAndLanguage result;
  private @Nullable ItemPredicateParseException error;

  public PredicateGetEvent(Player player, Block block) {
    super(player, block);
  }

  public @Nullable ItemPredicateParseException getError() {
    return error;
  }

  public @Nullable PredicateAndLanguage getResult() {
    return result;
  }

  public void setError(@Nullable ItemPredicateParseException error) {
    if (this.result != null)
      throw new IllegalStateException("There was already a result set on this event-instance");

    if (this.error != null)
      throw new IllegalStateException("There was already an error set on this event-instance");

    this.error = error;
  }

  public void setResult(@NotNull PredicateAndLanguage result) {
    if (this.result != null)
      throw new IllegalStateException("There was already a result set on this event-instance");

    if (this.error != null)
      throw new IllegalStateException("There was already an error set on this event-instance");

    this.result = result;
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
