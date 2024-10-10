package me.blvckbytes.item_predicate_parser.parse;

import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

/*
  Target: Syllables which are matched against; if a query-syllable is contained within a
          target-syllable, the latter may produce up to two remainders.
  Query:  Syllables whose existence is to be checked within the target; they are considered
          atomic and cannot be split up any further, thereby leave no remainders.
 */
public class SyllablesMatcher {

  /*
    NOTE: Books may have up to 1023 characters per page, and with average word-lengths,
    that'd make for ~170 syllables. So one long to store flags in is most definitely not
    enough to support unrestricted use of the parser; that's why arrays are required!
   */

  private static final char[] RAPID_LOWERCASE_CACHE = new char[128];

  static {
    int n = 0;

    while (n != 128)
      RAPID_LOWERCASE_CACHE[n++] = Character.toLowerCase((char) n);
  }

  private final Syllables targetRemainders;
  private long[] targetRemaindersMatchedFlags;

  private @Nullable Syllables target;
  private long[] targetMatchedFlags;

  private @Nullable Syllables query;
  private long[] queryMatchedFlags;

  public SyllablesMatcher() {
    this.targetRemainders = new Syllables(null);
    this.targetRemaindersMatchedFlags = new long[this.targetRemainders.capacity()];
  }

  public void resetQueryMatches() {
    if (this.query == null)
      return;

    Arrays.fill(this.queryMatchedFlags, 0);
  }

  public void setQuery(Syllables query) {
    this.query = query;

    var numberLongs = requiredLongs(query.size());

    if (this.queryMatchedFlags != null && numberLongs == this.queryMatchedFlags.length)
      Arrays.fill(this.queryMatchedFlags, 0);
    else
      this.queryMatchedFlags = new long[numberLongs];
  }

  public @Nullable Syllables getQuery() {
    return this.query;
  }

  public void resetTargetMatches() {
    if (this.target == null)
      return;

    Arrays.fill(this.targetMatchedFlags, 0);

    targetRemainders.clear();

    Arrays.fill(targetRemaindersMatchedFlags, 0);
  }

  public void setTarget(Syllables target) {
    this.target = target;

    var numberLongs = requiredLongs(target.size());

    if (this.targetMatchedFlags != null && numberLongs == this.targetMatchedFlags.length)
      Arrays.fill(this.targetMatchedFlags, 0);
    else
      this.targetMatchedFlags = new long[numberLongs];

    targetRemainders.clear();
    targetRemainders.container = target.container;

    Arrays.fill(targetRemaindersMatchedFlags, 0);
  }

  public @Nullable Syllables getTarget() {
    return this.target;
  }

  public boolean hasUnmatchedQuerySyllables() {
    if (query == null)
      return false;

    return hasUnmatchedBits(queryMatchedFlags, query.size());
  }

  public void forEachUnmatchedTargetSyllable(UnmatchedSyllableConsumer consumer) {
    if (target != null) {
      forEachUnmatched(target, targetMatchedFlags, consumer);
      forEachUnmatched(targetRemainders, targetRemaindersMatchedFlags, consumer);
    }
  }

  public void forEachUnmatchedQuerySyllable(UnmatchedSyllableConsumer consumer) {
    if (query != null)
      forEachUnmatched(query, queryMatchedFlags, consumer);
  }

  public boolean hasUnmatchedTargetSyllables() {
    if (target == null)
      return false;

    return (
      hasUnmatchedBits(targetMatchedFlags, target.size()) ||
      hasUnmatchedBits(targetRemaindersMatchedFlags, targetRemainders.size())
    );
  }

  public void match() {
    if (query == null || target == null)
      throw new IllegalStateException("Cannot match on a missing query and or a missing target");

    for (var querySyllableIndex = 0; querySyllableIndex < query.size(); ++querySyllableIndex) {
      if (isMarkedAsMatched(queryMatchedFlags, querySyllableIndex))
        continue;

      var querySyllable = query.getSyllable(querySyllableIndex);

      var didQuerySyllableMatch = matchQueryAgainstTargets(querySyllable, target, targetMatchedFlags);

      if (!didQuerySyllableMatch)
        didQuerySyllableMatch = matchQueryAgainstTargets(querySyllable, targetRemainders, targetRemaindersMatchedFlags);

      if (didQuerySyllableMatch) {
        // Do not remove negated query substrings that matched, as to keep the result a mismatch
        if (Syllables.isNegated(querySyllable))
          continue;

        markAsMatched(queryMatchedFlags, querySyllableIndex);
        continue;
      }

      // Remove negated query substrings which didn't find a match, as to allow the result to become a match
      if (Syllables.isNegated(querySyllable))
        markAsMatched(queryMatchedFlags, querySyllableIndex);
    }
  }

  private void forEachUnmatched(Syllables syllables, long[] matchedFlags, UnmatchedSyllableConsumer consumer) {
    for (var syllableIndex = 0; syllableIndex < syllables.size(); ++syllableIndex) {
      var syllable = syllables.getSyllable(syllableIndex);

      if (isMarkedAsMatched(matchedFlags, syllableIndex))
        continue;

      consumer.accept(syllables, syllable);
    }
  }

  private boolean matchQueryAgainstTargets(int querySyllable, Syllables target, long[] targetMatchedFlags) {
    // Don't bother iterating if there's nothing left; this check should be so cheap that it resembles a worthy fast-path
    if (!hasUnmatchedBits(targetMatchedFlags, target.size()))
      return false;

    for (var targetSyllableIndex = 0; targetSyllableIndex < target.size(); ++targetSyllableIndex) {
      if (isMarkedAsMatched(targetMatchedFlags, targetSyllableIndex))
        continue;

      var targetSyllable = target.getSyllable(targetSyllableIndex);
      var indexResult = relativeIndexOf(querySyllable, targetSyllable);

      // NOTE: Beginning index relative to the target's start
      var beginInTarget = (int) (indexResult >> 16);

      // Not a match
      if (beginInTarget == Integer.MAX_VALUE)
        continue;

      markAsMatched(targetMatchedFlags, targetSyllableIndex);

        /*
          Cases:
          vvv-----
          ABCDEFGH

          ---vvv--
          ABCDEFGH

          -----vvv
          ABCDEFGH
         */

      var targetSyllableStart = Syllables.getStartIndex(targetSyllable);
      var targetSyllableEnd = Syllables.getEndIndex(targetSyllable);
      var targetSyllableLength = targetSyllableEnd - targetSyllableStart + 1;

      // NOTE: The match-length may be greater than the query-syllable's length, due to color-sequences
      var matchLength = (int) (indexResult & 0xFFFF);

      // Remainder after match
      if (beginInTarget + matchLength < targetSyllableLength)
        addTargetRemainder(targetSyllableStart + beginInTarget + matchLength, targetSyllableEnd);

      // Remainder previous to match
      if (beginInTarget != 0)
        addTargetRemainder(targetSyllableStart, targetSyllableStart + beginInTarget - 1);

      return true;
    }

    return false;
  }

  private void addTargetRemainder(int start, int end) {
    targetRemainders.add(start, end, false);

    int newRequiredLongs = requiredLongs(targetRemainders.capacity());

    if (newRequiredLongs > targetRemaindersMatchedFlags.length) {
      var newArray = new long[newRequiredLongs];
      System.arraycopy(targetRemaindersMatchedFlags, 0, newArray, 0, targetRemaindersMatchedFlags.length);
      targetRemaindersMatchedFlags = newArray;
    }
  }

  /**
   * @return <32b begin_in_target><32b number_of_target_chars>;
   *         if begin_in_target == Integer.MAX_VALUE then it's not contained;
   *         number_of_target_chars may be larger than the target syllable itself,
   *         due to skipped-over color sequences
   */
  private long relativeIndexOf(int querySyllable, int targetSyllable) {
    assert query != null && target != null;

    var querySyllableLength = Syllables.getLength(querySyllable);
    var targetSyllableLength = Syllables.getLength(targetSyllable);

    if (targetSyllableLength < querySyllableLength)
      return ((long) Integer.MAX_VALUE) << 16;

    var querySyllableStart = Syllables.getStartIndex(querySyllable);
    var targetSyllableStart = Syllables.getStartIndex(targetSyllable);

    /*
      0: A B C
      1:   A B C
      2:     A B C
         A B C D E
     */
    var highestOffset = targetSyllableLength - querySyllableLength;

    // TODO: Can this algorithm still be improved?

    for (int targetOffset = 0; targetOffset <= highestOffset; ++targetOffset) {
      boolean didMatch = true;

      // As color-sequences are skipped on the outer-loop's counter, keep an internal
      // backup to respond the very front of the match, including said sequences.
      var initialTargetOffset = targetOffset;

      for (int queryOffset = 0; queryOffset < querySyllableLength; ++queryOffset) {
        var targetIndex = targetSyllableStart + queryOffset + targetOffset;
        var queryIndex = querySyllableStart + queryOffset;
        var targetChar = target.container.charAt(targetIndex);

        while (targetChar == '§' && targetOffset < highestOffset) {
          var nextContainerChar = target.container.charAt(targetIndex + 1);

          if (
            (nextContainerChar >= '0' && nextContainerChar <= '9')
              || (nextContainerChar >= 'a' && nextContainerChar <= 'f')
              || (nextContainerChar >= 'k' && nextContainerChar <= 'o')
              || nextContainerChar == 'r'
          ) {
            targetOffset += 2;
            targetIndex = targetSyllableStart + queryOffset + targetOffset;
            targetChar = target.container.charAt(targetIndex);
          }

          else
            break;
        }

        var containedChar = query.container.charAt(queryIndex);

        if (charToLower(targetChar) != charToLower(containedChar)) {
          didMatch = false;
          break;
        }
      }

      if (didMatch)
        return ((long) initialTargetOffset) << 16 | (targetOffset - initialTargetOffset + querySyllableLength);
    }

    return ((long) Integer.MAX_VALUE) << 16;
  }

  private static void markAsMatched(long[] flags, int index) {
    flags[index / Long.SIZE] |= 1L << (index % Long.SIZE);
  }

  private static boolean isMarkedAsMatched(long[] flags, int index) {
    return (flags[index / Long.SIZE] & (1L << (index % Long.SIZE))) != 0;
  }

  private static boolean hasUnmatchedBits(long[] flags, int size) {
    var numberOfAllFlagged = size / Long.SIZE;
    var numberOfRemainingFlaggedBits = size % Long.SIZE;

    int i;

    for (i = 0; i < numberOfAllFlagged; ++i) {
      if (flags[i] != Long.MAX_VALUE)
        return true;
    }

    return flags[i] != (1L << numberOfRemainingFlaggedBits) - 1;
  }

  private static int requiredLongs(int numberOfItems) {
    if (numberOfItems == 0)
      return 1;

    return (numberOfItems + (Long.SIZE - 1)) / Long.SIZE;
  }

  private static char charToLower(char c) {
    if (c <= '\u007f')
      return RAPID_LOWERCASE_CACHE[c];

    return Character.toLowerCase(c);
  }
}
