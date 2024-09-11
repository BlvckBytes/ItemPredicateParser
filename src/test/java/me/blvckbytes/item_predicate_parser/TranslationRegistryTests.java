package me.blvckbytes.item_predicate_parser;

import me.blvckbytes.item_predicate_parser.translation.TranslatableSource;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.junit.jupiter.api.Test;

import java.util.List;

public class TranslationRegistryTests extends TranslationRegistryDependentTests {

  @Test
  public void shouldAppendCollisionPrefixesBetweenSources() {
    new CollisionPrefixCaseBuilder(translationRegistry)
      .withSource(new TranslatableSource(Registry.ENCHANTMENT, "[Enchantment] "))
      .withSingleSource(Enchantment.UNBREAKING, "[Custom] ")
      .expectResult(Enchantment.UNBREAKING, "[Enchantment] ")
      .expectResult(Enchantment.UNBREAKING, "[Custom] ")
      .execute("unbr");
  }

  @Test
  public void shouldAppendCollisionPrefixesInSameSource() {
    new CollisionPrefixCaseBuilder(translationRegistry)
      .withSource(new TranslatableSource(List.of(
        Enchantment.UNBREAKING, Enchantment.UNBREAKING
      ), ""))
      .expectResult(Enchantment.UNBREAKING, "1 ")
      .expectResult(Enchantment.UNBREAKING, "2 ")
      .execute("unbr");
  }

  @Test
  public void shouldCombineCollisionPrefixes() {
    new CollisionPrefixCaseBuilder(translationRegistry)
      .withSource(new TranslatableSource(List.of(
        Enchantment.UNBREAKING, Enchantment.UNBREAKING
      ), "[A]"))
      .withSingleSource(Enchantment.UNBREAKING, "[B]")
      .expectResult(Enchantment.UNBREAKING, "[A] 1 ")
      .expectResult(Enchantment.UNBREAKING, "[A] 2 ")
      .expectResult(Enchantment.UNBREAKING, "[B] ")
      .execute("unbr");
  }
}
