package me.blvckbytes.item_predicate_parser.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;

public abstract class PredicateHandEvent extends Event {

  private final Player player;
  private final int heldSlot;

  private boolean acknowledged;

  public PredicateHandEvent(Player player, int heldSlot) {
    this.player = player;
    this.heldSlot = heldSlot;
  }

  public Player getPlayer() {
    return player;
  }

  public int getHeldSlot() {
    return heldSlot;
  }

  public boolean isAcknowledged() {
    return acknowledged;
  }

  public void acknowledge() {
    if (acknowledged)
      throw new IllegalStateException("This event-instance was already acknowledged");

    acknowledged = true;
  }
}
