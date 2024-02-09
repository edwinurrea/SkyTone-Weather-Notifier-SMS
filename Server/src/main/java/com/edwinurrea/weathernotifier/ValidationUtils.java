package com.edwinurrea.weathernotifier;

import java.io.BufferedReader;
import java.io.IOException;

import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ValidationUtils extends WeatherNotifier {
    private static final Logger logger = LoggerFactory.getLogger(WeatherNotifier.class);

    protected static boolean isValidPhoneNumber(String phoneNumber) {
        if (phoneNumber == null) {
            return false;
        }
        String phoneNumberPattern = "\\d{10}";
        return Pattern.matches(phoneNumberPattern, phoneNumber);
    }
    
    protected static String getValidPhoneNumber(BufferedReader reader) throws IOException {
        String phoneNumber = "";
        String countryCode = getValidCountryCode();
        String formattedPhoneNumber = "";

        while (!isValidPhoneNumber(phoneNumber)) {
            logger.info("Enter your phone number: ");
            phoneNumber = reader.readLine();

            if (!isValidPhoneNumber(phoneNumber)) {
                logger.info("Invalid phone number. Please enter a valid phone number.");
            } else {
                formattedPhoneNumber = formatToE164(phoneNumber, countryCode);
            }
        }

        return formattedPhoneNumber;
    }
    
    protected static boolean isValidPassword(String password) {
        if (password == null) {
            return false;
        }
        return password.length() >= 8;
    }
    
    protected static String getValidPassword(BufferedReader reader, String password) throws IOException {
        while (password.isEmpty() || !isValidPassword(password)) {
            if (password.isEmpty()) {
                logger.info("Password cannot be empty. Please enter your password.");
            } else if (!isValidPassword(password)) {
                logger.info("Invalid password format. Please choose a stronger password.");
            }
        }
        return password;
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
    
    protected static boolean isValidZipCode(String zipCode) {
        String zipCodePattern = "\\d{5}";
        return Pattern.matches(zipCodePattern, zipCode);
    }
    
    protected static String getValidZipCode(BufferedReader reader) throws IOException {
        String zipCode = "";
        while (!isValidZipCode(zipCode)) {
            logger.info("Enter the zip code: ");
            zipCode = reader.readLine();

            if (!isValidZipCode(zipCode)) {
                logger.info("Invalid zip code. Please enter a valid zip code.");
            }
        }
        return zipCode;
    }
}
