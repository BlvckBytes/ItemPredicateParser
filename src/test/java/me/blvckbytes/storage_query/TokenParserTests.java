package me.blvckbytes.storage_query;

import me.blvckbytes.storage_query.parse.ArgumentParseException;
import me.blvckbytes.storage_query.parse.ParseConflict;
import me.blvckbytes.storage_query.parse.TokenParser;
import me.blvckbytes.storage_query.token.IntegerToken;
import me.blvckbytes.storage_query.token.QuotedStringToken;
import me.blvckbytes.storage_query.token.Token;
import me.blvckbytes.storage_query.token.UnquotedStringToken;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TokenParserTests {

  @Test
  public void shouldParseAllTokenTypes() {
    makeCase(
      new String[] {
        "my-unquoted-string-a", "512", "\"single-arg-quoted-string-a\"",
        "\"multi", "arg", "quoted", "string\"", "32", "my-unquoted-string-b",
        "\"single-arg-quoted-string-b\"", "\"multi", "arg", "quoted", "string-2\""
      },
      new Token[] {
        new UnquotedStringToken(0, "my-unquoted-string-a"),
        new IntegerToken(1, 512),
        new QuotedStringToken(2, "single-arg-quoted-string-a"),
        new QuotedStringToken(3, "multi arg quoted string"),
        new IntegerToken(7, 32),
        new UnquotedStringToken(8, "my-unquoted-string-b"),
        new QuotedStringToken(9, "single-arg-quoted-string-b"),
        new QuotedStringToken(10, "multi arg quoted string-2"),
      }
    );

    // Trailing whitespace on quoted string
    makeCase(
      new String[] { "\"hello", "world", "\"" },
      new Token[] {
        new QuotedStringToken(0, "hello world "),
      }
    );

    // Leading whitespace on quoted string
    makeCase(
      new String[] { "\"", "hello", "world\"" },
      new Token[] {
        new QuotedStringToken(0, " hello world"),
      }
    );

    // Both trailing and leading whitespace on quoted string
    makeCase(
      new String[] { "\"", "hello", "world", "\"" },
      new Token[] {
        new QuotedStringToken(0, " hello world "),
      }
    );
  }

  @Test
  public void shouldThrowOnMalformedInput() {
    makeExceptionCase(new String[] { "22a" }, 0, ParseConflict.EXPECTED_INTEGER);
    makeExceptionCase(new String[] { "\"hello" }, 0, ParseConflict.MISSING_STRING_TERMINATION);
    makeExceptionCase(new String[] { "hello", "\"world", "22" }, 1, ParseConflict.MISSING_STRING_TERMINATION);
    makeExceptionCase(new String[] { "\"hel\"lo\"" }, 0, ParseConflict.MALFORMED_STRING_ARGUMENT);
    makeExceptionCase(new String[] { "\"hello", "\"a" }, 0, ParseConflict.MALFORMED_STRING_ARGUMENT);
    makeExceptionCase(new String[] { "hello\"" }, 0, ParseConflict.MALFORMED_STRING_ARGUMENT);
    makeExceptionCase(new String[] { "h\"ello" }, 0, ParseConflict.MALFORMED_STRING_ARGUMENT);
  }

  private void makeExceptionCase(String[] args, int expectedArgumentIndex, ParseConflict expectedConflict) {
    var exception = assertThrows(ArgumentParseException.class, () -> TokenParser.parseTokens(args));
    assertEquals(expectedArgumentIndex, exception.getArgumentIndex());
    assertEquals(expectedConflict, exception.getConflict());
  }

  private void makeCase(String[] args, Token[] expectedTokens) {
    var actualTokens = TokenParser.parseTokens(args);

    assertEquals(expectedTokens.length, actualTokens.size());

    for (var i = 0; i < expectedTokens.length; ++i)
      assertEquals(expectedTokens[i], actualTokens.get(i));
  }
}
