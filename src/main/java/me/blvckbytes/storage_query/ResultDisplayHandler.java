package me.blvckbytes.storage_query;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

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

  public ResultDisplayHandler() {
    this.stateByPlayerId = new HashMap<>();
  }

  public void displayItems(Player player, List<ChestItem> items) {
    var slots = IntStream.range(0, INVENTORY_N_ROWS * 9).boxed().collect(Collectors.toSet());

    slots.remove(BACKWARDS_SLOT_ID);
    slots.remove(FORWARDS_SLOT_ID);

    var state = new DisplayState(player, items, INVENTORY_N_ROWS, slots, inv -> {
      inv.setItem(BACKWARDS_SLOT_ID, makeHead(ARROW_LEFT_TEXTURES_URL, "§aBackwards"));
      inv.setItem(FORWARDS_SLOT_ID, makeHead(ARROW_RIGHT_TEXTURES_URL, "§aForwards"));
    });

    stateByPlayerId.put(player.getUniqueId(), state);

    state.show();
  }

  @EventHandler
  public void onClose(InventoryCloseEvent event) {
    var playerId = event.getPlayer().getUniqueId();
    var state = stateByPlayerId.get(playerId);

    // Only remove on inventory match, as to prevent removal on title update
    if (state != null && state.isInventory(event.getInventory()))
      stateByPlayerId.remove(playerId);
  }

  @EventHandler
  public void onClick(InventoryClickEvent event) {
    if (!(event.getWhoClicked() instanceof Player player))
      return;

    var state = stateByPlayerId.get(player.getUniqueId());

    if (state == null || !state.isInventory(event.getClickedInventory()))
      return;

    event.setCancelled(true);

    var slot = event.getSlot();

    if (slot == BACKWARDS_SLOT_ID) {
      state.previousPage();
      return;
    }

    if (slot == FORWARDS_SLOT_ID) {
      state.nextPage();
      return;
    }

    var chestItem = state.getItemCorrespondingToSlot(slot);

    if (chestItem == null)
      return;

    player.closeInventory();

    var item = chestItem.access();

    if (item != null) {
      chestItem.chest().getInventory().setItem(chestItem.slot(), null);
      player.getWorld().dropItem(player.getEyeLocation(), item);
      player.sendMessage("§aStorageQuery | Handed out item " + item.getType().name());
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

  private ItemStack makeHead(String texturesValue, String displayName) {
    var result = new ItemStack(Material.PLAYER_HEAD);

    if(result.getItemMeta() instanceof SkullMeta meta) {
      var profile = Bukkit.createPlayerProfile(UUID.randomUUID());
      var textures = profile.getTextures();

      try {
        textures.setSkin(URI.create(texturesValue).toURL());
      } catch (MalformedURLException e) {
        throw new RuntimeException(e);
      }

      profile.setTextures(textures);
      meta.setOwnerProfile(profile);
      meta.setDisplayName(displayName);
      result.setItemMeta(meta);
    }

    return result;
  }
}
