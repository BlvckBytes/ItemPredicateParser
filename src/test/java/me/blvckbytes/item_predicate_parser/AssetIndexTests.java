package me.blvckbytes.item_predicate_parser;

import be.seeseemelk.mockbukkit.MockBukkit;
import me.blvckbytes.item_predicate_parser.translation.AssetIndex;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class AssetIndexTests {

  @BeforeAll
  public static void setup() {
    MockBukkit.mock();
  }

  @Test
  public void shouldDownloadFromIndex() throws Exception {
    var assetIndex = new AssetIndex();
    assertEquals("1.21", assetIndex.serverVersion);

    var languageFileUrl = assetIndex.getLanguageFileUrl("de_de.json");
    assertNotNull(languageFileUrl);

    var languageFileContent = assetIndex.makePlainTextGetRequest(languageFileUrl);
    var languageJson = assetIndex.parseJson(languageFileContent);
    assertEquals("Diamantspitzhacke", languageJson.get("item.minecraft.diamond_pickaxe").getAsString());
  }

  @Test
  public void shouldDownloadEmbeddedIntoClient() throws Exception {
    var assetIndex = new AssetIndex();
    assertEquals("1.21", assetIndex.serverVersion);

    var languageFileContent = assetIndex.getClientEmbeddedLanguageFileContents();
    var languageJson = assetIndex.parseJson(languageFileContent);

    assertEquals("Diamond Pickaxe", languageJson.get("item.minecraft.diamond_pickaxe").getAsString());
  }

  @AfterAll
  public static void tearDown() {
    MockBukkit.unmock();
  }
}
