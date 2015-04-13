package com.atex.standard.util;

public final class WidgetUtil {

    private WidgetUtil() {
    }

    public static String abbreviate(final String str, final int maxWidth) {
        return str != null && str.length() > maxWidth
            ? str.substring(0, maxWidth - 3) + "..."
            : str;
    }
}
