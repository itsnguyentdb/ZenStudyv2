package com.example.quiz_clone.utils;

import java.util.Optional;

public class ConversionUtils {
    private ConversionUtils() {
    }

    @FunctionalInterface
    private interface Converter<T> {
        T parse(String value) throws NumberFormatException;
    }

    private static <T> T convertString(String value, Converter<T> converter) {
        if (StringUtils.isEmpty(value)) {
            return null;
        }
        try {
            return converter.parse(value.trim());
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private static <T> Optional<T> convertStringOrThrow(String value, Converter<T> converter) {
        var result = convertString(value, converter);
        if (result == null) {
            return Optional.empty();
        }
        return Optional.of(result);
    }

    public static Integer strToIntegerObj(String value) {
        return convertString(value, Integer::parseInt);
    }

    public static Optional<Integer> strToInt(String value) {
        return convertStringOrThrow(value, Integer::parseInt);
    }

    public static Float strToFloatObj(String value) {
        return convertString(value, Float::parseFloat);
    }

    public static Optional<Float> strToFloat(String value) {
        return convertStringOrThrow(value, Float::parseFloat);
    }

    public static Long strToLongObj(String value) {
        return convertString(value, Long::parseLong);
    }

    public static Optional<Long> strToLong(String value) {
        return convertStringOrThrow(value, Long::parseLong);
    }

    public static Double strToDoubleObj(String value) {
        return convertString(value, Double::parseDouble);
    }

    public static Optional<Double> strToDouble(String value) {
        return convertStringOrThrow(value, Double::parseDouble);
    }
}
