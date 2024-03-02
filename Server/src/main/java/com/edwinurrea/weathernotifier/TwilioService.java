package com.edwinurrea.weathernotifier;

import com.twilio.Twilio;
import com.twilio.exception.ApiException;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.rest.verify.v2.service.Verification;
import com.twilio.rest.verify.v2.service.VerificationCheck;
import com.twilio.type.PhoneNumber;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TwilioService extends WeatherNotifier {
    private static final Logger logger = LoggerFactory.getLogger(WeatherNotifier.class);
    
    protected static String sendVerificationCode(String formattedPhoneNumber) {
        String ACCOUNT_SID = System.getenv("twilio.account.sid");
        String AUTH_TOKEN = System.getenv("twilio.auth.token");
        String VERIFY_SERVICE_SID = System.getenv("twilio.verify.sid");
        
        Twilio.init(ACCOUNT_SID, AUTH_TOKEN);

        try {
            Verification verification = Verification.creator(VERIFY_SERVICE_SID, formattedPhoneNumber, "sms").create();
            return verification.getSid();
        } catch (ApiException e) {
            logger.error("Error: Failed to send verification code.", e);
            return null;
        }
    }

    protected static String verifyPhoneNumber(String verificationSid, String formattedPhoneNumber, String verificationCode) throws IOException {
        String ACCOUNT_SID = System.getenv("twilio.account.sid");
        String AUTH_TOKEN = System.getenv("twilio.auth.token");
        String VERIFY_SERVICE_SID = System.getenv("twilio.verify.sid");
        Twilio.init(ACCOUNT_SID, AUTH_TOKEN);

        try {
            VerificationCheck verificationCheck = VerificationCheck.creator(VERIFY_SERVICE_SID)
                    .setVerificationSid(verificationSid)
                    .setCode(verificationCode)
                    .create();
            return verificationCheck.getStatus();
        } catch (ApiException e) {
            logger.error("Error: Failed to verify phone number.", e);
            return "Error";
        }
    }

    protected static void sendMessage(String formattedPhoneNumber, String message) {
        String ACCOUNT_SID = System.getenv("twilio.account.sid");
        String AUTH_TOKEN = System.getenv("twilio.auth.token");
        String from = System.getenv("twilio.default.from.number");

        Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
        Message.creator(new PhoneNumber(formattedPhoneNumber), new PhoneNumber(from), message).create();
    }
}