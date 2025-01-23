package me.blvckbytes.item_predicate_parser;

import org.bukkit.Material;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SingletonTranslationTests extends ParseTestBase {

  @Test
  public void shouldRespondToBukkitSingletonsWithVanillaTranslations() {
    assertEquals("Diamond Chestplate", translationRegistry.getTranslationBySingleton(Material.DIAMOND_CHESTPLATE));
    assertEquals("Explorer Pottery Sherd", translationRegistry.getTranslationBySingleton(Material.EXPLORER_POTTERY_SHERD));
  }
}
