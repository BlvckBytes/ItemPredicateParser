package me.blvckbytes.item_predicate_parser.parse;

import java.util.function.Predicate;

public interface EnumPredicate<T extends Enum<?>> extends Predicate<NormalizedConstant<T>> {}
