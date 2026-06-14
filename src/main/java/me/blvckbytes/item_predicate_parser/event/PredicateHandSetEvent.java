package me.blvckbytes.item_predicate_parser.event;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class PredicateHandSetEvent extends PredicateHandEvent {

  private static final HandlerList handlers = new HandlerList();

  private final boolean all;
  private final @NotNull PredicateAndLanguage value;
  private int encounteredStacks;

  public PredicateHandSetEvent(Player player, int heldSlot, boolean all, @NotNull PredicateAndLanguage value) {
    super(player, heldSlot);

    this.all = all;
    this.value = value;
  }

  public void setEncounteredStacks(int encounteredStacks) {
    this.encounteredStacks = encounteredStacks;
  }

  public int getEncounteredStacks() {
    return encounteredStacks;
  }

  public boolean isAll() {
    return all;
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
