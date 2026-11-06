package ru.nikzarch.firstService.i18n;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.HttpHeaders;

import java.text.MessageFormat;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;

@ApplicationScoped
public class Messages {
    private static final String BUNDLE = "messages";
    private static final Locale DEFAULT_LOCALE = Locale.forLanguageTag("ru");
    private static final Set<String> LANGUAGES = Set.of(
            "ru",
            "en",
            "fr",
            "kk");

    public Locale locale(HttpHeaders headers) {
        return selectLocale(headers == null ? null : headers.getAcceptableLanguages());
    }

    public Locale selectLocale(List<Locale> locales) {
        if (locales != null) {
            for (Locale locale : locales) {
                if (locale != null && LANGUAGES.contains(locale.getLanguage())) {
                    return Locale.forLanguageTag(locale.getLanguage());
                }
            }
        }
        return DEFAULT_LOCALE;
    }

    public String text(Locale locale, String key, Object... parameters) {
        Locale selected = locale == null || !LANGUAGES.contains(locale.getLanguage())
                ? DEFAULT_LOCALE : Locale.forLanguageTag(locale.getLanguage());
        String pattern = getPattern(selected, key);
        return new MessageFormat(pattern, selected)
                .format(parameters == null ? new Object[0] : parameters);
    }

    public String validationText(Locale locale, String messageTemplate, String fallback) {
        if (messageTemplate != null && messageTemplate.startsWith("{validation.")
                && messageTemplate.endsWith("}")) {
            return text(locale, messageTemplate.substring(1, messageTemplate.length() - 1));
        }
        return fallback;
    }

    private String getPattern(Locale locale, String key) {
        ResourceBundle bundle = ResourceBundle.getBundle(BUNDLE, locale);
        if (bundle.containsKey(key)) {
            return bundle.getString(key);
        }
        ResourceBundle defaultLocale = ResourceBundle.getBundle(BUNDLE, DEFAULT_LOCALE);
        if (defaultLocale.containsKey(key)) {
            return defaultLocale.getString(key);
        }
        throw new MissingResourceException("Missing message key", BUNDLE, key);
    }
}
