package com.fb.workplan.client;

public class StringUtils {
    public static boolean hasText(String text) {
        return text!=null && text.trim().length()> 0;
    }
}
