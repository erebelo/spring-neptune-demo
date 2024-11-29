package com.erebelo.springneptunedemo.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class QueryUtil {

    public static boolean isValidProperty(String p) {
        return !(p == null || p.isBlank());
    }

    public static int[] calculatePaginationIndexes(Integer limit, Integer page) {
        int start = (page - 1) * limit;
        int end = page * limit;

        return new int[]{start, end};
    }
}
