package com.ada.erp.shared.validation;

import java.util.Locale;
import java.util.regex.Pattern;

/** CNPJ format and modulo-11 rules from Receita Federal's CNPJ DV manual. */
public final class CnpjSupport {
    private static final Pattern RAW = Pattern.compile("[A-Za-z0-9]{12}[0-9]{2}");
    private static final Pattern MASKED = Pattern.compile(
            "[A-Za-z0-9]{2}\\.[A-Za-z0-9]{3}\\.[A-Za-z0-9]{3}/[A-Za-z0-9]{4}-[0-9]{2}");

    private CnpjSupport() {
    }

    public static boolean isValid(String value) {
        if (value == null) {
            return false;
        }
        String trimmed = value.strip();
        if (!RAW.matcher(trimmed).matches() && !MASKED.matcher(trimmed).matches()) {
            return false;
        }
        String canonical = normalize(value);
        if (canonical.chars().allMatch(character -> character == canonical.charAt(0))) {
            return false;
        }
        return digit(canonical, 12) == canonical.charAt(12) - '0'
                && digit(canonical, 13) == canonical.charAt(13) - '0';
    }

    /** Removes only the known mask; never silently discards unexpected characters. */
    public static String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.strip();
        if (!RAW.matcher(trimmed).matches() && !MASKED.matcher(trimmed).matches()) {
            return trimmed.toUpperCase(Locale.ROOT);
        }
        return trimmed.replace(".", "").replace("/", "").replace("-", "").toUpperCase(Locale.ROOT);
    }

    private static int digit(String value, int length) {
        int sum = 0;
        int weight = 2;
        for (int index = length - 1; index >= 0; index--) {
            sum += (value.charAt(index) - '0') * weight;
            weight = weight == 9 ? 2 : weight + 1;
        }
        int remainder = sum % 11;
        return remainder < 2 ? 0 : 11 - remainder;
    }
}
