package com;

import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Currency;
import java.util.Date;
import java.util.Locale;
import java.util.Properties;

public class Info {
    
    public static void displayLocaleInfo(Locale locale, Properties messages) {
        try {
            System.out.println(MessageFormat.format(messages.getProperty("info", "Information about {0}:"), locale.toLanguageTag()));
            System.out.println("=" + "=".repeat(50));
            
            // Country information
            displayCountryInfo(locale);
            
            // Language information
            displayLanguageInfo(locale);
            
            // Currency information
            displayCurrencyInfo(locale);
            
            // Weekdays
            displayWeekdays(locale);
            
            // Months
            displayMonths(locale);
            
            // Current date
            displayCurrentDate(locale);
            
        } catch (Exception e) {
            System.out.println("Error displaying locale information: " + e.getMessage());
        }
    }
    
    private static void displayCountryInfo(Locale locale) {
        String country = locale.getDisplayCountry(Locale.ENGLISH);
        String countryNative = locale.getDisplayCountry(locale);
        
        if (!country.isEmpty()) {
            if (!country.equals(countryNative) && !countryNative.isEmpty()) {
                System.out.println("Country: " + country + " (" + countryNative + ")");
            } else {
                System.out.println("Country: " + country);
            }
        } else {
            System.out.println("Country: Not specified");
        }
    }
    
    private static void displayLanguageInfo(Locale locale) {
        String language = locale.getDisplayLanguage(Locale.ENGLISH);
        String languageNative = locale.getDisplayLanguage(locale);
        
        if (!language.isEmpty()) {
            if (!language.equals(languageNative) && !languageNative.isEmpty()) {
                System.out.println("Language: " + language + " (" + languageNative + ")");
            } else {
                System.out.println("Language: " + language);
            }
        } else {
            System.out.println("Language: " + locale.getLanguage());
        }
    }
    
    private static void displayCurrencyInfo(Locale locale) {
        try {
            Currency currency = Currency.getInstance(locale);
            String currencyCode = currency.getCurrencyCode();
            String currencyName = currency.getDisplayName(Locale.ENGLISH);
            String currencyNameNative = currency.getDisplayName(locale);
            
            if (!currencyName.equals(currencyNameNative) && !currencyNameNative.isEmpty()) {
                System.out.println("Currency: " + currencyCode + " (" + currencyName + " / " + currencyNameNative + ")");
            } else {
                System.out.println("Currency: " + currencyCode + " (" + currencyName + ")");
            }
        } catch (Exception e) {
            System.out.println("Currency: Not available for this locale");
        }
    }
    
    private static void displayWeekdays(Locale locale) {
        try {
            DateFormatSymbols symbols = new DateFormatSymbols(locale);
            String[] weekdays = symbols.getWeekdays();
            
            System.out.print("Week Days: ");
            boolean first = true;

            int[] dayOrder = {2, 3, 4, 5, 6, 7, 1}; // Mon, Tue, Wed, Thu, Fri, Sat, Sun
            
            for (int i : dayOrder) {
                if (i < weekdays.length && !weekdays[i].isEmpty()) {
                    if (!first) {
                        System.out.print(", ");
                    }
                    System.out.print(weekdays[i]);
                    first = false;
                }
            }
            System.out.println();
        } catch (Exception e) {
            System.out.println("Week Days: Not available for this locale");
        }
    }
    
    private static void displayMonths(Locale locale) {
        try {
            DateFormatSymbols symbols = new DateFormatSymbols(locale);
            String[] months = symbols.getMonths();
            
            System.out.print("Months: ");
            boolean first = true;
            for (int i = 0; i < 12; i++) {
                if (!months[i].isEmpty()) {
                    if (!first) {
                        System.out.print(", ");
                    }
                    System.out.print(months[i]);
                    first = false;
                }
            }
            System.out.println();
        } catch (Exception e) {
            System.out.println("Months: Not available for this locale");
        }
    }
    
    private static void displayCurrentDate(Locale locale) {
        try {
            Date now = new Date();
            
            // English format
            DateFormat englishFormat = DateFormat.getDateInstance(DateFormat.LONG, Locale.ENGLISH);
            String englishDate = englishFormat.format(now);
            
            // Native locale format
            DateFormat nativeFormat = DateFormat.getDateInstance(DateFormat.LONG, locale);
            String nativeDate = nativeFormat.format(now);
            
            if (!englishDate.equals(nativeDate)) {
                System.out.println("Today: " + englishDate + " (" + nativeDate + ")");
            } else {
                System.out.println("Today: " + englishDate);
            }
        } catch (Exception e) {
            LocalDate today = LocalDate.now();
            System.out.println("Today: " + today.format(DateTimeFormatter.ISO_LOCAL_DATE));
        }
    }
}
