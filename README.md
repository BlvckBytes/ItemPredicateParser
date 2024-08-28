# StorageQuery

## Things To Think About

- Handle chests being updated while the search-results are browsed
- Text search should ignore color sequences
- If a syllable equals to a wildcard, disregard direct matches
- If not all matches fit into the max_completer_results, add a "...and <x> more" as the last entry
- Store the exact input on predicates, as to keep abbreviations alive when persisting
- Effect and Enchantment: What if I only want the specified on there, and nothing else?