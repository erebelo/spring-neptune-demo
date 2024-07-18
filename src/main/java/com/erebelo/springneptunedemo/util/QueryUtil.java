package com.erebelo.springneptunedemo.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class QueryUtil {

    private static final String ANY_PROPERTY = ".*";

    public static int[] calculatePaginationIndexes(Integer limit, Integer page) {
        int start = (page - 1) * limit;
        int end = page * limit;

        return new int[]{start, end};
    }

    public static String propertyRegex(String p) {
        return (p == null || p.isBlank()) ? ANY_PROPERTY : p;
    }

    public static String propertyLikeRegex(String p) {
        return (p == null || p.isBlank()) ? ANY_PROPERTY : ANY_PROPERTY + p + ANY_PROPERTY;
    }
}
