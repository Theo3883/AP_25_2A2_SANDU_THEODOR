package com;

import java.util.Arrays;
import java.util.Locale;
import java.util.Properties;

public class DisplayLocales {
    
    public static void showAvailableLocales(Properties messages) {
        System.out.println(messages.getProperty("locales", "The available locales are:"));
        
        Locale[] availableLocales = Locale.getAvailableLocales();
        
        Arrays.sort(availableLocales, (l1, l2) -> l1.toLanguageTag().compareTo(l2.toLanguageTag()));

        System.out.println("\nCommon locales:");
        String[] commonTags = {"en", "en-US", "en-GB", "ro", "ro-RO", "fr", "fr-FR", "de", "de-DE", 
                              "es", "es-ES", "it", "it-IT", "pt", "pt-PT", "nl", "nl-NL", "ru", "ru-RU"};
        
        for (String tag : commonTags) {
            Locale locale = Locale.forLanguageTag(tag);
            if (Arrays.asList(availableLocales).contains(locale)) {
                displayLocaleEntry(locale);
            }
        }
        
        System.out.println("\nOther available locales:");
        int count = 0;
        for (Locale locale : availableLocales) {
            if (!Arrays.asList(commonTags).contains(locale.toLanguageTag()) && 
                !locale.toLanguageTag().isEmpty()) {
                displayLocaleEntry(locale);
                count++;
                if (count % 5 == 0) {
                    System.out.println(); 
                }
            }
        }
        
        System.out.println("\nTotal available locales: " + availableLocales.length);
    }
    
    private static void displayLocaleEntry(Locale locale) {
        String languageTag = locale.toLanguageTag();
        String displayName = locale.getDisplayName(Locale.ENGLISH);
        
        if (!displayName.isEmpty() && !languageTag.isEmpty()) {
            System.out.printf("  %-10s (%s)\n", languageTag, displayName);
        }
    }
}
