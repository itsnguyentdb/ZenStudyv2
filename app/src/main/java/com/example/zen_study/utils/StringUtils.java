package com.example.zen_study.utils;

public class StringUtils {
    public static boolean isEmpty(String s) {
        return s == null || s.trim().isEmpty() || s.trim().equalsIgnoreCase("null");
    }
}
