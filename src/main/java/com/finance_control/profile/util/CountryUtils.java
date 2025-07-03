package com.finance_control.profile.util;

import java.util.Map;

public final class CountryUtils {
    private static final String EUROPE_LONDON = "Europe/London";
    private static final String AMERICA_NEW_YORK = "America/New_York";
    // Outras constantes podem ser criadas para outros valores repetidos

    private static final Map<String, String> COUNTRY_TO_CURRENCY = Map.ofEntries(
        Map.entry("BRAZIL", "BRL"),
        Map.entry("BR", "BRL"),
        Map.entry("UNITED STATES", "USD"),
        Map.entry("USA", "USD"),
        Map.entry("US", "USD"),
        Map.entry("EUROPEAN UNION", "EUR"),
        Map.entry("EU", "EUR"),
        Map.entry("UNITED KINGDOM", "GBP"),
        Map.entry("UK", "GBP"),
        Map.entry("ENGLAND", "GBP"),
        Map.entry("CANADA", "CAD"),
        Map.entry("CA", "CAD"),
        Map.entry("AUSTRALIA", "AUD"),
        Map.entry("AU", "AUD"),
        Map.entry("JAPAN", "JPY"),
        Map.entry("JP", "JPY"),
        Map.entry("CHINA", "CNY"),
        Map.entry("CN", "CNY"),
        Map.entry("INDIA", "INR"),
        Map.entry("IN", "INR"),
        Map.entry("MEXICO", "MXN"),
        Map.entry("MX", "MXN"),
        Map.entry("ARGENTINA", "ARS"),
        Map.entry("AR", "ARS"),
        Map.entry("CHILE", "CLP"),
        Map.entry("CL", "CLP"),
        Map.entry("COLOMBIA", "COP"),
        Map.entry("CO", "COP"),
        Map.entry("PERU", "PEN"),
        Map.entry("PE", "PEN"),
        Map.entry("URUGUAY", "UYU"),
        Map.entry("UY", "UYU"),
        Map.entry("PARAGUAY", "PYG"),
        Map.entry("PY", "PYG"),
        Map.entry("BOLIVIA", "BOB"),
        Map.entry("BO", "BOB"),
        Map.entry("ECUADOR", "USD"), // Ecuador uses USD
        Map.entry("EC", "USD"),
        Map.entry("VENEZUELA", "VES"),
        Map.entry("VE", "VES")
    );

    private static final Map<String, String> COUNTRY_TO_TIMEZONE = Map.ofEntries(
        Map.entry("BRAZIL", "America/Sao_Paulo"),
        Map.entry("BR", "America/Sao_Paulo"),
        Map.entry("UNITED STATES", AMERICA_NEW_YORK),
        Map.entry("USA", AMERICA_NEW_YORK),
        Map.entry("US", AMERICA_NEW_YORK),
        Map.entry("EUROPEAN UNION", "Europe/Brussels"),
        Map.entry("EU", "Europe/Brussels"),
        Map.entry("UNITED KINGDOM", EUROPE_LONDON),
        Map.entry("UK", EUROPE_LONDON),
        Map.entry("ENGLAND", EUROPE_LONDON),
        Map.entry("CANADA", "America/Toronto"),
        Map.entry("CA", "America/Toronto"),
        Map.entry("AUSTRALIA", "Australia/Sydney"),
        Map.entry("AU", "Australia/Sydney"),
        Map.entry("JAPAN", "Asia/Tokyo"),
        Map.entry("JP", "Asia/Tokyo"),
        Map.entry("CHINA", "Asia/Shanghai"),
        Map.entry("CN", "Asia/Shanghai"),
        Map.entry("INDIA", "Asia/Kolkata"),
        Map.entry("IN", "Asia/Kolkata"),
        Map.entry("MEXICO", "America/Mexico_City"),
        Map.entry("MX", "America/Mexico_City"),
        Map.entry("ARGENTINA", "America/Argentina/Buenos_Aires"),
        Map.entry("AR", "America/Argentina/Buenos_Aires"),
        Map.entry("CHILE", "America/Santiago"),
        Map.entry("CL", "America/Santiago"),
        Map.entry("COLOMBIA", "America/Bogota"),
        Map.entry("CO", "America/Bogota"),
        Map.entry("PERU", "America/Lima"),
        Map.entry("PE", "America/Lima"),
        Map.entry("URUGUAY", "America/Montevideo"),
        Map.entry("UY", "America/Montevideo"),
        Map.entry("PARAGUAY", "America/Asuncion"),
        Map.entry("PY", "America/Asuncion"),
        Map.entry("BOLIVIA", "America/La_Paz"),
        Map.entry("BO", "America/La_Paz"),
        Map.entry("ECUADOR", "America/Guayaquil"),
        Map.entry("EC", "America/Guayaquil"),
        Map.entry("VENEZUELA", "America/Caracas"),
        Map.entry("VE", "America/Caracas")
    );

    private CountryUtils() {}

    public static String getCurrency(String country) {
        if (country == null) {
            return null;
        }
        return COUNTRY_TO_CURRENCY.get(country.trim().toUpperCase());
    }

    public static String getTimezone(String country) {
        if (country == null) {
            return null;
        }
        return COUNTRY_TO_TIMEZONE.get(country.trim().toUpperCase());
    }
} 