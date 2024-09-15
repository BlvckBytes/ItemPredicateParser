package me.blvckbytes.item_predicate_parser;

import me.blvckbytes.item_predicate_parser.parse.ItemPredicateParseException;
import me.blvckbytes.item_predicate_parser.parse.ParseConflict;
import me.blvckbytes.item_predicate_parser.parse.TokenParser;
import me.blvckbytes.item_predicate_parser.predicate.*;
import me.blvckbytes.item_predicate_parser.token.ComparisonMode;
import me.blvckbytes.item_predicate_parser.token.Token;
import me.blvckbytes.item_predicate_parser.translation.*;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class PredicateParserTests extends ParseTestBase {

  @Test
  public void shouldNormalizeTranslatables() {
    assertEquals(
      "[My-custom]-item-':]'",
      TranslatedTranslatable.normalize(
        "(My_custom) item \":)\""
      )
    );
  }

  @Test
  public void shouldStringifyStringsWithEscapedDoubleQuotes() {
    makeStringificationCase(
      new String[] { "\"string", "with", "escaped", "\\\"", "quote", "\"" },
      "\"string with escaped \\\" quote \"",
      false, false
    );

    makeStringificationCase(
      new String[] { "\"string", "with", "escaped", "\\\"", "quote", "\"" },
      "\"string with escaped \\\" quote \"",
      false, true
    );
  }

  @Test
  public void shouldStringifyQuotedStringsWithEscapeSequences() {
    makeStringificationCase(
      new String[] { "\"my-\\\"-string\"" },
      "\"my-\\\"-string\"",
      false, false
    );

    makeStringificationCase(
      new String[] { "\"my", "\\\"", "string\"" },
      "\"my \\\" string\"",
      false, false
    );
  }

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
      andJoin(
        new Token[] {
          null, null, null
        },
        materialPredicate(Material.DIAMOND_CHESTPLATE, unquotedStringToken(0, 0, "dia-ches")),
        enchantmentPredicate(
          Enchantment.UNBREAKING,
          integerToken(2, 0, 4),
          unquotedStringToken(1, 0, "unbr")
        ),
        potionEffectPredicate(
          PotionEffectType.NIGHT_VISION,
          null,
          null,
          unquotedStringToken(3, 0, "night-v")
        ),
        deteriorationPredicate(
          null,
          null,
          unquotedStringToken(4, 0, "deter")
        )
      )
    );
  }

  @Test
  public void shouldParseNegation() {
    makeCase(
      new String[] { "gold-boot", "not", "unbr" },
      andJoin(
        new Token[] {
          null
        },
        materialPredicate(Material.GOLDEN_BOOTS, unquotedStringToken(0, 0, "gold-boot")),
        negate(
          unquotedStringToken(1, 0, "not"),
          enchantmentPredicate(
            Enchantment.UNBREAKING,
            null,
            unquotedStringToken(2, 0, "unbr")
          )
        )
      )
    );

    makeCase(
      new String[] { "gold-boot", "not", "unbr", "not", "deter", "not", "\"test\"" },
      andJoin(
        new Token[] {
          null, null, null
        },
        materialPredicate(Material.GOLDEN_BOOTS, unquotedStringToken(0, 0, "gold-boot")),
        negate(
          unquotedStringToken(1, 0, "not"),
          enchantmentPredicate(
            Enchantment.UNBREAKING,
            null,
            unquotedStringToken(2, 0, "unbr")
          )
        ),
        negate(
          unquotedStringToken(3, 0, "not"),
          deteriorationPredicate(
            null,
            null,
            unquotedStringToken(4, 0, "deter")
          )
        ),
        negate(
          unquotedStringToken(5, 0, "not"),
          new TextSearchPredicate(
            quotedStringToken(6, 0, "test")
          )
        )
      )
    );
  }

  @Test
  public void shouldParseConjunction() {
    makeCase(
      new String[] { "mendi", "and", "unbr", "3", "and", "deter" },
      andJoin(
        new Token[] {
          unquotedStringToken(1, 0, "and"),
          unquotedStringToken(4, 0, "and")
        },
        enchantmentPredicate(
          Enchantment.MENDING,
          null,
          unquotedStringToken(0, 0, "mendi")
        ),
        enchantmentPredicate(
          Enchantment.UNBREAKING,
          integerToken(3, 0, 3),
          unquotedStringToken(2, 0, "unbr")
        ),
        deteriorationPredicate(
          null,
          null,
          unquotedStringToken(5, 0, "deter")
        )
      )
    );
  }

  @Test
  public void shouldParseDisjunction() {
    makeCase(
      new String[] { "dia-shov", "or", "dia-pick", "or", "unbr", "2", "or", "gold-boot" },
      orJoin(
        new Token[] {
          unquotedStringToken(1, 0, "or"),
          unquotedStringToken(3, 0, "or"),
          unquotedStringToken(6, 0, "or"),
        },
        materialPredicate(Material.DIAMOND_SHOVEL, unquotedStringToken(0, 0, "dia-shov")),
        materialPredicate(Material.DIAMOND_PICKAXE, unquotedStringToken(2, 0, "dia-pick")),
        enchantmentPredicate(
          Enchantment.UNBREAKING,
          integerToken(5, 0, 2),
          unquotedStringToken(4, 0, "unbr")
        ),
        materialPredicate(Material.GOLDEN_BOOTS, unquotedStringToken(7, 0, "gold-boot"))
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
          unquotedStringToken(1, 0, "dia-shov")
        )
      )
    );

    makeCase(
      new String[] { "(dia-shov", ")" },
      new ParenthesesNode(
        materialPredicate(
          Material.DIAMOND_SHOVEL,
          unquotedStringToken(0, 1, "dia-shov")
        )
      )
    );

    makeCase(
      new String[] { "(", "dia-shov)" },
      new ParenthesesNode(
        materialPredicate(
          Material.DIAMOND_SHOVEL,
          unquotedStringToken(1, 0, "dia-shov")
        )
      )
    );

    makeCase(
      new String[] { "(dia-shov)" },
      new ParenthesesNode(
        materialPredicate(
          Material.DIAMOND_SHOVEL,
          unquotedStringToken(0, 1, "dia-shov")
        )
      )
    );

    makeCase(
      new String[] { "(unbr", "fire-prot", "3", "thorn)" },
      new ParenthesesNode(
        andJoin(
          new Token[] {
            null, null
          },
          enchantmentPredicate(
            Enchantment.UNBREAKING,
            null,
            unquotedStringToken(0, 1, "unbr")
          ),
          enchantmentPredicate(
            Enchantment.FIRE_PROTECTION,
            integerToken(2, 0, 3),
            unquotedStringToken(1, 0, "fire-prot")
          ),
          enchantmentPredicate(
            Enchantment.THORNS,
            null,
            unquotedStringToken(3, 0, "thorn")
          )
        )
      )
    );

    makeCase(
      new String[] { "(unbr", "or", "fire-prot", "3", "or", "thorn)" },
      new ParenthesesNode(
        orJoin(
          new Token[] {
            unquotedStringToken(1, 0, "or"),
            unquotedStringToken(4, 0, "or"),
          },
          enchantmentPredicate(
            Enchantment.UNBREAKING,
            null,
            unquotedStringToken(0, 1, "unbr")
          ),
          enchantmentPredicate(
            Enchantment.FIRE_PROTECTION,
            integerToken(3, 0, 3),
            unquotedStringToken(2, 0, "fire-prot")
          ),
          enchantmentPredicate(
            Enchantment.THORNS,
            null,
            unquotedStringToken(5, 0, "thorn")
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
              unquotedStringToken(0, 3, "unbr")
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
              unquotedStringToken(3, 0, "unbr")
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
      andJoin(
        new Token[] {
          null, null
        },
        materialPredicate(Material.DIAMOND_CHESTPLATE, unquotedStringToken(0, 0, "dia-ches")),
        new ParenthesesNode(
          enchantmentPredicate(
            Enchantment.UNBREAKING,
            integerToken(2, 0, 2),
            unquotedStringToken(1, 1, "unbr")
          )
        )
      )
    );

    makeCase(
      new String[] { "(dia-ches)", "(unbr", "2)" },
      andJoin(
        new Token[] {
          null
        },
        new ParenthesesNode(
          materialPredicate(Material.DIAMOND_CHESTPLATE, unquotedStringToken(0, 1, "dia-ches"))
        ),
        new ParenthesesNode(
          enchantmentPredicate(
            Enchantment.UNBREAKING,
            integerToken(2, 0, 2),
            unquotedStringToken(1, 1, "unbr")
          )
        )
      )
    );

    makeCase(
      new String[] { "(dia-ches", "(unbr", "2))" },
        new ParenthesesNode(
          andJoin(
            new Token[] {
              null
            },
            materialPredicate(Material.DIAMOND_CHESTPLATE, unquotedStringToken(0, 1, "dia-ches")),
            new ParenthesesNode(
              enchantmentPredicate(
                Enchantment.UNBREAKING,
                integerToken(2, 0, 2),
                unquotedStringToken(1, 1, "unbr")
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
        andJoin(
          new Token[] {
            null
          },
          materialPredicate(Material.DIAMOND_SHOVEL, unquotedStringToken(0, 1, "dia-shov")),
          new ParenthesesNode(
            andJoin(
              new Token[] {
                null, null
              },
              materialPredicate(Material.DIAMOND_PICKAXE, unquotedStringToken(1, 1, "dia-pick")),
              new ParenthesesNode(
                materialPredicate(Material.DIAMOND_HOE, unquotedStringToken(2, 1, "dia-hoe"))
              ),
              materialPredicate(Material.DIAMOND_HELMET, unquotedStringToken(3, 0, "dia-hel"))
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
        new Token[] {
          unquotedStringToken(3, 0, "or")
        },
        andJoin(
          new Token[] {
            unquotedStringToken(1, 0, "and")
          },
          materialPredicate(Material.DIAMOND, unquotedStringToken(0, 0, "dia")),
          materialPredicate(Material.DIAMOND, unquotedStringToken(2, 0, "dia"))
        ),
        materialPredicate(Material.DIAMOND, unquotedStringToken(4, 0, "dia"))
      )
    );

    makeCase(
      new String[] { "dia", "and", "(dia", "or", "dia)" },
      andJoin(
        new Token[] {
          unquotedStringToken(1, 0, "and")
        },
        materialPredicate(Material.DIAMOND, unquotedStringToken(0, 0, "dia")),
        new ParenthesesNode(
          orJoin(
            new Token[] {
              unquotedStringToken(3, 0, "or")
            },
            materialPredicate(Material.DIAMOND, unquotedStringToken(2, 1, "dia")),
            materialPredicate(Material.DIAMOND, unquotedStringToken(4, 0, "dia"))
          )
        )
      )
    );

    makeCase(
      new String[] { "dia", "and", "not", "dia", "or", "dia" },
      orJoin(
        new Token[] {
          unquotedStringToken(4, 0, "or")
        },
        andJoin(
          new Token[] {
            unquotedStringToken(1, 0, "and")
          },
          materialPredicate(Material.DIAMOND, unquotedStringToken(0, 0, "dia")),
          negate(
            unquotedStringToken(2, 0, "not"),
            materialPredicate(Material.DIAMOND, unquotedStringToken(3, 0, "dia"))
          )
        ),
        materialPredicate(Material.DIAMOND, unquotedStringToken(5, 0, "dia"))
      )
    );

    makeCase(
      new String[] { "dia", "and", "not", "(dia", "or", "dia)" },
      andJoin(
        new Token[] {
          unquotedStringToken(1, 0, "and")
        },
        materialPredicate(Material.DIAMOND, unquotedStringToken(0, 0, "dia")),
        negate(
          unquotedStringToken(2, 0, "not"),
          new ParenthesesNode(
            orJoin(
              new Token[] {
                unquotedStringToken(4, 0, "or")
              },
              materialPredicate(Material.DIAMOND, unquotedStringToken(3, 1, "dia")),
              materialPredicate(Material.DIAMOND, unquotedStringToken(5, 0, "dia"))
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
        unquotedStringToken(0, 0, "dia-ches")
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
        unquotedStringToken(0, 0, "regen")
      )
    );

    makeCase(
      new String[] { "regen", "2" },
      potionEffectPredicate(
        PotionEffectType.REGENERATION,
        integerToken(1, 0, 2),
        null,
        unquotedStringToken(0, 0, "regen")
      )
    );

    makeCase(
      new String[] { "regen", "*" },
      potionEffectPredicate(
        PotionEffectType.REGENERATION,
        integerToken(1, 0, null),
        null,
        unquotedStringToken(0, 0, "regen")
      )
    );

    makeCase(
      new String[] { "regen", "2", "1600" },
      potionEffectPredicate(
        PotionEffectType.REGENERATION,
        integerToken(1, 0, 2),
        integerToken(2, 0, 1600),
        unquotedStringToken(0, 0, "regen")
      )
    );

    makeCase(
      new String[] { "regen", "*", "1600" },
      potionEffectPredicate(
        PotionEffectType.REGENERATION,
        integerToken(1, 0, null),
        integerToken(2, 0, 1600),
        unquotedStringToken(0, 0, "regen")
      )
    );

    makeCase(
      new String[] { "regen", "2", "*" },
      potionEffectPredicate(
        PotionEffectType.REGENERATION,
        integerToken(1, 0, 2),
        integerToken(2, 0, null),
        unquotedStringToken(0, 0, "regen")
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
        unquotedStringToken(0, 0, "thorn")
      )
    );

    makeCase(
      new String[] { "thorn", "2" },
      enchantmentPredicate(
        Enchantment.THORNS,
        integerToken(1, 0, 2),
        unquotedStringToken(0, 0, "thorn")
      )
    );

    makeCase(
      new String[] { "thorn", "*" },
      enchantmentPredicate(
        Enchantment.THORNS,
        integerToken(1, 0, null),
        unquotedStringToken(0, 0, "thorn")
      )
    );

    // ================================================================================
    // Text Search
    // ================================================================================

    makeCase(
      new String[] { "\"a", "long", "search\"" },
      new TextSearchPredicate(
        quotedStringToken(0, 0, "a long search")
      )
    );

    makeCase(
      new String[] { "\"short\"" },
      new TextSearchPredicate(
        quotedStringToken(0, 0, "short")
      )
    );

    makeCase(
      new String[] { "\"", "short\"" },
      new TextSearchPredicate(
        quotedStringToken(0, 0, " short")
      )
    );

    makeCase(
      new String[] { "\"short", "\"" },
      new TextSearchPredicate(
        quotedStringToken(0, 0, "short ")
      )
    );

    makeCase(
      new String[] { "\"", "short", "\"" },
      new TextSearchPredicate(
        quotedStringToken(0, 0, " short ")
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
        unquotedStringToken(0, 0, "deter")
      )
    );

    makeCase(
      new String[] { "deter", "5" },
      deteriorationPredicate(
        integerToken(1, 0, 5),
        null,
        unquotedStringToken(0, 0, "deter")
      )
    );

    makeCase(
      new String[] { "deter", "*" },
      deteriorationPredicate(
        integerToken(1, 0, null),
        null,
        unquotedStringToken(0, 0, "deter")
      )
    );

    makeCase(
      new String[] { "deter", "5", "85" },
      deteriorationPredicate(
        integerToken(1, 0, 5),
        integerToken(2, 0, 85),
        unquotedStringToken(0, 0, "deter")
      )
    );

    makeCase(
      new String[] { "deter", "*", "85" },
      deteriorationPredicate(
        integerToken(1, 0, null),
        integerToken(2, 0, 85),
        unquotedStringToken(0, 0, "deter")
      )
    );

    makeCase(
      new String[] { "deter", "5", "*" },
      deteriorationPredicate(
        integerToken(1, 0, 5),
        integerToken(2, 0, null),
        unquotedStringToken(0, 0, "deter")
      )
    );

    assertEquals(
      ParseConflict.DOES_NOT_ACCEPT_NON_EQUALS_COMPARISON,
      assertThrows(
        ItemPredicateParseException.class,
        () -> makeCase(
          new String[] { "deter", ">5" },
          amountPredicate(
            integerToken(1, 0, 5, false, ComparisonMode.GREATER_THAN),
            unquotedStringToken(0, 0, "deter")
          )
        )
      ).getConflict()
    );

    assertEquals(
      ParseConflict.DOES_NOT_ACCEPT_NON_EQUALS_COMPARISON,
      assertThrows(
        ItemPredicateParseException.class,
        () -> makeCase(
          new String[] { "deter", "*", "<5" },
          amountPredicate(
            integerToken(2, 0, 5, false, ComparisonMode.LESS_THAN),
            unquotedStringToken(0, 0, "deter")
          )
        )
      ).getConflict()
    );

    // ================================================================================
    // Amount
    // ================================================================================

    makeCase(
      new String[] { "amount", "32" },
      amountPredicate(
        integerToken(1, 0, 32),
        unquotedStringToken(0, 0, "amount")
      )
    );

    assertEquals(
      ParseConflict.EXPECTED_FOLLOWING_INTEGER,
      assertThrows(
        ItemPredicateParseException.class,
        () -> makeCase(
          new String[] { "amount", "*" },
          amountPredicate(
            integerToken(1, 0, 32),
            unquotedStringToken(0, 0, "amount")
          )
        )
      ).getConflict()
    );

    assertEquals(
      ParseConflict.EXPECTED_FOLLOWING_INTEGER,
      assertThrows(
        ItemPredicateParseException.class,
        () -> makeCase(
          new String[] { "amount" },
          amountPredicate(
            integerToken(1, 0, 32),
            unquotedStringToken(0, 0, "amount")
          )
        )
      ).getConflict()
    );
  }

  @Test
  public void shouldParseComplexMultiplePredicates() {
    makeCase(
      new String[] {
        "deter", "3",
        "unbr", "*",
        "\"text", "a\"",
        "regen", "4", "*",
        "dia-pick",
        "\"multi", "arg", "text", "b", "\""
      },
      andJoin(
        new Token[] {
          null, null, null, null, null
        },
        deteriorationPredicate(
          integerToken(1, 0, 3),
          null,
          unquotedStringToken(0, 0, "deter")
        ),
        enchantmentPredicate(
          Enchantment.UNBREAKING,
          integerToken(3, 0, null),
          unquotedStringToken(2, 0, "unbr")
        ),
        new TextSearchPredicate(
          quotedStringToken(4, 0, "text a")
        ),
        potionEffectPredicate(
          PotionEffectType.REGENERATION,
          integerToken(7, 0, 4),
          integerToken(8, 0, null),
          unquotedStringToken(6, 0, "regen")
        ),
        materialPredicate(
          Material.DIAMOND_PICKAXE,
          unquotedStringToken(9, 0, "dia-pick")
        ),
        new TextSearchPredicate(
          quotedStringToken(10, 0, "multi arg text b ")
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
        integerToken(1, 0, 2),
        integerToken(2, 0, 2 * 60 + 30, true, ComparisonMode.EQUALS),
        unquotedStringToken(0, 0, "regen")
      )
    );

    makeCase(
      new String[] { "regen", "*", "2:30" },
      potionEffectPredicate(
        PotionEffectType.REGENERATION,
        integerToken(1, 0, null),
        integerToken(2, 0, 2 * 60 + 30, true, ComparisonMode.EQUALS),
        unquotedStringToken(0, 0, "regen")
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
    makeExceptionCase(new String[] { "amount", "2:30" }, 1, ParseConflict.DOES_NOT_ACCEPT_TIME_NOTATION);
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
        unquotedStringToken(0, 0, "pickax-?"),
        Tag.ITEMS_PICKAXES.getValues()
      )
    );

    makeCase(
      new String[] { "hoe-?" },
      materialsPredicate(
        unquotedStringToken(0, 0, "hoe-?"),
        Tag.ITEMS_HOES.getValues()
      )
    );

    makeCase(
      new String[] { "boots-?" },
      materialsPredicate(
        unquotedStringToken(0, 0, "boots-?"),
        Tag.ITEMS_FOOT_ARMOR.getValues()
      )
    );

    makeCase(
      new String[] { "fen-gat-?" },
      materialsPredicate(
        unquotedStringToken(0, 0, "fen-gat-?"),
        Tag.FENCE_GATES.getValues()
      )
    );

    // Wildcards should not respond with direct matches
    makeCase(
      new String[] { "arrow-?" },
      materialsPredicate(
        unquotedStringToken(0, 0, "arrow-?"),
        List.of(Material.SPECTRAL_ARROW, Material.TIPPED_ARROW)
      )
    );
  }

  @Test
  public void shouldHandleExactKey() {
    makeCase(
      new String[] { "exact", "unbr", "2" },
      exact(
        unquotedStringToken(0, 0, "exact"),
        enchantmentPredicate(
          Enchantment.UNBREAKING,
          integerToken(2, 0, 2),
          unquotedStringToken(1, 0, "unbr")
        )
      )
    );

    makeCase(
      new String[] { "dia-ches", "and", "exact(", "unbr", "fire-prot", "1)", "or", "not", "exact", "feather-fall" },
      orJoin(
        new Token[] {
          unquotedStringToken(6, 0, "or")
        },
        andJoin(
          new Token[] {
            unquotedStringToken(1, 0, "and")
          },
          materialPredicate(
            Material.DIAMOND_CHESTPLATE,
            unquotedStringToken(0, 0, "dia-ches")
          ),
          exact(
            unquotedStringToken(2, 0, "exact"),
            new ParenthesesNode(
              andJoin(
                new Token[] {
                  null
                },
                enchantmentPredicate(
                  Enchantment.UNBREAKING,
                  null,
                  unquotedStringToken(3, 0, "unbr")
                ),
                enchantmentPredicate(
                  Enchantment.FIRE_PROTECTION,
                  integerToken(5, 0, 1),
                  unquotedStringToken(4, 0, "fire-prot")
                )
              )
            )
          )
        ),
        negate(
          unquotedStringToken(7, 0, "not"),
          exact(
            unquotedStringToken(8, 0, "exact"),
            enchantmentPredicate(
              Enchantment.FEATHER_FALLING,
              null,
              unquotedStringToken(9, 0, "feather-fall")
            )
          )
        )
      )
    );
  }

  @Test
  public void shouldUseShortestMatchByAlphabeticIndex() {
    makeCase(
      new String[] { "lu" }, // Luck, Lure
      potionEffectPredicate(
        PotionEffectType.LUCK,
        null,
        null,
        unquotedStringToken(0, 0, "lu")
      )
    );

    makeCase(
      new String[] { "bu" }, // Bucket, Bundle
      materialPredicate(
        Material.BUCKET,
        unquotedStringToken(0, 0, "bu")
      )
    );

    makeCase(
      new String[] { "wh" }, // Wheat, White
      materialPredicate(
        Material.WHEAT,
        unquotedStringToken(0, 0, "wh")
      )
    );
  }

  private void makeExceptionCase(String[] args, int expectedArgumentIndex, ParseConflict expectedConflict) {
    var predicateParser = parserFactory.create(TokenParser.parseTokens(args, 0), false);
    var exception = assertThrows(ItemPredicateParseException.class, predicateParser::parseAst);
    assertEquals(expectedArgumentIndex, exception.getToken().commandArgumentIndex());
    assertEquals(expectedConflict, exception.getConflict());
  }

  private void makeStringificationCase(String[] args, String expected, boolean allowMissingClosingParentheses, boolean stringifyTokens) {
    var predicateParser = parserFactory.create(TokenParser.parseTokens(args, 0), allowMissingClosingParentheses);
    var ast = predicateParser.parseAst();

    assertNotNull(ast);
    assertEquals(expected, ast.stringify(stringifyTokens));
  }

  private void makeCase(String[] args, @Nullable ItemPredicate expected) {
    makeCase(args, expected, false);
  }

  private void makeCase(String[] args, @Nullable ItemPredicate expected, boolean allowMissingClosingParentheses) {
    var predicateParser = parserFactory.create(TokenParser.parseTokens(args, 0), allowMissingClosingParentheses);
    var actual = predicateParser.parseAst();
    equalityChecker.check(expected, actual);
  }
}
