package me.blvckbytes.item_predicate_parser;

import me.blvckbytes.item_predicate_parser.parse.ItemPredicateParseException;
import me.blvckbytes.item_predicate_parser.parse.ParseConflict;
import me.blvckbytes.item_predicate_parser.parse.TokenParser;
import me.blvckbytes.item_predicate_parser.token.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TokenParserTests extends MakeAndCompareTokens {

  @Test
  public void shouldParseAllTokenTypes() {
    makeCase(
      new String[] {
        "my-unquoted-string-a", "512", "\"single-arg-quoted-string-a\"",
        "\"multi", "arg", "quoted", "string\"", "32", "my-unquoted-string-b",
        "\"single-arg-quoted-string-b\"", "\"multi", "arg", "quoted", "string-2\""
      },
      new Token[] {
        unquotedStringToken(0, 0, "my-unquoted-string-a"),
        integerToken(1, 0, 512),
        quotedStringToken(2, 0, "single-arg-quoted-string-a"),
        quotedStringToken(3, 0, "multi arg quoted string"),
        integerToken(7, 0, 32),
        unquotedStringToken(8, 0, "my-unquoted-string-b"),
        quotedStringToken(9, 0, "single-arg-quoted-string-b"),
        quotedStringToken(10, 0, "multi arg quoted string-2"),
      }
    );

    // Trailing whitespace on quoted string
    makeCase(
      new String[] { "\"hello", "world", "\"" },
      new Token[] {
        quotedStringToken(0, 0, "hello world "),
      }
    );

    // Leading whitespace on quoted string
    makeCase(
      new String[] { "\"", "hello", "world\"" },
      new Token[] {
        quotedStringToken(0, 0, " hello world"),
      }
    );

    // Both trailing and leading whitespace on quoted string
    makeCase(
      new String[] { "\"", "hello", "world", "\"" },
      new Token[] {
        quotedStringToken(0, 0, " hello world "),
      }
    );

    // Support back-to-back tokens if separated by a string (unambiguous, why not)
    makeCase(
      new String[] { "b\"hello", "\"a" },
      new Token[] {
        unquotedStringToken(0, 0, "b"),
        quotedStringToken(0, 1, "hello "),
        unquotedStringToken(1, 1, "a")
      }
    );

    makeCase(
      new String[] {
        "\"This", "string", "contains", "\\\"", "escaped", "quotes", "\\\"\""
      },
      new Token[] {
        quotedStringToken(0, 0, "This string contains \" escaped quotes \"")
      }
    );
  }

  @Test
  public void shouldThrowOnEmptyOrBlankStrings() {
    makeExceptionCase(
      new String[] {
        "\"\""
      },
      0,
      ParseConflict.EMPTY_OR_BLANK_QUOTED_STRING
    );

    makeExceptionCase(
      new String[] {
        "\" \""
      },
      0,
      ParseConflict.EMPTY_OR_BLANK_QUOTED_STRING
    );

    makeExceptionCase(
      new String[] {
        "hello-world", "\"\t\n\r \""
      },
      1,
      ParseConflict.EMPTY_OR_BLANK_QUOTED_STRING
    );

    makeExceptionCase(
      new String[] {
        "hello-world", "\"       \""
      },
      1,
      ParseConflict.EMPTY_OR_BLANK_QUOTED_STRING
    );
  }

  @Test
  public void shouldSetCorrectFirstCharIndex() {
    makeCase(
      new String[] { "abc(def(geh))" },
      new Token[] {
        unquotedStringToken(0, 0, "abc"),
        parenthesisToken(0, 3, true),
        unquotedStringToken(0, 4, "def"),
        parenthesisToken(0, 7, true),
        unquotedStringToken(0, 8, "geh"),
        parenthesisToken(0, 11, false),
        parenthesisToken(0, 12, false)
      }
    );
  }

  @Test
  public void shouldThrowOnMalformedInput() {
    makeExceptionCase(new String[] { "22a" }, 0, ParseConflict.EXPECTED_CORRECT_INTEGER);
    makeExceptionCase(new String[] { "\"hello" }, 0, ParseConflict.MISSING_STRING_TERMINATION);
    makeExceptionCase(new String[] { "hello", "\"world", "22" }, 1, ParseConflict.MISSING_STRING_TERMINATION);
    makeExceptionCase(new String[] { "\"hel\"lo\"" }, 0, ParseConflict.MISSING_STRING_TERMINATION);
    makeExceptionCase(new String[] { "hello\"" }, 0, ParseConflict.MISSING_STRING_TERMINATION);
    makeExceptionCase(new String[] { "h\"ello" }, 0, ParseConflict.MISSING_STRING_TERMINATION);
  }

  @Test
  public void shouldParseTimeNotation() {
    makeCase(
      new String[] { "2:30" },
      new Token[] {
        integerToken(0, 0, 2*60 + 30, true, ComparisonMode.EQUALS)
      }
    );

    makeCase(
      new String[] { "12:23" },
      new Token[] {
        integerToken(0, 0, 12*60 + 23, true, ComparisonMode.EQUALS)
      }
    );

    makeCase(
      new String[] { "12:34:56" },
      new Token[] {
        integerToken(0, 0, 12*60*60 + 34*60 + 56, true, ComparisonMode.EQUALS)
      }
    );

    makeCase(
      new String[] { "12::56" },
      new Token[] {
        integerToken(0, 0, 12*60*60 + 56, true, ComparisonMode.EQUALS)
      }
    );
  }

  @Test
  public void shouldParseComparisonNotation() {
    makeCase(
      new String[] { ">200" },
      new Token[] {
        integerToken(0, 0, 200, false, ComparisonMode.GREATER_THAN)
      }
    );

    makeCase(
      new String[] { ">2:20" },
      new Token[] {
        integerToken(0, 0, 2*60+20, true, ComparisonMode.GREATER_THAN)
      }
    );

    makeCase(
      new String[] { "<200" },
      new Token[] {
        integerToken(0, 0, 200, false, ComparisonMode.LESS_THAN)
      }
    );

    makeCase(
      new String[] { "<2:20" },
      new Token[] {
        integerToken(0, 0, 2*60+20, true, ComparisonMode.LESS_THAN)
      }
    );

    makeExceptionCase(
      new String[] { "1:2:3:4" },
      0,
      ParseConflict.TOO_MANY_TIME_NOTATION_BLOCKS
    );
  }

  @Test
  public void shouldParseParentheses() {
    makeCase(
      new String[] { "(abc-de", "f", "\"free", "text", ")", "(", "searc)h\")" },
      new Token[] {
        parenthesisToken(0, 0, true),
        unquotedStringToken(0, 1, "abc-de"),
        unquotedStringToken(1, 0, "f"),
        quotedStringToken(2, 0, "free text ) ( searc)h"),
        parenthesisToken(6, 8, false)
      }
    );

    makeCase(
      new String[] { "(abc-de)" },
      new Token[] {
        parenthesisToken(0, 0, true),
        unquotedStringToken(0, 1, "abc-de"),
        parenthesisToken(0, 7, false),
      }
    );

    makeCase(
      new String[] { "(\"hello\")" },
      new Token[] {
        parenthesisToken(0, 0, true),
        quotedStringToken(0, 1, "hello"),
        parenthesisToken(0, 8, false),
      }
    );

    makeCase(
      new String[] { "(", "abc-de", "\"a", "test\"", ")" },
      new Token[] {
        parenthesisToken(0, 0, true),
        unquotedStringToken(1, 0, "abc-de"),
        quotedStringToken(2, 0, "a test"),
        parenthesisToken(4, 0, false),
      }
    );

    makeCase(
      new String[] { "(((test))))" },
      new Token[] {
        parenthesisToken(0, 0, true),
        parenthesisToken(0, 1, true),
        parenthesisToken(0, 2, true),
        unquotedStringToken(0, 3, "test"),
        parenthesisToken(0, 7, false),
        parenthesisToken(0, 8, false),
        parenthesisToken(0, 9, false),
        parenthesisToken(0, 10, false),
      }
    );

    makeCase(
      new String[] { "(", "(", "(", "test", ")", ")", ")", ")" },
      new Token[] {
        parenthesisToken(0, 0, true),
        parenthesisToken(1, 0, true),
        parenthesisToken(2, 0, true),
        unquotedStringToken(3, 0, "test"),
        parenthesisToken(4, 0, false),
        parenthesisToken(5, 0, false),
        parenthesisToken(6, 0, false),
        parenthesisToken(7, 0, false),
      }
    );

    makeCase(
      new String[] { "exact(" },
      new Token[] {
        unquotedStringToken(0, 0, "exact"),
        parenthesisToken(0, 5, true)
      }
    );
  }

  private void makeExceptionCase(String[] args, int expectedArgumentIndex, ParseConflict expectedConflict) {
    var exception = assertThrows(ItemPredicateParseException.class, () -> TokenParser.parseTokens(args, 0));
    assertEquals(expectedArgumentIndex, exception.getArgumentIndex());
    assertEquals(expectedConflict, exception.getConflict());
  }

  private void makeCase(String[] args, Token[] expectedTokens) {
    // Always execute cases on both types of parsing methods
    validateTokens(expectedTokens, TokenParser.parseTokens(args, 0));
    validateTokens(expectedTokens, TokenParser.parseTokens(String.join(" ", args)));
  }

  private void validateTokens(Token[] expectedTokens, List<Token> actualTokens) {
    assertEquals(expectedTokens.length, actualTokens.size());

    for (var i = 0; i < expectedTokens.length; ++i)
      compareTokens(expectedTokens[i], actualTokens.get(i));
  }
}
