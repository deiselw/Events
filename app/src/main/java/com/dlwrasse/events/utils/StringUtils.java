package com.dlwrasse.events.utils;

import java.text.Normalizer;

public class StringUtils {
    public static String removeAccents(String text) {
        return Normalizer
                .normalize(text, Normalizer.Form.NFD)
                .replaceAll("[^\\p{ASCII}]", "");
    }
}
