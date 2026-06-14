package me.blvckbytes.item_predicate_parser.event;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PredicateHandRemoveEvent extends PredicateHandEvent {

  private static final HandlerList handlers = new HandlerList();

  private final boolean all;
  private @Nullable List<PredicateAndLanguage> removedPredicates;
  private int encounteredStacks;

  public PredicateHandRemoveEvent(Player player, int heldSlot, boolean all) {
    super(player, heldSlot);

    this.all = all;
  }

  public int getEncounteredStacks() {
    return encounteredStacks;
  }

  public void setEncounteredStacks(int encounteredStacks) {
    this.encounteredStacks = encounteredStacks;
  }

  public boolean isAll() {
    return all;
  }

  public @Nullable List<PredicateAndLanguage> getRemovedPredicates() {
    return removedPredicates;
  }

  public void setRemovedPredicates(@NotNull List<PredicateAndLanguage> removedPredicates) {
    if (this.removedPredicates != null)
      throw new IllegalStateException("There was already a list of removed predicates set on this event-instance");

    this.removedPredicates = removedPredicates;
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
