package com.example.quiz_clone.utils;

public class StringUtils {
    public static boolean isEmpty(String s) {
        return s == null || s.trim().isEmpty() || s.trim().equalsIgnoreCase("null");
    }
}
