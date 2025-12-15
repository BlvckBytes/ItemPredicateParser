package me.blvckbytes.item_predicate_parser.predicate;

import me.blvckbytes.item_predicate_parser.token.IntegerToken;
import me.blvckbytes.item_predicate_parser.token.Token;
import me.blvckbytes.item_predicate_parser.translation.TranslatedLangKeyed;
import me.blvckbytes.item_predicate_parser.translation.keyed.LangKeyedEnchantment;
import org.bukkit.enchantments.Enchantment;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public record EnchantmentPredicate(
  Token token,
  TranslatedLangKeyed<LangKeyedEnchantment> translatedLangKeyed,
  @Nullable IntegerToken levelArgument
) implements ItemPredicate {

  @Override
  public @Nullable ItemPredicate testForFailure(PredicateState state) {
    for (var entryIterator = state.getEnchantments().iterator(); entryIterator.hasNext();) {
      var entry = entryIterator.next();

      if (doesEnchantmentMatch(entry.getKey(), entry.getValue())) {
        if (state.isExactMode)
          entryIterator.remove();

        return null;
      }
    }

    return this;
  }

  private boolean doesEnchantmentMatch(Enchantment enchantment, int level) {
    if (!translatedLangKeyed.langKeyed.getWrapped().equals(enchantment))
      return false;

    return this.levelArgument == null || this.levelArgument.matches(level);
  }

  @Override
  public void stringify(StringifyState state) {
    if (state.useTokens)
      state.appendString(token.stringify());
    else
      state.appendString(translatedLangKeyed.normalizedPrefixedTranslation);

    if (this.levelArgument != null) {
      state.appendSpace();
      state.appendString(this.levelArgument.stringify());
    }
  }

  @Override
  public boolean isTransitiveParentTo(ItemPredicate node) {
    return false;
  }
}
