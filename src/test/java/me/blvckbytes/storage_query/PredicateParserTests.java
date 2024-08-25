package me.blvckbytes.storage_query;

import me.blvckbytes.storage_query.parse.PredicateParser;
import me.blvckbytes.storage_query.parse.TokenParser;
import me.blvckbytes.storage_query.predicate.*;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.potion.PotionEffectType;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

public class PredicateParserTests extends TranslationRegistryDependentTests {

  @Test
  public void shouldParseSimpleSinglePredicates() {

    // ================================================================================
    // Material
    // ================================================================================

    parse(new String[]{ "dia-ches" }, 1)
      .validate(0, MaterialPredicate.class, it -> {
        assertEquals(Material.DIAMOND_CHESTPLATE, it.material());
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
        assertEquals(Material.DIAMOND_PICKAXE, it.material());
      })
      .validate(5, TextSearchPredicate.class, it -> {
        assertEquals("multi arg text b ", it.text);
      });
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
