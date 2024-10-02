package me.blvckbytes.item_predicate_parser;

import me.blvckbytes.item_predicate_parser.translation.resolver.TranslationResolver;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TranslationResolverTests {

  @Test
  public void shouldSanitizeSimpleColors() {
    makeCase("&cMyEnchant", "MyEnchant");
    makeCase("&c MyEnchant", "MyEnchant");
    makeCase("&c My&bEnchant&d ", "MyEnchant");
  }

  @Test
  public void shouldSanitizeHexColors() {
    makeCase("&#FAC9fbMyEnchantment", "MyEnchantment");
    makeCase("&#FACMyEnchantment", "MyEnchantment");
    makeCase("&#FAC9fbabcdeMyEnchantment", "abcdeMyEnchantment");

    // Leave malformed as-is
    makeCase("&#MyEnchantment", "&#MyEnchantment");
    makeCase("&#FMyEnchantment", "&#FMyEnchantment");
    makeCase("&#FAMyEnchantment", "&#FAMyEnchantment");
    makeCase("&#FAC9MyEnchantment", "&#FAC9MyEnchantment");
    makeCase("&#FAC9fMyEnchantment", "&#FAC9fMyEnchantment");
  }

  @Test
  public void shouldSanitizeXML() {
    makeCase("<red>MyEnchantment</red>", "MyEnchantment");
    makeCase("<red>MyEnchantment", "MyEnchantment");
    makeCase("MyEnchantment</red>", "MyEnchantment");
    makeCase("MyEnchantment<red>", "MyEnchantment");
    makeCase("<tag:argument>MyEnchantment</tag>", "MyEnchantment");
    makeCase("<tag:argument:\"string\">MyEnchantment</tag>", "MyEnchantment");
    makeCase("<tag:argument:'string'>MyEnchantment</tag>", "MyEnchantment");
    makeCase("<tag:argument:\"<inner-tag></inner-tag>\">MyEnchantment</tag>", "MyEnchantment");
    makeCase("<tag:argument:\"<inner-tag>\">MyEnchantment</tag>", "MyEnchantment");
    makeCase("<tag:argument:\">\">MyEnchantment</tag>", "MyEnchantment");
    makeCase("<tag:argument:'<inner-tag></inner-tag>'>MyEnchantment</tag>", "MyEnchantment");
    makeCase("<tag:argument:'<inner-tag>'>MyEnchantment</tag>", "MyEnchantment");
    makeCase("<tag:argument:'>'>MyEnchantment</tag>", "MyEnchantment");

    // Let's exercise the quote-stack ;)
    makeCase(
      "<tag:\"<tag:'<tag:\"<tag:'>'\">'>\">MyEnchantment",
      "MyEnchantment"
    );

    // Should keep malformed tags, meaning tags who miss their closing char: >

    makeCase("<tagMyEnchantment", "<tagMyEnchantment");
    makeCase("<tag MyEnchantment", "<tag MyEnchantment");
    makeCase("<tag:argMyEnchantment", "<tag:argMyEnchantment");
    makeCase("<tag:\"arg\" MyEnchantment", "<tag:\"arg\" MyEnchantment");
    makeCase("<tag:\"arg\" MyEnchantment <tag2", "<tag:\"arg\" MyEnchantment <tag2");
    makeCase("<tag:\"arg\" MyEnchantment <tag2", "<tag:\"arg\" MyEnchantment <tag2");
  }

  private void makeCase(String input, String expected) {
    assertEquals(expected, TranslationResolver.sanitize(input));

    // Also make sure that the internal version of colors passes the tests
    assertEquals(
      expected.replace('&', '§'),
      TranslationResolver.sanitize(input.replace('&', '§'))
    );
  }
}
