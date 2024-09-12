package me.blvckbytes.item_predicate_parser;

import me.blvckbytes.item_predicate_parser.token.UnquotedStringToken;
import me.blvckbytes.item_predicate_parser.translation.TranslatableSource;
import me.blvckbytes.item_predicate_parser.translation.TranslationRegistry;
import org.bukkit.Translatable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CollisionPrefixCaseBuilder {

  private final TranslationRegistry translationRegistry;
  private final List<TranslatableSource> sources;
  private final List<TranslatedTranslatablePredicate> predicates;

  public CollisionPrefixCaseBuilder(TranslationRegistry translationRegistry) {
    this.translationRegistry = translationRegistry;
    this.sources = new ArrayList<>();
    this.predicates = new ArrayList<>();
  }

  public CollisionPrefixCaseBuilder withSource(TranslatableSource source) {
    this.sources.add(source);
    return this;
  }

  public CollisionPrefixCaseBuilder withSingleSource(Translatable translatable, String collisionPrefix) {
    this.sources.add(new TranslatableSource(Collections.singletonList(translatable), collisionPrefix));
    return this;
  }

  public CollisionPrefixCaseBuilder expectResult(Translatable translatable, String prefix) {
    this.predicates.add(new TranslatedTranslatablePredicate(translationRegistry, translatable, prefix));
    return this;
  }

  public void execute(String search) {
    translationRegistry.initialize(sources);

    var searchToken = new UnquotedStringToken(0, 0, null, search);
    var searchResult = translationRegistry.search(searchToken);

    var remainingPredicates = new ArrayList<>(predicates);
    var remainingResults = new ArrayList<>(searchResult.result());

    predicateLoop: while (!remainingPredicates.isEmpty()) {
      var currentPredicate = remainingPredicates.removeFirst();

      for (var resultIterator = remainingResults.iterator(); resultIterator.hasNext();) {
        if (!currentPredicate.test(resultIterator.next()))
          continue;

        resultIterator.remove();
        continue predicateLoop;
      }

      throw new IllegalStateException("No item matched the current predicate: " + currentPredicate + "; items: " + remainingResults);
    }

    if (!remainingResults.isEmpty())
      throw new IllegalStateException("Not all results have been matched by a predicate: " + remainingResults);
  }
}
