package com.edwinurrea.weathernotifier;

import org.junit.Test;
import static org.junit.Assert.*;

public class ValidationUtilsTest {    
    @Test
    public void testIsValidPhoneNumber_ValidNumber() {
        String phoneNumber = "1234567890";
        boolean result = ValidationUtils.isValidPhoneNumber(phoneNumber);
        assertTrue(result);
    }
    
    @Test
    public void testIsValidPhoneNumber_NullNumber() {
        boolean result = ValidationUtils.isValidPhoneNumber(null);
        assertFalse(result);
    }
    
    @Test
    public void testIsValidPhoneNumber_InvalidNumber() {
        String phoneNumber = "123";
        boolean result = ValidationUtils.isValidPhoneNumber(phoneNumber);
        assertFalse(result);
    }
    
    @Test
    public void testIsValidPassword_ValidPassword() {
        String password = "testpassword";
        boolean result = ValidationUtils.isValidPassword(password);
        assertTrue(result);
    }

    @Test
    public void testIsValidPassword_NullPassword() {
        boolean result = ValidationUtils.isValidPassword(null);
        assertFalse(result);
    }

    @Test
    public void testIsValidPassword_InvalidPassword() {
        String password = "123";
        boolean result = ValidationUtils.isValidPassword(password);
        assertFalse(result);
    }
    
    @Test
    public void testFormatToE164_HarshNumber() {
        String phoneNumber = "@123^45$$678@90!@#";
        String countryCode = ValidationUtils.getValidCountryCode();
        String result = ValidationUtils.formatToE164(phoneNumber, countryCode);
        assertEquals("+11234567890", result);
    }
}
