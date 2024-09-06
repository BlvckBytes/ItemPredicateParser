package me.blvckbytes.item_predicate_parser;

import be.seeseemelk.mockbukkit.MockBukkit;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import me.blvckbytes.item_predicate_parser.parse.PredicateParserFactory;
import me.blvckbytes.item_predicate_parser.translation.*;
import org.bukkit.Registry;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

public abstract class TranslationRegistryDependentTests {

  private static final Gson gson = new GsonBuilder().create();
  protected static final Logger logger = Logger.getAnonymousLogger();
  protected static TranslationRegistry translationRegistry;
  protected static PredicateParserFactory parserFactory;

  @BeforeAll
  public static void setup() throws IOException {
    MockBukkit.mock();

    try (var inputStream = TranslationRegistry.class.getResourceAsStream("/en_us.json")) {
      if (inputStream == null)
        throw new IllegalStateException("Resource stream was null");

      var languageJson = gson.fromJson(new InputStreamReader(inputStream), JsonObject.class);

      translationRegistry = new TranslationRegistry(languageJson, logger);
      translationRegistry.initialize(makeSources());
    }

    parserFactory = new PredicateParserFactory(translationRegistry);
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
