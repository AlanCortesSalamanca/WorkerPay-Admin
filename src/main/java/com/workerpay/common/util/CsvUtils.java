package com.workerpay.common.util;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.stream.Collectors;

public final class CsvUtils {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private CsvUtils() {
    }

    public static String row(Object... values) {
        return Arrays.stream(values)
            .map(CsvUtils::cell)
            .collect(Collectors.joining(",")) + System.lineSeparator();
    }

    public static String cell(Object value) {
        String text = neutralizeFormula(text(value));
        boolean shouldQuote = text.contains(",") || text.contains("\"") || text.contains("\n") || text.contains("\r");
        String escaped = text.replace("\"", "\"\"");
        return shouldQuote ? "\"" + escaped + "\"" : escaped;
    }

    public static String text(Object value) {
        if (value == null) {
            return "";
        }
        if (value instanceof BigDecimal amount) {
            return money(amount);
        }
        if (value instanceof LocalDateTime dateTime) {
            return dateTime.format(DATE_TIME_FORMATTER);
        }
        if (value instanceof LocalDate date) {
            return date.toString();
        }
        return value.toString();
    }

    public static String money(BigDecimal amount) {
        return "$" + MoneyUtils.normalize(amount);
    }

    private static String neutralizeFormula(String text) {
        if (text.isEmpty()) {
            return text;
        }
        char first = text.charAt(0);
        if (first == '=' || first == '+' || first == '-' || first == '@' || first == '\t' || first == '\r') {
            return "'" + text;
        }
        return text;
    }
}
