package app;

import com.DisplayLocales;
import com.Info;
import com.SetLocale;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Scanner;

public class LocaleExplore {
    private static Locale currentLocale = Locale.getDefault();
    private static Properties messages;
    
    public static void main(String[] args) {
        updateMessages();
        Scanner scanner = new Scanner(System.in);
        
        System.out.println("=== Locale Explorer ===");
        System.out.println("Commands: locales, set <language_tag>, info [language_tag], exit");
        
        while (true) {
            System.out.print(getProperty("prompt") + " ");
            String input = scanner.nextLine().trim();
            
            if (input.equalsIgnoreCase("exit")) {
                break;
            }
            
            String[] parts = input.split("\\s+");
            String command = parts[0].toLowerCase();
            
            switch (command) {
                case "locales":
                    DisplayLocales.showAvailableLocales(messages);
                    break;
                case "set":
                    if (parts.length > 1) {
                        if (SetLocale.setCurrentLocale(parts[1], messages)) {
                            currentLocale = SetLocale.getCurrentLocale();
                            updateMessages();
                            System.out.println(java.text.MessageFormat.format(
                                getProperty("locale.set"), currentLocale.toLanguageTag()));
                        }
                    } else {
                        System.out.println("Usage: set <language_tag>");
                    }
                    break;
                case "info":
                    Locale localeToShow = currentLocale;
                    if (parts.length > 1) {
                        localeToShow = Locale.forLanguageTag(parts[1]);
                    }
                    Info.displayLocaleInfo(localeToShow, messages);
                    break;
                default:
                    System.out.println(getProperty("invalid"));
            }
            System.out.println();
        }
        
        scanner.close();
    }
    
    private static void updateMessages() {
        messages = new Properties();
        String basePath = "src/main/java/res/Messages";
        String fileName = basePath + ".properties";
        
        if (!currentLocale.getLanguage().equals("en")) {
            String localeFileName = basePath + "_" + currentLocale.getLanguage() + ".properties";
            try (InputStream is = new FileInputStream(localeFileName)) {
                messages.load(is);
                return;
            } catch (Exception e) {
            }
        }
        
        try (InputStream is = new FileInputStream(fileName)) {
            messages.load(is);
        } catch (Exception e) {
            System.err.println("Error loading properties file: " + e.getMessage());
            // fallback properties
            messages = new Properties();
            messages.setProperty("prompt", "Input command:");
            messages.setProperty("locales", "The available locales are:");
            messages.setProperty("locale.set", "The current locale is {0}");
            messages.setProperty("info", "Information about {0}:");
            messages.setProperty("invalid", "Unknown command");
        }
    }
    
    private static String getProperty(String key) {
        return messages.getProperty(key, key);
    }
    
    public static Locale getCurrentLocale() {
        return currentLocale;
    }
}