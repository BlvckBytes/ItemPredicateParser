package me.blvckbytes.item_predicate_parser;

import be.seeseemelk.mockbukkit.MockBukkit;
import com.google.gson.GsonBuilder;
import me.blvckbytes.item_predicate_parser.translation.AssetIndex;
import me.blvckbytes.item_predicate_parser.translation.TranslationLanguage;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AssetIndexTests {

  @BeforeAll
  public static void setup() {
    MockBukkit.mock();
  }

  @Test
  public void shouldDownloadFromIndex() throws Exception {
    var assetIndex = new AssetIndex(null); // MockBukkit is at 1.21
    assertEquals("1.21", assetIndex.serverVersion);

    var languageJson = assetIndex.getLanguageFile(TranslationLanguage.GERMAN_DE);
    assertEquals("Diamantspitzhacke", languageJson.get("item.minecraft.diamond_pickaxe").getAsString());
  }

  @Test
  public void shouldDownloadEmbeddedIntoClient() throws Exception {
    var assetIndex = new AssetIndex("1.20");
    assertEquals("1.20", assetIndex.serverVersion);

    var languageJson = assetIndex.getLanguageFile(TranslationLanguage.ENGLISH_US);
    assertEquals("Diamond Pickaxe", languageJson.get("item.minecraft.diamond_pickaxe").getAsString());
  }

  @Test
  public void shouldDownloadOldLangFormatEmbeddedIntoClient() throws Exception {
    var assetIndex = new AssetIndex("1.12");
    assertEquals("1.12", assetIndex.serverVersion);

    var languageJson = assetIndex.getLanguageFile(TranslationLanguage.ENGLISH_US);
    assertEquals("Diamond Pickaxe", languageJson.get("item.pickaxeDiamond.name").getAsString());
  }

  @Test
  public void shouldDownloadOldLangFormatFromIndex() throws Exception {
    var assetIndex = new AssetIndex("1.12");
    assertEquals("1.12", assetIndex.serverVersion);

    var languageJson = assetIndex.getLanguageFile(TranslationLanguage.GERMAN_DE);
    assertEquals("Diamantspitzhacke", languageJson.get("item.pickaxeDiamond.name").getAsString());
  }

  @AfterAll
  public static void tearDown() {
    MockBukkit.unmock();
  }
}
