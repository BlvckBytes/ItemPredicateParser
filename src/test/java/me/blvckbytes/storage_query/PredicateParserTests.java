package me.blvckbytes.storage_query;

import me.blvckbytes.storage_query.parse.ArgumentParseException;
import me.blvckbytes.storage_query.parse.ParseConflict;
import me.blvckbytes.storage_query.parse.PredicateParser;
import me.blvckbytes.storage_query.parse.TokenParser;
import me.blvckbytes.storage_query.predicate.*;
import me.blvckbytes.storage_query.token.ComparisonMode;
import me.blvckbytes.storage_query.token.IntegerToken;
import me.blvckbytes.storage_query.token.QuotedStringToken;
import me.blvckbytes.storage_query.token.UnquotedStringToken;
import me.blvckbytes.storage_query.translation.*;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.potion.PotionEffectType;
import org.hamcrest.Matchers;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

public class PredicateParserTests extends TranslationRegistryDependentTests {

  @Test
  public void shouldStringifyAsParsed() {
    makeStringificationCase(
      new String[] { "dia-ches", "unbr", "2", "regen", "3", "sign-?" },
      "dia-ches unbr 2 regen 3 sign-?",
      false, true
    );

    makeStringificationCase(
      new String[] { "dia-ches", "and", "unbr", "2", "regen", "3", "sign-?" },
      "dia-ches and unbr 2 regen 3 sign-?",
      false, true
    );

    makeStringificationCase(
      new String[] { "dia-ches", "and", "unbr", "2", "or", "regen", "3", "sign-?" },
      "dia-ches and unbr 2 or regen 3 sign-?",
      false, true
    );

    makeStringificationCase(
      new String[] { "(dia-ches", "and", "unbr", "2)", "or", "(regen", "3", "sign-?)" },
      "(dia-ches and unbr 2) or (regen 3 sign-?)",
      false, true
    );

    // But we most definitely want parentheses to be closed
    makeStringificationCase(
      new String[] { "(dia-ches", "and", "(unbr", "2" },
      "(dia-ches and (unbr 2))",
      true, true
    );
  }

  @Test
  public void shouldStringifyExpandedAbbreviations() {
    makeStringificationCase(
      new String[] { "dia-ches", "unbr", "2", "regen", "3", "sign-?" },
      "Diamond-Chestplate Unbreaking 2 Regeneration 3 sign-?",
      false, false
    );

    makeStringificationCase(
      new String[] { "dia-ches", "and", "unbr", "2", "regen", "3", "sign-?" },
      "Diamond-Chestplate and Unbreaking 2 Regeneration 3 sign-?",
      false, false
    );

    makeStringificationCase(
      new String[] { "dia-ches", "and", "unbr", "2", "or", "regen", "3", "sign-?" },
      "Diamond-Chestplate and Unbreaking 2 or Regeneration 3 sign-?",
      false, false
    );

    makeStringificationCase(
      new String[] { "(dia-ches", "and", "unbr", "2)", "or", "(regen", "3", "sign-?)" },
      "(Diamond-Chestplate and Unbreaking 2) or (Regeneration 3 sign-?)",
      false, false
    );

    // But we most definitely want parentheses to be closed
    makeStringificationCase(
      new String[] { "(dia-ches", "and", "(unbr", "2" },
      "(Diamond-Chestplate and (Unbreaking 2))",
      true, false
    );
  }

  @Test
  public void shouldYieldNullOnEmptyInput() {
    makeCase(new String[] {}, null);
    makeCase(new String[] { "" }, null);
    makeCase(new String[] { "", "" }, null);
  }

  @Test
  public void shouldConjunctByDefault() {
    makeCase(
      new String[] { "dia-ches", "unbr", "4", "night-v", "deter" },
      andJoin(true,
        materialPredicate(Material.DIAMOND_CHESTPLATE, new UnquotedStringToken(0, "dia-ches")),
        enchantmentPredicate(
          Enchantment.UNBREAKING,
          new IntegerToken(2, 4),
          new UnquotedStringToken(1, "unbr")
        ),
        potionEffectPredicate(
          PotionEffectType.NIGHT_VISION,
          null,
          null,
          new UnquotedStringToken(3, "night-v")
        ),
        deteriorationPredicate(
          null,
          null,
          new UnquotedStringToken(4, "deter")
        )
      )
    );
  }

  @Test
  public void shouldParseNegation() {
    makeCase(
      new String[] { "gold-boot", "not", "unbr" },
      andJoin(true,
        materialPredicate(Material.GOLDEN_BOOTS, new UnquotedStringToken(0, "gold-boot")),
        negate(
          enchantmentPredicate(
            Enchantment.UNBREAKING,
            null,
            new UnquotedStringToken(2, "unbr")
          )
        )
      )
    );

    makeCase(
      new String[] { "gold-boot", "not", "unbr", "not", "deter", "not", "\"test\"" },
      andJoin(true,
        materialPredicate(Material.GOLDEN_BOOTS, new UnquotedStringToken(0, "gold-boot")),
        negate(
          enchantmentPredicate(
            Enchantment.UNBREAKING,
            null,
            new UnquotedStringToken(2, "unbr")
          )
        ),
        negate(
          deteriorationPredicate(
            null,
            null,
            new UnquotedStringToken(4, "deter")
          )
        ),
        negate(
          new TextSearchPredicate(
            new QuotedStringToken(6, "test")
          )
        )
      )
    );
  }

  @Test
  public void shouldParseConjunction() {
    makeCase(
      new String[] { "mendi", "and", "unbr", "3", "and", "deter" },
      andJoin(false,
        enchantmentPredicate(
          Enchantment.MENDING,
          null,
          new UnquotedStringToken(0, "mendi")
        ),
        enchantmentPredicate(
          Enchantment.UNBREAKING,
          new IntegerToken(3, 3),
          new UnquotedStringToken(2, "unbr")
        ),
        deteriorationPredicate(
          null,
          null,
          new UnquotedStringToken(5, "deter")
        )
      )
    );
  }

  @Test
  public void shouldParseDisjunction() {
    makeCase(
      new String[] { "dia-shov", "or", "dia-pick", "or", "unbr", "2", "or", "gold-boot" },
      orJoin(
        materialPredicate(Material.DIAMOND_SHOVEL, new UnquotedStringToken(0, "dia-shov")),
        materialPredicate(Material.DIAMOND_PICKAXE, new UnquotedStringToken(2, "dia-pick")),
        enchantmentPredicate(
          Enchantment.UNBREAKING,
          new IntegerToken(5, 2),
          new UnquotedStringToken(4, "unbr")
        ),
        materialPredicate(Material.GOLDEN_BOOTS, new UnquotedStringToken(7, "gold-boot"))
      )
    );
  }

  @Test
  public void shouldParseParentheses() {
    makeCase(
      new String[] { "(", "dia-shov", ")" },
      new ParenthesesNode(
        materialPredicate(
          Material.DIAMOND_SHOVEL,
          new UnquotedStringToken(1, "dia-shov")
        )
      )
    );

    makeCase(
      new String[] { "(dia-shov", ")" },
      new ParenthesesNode(
        materialPredicate(
          Material.DIAMOND_SHOVEL,
          new UnquotedStringToken(0, "dia-shov")
        )
      )
    );

    makeCase(
      new String[] { "(", "dia-shov)" },
      new ParenthesesNode(
        materialPredicate(
          Material.DIAMOND_SHOVEL,
          new UnquotedStringToken(1, "dia-shov")
        )
      )
    );

    makeCase(
      new String[] { "(dia-shov)" },
      new ParenthesesNode(
        materialPredicate(
          Material.DIAMOND_SHOVEL,
          new UnquotedStringToken(0, "dia-shov")
        )
      )
    );

    makeCase(
      new String[] { "(unbr", "fire-prot", "3", "thorn)" },
      new ParenthesesNode(
        andJoin(false,
          enchantmentPredicate(
            Enchantment.UNBREAKING,
            null,
            new UnquotedStringToken(0, "unbr")
          ),
          enchantmentPredicate(
            Enchantment.FIRE_PROTECTION,
            new IntegerToken(2, 3),
            new UnquotedStringToken(1, "fire-prot")
          ),
          enchantmentPredicate(
            Enchantment.THORNS,
            null,
            new UnquotedStringToken(3, "thorn")
          )
        )
      )
    );

    makeCase(
      new String[] { "(unbr", "or", "fire-prot", "3", "or", "thorn)" },
      new ParenthesesNode(
        orJoin(
          enchantmentPredicate(
            Enchantment.UNBREAKING,
            null,
            new UnquotedStringToken(0, "unbr")
          ),
          enchantmentPredicate(
            Enchantment.FIRE_PROTECTION,
            new IntegerToken(3, 3),
            new UnquotedStringToken(2, "fire-prot")
          ),
          enchantmentPredicate(
            Enchantment.THORNS,
            null,
            new UnquotedStringToken(5, "thorn")
          )
        )
      )
    );

    makeCase(
      new String[] { "(((unbr)))" },
      new ParenthesesNode(
        new ParenthesesNode(
          new ParenthesesNode(
            enchantmentPredicate(
              Enchantment.UNBREAKING,
              null,
              new UnquotedStringToken(0, "unbr")
            )
          )
        )
      )
    );

    makeCase(
      new String[] { "(", "(", "(", "unbr", ")", ")", ")" },
      new ParenthesesNode(
        new ParenthesesNode(
          new ParenthesesNode(
            enchantmentPredicate(
              Enchantment.UNBREAKING,
              null,
              new UnquotedStringToken(3, "unbr")
            )
          )
        )
      )
    );
  }

  @Test
  public void shouldImplicitlyAddConjunctionToParentheses() {
    makeCase(
      new String[] { "dia-ches", "(unbr", "2)" },
      andJoin(true,
        materialPredicate(Material.DIAMOND_CHESTPLATE, new UnquotedStringToken(0, "dia-ches")),
        new ParenthesesNode(
          enchantmentPredicate(
            Enchantment.UNBREAKING,
            new IntegerToken(2, 2),
            new UnquotedStringToken(1, "unbr")
          )
        )
      )
    );

    makeCase(
      new String[] { "(dia-ches)", "(unbr", "2)" },
      andJoin(true,
        new ParenthesesNode(
          materialPredicate(Material.DIAMOND_CHESTPLATE, new UnquotedStringToken(0, "dia-ches"))
        ),
        new ParenthesesNode(
          enchantmentPredicate(
            Enchantment.UNBREAKING,
            new IntegerToken(2, 2),
            new UnquotedStringToken(1, "unbr")
          )
        )
      )
    );

    makeCase(
      new String[] { "(dia-ches", "(unbr", "2))" },
        new ParenthesesNode(
          andJoin(true,
            materialPredicate(Material.DIAMOND_CHESTPLATE, new UnquotedStringToken(0, "dia-ches")),
            new ParenthesesNode(
              enchantmentPredicate(
                Enchantment.UNBREAKING,
                new IntegerToken(2, 2),
                new UnquotedStringToken(1, "unbr")
              )
            )
         )
      )
    );

    makeExceptionCase(
      new String[] { "(dia-ches", "(unbr", "2)" },
      0,
      ParseConflict.EXPECTED_CLOSING_PARENTHESIS
    );
  }

  @Test
  public void shouldThrowOnMalformedParentheses() {
    makeExceptionCase(
      new String[] { ")" },
      0,
      ParseConflict.EXPECTED_OPENING_PARENTHESIS
    );

    makeExceptionCase(
      new String[] { "(" },
      0,
      ParseConflict.EXPECTED_SEARCH_PATTERN
    );

    makeExceptionCase(
      new String[] { "(", ")" },
      0,
      ParseConflict.EXPECTED_SEARCH_PATTERN
    );

    makeExceptionCase(
      new String[] { "(", "dia-ches" },
      0,
      ParseConflict.EXPECTED_CLOSING_PARENTHESIS
    );

    makeExceptionCase(
      new String[] { "(", "(dia-ches", ")" },
      0,
      ParseConflict.EXPECTED_CLOSING_PARENTHESIS
    );
  }

  @Test
  public void shouldAddMissingClosingParensToTheVeryEnd() {
    makeCase(
      new String[] {
        "(dia-shov", "(dia-pick", "(dia-hoe)", "dia-hel"
      },
      new ParenthesesNode(
        andJoin(true,
          materialPredicate(Material.DIAMOND_SHOVEL, new UnquotedStringToken(0, "dia-shov")),
          new ParenthesesNode(
            andJoin(true,
              materialPredicate(Material.DIAMOND_PICKAXE, new UnquotedStringToken(1, "dia-pick")),
              new ParenthesesNode(
                materialPredicate(Material.DIAMOND_HOE, new UnquotedStringToken(2, "dia-hoe"))
              ),
              materialPredicate(Material.DIAMOND_HELMET, new UnquotedStringToken(3, "dia-hel"))
            )
          )
        )
      ),
      true
    );
  }

  @Test
  public void shouldAdhereToPrecedences() {
    makeCase(
      new String[] { "dia", "and", "dia", "or", "dia" },
      orJoin(
        andJoin(false,
          materialPredicate(Material.DIAMOND, new UnquotedStringToken(0, "dia")),
          materialPredicate(Material.DIAMOND, new UnquotedStringToken(2, "dia"))
        ),
        materialPredicate(Material.DIAMOND, new UnquotedStringToken(4, "dia"))
      )
    );

    makeCase(
      new String[] { "dia", "and", "(dia", "or", "dia)" },
      andJoin(false,
        materialPredicate(Material.DIAMOND, new UnquotedStringToken(0, "dia")),
        new ParenthesesNode(
          orJoin(
            materialPredicate(Material.DIAMOND, new UnquotedStringToken(2, "dia")),
            materialPredicate(Material.DIAMOND, new UnquotedStringToken(4, "dia"))
          )
        )
      )
    );

    makeCase(
      new String[] { "dia", "and", "not", "dia", "or", "dia" },
      orJoin(
        andJoin(false,
          materialPredicate(Material.DIAMOND, new UnquotedStringToken(0, "dia")),
          negate(
            materialPredicate(Material.DIAMOND, new UnquotedStringToken(3, "dia"))
          )
        ),
        materialPredicate(Material.DIAMOND, new UnquotedStringToken(5, "dia"))
      )
    );

    makeCase(
      new String[] { "dia", "and", "not", "(dia", "or", "dia)" },
      orJoin(
        andJoin(false,
          materialPredicate(Material.DIAMOND, new UnquotedStringToken(0, "dia")),
          negate(
            new ParenthesesNode(
              orJoin(
                materialPredicate(Material.DIAMOND, new UnquotedStringToken(3, "dia")),
                materialPredicate(Material.DIAMOND, new UnquotedStringToken(5, "dia"))
              )
            )
          )
        )
      )
    );
  }

  @Test
  public void shouldParseSimpleSinglePredicates() {

    // ================================================================================
    // Material
    // ================================================================================

    makeCase(
      new String[] { "dia-ches" },
      materialPredicate(
        Material.DIAMOND_CHESTPLATE,
        new UnquotedStringToken(0, "dia-ches")
      )
    );

    // ================================================================================
    // Potion Effect
    // ================================================================================

    makeCase(
      new String[] { "regen" },
      potionEffectPredicate(
        PotionEffectType.REGENERATION,
        null,
        null,
        new UnquotedStringToken(0, "regen")
      )
    );

    makeCase(
      new String[] { "regen", "2" },
      potionEffectPredicate(
        PotionEffectType.REGENERATION,
        new IntegerToken(1, 2),
        null,
        new UnquotedStringToken(0, "regen")
      )
    );

    makeCase(
      new String[] { "regen", "*" },
      potionEffectPredicate(
        PotionEffectType.REGENERATION,
        new IntegerToken(1, null),
        null,
        new UnquotedStringToken(0, "regen")
      )
    );

    makeCase(
      new String[] { "regen", "2", "1600" },
      potionEffectPredicate(
        PotionEffectType.REGENERATION,
        new IntegerToken(1, 2),
        new IntegerToken(2, 1600),
        new UnquotedStringToken(0, "regen")
      )
    );

    makeCase(
      new String[] { "regen", "*", "1600" },
      potionEffectPredicate(
        PotionEffectType.REGENERATION,
        new IntegerToken(1, null),
        new IntegerToken(2, 1600),
        new UnquotedStringToken(0, "regen")
      )
    );

    makeCase(
      new String[] { "regen", "2", "*" },
      potionEffectPredicate(
        PotionEffectType.REGENERATION,
        new IntegerToken(1, 2),
        new IntegerToken(2, null),
        new UnquotedStringToken(0, "regen")
      )
    );

    // ================================================================================
    // Enchantment
    // ================================================================================

    makeCase(
      new String[] { "thorn" },
      enchantmentPredicate(
        Enchantment.THORNS,
        null,
        new UnquotedStringToken(0, "thorn")
      )
    );

    makeCase(
      new String[] { "thorn", "2" },
      enchantmentPredicate(
        Enchantment.THORNS,
        new IntegerToken(1, 2),
        new UnquotedStringToken(0, "thorn")
      )
    );

    makeCase(
      new String[] { "thorn", "*" },
      enchantmentPredicate(
        Enchantment.THORNS,
        new IntegerToken(1, null),
        new UnquotedStringToken(0, "thorn")
      )
    );

    // ================================================================================
    // Text Search
    // ================================================================================

    makeCase(
      new String[] { "\"a", "long", "search\"" },
      new TextSearchPredicate(
        new QuotedStringToken(0, "a long search")
      )
    );

    makeCase(
      new String[] { "\"short\"" },
      new TextSearchPredicate(
        new QuotedStringToken(0, "short")
      )
    );

    makeCase(
      new String[] { "\"", "short\"" },
      new TextSearchPredicate(
        new QuotedStringToken(0, " short")
      )
    );

    makeCase(
      new String[] { "\"short", "\"" },
      new TextSearchPredicate(
        new QuotedStringToken(0, "short ")
      )
    );

    makeCase(
      new String[] { "\"", "short", "\"" },
      new TextSearchPredicate(
        new QuotedStringToken(0, " short ")
      )
    );

    // ================================================================================
    // Deterioration
    // ================================================================================

    makeCase(
      new String[] { "deter" },
      deteriorationPredicate(
        null,
        null,
        new UnquotedStringToken(0, "deter")
      )
    );

    makeCase(
      new String[] { "deter", "5" },
      deteriorationPredicate(
        new IntegerToken(1, 5),
        null,
        new UnquotedStringToken(0, "deter")
      )
    );

    makeCase(
      new String[] { "deter", "*" },
      deteriorationPredicate(
        new IntegerToken(1, null),
        null,
        new UnquotedStringToken(0, "deter")
      )
    );

    makeCase(
      new String[] { "deter", "5", "85" },
      deteriorationPredicate(
        new IntegerToken(1, 5),
        new IntegerToken(2, 85),
        new UnquotedStringToken(0, "deter")
      )
    );

    makeCase(
      new String[] { "deter", "*", "85" },
      deteriorationPredicate(
        new IntegerToken(1, null),
        new IntegerToken(2, 85),
        new UnquotedStringToken(0, "deter")
      )
    );

    makeCase(
      new String[] { "deter", "5", "*" },
      deteriorationPredicate(
        new IntegerToken(1, 5),
        new IntegerToken(2, null),
        new UnquotedStringToken(0, "deter")
      )
    );
  }

  @Test
  public void shouldParseComplexMultiplePredicates() {
    makeCase(
      new String[] {
        "deter", "3",
        "unbr", "*",
        "\"text a\"",
        "regen", "4", "*",
        "dia-pick",
        "\"multi", "arg", "text", "b", "\""
      },
      andJoin(true,
        deteriorationPredicate(
          new IntegerToken(1, 3),
          null,
          new UnquotedStringToken(0, "deter")
        ),
        enchantmentPredicate(
          Enchantment.UNBREAKING,
          new IntegerToken(3, null),
          new UnquotedStringToken(2, "unbr")
        ),
        new TextSearchPredicate(
          new QuotedStringToken(4, "text a")
        ),
        potionEffectPredicate(
          PotionEffectType.REGENERATION,
          new IntegerToken(6, 4),
          new IntegerToken(7, null),
          new UnquotedStringToken(5, "regen")
        ),
        materialPredicate(
          Material.DIAMOND_PICKAXE,
          new UnquotedStringToken(8, "dia-pick")
        ),
        new TextSearchPredicate(
          new QuotedStringToken(9, "multi arg text b ")
        )
      )
    );
  }

  @Test
  public void onlyEffectDurationShouldAcceptTimeNotation() {
    makeCase(
      new String[] { "regen", "2", "2:30" },
      potionEffectPredicate(
        PotionEffectType.REGENERATION,
        new IntegerToken(1, 2),
        new IntegerToken(2, 2 * 60 + 30, true, ComparisonMode.EQUALS),
        new UnquotedStringToken(0, "regen")
      )
    );

    makeCase(
      new String[] { "regen", "*", "2:30" },
      potionEffectPredicate(
        PotionEffectType.REGENERATION,
        new IntegerToken(1, null),
        new IntegerToken(2, 2 * 60 + 30, true, ComparisonMode.EQUALS),
        new UnquotedStringToken(0, "regen")
      )
    );

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
    makeCase(
      new String[] { "pickax-?" },
      materialsPredicate(
        new UnquotedStringToken(0, "pickax-?"),
        Tag.ITEMS_PICKAXES.getValues()
      )
    );

    makeCase(
      new String[] { "hoe-?" },
      materialsPredicate(
        new UnquotedStringToken(0, "hoe-?"),
        Tag.ITEMS_HOES.getValues()
      )
    );

    makeCase(
      new String[] { "boots-?" },
      materialsPredicate(
        new UnquotedStringToken(0, "boots-?"),
        Tag.ITEMS_FOOT_ARMOR.getValues()
      )
    );

    makeCase(
      new String[] { "fen-gat-?" },
      materialsPredicate(
        new UnquotedStringToken(0, "fen-gat-?"),
        Tag.FENCE_GATES.getValues()
      )
    );
  }

  @Test
  public void shouldHandleExactKey() {
    makeCase(
      new String[] { "exact", "unbr", "2" },
      exact(
        enchantmentPredicate(
          Enchantment.UNBREAKING,
          new IntegerToken(2, 2),
          new UnquotedStringToken(1, "unbr")
        )
      )
    );

    makeCase(
      new String[] { "dia-ches", "and", "exact(", "unbr", "fire-prot", "1)", "or", "not", "exact", "feather-fall" },
      orJoin(
        andJoin(false,
          materialPredicate(
            Material.DIAMOND_CHESTPLATE,
            new UnquotedStringToken(0, "dia-ches")
          ),
          exact(
            new ParenthesesNode(
              andJoin(true,
                enchantmentPredicate(
                  Enchantment.UNBREAKING,
                  null,
                  new UnquotedStringToken(3, "unbr")
                ),
                enchantmentPredicate(
                  Enchantment.FIRE_PROTECTION,
                  new IntegerToken(5, 1),
                  new UnquotedStringToken(4, "fire-prot")
                )
              )
            )
          )
        ),
        negate(
          exact(
            enchantmentPredicate(
              Enchantment.FEATHER_FALLING,
              null,
              new UnquotedStringToken(9, "feather-fall")
            )
          )
        )
      )
    );
  }

  private void assertTreesEqual(
    @Nullable ItemPredicate rootExpected,
    @Nullable ItemPredicate rootActual,
    @Nullable ItemPredicate expected,
    @Nullable ItemPredicate actual
  ) {
    if (expected == null && actual == null)
      return;

    assertNotNull(expected);
    assertNotNull(actual);

    assertEquals(expected.getClass(), actual.getClass(), () -> (
      Objects.requireNonNull(rootExpected).stringify(false) + " =/= " + Objects.requireNonNull(rootActual).stringify(false)
    ));

    switch (expected) {
      case ConjunctionNode expectedConjunction -> {
        var actualConjunction = (ConjunctionNode) actual;
        assertTreesEqual(rootExpected, rootActual, expectedConjunction.lhs(), actualConjunction.lhs());
        assertEquals(expectedConjunction.translatedTranslatable(), actualConjunction.translatedTranslatable());
        assertTreesEqual(rootExpected, rootActual, expectedConjunction.rhs(), actualConjunction.rhs());
      }
      case DisjunctionNode expectedDisjunction -> {
        var actualDisjunction = (DisjunctionNode) actual;
        assertTreesEqual(rootExpected, rootActual, expectedDisjunction.lhs(), actualDisjunction.lhs());
        assertEquals(expectedDisjunction.translatedTranslatable(), actualDisjunction.translatedTranslatable());
        assertTreesEqual(rootExpected, rootActual, expectedDisjunction.rhs(), actualDisjunction.rhs());
      }
      case NegationNode expectedNegation -> {
        var actualNegation = (NegationNode) actual;
        assertEquals(expectedNegation.translatedTranslatable(), actualNegation.translatedTranslatable());
        assertTreesEqual(rootExpected, rootActual, expectedNegation.operand(), actualNegation.operand());
      }
      case ExactNode expectedExact -> {
        var actualExact = (ExactNode) actual;
        assertEquals(expectedExact.translatedTranslatable(), actualExact.translatedTranslatable());
        assertTreesEqual(rootExpected, rootActual, expectedExact.operand(), actualExact.operand());
      }
      case ParenthesesNode expectedParentheses -> {
        assertTreesEqual(rootExpected, rootActual, expectedParentheses.inner(), ((ParenthesesNode) actual).inner());
      }
      case MaterialPredicate expectedMaterial -> {
        var actualMaterial = (MaterialPredicate) actual;
        assertEquals(expectedMaterial.translatedTranslatable(), actualMaterial.translatedTranslatable());
        assertEquals(expectedMaterial.token(), actualMaterial.token());
        assertThat(actualMaterial.materials(), Matchers.containsInAnyOrder(expectedMaterial.materials().toArray()));
      }
      default -> {
        assertEquals(expected, actual);
      }
    }
  }

  private void makeExceptionCase(String[] args, int expectedArgumentIndex, ParseConflict expectedConflict) {
    var predicateParser = new PredicateParser(translationRegistry, TokenParser.parseTokens(args), false);
    var exception = assertThrows(ArgumentParseException.class, predicateParser::parseAst);
    assertEquals(expectedArgumentIndex, exception.getArgumentIndex());
    assertEquals(expectedConflict, exception.getConflict());
  }

  private void makeStringificationCase(String[] args, String expected, boolean allowMissingClosingParentheses, boolean stringifyTokens) {
    var predicateParser = new PredicateParser(translationRegistry, TokenParser.parseTokens(args), allowMissingClosingParentheses);
    var ast = predicateParser.parseAst();

    assertNotNull(ast);
    assertEquals(expected, ast.stringify(stringifyTokens));
  }

  private void makeCase(String[] args, @Nullable ItemPredicate expected) {
    makeCase(args, expected, false);
  }

  private void makeCase(String[] args, @Nullable ItemPredicate expected, boolean allowMissingClosingParentheses) {
    var predicateParser = new PredicateParser(translationRegistry, TokenParser.parseTokens(args), allowMissingClosingParentheses);
    var actual = predicateParser.parseAst();
    assertTreesEqual(expected, actual, expected, actual);
  }

  private ItemPredicate andJoin(boolean implicit, ItemPredicate... predicates) {
    var result = predicates[0];
    for (var i = 1; i < predicates.length; ++i)
      result = new ConjunctionNode(null, translationRegistry.lookup(ConjunctionKey.INSTANCE), result, predicates[i], implicit);
    return result;
  }

  private ItemPredicate orJoin(ItemPredicate... predicates) {
    var result = predicates[0];
    for (var i = 1; i < predicates.length; ++i)
      result = new DisjunctionNode(null, translationRegistry.lookup(DisjunctionKey.INSTANCE), result, predicates[i]);
    return result;
  }

  private ItemPredicate negate(ItemPredicate predicate) {
    return new NegationNode(null, translationRegistry.lookup(NegationKey.INSTANCE), predicate);
  }

  private ItemPredicate exact(ItemPredicate predicate) {
    return new ExactNode(null, translationRegistry.lookup(ExactKey.INSTANCE), predicate);
  }

  private EnchantmentPredicate enchantmentPredicate(Enchantment enchantment, @Nullable IntegerToken level, UnquotedStringToken search) {
    return new EnchantmentPredicate(search, translationRegistry.lookup(enchantment), enchantment, level);
  }

  private MaterialPredicate materialPredicate(Material material, UnquotedStringToken search) {
    return new MaterialPredicate(search, translationRegistry.lookup(material), List.of(material));
  }

  private MaterialPredicate materialsPredicate(UnquotedStringToken search, Collection<Material> materials) {
    return new MaterialPredicate(search, null, new ArrayList<>(materials));
  }

  private PotionEffectPredicate potionEffectPredicate(PotionEffectType type, @Nullable IntegerToken amplifier, @Nullable IntegerToken duration, UnquotedStringToken search) {
    return new PotionEffectPredicate(search, translationRegistry.lookup(type), type, amplifier, duration);
  }

  private DeteriorationPredicate deteriorationPredicate(@Nullable IntegerToken min, @Nullable IntegerToken max, UnquotedStringToken search) {
    return new DeteriorationPredicate(search, translationRegistry.lookup(DeteriorationKey.INSTANCE), min, max);
  }
}
