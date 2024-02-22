package com.edwinurrea.weathernotifier;

import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ValidationUtils extends WeatherNotifier {
    private static final Logger logger = LoggerFactory.getLogger(WeatherNotifier.class);

    protected static boolean isValidPhoneNumber(String phoneNumber) {
        if (phoneNumber == null) {
            logger.warn("Phone number is null.");
            return false;
        }
        String phoneNumberPattern = "\\d{10}";
        return Pattern.matches(phoneNumberPattern, phoneNumber);
    }
    
    protected static boolean isValidPassword(String password) {
        if (password == null) {
            logger.warn("Password is null.");
            return false;
        }
        return password.length() >= 8;
    }
    
    protected static String getValidCountryCode() {
        String countryCode = "1";
        return countryCode;
    }

    protected static String formatToE164(String phoneNumber, String countryCode) {
        // Removes non-numeric characters
        String cleanedNumber = phoneNumber.replaceAll("[^\\d]", "");

        return "+" + countryCode + cleanedNumber;
    }
}
