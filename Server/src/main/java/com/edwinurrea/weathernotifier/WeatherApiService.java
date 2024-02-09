package com.edwinurrea.weathernotifier;

import java.io.IOException;

import java.sql.Date;
import java.sql.SQLException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WeatherApiService extends WeatherNotifier {
    private static final Logger logger = LoggerFactory.getLogger(WeatherNotifier.class);
    
    protected static String buildForecastApiEndpoint(String locationKey, String apiKey) {
        return "http://dataservice.accuweather.com/forecasts/v1/daily/1day/" + locationKey + "?apikey=" + apiKey + "&details=true";
    }

    protected static String buildLocationApiEndpoint(String zipCode, String apiKey) {
        return "http://dataservice.accuweather.com/locations/v1/search?q=" + zipCode + "," + ValidationUtils.getValidCountryCode() + "&apikey=" + apiKey;
    }
    
    protected static String buildAutoLocationApiEndpoint(String query, String apiKey) {
        if (isNumeric(query)) {
            return "http://dataservice.accuweather.com/locations/v1/postalcodes/autocomplete?q=" + query + "&apikey=" + apiKey;
        } else {
            return "http://dataservice.accuweather.com/locations/v1/cities/autocomplete?q=" + query + "&apikey=" + apiKey;
        }
    }
    
    private static boolean isNumeric(String str) {
        return str.matches("\\d+");
    }

    protected static class LocationInfo {

        protected final String locationKey;
        protected final String locationName;

        public LocationInfo(String locationKey, String locationName) {
            this.locationKey = locationKey;
            this.locationName = locationName;
        }

        public String getLocationKey() {
            return locationKey;
        }

        public String getLocationName() {
            return locationName;
        }
    }
    
    protected static LocationInfo getLocationInfo(String zipCode, String apiKey) {
        String locationEndpoint = buildLocationApiEndpoint(zipCode, apiKey);
        CloseableHttpClient httpClient = HttpClients.createDefault();

        try {
            ClassicHttpResponse response = httpClient.execute(new HttpGet(locationEndpoint));
            HttpEntity entity = response.getEntity();

            if (entity != null) {
                String responseString = EntityUtils.toString(entity);

                JSONParser parser = new JSONParser();
                JSONArray jsonArray = (JSONArray) parser.parse(responseString);

                for (Object obj : jsonArray) {
                    JSONObject jsonObject = (JSONObject) obj;
                    if (jsonObject.containsKey("Key") && jsonObject.containsKey("LocalizedName") && jsonObject.containsKey("AdministrativeArea")) {
                        String locationKey = jsonObject.get("Key").toString();
                        String cityName = jsonObject.get("LocalizedName").toString();
                        String stateName = ((JSONObject) jsonObject.get("AdministrativeArea")).get("LocalizedName").toString();
                        String locationName = cityName + ", " + stateName;
                        return new LocationInfo(locationKey, locationName);
                    }
                }
            }
        } catch (IOException e) {
            logger.error("Error: Failed to connect to the API. Please check your network connection.", e);
        } catch (org.json.simple.parser.ParseException | ParseException e) {
            logger.error("Error: Failed to parse the API response. Please try again later.", e);
        } finally {
            try {
                httpClient.close();
            } catch (IOException e) {
                logger.error("Error: Failed to close the HTTP client.", e);
            }
        }

        return null;
    }
    
    protected static List<LocationInfo> getLocationSuggestions(String query, String apikey) {
        String locationEndpoint = buildLocationApiEndpoint(query, apikey);
        CloseableHttpClient httpClient = HttpClients.createDefault();
        
        try {
            ClassicHttpResponse response = httpClient.execute(new HttpGet(locationEndpoint));
            HttpEntity entity = response.getEntity();
            
            if (entity != null) {
                String responseString = EntityUtils.toString(entity);
                
                JSONParser parser = new JSONParser();
                JSONArray jsonArray = (JSONArray) parser.parse(responseString);
                    
                List<LocationInfo> suggestions = new ArrayList<>();
                    
                for (Object obj : jsonArray) {
                    JSONObject jsonObject = (JSONObject) obj;
                    if (jsonObject.containsKey("Key") && jsonObject.containsKey("LocalizedName") && jsonObject.containsKey("AdminstrativeArea")) {
                        String locationKey = jsonObject.get("Key").toString();
                        String cityName = jsonObject.get("LocalizedName").toString();
                        String stateName = ((JSONObject) jsonObject.get("AdminstrativeArea")).get("LocalizedName").toString();
                        String locationName = cityName + ", " + stateName;
                        suggestions.add(new LocationInfo(locationKey, locationName));
                    }
                }
                    
                return suggestions;
            }
        } catch (IOException e) {
            logger.error("Error: Failed to connect to the API. Please check your network connection.", e);
        } catch (org.json.simple.parser.ParseException | ParseException e) {
            logger.error("Error: Failed to parse the API response. Please try again later.", e);
        } finally {
            try {
                httpClient.close();
            } catch (IOException e) {
                logger.error("Error: Failed to close the HTTP client.", e);
            }
        }
        return Collections.emptyList();
    }


    protected static WeatherData fetchWeatherData(String zipCode, String apiKey, int userId) throws SQLException {      
        LocationInfo locationInfo = getLocationInfo(zipCode, apiKey);
        if(locationInfo != null) {
            String locationKey = locationInfo.getLocationKey();
            String forecastEndpoint = buildForecastApiEndpoint(locationKey, apiKey);
                        
            try {
                CloseableHttpClient httpClient = HttpClients.createDefault();
                ClassicHttpResponse forecastResponse = httpClient.execute(new HttpGet(forecastEndpoint));
                HttpEntity forecastEntity = forecastResponse.getEntity();
                
                if (forecastEntity != null) {
                    String forecastResponseString = EntityUtils.toString(forecastEntity);
                    
                    JSONParser parser = new JSONParser();
                    JSONObject forecastJson = (JSONObject) parser.parse(forecastResponseString);
                    
                    WeatherData weatherData = parseForecastJson(forecastJson, zipCode, userId);
                    return weatherData;
                }
            } catch (IOException e) {
                logger.error("Error: Failed to connect to the API. Please check your network connection.", e);
            } catch (org.json.simple.parser.ParseException | ParseException e) {
                logger.error("Error: Failed to parse the API response. Please try again later.", e);
            }
        } else {
            logger.error("Error: Failed to obtain the location key for the specified zip code.");
        }
        return null;
    }
    
    protected static WeatherData parseForecastJson(JSONObject forecastJson, String zipCode, int userId) throws SQLException {
        int subscriberId = SubscriberDao.getSubscriberId(userId);
        logger.info("ParseSubscriber ID: {}", subscriberId);
        
        try {
            String apiKey = DatabaseConnector.config.getProperty("accuweather.api.key");
            LocationInfo locationInfo = getLocationInfo(zipCode, apiKey);
            
            logger.info("Location Info: {}", locationInfo);
            
            if (locationInfo != null) {
                String locationName = locationInfo.getLocationName();
                logger.info("Location Name: {}", locationName);
                LocalDate currentDate = LocalDate.now();
                String formattedDate = currentDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

                JSONArray dailyForecasts = (JSONArray) forecastJson.get("DailyForecasts");
                JSONObject dayForecast = WeatherData.getDayForecast(dailyForecasts);

                int maxTemperature = WeatherData.getMaxTemperature(dayForecast);
                int minTemperature = WeatherData.getMinTemperature(dayForecast);
                String weatherCondition = WeatherData.getWeatherCondition(dayForecast);
                int chanceOfRain = DateTimeUtils.extractChanceOfRain(dayForecast);
                String windSpeed = DateTimeUtils.extractWindSpeed(dayForecast);
                String windDirection = DateTimeUtils.extractWindDirection(dayForecast);
                String sunrise = DateTimeUtils.extractSunrise(dayForecast);
                String sunset = DateTimeUtils.extractSunset(dayForecast);

                logger.info("Max Temperature: {}", maxTemperature);
                logger.info("Min Temperature: {}", minTemperature);
                logger.info("Weather Condition: {}", weatherCondition);
                logger.info("Chance of Rain: {}", chanceOfRain);
                logger.info("Wind Speed: {}", windSpeed);
                logger.info("Wind Direction: {}", windDirection);
                logger.info("Sunrise: {}", sunrise);
                logger.info("Sunset: {}", sunset);
                logger.info("Date: {}", Date.valueOf(formattedDate));
                return new WeatherData(subscriberId, locationName, Date.valueOf(formattedDate),
                        maxTemperature, minTemperature, weatherCondition, chanceOfRain,
                        windSpeed, windDirection, sunrise, sunset, zipCode);
            } else {
                logger.error("Error: Failed to obtain location information for the specified zip code.");
            }
        } catch (Exception e) {
            logger.error("Error: {}",e);
        }
        return null;
    }
}
