package me.blvckbytes.item_predicate_parser.display.overview;

import java.util.ArrayList;
import java.util.List;

public class TokenLine {

  public boolean wraps;
  public final List<String> tokens = new ArrayList<>();

  public void add(String token) {
    tokens.add(token);
  }

  public void append(String token) {
    var lastIndex = tokens.size() - 1;
    tokens.set(lastIndex, tokens.get(lastIndex) + " " + token);
  }

  public boolean isEmpty() {
    return tokens.isEmpty();
  }
}
