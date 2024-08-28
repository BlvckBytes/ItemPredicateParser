package me.blvckbytes.storage_query;

import be.seeseemelk.mockbukkit.MockBukkit;
import me.blvckbytes.storage_query.parse.PredicateParser;
import me.blvckbytes.storage_query.translation.*;
import org.bukkit.Keyed;
import org.bukkit.Registry;
import org.bukkit.Translatable;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public abstract class TranslationRegistryDependentTests {

  protected static final Logger logger = Logger.getAnonymousLogger();
  protected static TranslationRegistry translationRegistry;
  private static final Map<Translatable, Translatable> spyByMock = new HashMap<>();

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

  @SuppressWarnings("unchecked")
  protected static <T extends Translatable> T getSpy(T mock) {
    return (T) spyByMock.get(mock);
  }


  /*
    For whatever odd reason, MockBukkit does not implement getTranslationKey() on Enchantments and Effects.
   */

  private static Iterable<TranslatableSource> makeSources() {
    return Arrays.asList(
      new TranslatableSource(
        Registry.ENCHANTMENT.stream().map(x -> patchTranslatable(x, "enchantment.minecraft.")).toList(),
        "[Enchantment] "
      ),
      new TranslatableSource(
        Registry.EFFECT.stream().map(x -> patchTranslatable(x, "effect.minecraft.")).toList(),
        "[Effect] "
      ),
      new TranslatableSource(Registry.MATERIAL, "[Material] "),
      new TranslatableSource(List.of(DeteriorationKey.INSTANCE), ""),
      new TranslatableSource(List.of(NegationKey.INSTANCE), ""),
      new TranslatableSource(List.of(DisjunctionKey.INSTANCE), ""),
      new TranslatableSource(List.of(ConjunctionKey.INSTANCE), "")
    );
  }

  private static <T extends Translatable & Keyed> T patchTranslatable(T translatable, String prefix) {
    var spiedTranslatable = Mockito.spy(translatable);
    Mockito.doReturn(prefix + translatable.getKey().value()).when(spiedTranslatable).getTranslationKey();
    spyByMock.put(translatable, spiedTranslatable);
    return spiedTranslatable;
  }
}
