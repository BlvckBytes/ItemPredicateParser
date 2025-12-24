package me.blvckbytes.item_predicate_parser.predicate;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class PredicateState {

  public final ItemStack item;

  // Caching the meta spares the need to access it over and over again, which internally causes useless allocations
  private @Nullable ItemMeta _meta;
  private boolean didGetMeta;

  public final boolean isExactMode;

  private List<Map.Entry<Enchantment, Integer>> remainingEnchantments = null;
  private List<PotionEffect> remainingEffects = null;

  public PredicateState(ItemStack item) {
    this.item = item;
    this.isExactMode = false;
  }

  public @Nullable ItemMeta getMeta() {
    if (!didGetMeta) {
      didGetMeta = true;
      _meta = item.getItemMeta();
    }

    return _meta;
  }

  private PredicateState(ItemStack item, @Nullable ItemMeta _meta, boolean didGetMeta, boolean isExactMode) {
    this.item = item;
    this._meta = _meta;
    this.didGetMeta = didGetMeta;
    this.isExactMode = isExactMode;
  }

  public boolean hasRemains() {
    if (this.remainingEnchantments != null && !this.remainingEnchantments.isEmpty())
      return true;

    return this.remainingEffects != null && !this.remainingEffects.isEmpty();
  }

  public PredicateState copyAndEnterExact() {
    var state = new PredicateState(this.item, this._meta, this.didGetMeta, true);

    // Deeper exact-nodes should not influence the remaining state of their parents

    if (this.remainingEnchantments != null)
      state.remainingEnchantments = new ArrayList<>(this.remainingEnchantments);

    if (this.remainingEffects != null)
      state.remainingEffects = new ArrayList<>(remainingEffects);

    return state;
  }

  // Lazily access enchantments and effects to speed up simple predicates

  public List<Map.Entry<Enchantment, Integer>> getEnchantments() {
    var meta = getMeta();

    if (meta == null)
      return List.of();

    if (this.remainingEnchantments == null) {
      if (meta instanceof EnchantmentStorageMeta enchantmentStorageMeta)
        this.remainingEnchantments = new ArrayList<>(enchantmentStorageMeta.getStoredEnchants().entrySet());
      else
        this.remainingEnchantments = new ArrayList<>(meta.getEnchants().entrySet());
    }

    return remainingEnchantments;
  }

  public List<PotionEffect> getEffects() {
    if (this.remainingEffects == null) {
      if (!(getMeta() instanceof PotionMeta potionMeta))
        return List.of();

      var baseType = potionMeta.getBasePotionType();

      if (baseType != null)
        this.remainingEffects = new ArrayList<>(baseType.getPotionEffects());

      if (this.remainingEffects != null)
        this.remainingEffects.addAll(potionMeta.getCustomEffects());
      else
        this.remainingEffects = new ArrayList<>(potionMeta.getCustomEffects());
    }

    return this.remainingEffects;
  }
}
