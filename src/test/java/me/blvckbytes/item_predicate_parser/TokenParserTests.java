package me.blvckbytes.item_predicate_parser;

import me.blvckbytes.item_predicate_parser.parse.ArgumentParseException;
import me.blvckbytes.item_predicate_parser.parse.ParseConflict;
import me.blvckbytes.item_predicate_parser.parse.TokenParser;
import me.blvckbytes.item_predicate_parser.token.*;
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
    makeExceptionCase(new String[] { "22a" }, 0, ParseConflict.EXPECTED_CORRECT_INTEGER);
    makeExceptionCase(new String[] { "\"hello" }, 0, ParseConflict.MISSING_STRING_TERMINATION);
    makeExceptionCase(new String[] { "hello", "\"world", "22" }, 1, ParseConflict.MISSING_STRING_TERMINATION);
    makeExceptionCase(new String[] { "\"hel\"lo\"" }, 0, ParseConflict.MALFORMED_STRING_ARGUMENT);
    makeExceptionCase(new String[] { "\"hello", "\"a" }, 1, ParseConflict.MALFORMED_STRING_ARGUMENT);
    makeExceptionCase(new String[] { "hello\"" }, 0, ParseConflict.MALFORMED_STRING_ARGUMENT);
    makeExceptionCase(new String[] { "h\"ello" }, 0, ParseConflict.MALFORMED_STRING_ARGUMENT);
  }

  @Test
  public void shouldParseTimeNotation() {
    makeCase(
      new String[] { "2:30" },
      new Token[] {
        new IntegerToken(0, 2*60 + 30, true, ComparisonMode.EQUALS)
      }
    );

    makeCase(
      new String[] { "12:23" },
      new Token[] {
        new IntegerToken(0, 12*60 + 23, true, ComparisonMode.EQUALS)
      }
    );

    makeCase(
      new String[] { "12:34:56" },
      new Token[] {
        new IntegerToken(0, 12*60*60 + 34*60 + 56, true, ComparisonMode.EQUALS)
      }
    );
  }

  @Test
  public void shouldParseComparisonNotation() {
    makeCase(
      new String[] { ">200" },
      new Token[] {
        new IntegerToken(0, 200, false, ComparisonMode.GREATER_THAN)
      }
    );

    makeCase(
      new String[] { ">2:20" },
      new Token[] {
        new IntegerToken(0, 2*60+20, true, ComparisonMode.GREATER_THAN)
      }
    );

    makeCase(
      new String[] { "<200" },
      new Token[] {
        new IntegerToken(0, 200, false, ComparisonMode.LESS_THAN)
      }
    );

    makeCase(
      new String[] { "<2:20" },
      new Token[] {
        new IntegerToken(0, 2*60+20, true, ComparisonMode.LESS_THAN)
      }
    );
  }

  @Test
  public void shouldParseParentheses() {
    makeCase(
      new String[] { "(abc-de", "f", "\"free", "text", ")", "(", "searc)h\")" },
      new Token[] {
        new ParenthesisToken(0, true),
        new UnquotedStringToken(0, "abc-de"),
        new UnquotedStringToken(1, "f"),
        new QuotedStringToken(2, "free text ) ( searc)h"),
        new ParenthesisToken(6, false)
      }
    );

    makeCase(
      new String[] { "(abc-de)" },
      new Token[] {
        new ParenthesisToken(0, true),
        new UnquotedStringToken(0, "abc-de"),
        new ParenthesisToken(0, false),
      }
    );

    makeCase(
      new String[] { "(\"hello\")" },
      new Token[] {
        new ParenthesisToken(0, true),
        new QuotedStringToken(0, "hello"),
        new ParenthesisToken(0, false),
      }
    );

    makeCase(
      new String[] { "(", "abc-de", "\"a", "test\"", ")" },
      new Token[] {
        new ParenthesisToken(0, true),
        new UnquotedStringToken(1, "abc-de"),
        new QuotedStringToken(2, "a test"),
        new ParenthesisToken(4, false),
      }
    );

    makeCase(
      new String[] { "(((test))))" },
      new Token[] {
        new ParenthesisToken(0, true),
        new ParenthesisToken(0, true),
        new ParenthesisToken(0, true),
        new UnquotedStringToken(0, "test"),
        new ParenthesisToken(0, false),
        new ParenthesisToken(0, false),
        new ParenthesisToken(0, false),
        new ParenthesisToken(0, false),
      }
    );

    makeCase(
      new String[] { "(", "(", "(", "test", ")", ")", ")", ")" },
      new Token[] {
        new ParenthesisToken(0, true),
        new ParenthesisToken(1, true),
        new ParenthesisToken(2, true),
        new UnquotedStringToken(3, "test"),
        new ParenthesisToken(4, false),
        new ParenthesisToken(5, false),
        new ParenthesisToken(6, false),
        new ParenthesisToken(7, false),
      }
    );

    makeCase(
      new String[] { "exact(" },
      new Token[] {
        new UnquotedStringToken(0, "exact"),
        new ParenthesisToken(0, true)
      }
    );
  }

  private void makeExceptionCase(String[] args, int expectedArgumentIndex, ParseConflict expectedConflict) {
    var exception = assertThrows(ArgumentParseException.class, () -> TokenParser.parseTokens(args, 0));
    assertEquals(expectedArgumentIndex, exception.getArgumentIndex());
    assertEquals(expectedConflict, exception.getConflict());
  }

  private void makeCase(String[] args, Token[] expectedTokens) {
    var actualTokens = TokenParser.parseTokens(args, 0);

    assertEquals(expectedTokens.length, actualTokens.size());

    for (var i = 0; i < expectedTokens.length; ++i)
      assertEquals(expectedTokens[i], actualTokens.get(i));
  }
}
