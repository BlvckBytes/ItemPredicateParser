package me.blvckbytes.item_predicate_parser.predicate;

import me.blvckbytes.item_predicate_parser.predicate.stringify.StringifyHandler;
import me.blvckbytes.item_predicate_parser.token.Token;
import me.blvckbytes.item_predicate_parser.translation.TranslatedLangKeyed;
import me.blvckbytes.item_predicate_parser.translation.keyed.LangKeyedItemMaterial;
import org.bukkit.Material;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.List;
import java.util.Objects;

public record MaterialPredicate(
  Token token,
  @Nullable TranslatedLangKeyed<LangKeyedItemMaterial> translatedLangKeyed,
  @Nullable List<Material> materials
) implements ItemPredicate {

  @Override
  public @Nullable ItemPredicate testForFailure(PredicateState state) {
    if (translatedLangKeyed != null) {
      if (translatedLangKeyed.langKeyed.getWrapped().equals(state.item.getType()))
        return null;

      return this;
    }

    if (materials == null)
      return this;

    for (var material : materials) {
      if (material.equals(state.item.getType()))
        return null;
    }

    return this;
  }

  @Override
  public void stringify(StringifyHandler handler) {
    handler.stringify(this, output -> {
      if (handler.useTokens() || translatedLangKeyed == null)
        output.appendString(token.stringify());
      else
        output.appendString(translatedLangKeyed.normalizedPrefixedTranslation);
    });
  }

  @Override
  public boolean containsOrEqualsPredicate(ItemPredicate node, EnumSet<ComparisonFlag> comparisonFlags) {
    return _equals(node, comparisonFlags);
  }

  @Override
  public boolean equals(Object other) {
    return _equals(other, EnumSet.noneOf(ComparisonFlag.class));
  }

  private boolean _equals(Object other, EnumSet<ComparisonFlag> comparisonFlags) {
    if (!(other instanceof MaterialPredicate otherPredicate))
      return false;

    if (comparisonFlags.contains(ComparisonFlag.MATERIAL_PREDICATE__INTERSECTION_SUFFICES))
      return doesIntersectWith(otherPredicate);

    if (this.translatedLangKeyed != null) {
      if (otherPredicate.translatedLangKeyed == null)
        return false;

      return this.translatedLangKeyed.equals(otherPredicate.translatedLangKeyed);
    }

    return Objects.equals(this.materials, otherPredicate.materials);
  }

  private boolean doesIntersectWith(MaterialPredicate otherPredicate) {
    if (this.translatedLangKeyed != null) {
      if (otherPredicate.translatedLangKeyed != null)
        return this.translatedLangKeyed.equals(otherPredicate.translatedLangKeyed);

      if (otherPredicate.materials != null)
        return otherPredicate.materials.contains(this.translatedLangKeyed.langKeyed.getWrapped());

      return false;
    }

    if (this.materials == null)
      return false;

    if (otherPredicate.translatedLangKeyed != null)
      return this.materials.contains(otherPredicate.translatedLangKeyed.langKeyed.getWrapped());

    if (otherPredicate.materials == null)
      return false;

    return this.materials.stream().anyMatch(otherPredicate.materials::contains);
  }
}
