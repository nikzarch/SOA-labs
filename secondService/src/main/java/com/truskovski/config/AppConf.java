package com.truskovski.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.FixedLocaleResolver;
import org.springframework.web.client.RestTemplate;

import java.util.Locale;

@Configuration
public class AppConf {

    @Bean
    public MessageSource messageSource() {
        var messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasename("classpath:messages");
        messageSource.setDefaultEncoding("UTF-8");
        messageSource.setFallbackToSystemLocale(false);
        return messageSource;
    }

    @Bean
    public LocaleResolver localeResolver(@Value("${app.locale:ru}") String languageTag) {
        return new FixedLocaleResolver(Locale.forLanguageTag(languageTag));
    }

    @Bean
    public RestTemplate restTemplate() {
        var factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(3000);
        factory.setReadTimeout(5000);
        return new RestTemplate(factory);
    }
}
