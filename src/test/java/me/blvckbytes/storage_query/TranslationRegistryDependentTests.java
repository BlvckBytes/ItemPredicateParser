package me.blvckbytes.storage_query;

import be.seeseemelk.mockbukkit.MockBukkit;
import me.blvckbytes.storage_query.translation.TranslationRegistry;
import org.bukkit.Keyed;
import org.bukkit.Registry;
import org.bukkit.Translatable;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public abstract class TranslationRegistryDependentTests {

  protected static final Logger logger = Logger.getAnonymousLogger();
  protected static TranslationRegistry translationregistry;

  @BeforeAll
  public static void setup() {
    MockBukkit.mock();

    translationregistry = TranslationRegistry.load("/en_us.json", makeSources(), logger);
    assertNotNull(translationregistry);
  }

  @AfterAll
  public static void tearDown() {
    MockBukkit.unmock();
  }

  /*
    For whatever odd reason, MockBukkit does not implement getTranslationKey() on Enchantments and Effects.
   */

  private static Iterable<Iterable<? extends Translatable>> makeSources() {
    return Arrays.asList(
      Registry.ENCHANTMENT.stream().map(x -> patchTranslatable(x, "enchantment.minecraft.")).toList(),
      Registry.MATERIAL,
      Registry.EFFECT.stream().map(x -> patchTranslatable(x, "effect.minecraft.")).toList()
    );
  }

  private static <T extends Translatable & Keyed> T patchTranslatable(T translatable, String prefix) {
    var spiedTranslatable = Mockito.spy(translatable);
    Mockito.doReturn(prefix + translatable.getKey().value()).when(spiedTranslatable).getTranslationKey();
    return spiedTranslatable;
  }
}
