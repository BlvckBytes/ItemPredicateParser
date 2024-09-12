package me.blvckbytes.item_predicate_parser;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.blvckbytes.item_predicate_parser.translation.LangToJsonUtil;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LangToJsonUtilTests {

  private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

  @Test
  public void shouldParseComplexInputCorrectly() {
    var result = LangToJsonUtil.convertLangContentsToJsonObject(
      """
        
        ## This is a fully commented line
        first key=first value\s
        second key=second value##partially commented line
        third key=third value ##partially commented line
        
        fourth key=fourth value
        
        """
    );

    assertEquals(
      """
        {
          "first key": "first value ",
          "second key": "second value",
          "third key": "third value ",
          "fourth key": "fourth value"
        }""",
      gson.toJson(result)
    );
  }
}
