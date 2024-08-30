package me.blvckbytes.storage_query;

import be.seeseemelk.mockbukkit.MockBukkit;
import me.blvckbytes.storage_query.translation.*;
import org.bukkit.Registry;
import org.bukkit.Translatable;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public abstract class TranslationRegistryDependentTests {

  protected static final Logger logger = Logger.getAnonymousLogger();
  protected static TranslationRegistry translationRegistry;

  @BeforeAll
  public static void setup() {
    MockBukkit.mock();

    translationRegistry = TranslationRegistry.load("/en_us.json", makeSources(), logger);
    assertNotNull(translationRegistry);
  }

  @AfterAll
  public static void tearDown() {
    MockBukkit.unmock();
  }

  private static Iterable<TranslatableSource> makeSources() {
    return Arrays.asList(
      new TranslatableSource(Registry.ENCHANTMENT, "[Enchantment] "),
      new TranslatableSource(Registry.EFFECT, "[Effect] "),
      new TranslatableSource(Registry.MATERIAL, "[Material] "),
      new TranslatableSource(List.of(
        DeteriorationKey.INSTANCE,
        NegationKey.INSTANCE,
        DisjunctionKey.INSTANCE,
        ConjunctionKey.INSTANCE,
        ExactKey.INSTANCE,
        AmountKey.INSTANCE
      ), "")
    );
  }
}
