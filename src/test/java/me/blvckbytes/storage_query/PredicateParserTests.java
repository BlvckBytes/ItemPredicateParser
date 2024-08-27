package me.blvckbytes.storage_query;

import me.blvckbytes.storage_query.parse.ArgumentParseException;
import me.blvckbytes.storage_query.parse.ParseConflict;
import me.blvckbytes.storage_query.parse.PredicateParser;
import me.blvckbytes.storage_query.parse.TokenParser;
import me.blvckbytes.storage_query.predicate.*;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.potion.PotionEffectType;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.Consumer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

public class PredicateParserTests extends TranslationRegistryDependentTests {

  @Test
  public void shouldParseSimpleSinglePredicates() {

    // ================================================================================
    // Material
    // ================================================================================

    parse(new String[]{ "dia-ches" }, 1)
      .validate(0, MaterialPredicate.class, it -> {
        assertEquals(Material.DIAMOND_CHESTPLATE, it.materials().getFirst());
      });

    // ================================================================================
    // Potion Effect
    // ================================================================================

    parse(new String[] { "regen" }, 1)
      .validate(0, PotionEffectPredicate.class, it -> {
        assertEquals(PotionEffectType.REGENERATION, it.type());
        assertNull(it.amplifierArgument());
        assertNull(it.durationArgument());
      });

    parse(new String[] { "regen", "2" }, 1)
      .validate(0, PotionEffectPredicate.class, it -> {
        assertEquals(PotionEffectType.REGENERATION, it.type());
        assertEquals(2, it.amplifierArgument().value());
        assertNull(it.durationArgument());
      });

    parse(new String[] { "regen", "*" }, 1)
      .validate(0, PotionEffectPredicate.class, it -> {
        assertEquals(PotionEffectType.REGENERATION, it.type());
        assertNull(it.amplifierArgument().value());
        assertNull(it.durationArgument());
      });

    parse(new String[] { "regen", "2", "1600" }, 1)
      .validate(0, PotionEffectPredicate.class, it -> {
        assertEquals(PotionEffectType.REGENERATION, it.type());
        assertEquals(2, it.amplifierArgument().value());
        assertEquals(1600, it.durationArgument().value());
      });

    parse(new String[] { "regen", "*", "1600" }, 1)
      .validate(0, PotionEffectPredicate.class, it -> {
        assertEquals(PotionEffectType.REGENERATION, it.type());
        assertNull(it.amplifierArgument().value());
        assertEquals(1600, it.durationArgument().value());
      });

    parse(new String[] { "regen", "2", "*" }, 1)
      .validate(0, PotionEffectPredicate.class, it -> {
        assertEquals(PotionEffectType.REGENERATION, it.type());
        assertEquals(2, it.amplifierArgument().value());
        assertNull(it.durationArgument().value());
      });

    // ================================================================================
    // Enchantment
    // ================================================================================

    parse(new String[] { "thorn" }, 1)
      .validate(0, EnchantmentPredicate.class, it -> {
        assertEquals(Enchantment.THORNS.key(), it.enchantment().key());
        assertNull(it.levelArgument());
      });

    parse(new String[] { "thorn", "2" }, 1)
      .validate(0, EnchantmentPredicate.class, it -> {
        assertEquals(Enchantment.THORNS.key(), it.enchantment().key());
        assertEquals(2, it.levelArgument().value());
      });

    parse(new String[] { "thorn", "*" }, 1)
      .validate(0, EnchantmentPredicate.class, it -> {
        assertEquals(Enchantment.THORNS.key(), it.enchantment().key());
        assertNull(it.levelArgument().value());
      });

    // ================================================================================
    // Text Search
    // ================================================================================

    parse(new String[] { "\"a", "long", "search\"" }, 1)
      .validate(0, TextSearchPredicate.class, it -> {
        assertEquals("a long search", it.text);
      });

    // ================================================================================
    // Deterioration
    // ================================================================================

    parse(new String[] { "deter" }, 1)
      .validate(0, DeteriorationPredicate.class, it -> {
        assertNull(it.deteriorationPercentageMin());
        assertNull(it.deteriorationPercentageMax());
      });

    parse(new String[] { "deter", "5" }, 1)
      .validate(0, DeteriorationPredicate.class, it -> {
        assertEquals(5, it.deteriorationPercentageMin().value());
        assertNull(it.deteriorationPercentageMax());
      });

    parse(new String[] { "deter", "*" }, 1)
      .validate(0, DeteriorationPredicate.class, it -> {
        assertNull(it.deteriorationPercentageMin().value());
        assertNull(it.deteriorationPercentageMax());
      });

    parse(new String[] { "deter", "5", "85" }, 1)
      .validate(0, DeteriorationPredicate.class, it -> {
        assertEquals(5, it.deteriorationPercentageMin().value());
        assertEquals(85, it.deteriorationPercentageMax().value());
      });

    parse(new String[] { "deter", "*", "85" }, 1)
      .validate(0, DeteriorationPredicate.class, it -> {
        assertNull(it.deteriorationPercentageMin().value());
        assertEquals(85, it.deteriorationPercentageMax().value());
      });

    parse(new String[] { "deter", "5", "*" }, 1)
      .validate(0, DeteriorationPredicate.class, it -> {
        assertEquals(5, it.deteriorationPercentageMin().value());
        assertNull(it.deteriorationPercentageMax().value());
      });
  }

  @Test
  public void shouldParseComplexMultiplePredicates() {
    parse(new String[]{
      "deter", "3",
      "unbr", "*",
      "\"text a\"",
      "regen", "4", "*",
      "dia-pick",
      "\"multi", "arg", "text", "b", "\""
    }, 6)
      .validate(0, DeteriorationPredicate.class, it -> {
        assertEquals(3, it.deteriorationPercentageMin().value());
        assertNull(it.deteriorationPercentageMax());
      })
      .validate(1, EnchantmentPredicate.class, it -> {
        assertEquals(Enchantment.UNBREAKING.key(), it.enchantment().key());
        assertNull(it.levelArgument().value());
      })
      .validate(2, TextSearchPredicate.class, it -> {
        assertEquals("text a", it.text);
      })
      .validate(3, PotionEffectPredicate.class, it -> {
        assertEquals(PotionEffectType.REGENERATION, it.type());
        assertEquals(4, it.amplifierArgument().value());
        assertNull(it.durationArgument().value());
      })
      .validate(4, MaterialPredicate.class, it -> {
        assertEquals(Material.DIAMOND_PICKAXE, it.materials().getFirst());
      })
      .validate(5, TextSearchPredicate.class, it -> {
        assertEquals("multi arg text b ", it.text);
      });
  }

  @Test
  public void onlyEffectDurationShouldAcceptTimeNotation() {
    parse(new String[] { "regen", "2", "2:30" }, 1)
      .validate(0, PotionEffectPredicate.class, it -> {
        assertEquals(PotionEffectType.REGENERATION, it.type());
        assertEquals(2, it.amplifierArgument().value());
        assertEquals(60*2 + 30, it.durationArgument().value());
      });

    parse(new String[] { "regen", "*", "2:30" }, 1)
      .validate(0, PotionEffectPredicate.class, it -> {
        assertEquals(PotionEffectType.REGENERATION, it.type());
        assertNull(it.amplifierArgument().value());
        assertEquals(60*2 + 30, it.durationArgument().value());
      });

    makeExceptionCase(new String[] { "effi", "2:30" }, 1, ParseConflict.DOES_NOT_ACCEPT_TIME_NOTATION);
    makeExceptionCase(new String[] { "deterio", "2:30" }, 1, ParseConflict.DOES_NOT_ACCEPT_TIME_NOTATION);
    makeExceptionCase(new String[] { "deterio", "*", "2:30" }, 2, ParseConflict.DOES_NOT_ACCEPT_TIME_NOTATION);
    makeExceptionCase(new String[] { "deterio", "2:30", "*" }, 1, ParseConflict.DOES_NOT_ACCEPT_TIME_NOTATION);
    makeExceptionCase(new String[] { "deterio", "2:30", "2:30" }, 1, ParseConflict.DOES_NOT_ACCEPT_TIME_NOTATION);
    makeExceptionCase(new String[] { "regen", "2:30" }, 1, ParseConflict.DOES_NOT_ACCEPT_TIME_NOTATION);
    makeExceptionCase(new String[] { "regen", "2:30", "*" }, 1, ParseConflict.DOES_NOT_ACCEPT_TIME_NOTATION);
    makeExceptionCase(new String[] { "regen", "2:30", "2:30" }, 1, ParseConflict.DOES_NOT_ACCEPT_TIME_NOTATION);
  }

  @Test
  public void shouldThrowOnInvalidBeginning() {
    makeExceptionCase(new String[] { "5" }, 0, ParseConflict.EXPECTED_SEARCH_PATTERN);
    makeExceptionCase(new String[] { "2:30" }, 0, ParseConflict.EXPECTED_SEARCH_PATTERN);
  }

  @Test
  public void shouldThrowOnMultiplePatternWildcards() {
    makeExceptionCase(new String[] { "sign-?-?" }, 0, ParseConflict.MULTIPLE_SEARCH_PATTERN_WILDCARDS);
    makeExceptionCase(new String[] { "?-sign-?-oak" }, 0, ParseConflict.MULTIPLE_SEARCH_PATTERN_WILDCARDS);
  }

  @Test
  public void shouldThrowOnNoMatches() {
    makeExceptionCase(new String[] { "gibberish-gobbledygook" }, 0, ParseConflict.NO_SEARCH_MATCH);
  }

  @Test
  public void shouldHandleMaterialWildcards() {
    parse(new String[] { "pickax-?" }, 1)
      .validate(0, MaterialPredicate.class, it -> {
        assertThat(it.materials(), Matchers.containsInAnyOrder(Tag.ITEMS_PICKAXES.getValues().toArray()));
      });

    parse(new String[] { "hoe-?" }, 1)
      .validate(0, MaterialPredicate.class, it -> {
        assertThat(it.materials(), Matchers.containsInAnyOrder(Tag.ITEMS_HOES.getValues().toArray()));
      });

    parse(new String[] { "boots-?" }, 1)
      .validate(0, MaterialPredicate.class, it -> {
        assertThat(it.materials(), Matchers.containsInAnyOrder(Tag.ITEMS_FOOT_ARMOR.getValues().toArray()));
      });

    parse(new String[] { "fen-gat-?" }, 1)
      .validate(0, MaterialPredicate.class, it -> {
        assertThat(it.materials(), Matchers.containsInAnyOrder(Tag.FENCE_GATES.getValues().toArray()));
      });
  }

  private void makeExceptionCase(String[] args, int expectedArgumentIndex, ParseConflict expectedConflict) {
    var exception = assertThrows(ArgumentParseException.class, () -> parse(args, -1));
    assertEquals(expectedArgumentIndex, exception.getArgumentIndex());
    assertEquals(expectedConflict, exception.getConflict());
  }

  private record PredicateListValidationBuilder (List<ItemPredicate> items) {
    public <T> PredicateListValidationBuilder validate(int index, Class<T> expectedType, Consumer<T> validator) {
      var item = items.get(index);
      assertInstanceOf(expectedType, item);
      validator.accept(expectedType.cast(item));
      return this;
    }
  }

  private PredicateListValidationBuilder parse(String[] args, int expectedNumberOfPredicates) {
    var predicates = PredicateParser.parsePredicates(TokenParser.parseTokens(args), translationregistry);
    assertEquals(expectedNumberOfPredicates, predicates.size());
    return new PredicateListValidationBuilder(predicates);
  }
}
