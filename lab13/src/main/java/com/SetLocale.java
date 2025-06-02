package com;

import java.util.Locale;
import java.util.Properties;

public class SetLocale {
    private static Locale currentLocale = Locale.getDefault();
    
    public static boolean setCurrentLocale(String languageTag, Properties messages) {
        try {
            Locale newLocale = Locale.forLanguageTag(languageTag);
            
            if (isLocaleSupported(newLocale)) {
                currentLocale = newLocale;
                Locale.setDefault(newLocale);
                return true;
            } else {
                System.out.println("Locale '" + languageTag + "' is not supported or invalid.");
                return false;
            }
        } catch (Exception e) {
            System.out.println("Error setting locale '" + languageTag + "': " + e.getMessage());
            return false;
        }
    }
    
    public static Locale getCurrentLocale() {
        return currentLocale;
    }
    
    private static boolean isLocaleSupported(Locale locale) {
        Locale[] availableLocales = Locale.getAvailableLocales();
        
        for (Locale availableLocale : availableLocales) {
            if (availableLocale.equals(locale)) {
                return true;
            }
        }
        
        //  check if it's a valid language tag with at least a language part
        String languageTag = locale.toLanguageTag();
        return !languageTag.isEmpty() && !locale.getLanguage().isEmpty();
    }
    
    public static void resetToDefault() {
        currentLocale = Locale.getDefault();
    }
}
