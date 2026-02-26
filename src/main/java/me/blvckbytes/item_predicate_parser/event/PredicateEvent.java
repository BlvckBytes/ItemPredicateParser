package me.blvckbytes.item_predicate_parser.event;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

public abstract class PredicateEvent extends Event {

  private final Player player;
  private final Block block;

  private boolean acknowledged;

  private @Nullable Block deniedAccessBlock;
  private @Nullable Block dataHoldingBlock;

  public PredicateEvent(Player player, Block block) {
    this.player = player;
    this.block = block;
  }

  public Player getPlayer() {
    return player;
  }

  public Block getBlock() {
    return block;
  }

  public boolean isAcknowledged() {
    return acknowledged;
  }

  public void acknowledge() {
    if (acknowledged)
      throw new IllegalStateException("This event-instance was already acknowledged");

    acknowledged = true;
  }

  public @Nullable Block getDeniedAccessBlock() {
    return deniedAccessBlock;
  }

  public void setDeniedAccessBlock(@Nullable Block deniedAccessBlock) {
    this.deniedAccessBlock = deniedAccessBlock;
  }

  public @Nullable Block getDataHoldingBlock() {
    return dataHoldingBlock;
  }

  public void setDataHoldingBlock(@Nullable Block dataHoldingBlock) {
    this.dataHoldingBlock = dataHoldingBlock;
  }
}
