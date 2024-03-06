package com.edwinurrea.weathernotifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import java.sql.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import spark.Request;
import spark.Response;
import spark.Spark;
import static spark.Spark.*;

public class WeatherNotifier {
    private static String phoneNumber;
    private static String countryCode;
    private static String formattedPhoneNumber;
    private static String password;
    private static String zipCode;
    private static int zipCodeId;
    private static String verificationCode;
    private static String verificationSid;
    private static int userId;
    private static String cacheKey;
    private static final Logger logger = LoggerFactory.getLogger(WeatherNotifier.class);
    private static final int HTTP_OK = 200;
    private static final int HTTP_BAD_REQUEST = 400;
    private static final int HTTP_UNAUTHORIZED = 401;
    private static final int HTTP_CONFLICT = 409;
    private static final int HTTP_INTERNAL_SERVER_ERROR = 500;
    
    public static void main(String[] args) {
        Spark.port(5000);
        startSparkServer();
        logger.info("WeatherNotifier application started.");  
        
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("WeatherNotifier application stopped.");
        }));
    }
        
    private static void startSparkServer() {
        before((request, response) -> {
            response.header("Access-Control-Allow-Origin", "*");
            response.header("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
            response.header("Access-Control-Allow-Headers", "Content-Type, Authorization");
        });
                
        options("/*", (request, response) -> {
            String accessControlRequestHeaders = request.headers("Access-Control-Request-Headers");
            if (accessControlRequestHeaders != null) {
                response.header("Access-Control-Allow-Headers", accessControlRequestHeaders);
            }

            String accessControlRequestMethod = request.headers("Access-Control-Request-Method");
            if (accessControlRequestMethod != null) {
                response.header("Access-Control-Allow-Methods", accessControlRequestMethod);
            }

            return "OK";
        });
        
        post("/api/login", (Request request, Response response) -> {
            logger.info("Received login request.");
            try {
                JsonObject json = parseJsonRequestBody(request);

                phoneNumber = json.get("phoneNumber").getAsString();
                password = json.get("password").getAsString();

                countryCode = ValidationUtils.getValidCountryCode();
                formattedPhoneNumber = ValidationUtils.formatToE164(phoneNumber, countryCode);
                userId = UserManager.getUserId(formattedPhoneNumber);
                request.session().attribute("formattedPhoneNumber", formattedPhoneNumber);

                if (userId != -1 && UserManager.isUserAuthenticated(formattedPhoneNumber, password)) {
                    String token = UserManager.authenticateAndGetToken(formattedPhoneNumber, password, userId);

                    ZipCodeManager zipCodeManager = new ZipCodeManager();
                    List<ZipCodeManager.ZipCodeData> zipCodeDataList = zipCodeManager.getZipCodesAndDeliveryTimes(userId);

                    if (token != null) {
                        JsonObject responseJson = new JsonObject();
                        responseJson.addProperty("token", token);

                        if (zipCodeDataList != null && !zipCodeDataList.isEmpty()) {
                            JsonArray zipCodesArray = new JsonArray();
                            for (ZipCodeManager.ZipCodeData data : zipCodeDataList) {
                                JsonObject zipCodeObject = new JsonObject();
                                zipCodeObject.addProperty("zipCode", data.getZipCode());
                                zipCodeObject.addProperty("deliveryTime", data.getDeliveryTime());
                                zipCodesArray.add(zipCodeObject);
                            }
                            responseJson.add("zipCodes", zipCodesArray);
                        }

                        response.status(HTTP_OK);
                        return responseJson.toString();
                    } else {
                        response.status(HTTP_INTERNAL_SERVER_ERROR);
                        return "Error generating token.";
                    }
                } else {
                    response.status(HTTP_UNAUTHORIZED);
                    JsonObject error401 = new JsonObject();
                    error401.addProperty("error", "Invalid credentials.");
                    return error401.toString();
                }
            } catch (JsonSyntaxException e) {
                logger.error("Error parsing JSON during login.", e);
                response.status(HTTP_INTERNAL_SERVER_ERROR);
                return "An error occurred during login.";
            }
        });

        post("/api/forgotpassword", (Request request, Response response) -> {
            logger.info("Received forgotten password request.");
            try {
                JsonObject json = parseJsonRequestBody(request);

                phoneNumber = json.get("phoneNumber").getAsString();

                if (!ValidationUtils.isValidPhoneNumber(phoneNumber)) {
                    response.status(HTTP_BAD_REQUEST);
                    JsonObject error400 = new JsonObject();
                    error400.addProperty("error", "Invalid phone number format.");
                    return error400.toString();
                } else {
                    request.session().attribute("phoneNumber", phoneNumber);
                    countryCode = ValidationUtils.getValidCountryCode();
                    formattedPhoneNumber = ValidationUtils.formatToE164(phoneNumber, countryCode);
                }

                if (UserManager.getUserId(formattedPhoneNumber) != -1) {
                    response.status(HTTP_OK);
                    verificationSid = TwilioService.sendVerificationCode(formattedPhoneNumber);
                    return "Phone number exists.";
                } else {
                    response.status(HTTP_CONFLICT);
                    JsonObject error409 = new JsonObject();
                    error409.addProperty("error", "User doesn't exist, try signing up.");
                    return error409.toString();
                }
            } catch (JsonSyntaxException e) {
                logger.error("Error parsing JSON while resetting the forgotton password.", e);
                response.status(HTTP_INTERNAL_SERVER_ERROR);
                return "An error occurred resetting the forgotton password.";
            }
        });

        post("/api/forgotpasswordverify", (Request request, Response response) -> {
            logger.info("Received forgotten password verification request.");
            try {
                JsonObject json = parseJsonRequestBody(request);
                verificationCode = json.get("verificationCode").getAsString();

                String verificationStatus = TwilioService.verifyPhoneNumber(verificationSid, formattedPhoneNumber, verificationCode);
                logger.info("Verification check status: {}", verificationStatus);

                if ("approved".equals(verificationStatus)) {
                    response.status(HTTP_OK);
                    userId = UserManager.getUserId(formattedPhoneNumber);
                    request.session().attribute("formattedPhoneNumber", formattedPhoneNumber);
                    return "Phone number verified.";
                } else {
                    response.status(HTTP_BAD_REQUEST);
                    JsonObject error400 = new JsonObject();
                    error400.addProperty("error", "Invalid verification code.");
                    return error400.toString();
                }
            } catch (JsonSyntaxException e) {
                logger.error("Error parsing JSON while sending the forgotten password verification code.", e);
                response.status(HTTP_INTERNAL_SERVER_ERROR);
                return "An error occurred sending the forgotton password verification code.";
            }
        });

        post("/api/resetpassword", (Request request, Response response) -> {
            logger.info("Received reset password request.");
            try {
                JsonObject json = parseJsonRequestBody(request);

                password = json.get("password").getAsString();

                if (!ValidationUtils.isValidPassword(password)) {
                    response.status(HTTP_BAD_REQUEST);
                    JsonObject error400 = new JsonObject();
                    error400.addProperty("error", "Invalid password format.");
                    return error400.toString();
                } else {
                    response.status(HTTP_OK);
                    UserManager.resetPassword(formattedPhoneNumber, password);
                    return "Password successfully reset.";
                }
            } catch (JsonSyntaxException e) {
                logger.error("Error parsing JSON while resetting the password.", e);
                response.status(HTTP_INTERNAL_SERVER_ERROR);
                return "An error occurred while resetting the password.";
            }
        });

        post("/api/signup", (Request request, Response response) -> {
            logger.info("Received sign-up request.");
            try {
                JsonObject json = parseJsonRequestBody(request);

                phoneNumber = json.get("phoneNumber").getAsString();
                password = json.get("password").getAsString();

                if (!ValidationUtils.isValidPhoneNumber(phoneNumber) || !ValidationUtils.isValidPassword(password)) {
                    response.status(HTTP_BAD_REQUEST);
                    JsonObject error400 = new JsonObject();
                    error400.addProperty("error", "Invalid phone number or password format.");
                    return error400.toString();
                } else {
                    countryCode = ValidationUtils.getValidCountryCode();
                    formattedPhoneNumber = ValidationUtils.formatToE164(phoneNumber, countryCode);
                }

                if (UserManager.isUserAuthenticated(formattedPhoneNumber, password)) {
                    response.status(HTTP_CONFLICT);
                    JsonObject error409 = new JsonObject();
                    error409.addProperty("error", "User already registered, try logging in.");
                    return error409.toString();
                } else {
                    response.status(HTTP_OK);
                    verificationSid = TwilioService.sendVerificationCode(formattedPhoneNumber);
                    return "Sign-up successful.";
                }
            } catch (JsonSyntaxException e) {
                logger.error("Error parsing JSON during sign-up.", e);
                response.status(HTTP_INTERNAL_SERVER_ERROR);
                return "An error occurred during sign-up.";
            }
        });

        post("/api/verify", (Request request, Response response) -> {
            logger.info("Received verification request.");
            try {
                JsonObject json = parseJsonRequestBody(request);
                verificationCode = json.get("verificationCode").getAsString();

                String verificationStatus = TwilioService.verifyPhoneNumber(verificationSid, formattedPhoneNumber, verificationCode);
                logger.info("Verification check status: {}", verificationStatus);

                if ("approved".equals(verificationStatus)) {
                    response.status(HTTP_OK);
                    UserManager.registerNewUser(formattedPhoneNumber, password);
                    userId = UserManager.getUserId(formattedPhoneNumber);
                    request.session().attribute("formattedPhoneNumber", formattedPhoneNumber);
                    String token = UserManager.authenticateAndGetToken(formattedPhoneNumber, password, userId);

                    if (token != null) {
                        JsonObject jsonResponse = new JsonObject();
                        jsonResponse.addProperty("message", "Phone number successfully verified.");
                        jsonResponse.addProperty("token", token);
                        return jsonResponse.toString();
                    } else {
                        response.status(HTTP_INTERNAL_SERVER_ERROR);
                        return "Error generating token.";
                    }
                } else {
                    response.status(HTTP_BAD_REQUEST);
                    JsonObject error400 = new JsonObject();
                    error400.addProperty("error", "Invalid verification code.");
                    return error400.toString();
                }
            } catch (JsonSyntaxException e) {
                logger.error("Error parsing JSON during verification.", e);
                response.status(HTTP_INTERNAL_SERVER_ERROR);
                return "An error occurred during verification.";
            }
        });

        post("/api/forgotpasswordresend", (Request request, Response response) -> {
            logger.info("Received forgotten password verification resend request");
            try {
                JsonObject json = parseJsonRequestBody(request);

                phoneNumber = json.get("phoneNumber").getAsString();

                if (!ValidationUtils.isValidPhoneNumber(phoneNumber)) {
                    response.status(HTTP_BAD_REQUEST);
                    JsonObject error400 = new JsonObject();
                    error400.addProperty("error", "Invalid phone number format.");
                    return error400.toString();
                } else {
                    formattedPhoneNumber = ValidationUtils.formatToE164(phoneNumber, ValidationUtils.getValidCountryCode());
                    verificationSid = TwilioService.sendVerificationCode(formattedPhoneNumber);
                    response.status(HTTP_OK);
                    return "Verification code resent.";
                }
            } catch (JsonSyntaxException e) {
                logger.error("Error parsing JSON while resending the forgotten password verification code.", e);
                response.status(HTTP_INTERNAL_SERVER_ERROR);
                return "An error occurred resending the forgotten password verification code.";
            }
        });

        post("/api/addZipCode", (Request request, Response response) -> {
            logger.info("Received add a zip code request.");
            try {
                String secretKey = System.getenv("jwt_secret_key");
                formattedPhoneNumber = request.session().attribute("formattedPhoneNumber");
                userId = UserManager.getUserId(formattedPhoneNumber);
                String token = request.headers("Authorization").replace("Bearer ", "");
                DecodedJWT jwt = JWT.require(Algorithm.HMAC256(secretKey)).build().verify(token);
                userId = Integer.parseInt(jwt.getSubject());

                JsonObject json = parseJsonRequestBody(request);
                zipCode = json.get("zipCode").getAsString();
                String deliveryTime = json.get("deliveryTime").getAsString();

                zipCodeId = ZipCodeManager.getZipCodeId(zipCode);

                if (zipCodeId == -1) {
                    ZipCodeManager.insertZipCode(zipCode);
                    zipCodeId = ZipCodeManager.getZipCodeId(zipCode);
                    ZipCodeManager.associateUserWithZipDelivery(userId, zipCodeId, deliveryTime);
                } else {
                    String existingDeliveryTime = ZipCodeManager.getDeliveryTimeForZipCode(userId, zipCodeId);
                    if (existingDeliveryTime == null || !existingDeliveryTime.equals(deliveryTime)) {
                        ZipCodeManager.updateZipCodeAndDeliveryTime(userId, zipCodeId, deliveryTime);
                    }
                }
                
                request.session().attribute("zipCode", zipCode);
                request.session().attribute("deliveryTime", deliveryTime);
                response.status(HTTP_OK);
                return "Zip code and delivery time added successfully.";
            } catch (JsonSyntaxException e) {
                logger.error("Error parsing JSON while adding the zip code.", e);
                response.status(HTTP_INTERNAL_SERVER_ERROR);
                return "An error occurred adding the zip code.";
            } catch (SQLException e) {
                logger.error("SQL error while adding the zip code.", e);
                response.status(HTTP_INTERNAL_SERVER_ERROR);
                return "An error occurred adding the zip code.";
            }
        });

        post("/api/editZipCode", (Request request, Response response) -> {
            logger.info("Received edit a zip code request");
            try {
                String secretKey = System.getenv("jwt_secret_key");
                formattedPhoneNumber = request.session().attribute("formattedPhoneNumber");
                userId = UserManager.getUserId(formattedPhoneNumber);
                String token = request.headers("Authorization").replace("Bearer ", "");
                DecodedJWT jwt = JWT.require(Algorithm.HMAC256(secretKey)).build().verify(token);
                userId = Integer.parseInt(jwt.getSubject());

                JsonObject json = parseJsonRequestBody(request);
                zipCode = json.get("zipCode").getAsString();
                String oldDeliveryTime = json.get("oldDeliveryTime").getAsString();
                String newDeliveryTime = json.get("newDeliveryTime").getAsString();

                ZipCodeManager.editZipCodeAndDeliveryTime(userId, zipCode, oldDeliveryTime, newDeliveryTime);

                response.status(HTTP_OK);
                return "Delivery time updated successfully.";
            } catch (JsonSyntaxException e) {
                logger.error("Error parsing JSON while updating the delivery time.", e);
                response.status(HTTP_INTERNAL_SERVER_ERROR);
                return "An error occurred updaing the delivery time.";
            } catch (SQLException e) {
                logger.error("SQL Error while updating the delivery time.", e);
                response.status(HTTP_INTERNAL_SERVER_ERROR);
                return "An error occurred updaing the delivery time.";
            }
        });

        post("/api/deleteZipCode", (Request request, Response response) -> {
            logger.info("Received delete a zip code request");
            try {
                String secretKey = System.getenv("jwt_secret_key");
                formattedPhoneNumber = request.session().attribute("formattedPhoneNumber");
                userId = UserManager.getUserId(formattedPhoneNumber);
                String token = request.headers("Authorization").replace("Bearer ", "");
                DecodedJWT jwt = JWT.require(Algorithm.HMAC256(secretKey)).build().verify(token);
                userId = Integer.parseInt(jwt.getSubject());

                JsonObject json = parseJsonRequestBody(request);
                zipCode = json.get("zipCode").getAsString();
                String deliveryTime = json.get("deliveryTime").getAsString();

                ZipCodeManager.deleteZipCodeAndDeliveryTime(userId, zipCode, deliveryTime);

                response.status(HTTP_OK);
                return "Zip code and delivery time deleted successfully.";
            } catch (SQLException e) {
                logger.error("SQL error while deleting zip code.", e);
                response.status(HTTP_INTERNAL_SERVER_ERROR);
                return "Error deleting zip code.";
            } catch (JsonSyntaxException e) {
                logger.error("Error parsing JSON while deleting zip code.", e);
                response.status(HTTP_INTERNAL_SERVER_ERROR);
                return "Error deleting zip code.";
            }
        });

        get("/api/weather", (Request request, Response response) -> {
            logger.info("Received weather data request");
            try {
                String apiKey = System.getenv("accuweather_api_key");
                String[] zipCodes = request.queryParamsValues("zipCodes");

                List<WeatherData> weatherDataList = new ArrayList<>();

                Set<String> uniqueLocationNames = Collections.synchronizedSet(new HashSet<>());

                if (zipCodes != null) {
                    for (String currentZipCode : zipCodes) {
                        zipCode = currentZipCode;
                        logger.info("Processing Zip Code: {}", zipCode);
                        cacheKey = CacheUtils.generateCacheKey(zipCode);
                        WeatherData weatherData = CacheUtils.retrieveCachedWeatherData(cacheKey, zipCode);
                        if (weatherData == null) {
                            logger.info("weather data retrieval was null, fetching now...");
                            weatherData = WeatherApiService.fetchWeatherData(zipCode, apiKey, userId);
                            CacheUtils.storeWeatherDataInCache(cacheKey, weatherData.getLocationName(), 
                                                               weatherData.getDate(), weatherData.getMaxTemperature(), weatherData.getMinTemperature(),
                                                               weatherData.getWeatherCondition(), weatherData.getChanceOfRain(), weatherData.getWindSpeed(), 
                                                               weatherData.getWindDirection(), weatherData.getSunriseTime(), weatherData.getSunsetTime());
                        }
                        if (!uniqueLocationNames.contains(weatherData.getLocationName())) {
                            uniqueLocationNames.add(weatherData.getLocationName());
                            weatherDataList.add(weatherData);
                        }
                    }
                }
                Gson gson = new Gson();
                String jsonWeatherData = gson.toJson(weatherDataList);

                response.type("application/json");
                return jsonWeatherData;
            } catch (SQLException e) {
                logger.error("SQL error while retrieving weather data.", e);
                response.status(HTTP_INTERNAL_SERVER_ERROR);
                return "Error retrieving weather data.";
            } catch (JsonSyntaxException e) {
                logger.error("Error parsing JSON while retrieving weather data.", e);
                response.status(HTTP_INTERNAL_SERVER_ERROR);
                return "Error retrieving weather data.";
            } catch (Exception e) {
                logger.error("Error retrieving weather data.", e);
                response.status(HTTP_INTERNAL_SERVER_ERROR);
                return "Error retrieving weather data.";
            }
        });
        
        awaitInitialization();
    }
    
    private static JsonObject parseJsonRequestBody(Request request) {
        String requestBody = request.body();
        return JsonParser.parseString(requestBody).getAsJsonObject();
    }
}

    


