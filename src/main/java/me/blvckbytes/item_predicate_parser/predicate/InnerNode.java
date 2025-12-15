package me.blvckbytes.item_predicate_parser.predicate;

import me.blvckbytes.item_predicate_parser.token.Token;
import me.blvckbytes.item_predicate_parser.translation.TranslatedLangKeyed;
import org.bukkit.block.Container;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.BundleMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public abstract class InnerNode implements ItemPredicate {

  private static final List<ItemStack> AIR_LIST;

  static {
    AIR_LIST = Collections.singletonList(ItemPredicate.AIR_ITEM);
  }

  private final Token token;
  private final TranslatedLangKeyed<?> translatedLangKeyed;
  private final ItemPredicate operand;
  private final InnerMode mode;

  protected InnerNode(Token token, TranslatedLangKeyed<?> translatedLangKeyed, ItemPredicate operand, InnerMode mode) {
    this.token = token;
    this.translatedLangKeyed = translatedLangKeyed;
    this.operand = operand;
    this.mode = mode;
  }

  @Override
  public @Nullable ItemPredicate testForFailure(PredicateState state) {
    var innerItems = getInnerItems(state.meta);

    // Mismatch if the item does not even have any inner items
    if (innerItems == null)
      return this;

    if (mode == InnerMode.SOME) {
      if (innerItems.stream().anyMatch(operand))
        return null;

      return this;
    }

    if (mode == InnerMode.ALL) {
      if (innerItems.stream().allMatch(operand))
        return null;

      return this;
    }

    return null;
  }

  @Override
  public void stringify(StringifyState state) {
    if (state.useTokens)
      state.appendString(token.stringify());
    else
      state.appendString(translatedLangKeyed.normalizedPrefixedTranslation);

    if (!(operand instanceof ParenthesesNode))
      state.appendSpace();

    state.appendPredicate(operand);
  }

  @Override
  public boolean isTransitiveParentTo(ItemPredicate node) {
    return operand == node || operand.isTransitiveParentTo(node);
  }

  @SuppressWarnings("UnstableApiUsage")
  private List<ItemStack> getInnerItems(ItemMeta meta) {
    if (meta instanceof BlockStateMeta blockStateMeta) {
      if (blockStateMeta.getBlockState() instanceof Container container)
        return Arrays.asList(container.getInventory().getContents());

      return null;
    }

    if (meta instanceof BundleMeta bundleMeta) {
      var bundleItems = bundleMeta.getItems();
      return bundleItems.isEmpty() ? AIR_LIST : bundleItems;
    }

    return null;
  }
}
