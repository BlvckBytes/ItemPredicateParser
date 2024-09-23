package me.blvckbytes.item_predicate_parser;

import me.blvckbytes.item_predicate_parser.translation.LangKeyedSource;
import me.blvckbytes.item_predicate_parser.translation.keyed.LangKeyedEnchantment;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.junit.jupiter.api.Test;

import java.util.List;

public class TranslationRegistryTests extends ParseTestBase {

  private static final List<LangKeyedEnchantment> ENCHANTMENTS = Registry.ENCHANTMENT.stream().map(LangKeyedEnchantment::new).toList();
  private static final LangKeyedEnchantment UNBREAKING = new LangKeyedEnchantment(Enchantment.UNBREAKING);

  @Test
  public void shouldAppendCollisionPrefixesBetweenSources() {
    new CollisionPrefixCaseBuilder(translationRegistry)
      .withSource(new LangKeyedSource(ENCHANTMENTS, "[Enchantment] "))
      .withSingleSource(UNBREAKING, "[Custom] ")
      .expectResult(UNBREAKING, "[Enchantment] ")
      .expectResult(UNBREAKING, "[Custom] ")
      .execute("unbr");
  }

  @Test
  public void shouldAppendCollisionPrefixesInSameSource() {
    new CollisionPrefixCaseBuilder(translationRegistry)
      .withSource(new LangKeyedSource(List.of(
        UNBREAKING, UNBREAKING
      ), ""))
      .expectResult(UNBREAKING, "1 ")
      .expectResult(UNBREAKING, "2 ")
      .execute("unbr");
  }

  @Test
  public void shouldCombineCollisionPrefixes() {
    new CollisionPrefixCaseBuilder(translationRegistry)
      .withSource(new LangKeyedSource(List.of(
        UNBREAKING, UNBREAKING
      ), "[A]"))
      .withSingleSource(UNBREAKING, "[B]")
      .expectResult(UNBREAKING, "[A] 1 ")
      .expectResult(UNBREAKING, "[A] 2 ")
      .expectResult(UNBREAKING, "[B] ")
      .execute("unbr");
  }
}
