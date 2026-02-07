package me.blvckbytes.item_predicate_parser.predicate;

import me.blvckbytes.item_predicate_parser.predicate.stringify.StringifyHandler;
import me.blvckbytes.item_predicate_parser.token.IntegerToken;
import me.blvckbytes.item_predicate_parser.token.Token;
import me.blvckbytes.item_predicate_parser.translation.TranslatedLangKeyed;
import me.blvckbytes.item_predicate_parser.translation.keyed.LangKeyedEnchantment;
import org.bukkit.enchantments.Enchantment;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

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
  public void stringify(StringifyHandler handler) {
    handler.stringify(this, output -> {
      if (handler.useTokens())
        output.appendString(token.stringify());
      else
        output.appendString(translatedLangKeyed.normalizedPrefixedTranslation);

      if (this.levelArgument != null) {
        output.appendSpace();
        output.appendString(this.levelArgument.stringify());
      }
    });
  }

  @Override
  public boolean equals(Object other) {
    if (!(other instanceof EnchantmentPredicate otherPredicate))
      return false;

    if (!this.translatedLangKeyed.equals(otherPredicate.translatedLangKeyed))
      return false;

    return Objects.equals(levelArgument, otherPredicate.levelArgument);
  }
}
