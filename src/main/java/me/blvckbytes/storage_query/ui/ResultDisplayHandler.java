package me.blvckbytes.storage_query.ui;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

import java.net.MalformedURLException;
import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ResultDisplayHandler implements Listener {

  private static final int BACKWARDS_SLOT_ID = 45;
  private static final int FORWARDS_SLOT_ID = 53;
  private static final int INVENTORY_N_ROWS = 6;

  private static final String ARROW_LEFT_TEXTURES_URL = "http://textures.minecraft.net/texture/76ebaa41d1d405eb6b60845bb9ac724af70e85eac8a96a5544b9e23ad6c96c62";
  private static final String ARROW_RIGHT_TEXTURES_URL = "http://textures.minecraft.net/texture/8399e5da82ef7765fd5e472f3147ed118d981887730ea7bb80d7a1bed98d5ba";

  private final Map<UUID, DisplayState> stateByPlayerId;

  public ResultDisplayHandler(Plugin plugin) {
    this.stateByPlayerId = new HashMap<>();

    Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
      stateByPlayerId.values().forEach(DisplayState::tickSelectionBlink);
    }, 0, 7);
  }

  public void displayItems(Player player, List<ChestItem> items) {
    var slots = IntStream.range(0, INVENTORY_N_ROWS * 9).boxed().collect(Collectors.toSet());

    slots.remove(BACKWARDS_SLOT_ID);
    slots.remove(FORWARDS_SLOT_ID);

    var state = new DisplayState(player, items, INVENTORY_N_ROWS, slots, inv -> {
      inv.setItem(BACKWARDS_SLOT_ID, makeHead(ARROW_LEFT_TEXTURES_URL, "§8» §5Vorherige Seite §8«", true));
      inv.setItem(FORWARDS_SLOT_ID, makeHead(ARROW_RIGHT_TEXTURES_URL, "§8» §5Nächste Seite §8«", false));
    });

    stateByPlayerId.put(player.getUniqueId(), state);

    state.show();
  }

  @EventHandler
  public void onClose(InventoryCloseEvent event) {
    if (!(event.getPlayer() instanceof Player player))
      return;

    var playerId = player.getUniqueId();
    var state = stateByPlayerId.get(playerId);

    // Only remove on inventory match, as to prevent removal on title update
    if (state != null && state.isInventory(event.getInventory())) {
      stateByPlayerId.remove(playerId);
      state.forEachSelection(selectedItem -> handOutItem(player, selectedItem));
      state.clearItems();
    }
  }

  @EventHandler
  public void onQuit(PlayerQuitEvent event) {
    var state = stateByPlayerId.remove(event.getPlayer().getUniqueId());

    if (state != null)
      state.clearItems();
  }

  @EventHandler
  public void onClick(InventoryClickEvent event) {
    if (!(event.getWhoClicked() instanceof Player player))
      return;

    var state = stateByPlayerId.get(player.getUniqueId());

    if (state == null || !state.isInventory(player.getOpenInventory().getTopInventory()))
      return;

    event.setCancelled(true);

    var slot = event.getRawSlot();

    if (slot >= INVENTORY_N_ROWS * 9)
      return;

    var clickType = event.getClick();

    if (clickType == ClickType.DROP) {
      state.toggleSlotSelection(slot);
      return;
    }

    var chestItem = state.getItemCorrespondingToSlot(slot);

    if (clickType == ClickType.LEFT) {
      // Since dropping on an empty slot does not register, and the item is blinking,
      // un-selecting it may be hard, so left-clicking serves as an alternative in this mode
      if (state.isSlotSelected(slot)) {
        state.toggleSlotSelection(slot);
        return;
      }

      if (slot == BACKWARDS_SLOT_ID) {
        state.previousPage();
        return;
      }

      if (slot == FORWARDS_SLOT_ID) {
        state.nextPage();
        return;
      }

      if (chestItem == null)
        return;

      if (handOutItem(player, chestItem))
        player.closeInventory();

      return;
    }

    if (clickType == ClickType.RIGHT) {
      if (slot == BACKWARDS_SLOT_ID) {
        state.firstPage();
        return;
      }

      if (slot == FORWARDS_SLOT_ID) {
        state.lastPage();
        return;
      }

      if (chestItem == null)
        return;

      if (teleportToChest(player, chestItem))
        player.closeInventory();
    }
  }

  @EventHandler
  public void onDrag(InventoryDragEvent event) {
    if (!(event.getWhoClicked() instanceof Player player))
      return;

    var state = stateByPlayerId.get(player.getUniqueId());

    if (state == null || state.isInventory(event.getInventory()))
      return;

    event.setCancelled(true);
  }

  private boolean teleportToChest(Player player, ChestItem chestItem) {
    var chest = chestItem.chest();

    var teleportFaces = new BlockFace[] { BlockFace.UP, BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST };

    Location[] blockLocations;

    if (chest.getInventory() instanceof DoubleChestInventory doubleChest)
      blockLocations = new Location[] { doubleChest.getLeftSide().getLocation(), doubleChest.getRightSide().getLocation() };
    else
      blockLocations = new Location[] { chest.getLocation() };

    Location destination;

    for (var blockLocation : blockLocations) {
      for (var teleportFace : teleportFaces) {
        if ((destination = returnDestinationIfPassable(blockLocation, teleportFace)) != null) {
          player.teleport(destination.add(.5, .1, .5));
          return true;
        }
      }
    }

    player.sendMessage("§cStorageQuery | This chest is obstructed at all of it's faces");
    return false;
  }

  private @Nullable Location returnDestinationIfPassable(Location location, BlockFace face) {
    var destination = location.clone().add(face.getDirection());

    if (destination.getBlock().isPassable())
      return destination;

    return null;
  }

  private boolean handOutItem(Player player, ChestItem chestItem) {
    var item = chestItem.access();

    if (item == null || item.getType() == Material.AIR)
      return false;

    var remainder = InventoryUtil.addToInventory(player.getInventory(), item, true);

    if (remainder != 0) {
      player.sendMessage("§cStorageQuery | Your inventory can only hold " + remainder + " too few of the item " + item.getType().name() + " (x" + item.getAmount() + ")");
      return false;
    }

    chestItem.chest().getInventory().setItem(chestItem.slot(), null);
    player.sendMessage("§aStorageQuery | Handed out item " + item.getType().name() + " (x" + item.getAmount() + ")");
    return true;
  }

  private ItemStack makeHead(String texturesValue, String displayName, boolean isBackwards) {
    var result = new ItemStack(Material.PLAYER_HEAD);

    if(result.getItemMeta() instanceof SkullMeta meta) {
      var profile = Bukkit.createPlayerProfile(UUID.randomUUID());
      var textures = profile.getTextures();

      try {
        textures.setSkin(URI.create(texturesValue).toURL());
      } catch (MalformedURLException ignored) {}

      profile.setTextures(textures);
      meta.setOwnerProfile(profile);
      meta.setDisplayName(displayName);

      meta.setLore(List.of(
        " ",
        "§8§m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━",
        "§7Aktionen für die Suchergebnisse",
        "§8➥ §dLinksklick §7Gewähltes Item anfordern",
        "§8➥ §dRechtsklick §7Zur Truhe teleportieren",
        "§8➥ §dDroppen §7Mehrfachauswahl umschalten",
        "§8§m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━",
        "§7Aktionen für den Seitenwechsel",
        "§8➥ §dLinksklick §7Eine Seite " + (isBackwards ? "zurück" : "vorwärts"),
        "§8➥ §dRechtsklick §7Bis zum " + (isBackwards ? "Anfang" : "Ende")
      ));

      result.setItemMeta(meta);
    }

    return result;
  }
}
