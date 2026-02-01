package me.blvckbytes.item_predicate_parser;

import me.blvckbytes.item_predicate_parser.translation.AssetIndex;
import me.blvckbytes.item_predicate_parser.translation.TranslationLanguage;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;

import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AssetIndexTests {

  private static final Logger logger = Logger.getAnonymousLogger();

  @BeforeAll
  public static void setup() {
    MockBukkit.mock();
  }

  @Test
  public void shouldDownloadFromIndex() throws Exception {
    var assetIndex = new AssetIndex(null, logger); // MockBukkit is at 1.21.11
    assertEquals("1.21.11", assetIndex.serverVersion.original());

    var languageJson = assetIndex.getLanguageFile(TranslationLanguage.GERMAN_DE);
    assertEquals("Diamantspitzhacke", languageJson.get("item.minecraft.diamond_pickaxe").getAsString());
  }

  @Test
  public void shouldDownloadEmbeddedIntoClient() throws Exception {
    var assetIndex = new AssetIndex("1.20", logger);
    assertEquals("1.20", assetIndex.serverVersion.original());

    var languageJson = assetIndex.getLanguageFile(TranslationLanguage.ENGLISH_US);
    assertEquals("Diamond Pickaxe", languageJson.get("item.minecraft.diamond_pickaxe").getAsString());
  }

  @Test
  public void shouldDownloadOldLangFormatEmbeddedIntoClient() throws Exception {
    var assetIndex = new AssetIndex("1.12", logger);
    assertEquals("1.12", assetIndex.serverVersion.original());

    var languageJson = assetIndex.getLanguageFile(TranslationLanguage.ENGLISH_US);
    assertEquals("Diamond Pickaxe", languageJson.get("item.pickaxeDiamond.name").getAsString());
  }

  @Test
  public void shouldDownloadOldLangFormatFromIndex() throws Exception {
    var assetIndex = new AssetIndex("1.12", logger);
    assertEquals("1.12", assetIndex.serverVersion.original());

    var languageJson = assetIndex.getLanguageFile(TranslationLanguage.GERMAN_DE);
    assertEquals("Diamantspitzhacke", languageJson.get("item.pickaxeDiamond.name").getAsString());
  }

  @AfterAll
  public static void tearDown() {
    MockBukkit.unmock();
  }
}
