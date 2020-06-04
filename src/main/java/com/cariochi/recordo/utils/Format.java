package com.cariochi.recordo.utils;

import org.slf4j.helpers.MessageFormatter;

public final class Format {

    private Format() {
    }

    public static String format(String format, Object... args) {
        return MessageFormatter.arrayFormat(format, args).getMessage();
    }
}
