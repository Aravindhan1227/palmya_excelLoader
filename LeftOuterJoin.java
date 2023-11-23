package com.palmyra.excel;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.stream.Collectors;

class LeftOuterJoin {
    public static <V, F, R> List<R> leftJoin(List<V> input, List<F> filter, Predicate<F> prd, BiFunction<V, F, R> sd) {
        return input.stream()
                .flatMap(v -> filter.stream()
                        .filter(prd)
                        .map(f -> sd.apply(v, f))
                        .findFirst()
                        .stream())
                .collect(Collectors.toList());
    }
}
