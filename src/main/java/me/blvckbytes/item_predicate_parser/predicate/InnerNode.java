package me.blvckbytes.item_predicate_parser.predicate;

import me.blvckbytes.item_predicate_parser.predicate.stringify.StringifyHandler;
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

public abstract class InnerNode implements UnaryNode {

  private static final List<ItemStack> AIR_LIST;

  static {
    AIR_LIST = Collections.singletonList(ItemPredicate.AIR_ITEM);
  }

  protected final Token token;
  protected final TranslatedLangKeyed<?> translatedLangKeyed;
  private final ItemPredicate operand;
  private final InnerMode mode;
  private final boolean allowSelf;

  protected InnerNode(
    Token token,
    TranslatedLangKeyed<?> translatedLangKeyed,
    ItemPredicate operand,
    InnerMode mode,
    boolean allowSelf
  ) {
    this.token = token;
    this.translatedLangKeyed = translatedLangKeyed;
    this.operand = operand;
    this.mode = mode;
    this.allowSelf = allowSelf;
  }

  @Override
  public @Nullable ItemPredicate testForFailure(PredicateState state) {
    var innerItems = getInnerItems(state.getMeta());

    if (innerItems == null) {
      if (allowSelf && operand.test(state.item))
        return null;

      // Mismatch if the item does not even have any inner items
      return this;
    }

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
  public void stringify(StringifyHandler handler) {
    handler.stringify(this, output -> {
      if (handler.useTokens())
        output.appendString(token.stringify());
      else
        output.appendString(translatedLangKeyed.normalizedPrefixedTranslation);

      if (!(operand instanceof ParenthesesNode))
        output.appendSpace();
    });

    operand.stringify(handler);
  }

  @Override
  public ItemPredicate getOperand() {
    return operand;
  }

  @Override
  public boolean equals(Object other) {
    if (!(other instanceof InnerNode otherPredicate))
      return false;

    if (this.mode != otherPredicate.mode)
      return false;

    if (this.allowSelf != otherPredicate.allowSelf)
      return false;

    return this.operand.equals(otherPredicate.operand);
  }

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
