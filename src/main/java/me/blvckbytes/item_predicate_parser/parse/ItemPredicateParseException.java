package me.blvckbytes.item_predicate_parser.parse;

import at.blvckbytes.cm_mapper.cm.ComponentMarkup;
import at.blvckbytes.component_markup.constructor.SlotType;
import at.blvckbytes.component_markup.expression.interpreter.InterpretationEnvironment;
import me.blvckbytes.item_predicate_parser.token.Token;
import net.kyori.adventure.text.Component;

public class ItemPredicateParseException extends RuntimeException {

  private final Token token;
  private final ParseConflict conflict;

  public ItemPredicateParseException(Token token, ParseConflict conflict) {
    this.token = token;
    this.conflict = conflict;
  }

  public Token getToken() {
    return token;
  }

  public ParseConflict getConflict() {
    return conflict;
  }

  public Component highlightedInput(ComponentMarkup malformedRender, ComponentMarkup remainingRender) {
    var result = Component.empty();

    var input = token.parserInput();
    var args = input.getInputAsArguments();

    String part;

    /*
                            begin                        end
      argumentIndex         0                            5
      charIndex             8                            6
                    unquoted"hello world this is a string"another
     */

    // This is a bit verbose... but maybe I shouldn't try to be too clever about it.

    var encounteredNonEmpty = false;

    for (var argIndex = input.getArgumentsOffset(); argIndex < args.length; ++argIndex) {
      var arg = args[argIndex];

      // Arg-separating spaces
      if (encounteredNonEmpty)
        result = result.append(Component.space());

      if (arg.isEmpty())
        continue;

      encounteredNonEmpty = true;

      // The current argument lies outside the span of the token
      if (argIndex < token.beginCommandArgumentIndex() || argIndex > token.endCommandArgumentIndex()) {
        result = result.append(renderPredicatePart(remainingRender, arg));
        continue;
      }

      // The token has its beginning within the current argument
      if (argIndex == token.beginCommandArgumentIndex()) {
        // And also its end => the token is contained entirely within the current argument
        if (argIndex == token.endCommandArgumentIndex()) {
          // The current argument ends with the token
          if (arg.length() == token.endLastCharIndex() + 1) {
            // But it does not begin with it, so there's a leading remainder
            if (token.beginFirstCharIndex() != 0) {
              part = arg.substring(0, token.beginFirstCharIndex());
              result = result.append(renderPredicatePart(remainingRender, part));
            }

            part = arg.substring(token.beginFirstCharIndex());
            result = result.append(renderPredicatePart(malformedRender, part));
            continue;
          }

          // The current argument does not end with the token, so there's a trailing remainder
          // And it also does not begin with it, so there's a leading remainder
          if (token.beginFirstCharIndex() != 0) {
            part = arg.substring(0, token.beginFirstCharIndex());
            result = result.append(renderPredicatePart(remainingRender, part));
          }

          // Span of the token somewhere in the middle
          part = arg.substring(token.beginFirstCharIndex(), token.endLastCharIndex() + 1);
          result = result.append(renderPredicatePart(malformedRender, part));

          // Trailing content
          part = arg.substring(token.endLastCharIndex() + 1);
          result = result.append(renderPredicatePart(remainingRender, part));

          continue;
        }

        // The token begins within this argument, but spans further than that

        // But it does not begin with it, so there's a leading remainder
        if (token.beginFirstCharIndex() != 0) {
          part = arg.substring(0, token.beginFirstCharIndex());
          result = result.append(renderPredicatePart(remainingRender, part));
        }

        // The rest is part of the token, seeing how it will also occupy successor token(s)
        part = arg.substring(token.beginFirstCharIndex());
        result = result.append(renderPredicatePart(malformedRender, part));

        continue;
      }

      // The token had its beginning in another argument, so there cannot be any leading remainders

      // The token has its end within the current argument
      if (argIndex == token.endCommandArgumentIndex()) {
        // The current argument ends with the token
        if (arg.length() == token.endLastCharIndex() + 1) {
          result = result.append(renderPredicatePart(malformedRender, arg));
          continue;
        }

        // There is trailing content
        part = arg.substring(0, token.endLastCharIndex() + 1);
        result = result.append(renderPredicatePart(malformedRender, part));

        part = arg.substring(token.endLastCharIndex() + 1);
        result = result.append(renderPredicatePart(remainingRender,  part));

        continue;
      }

      // Argument in-between beginning and end of a multi-arg spanning token
      result = result.append(renderPredicatePart(malformedRender, arg));
    }

    return result;
  }

  private Component renderPredicatePart(ComponentMarkup renderer, String input) {
    return renderer.interpret(
      SlotType.SINGLE_LINE_CHAT,
      new InterpretationEnvironment()
        .withVariable("predicate", input)
    ).get(0);
  }
}
