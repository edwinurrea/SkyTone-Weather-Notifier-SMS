package com.edwinurrea.weathernotifier;

import java.io.IOException;

import java.sql.Date;
import java.sql.SQLException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

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
        try {
            String apiKey = System.getenv("accuweather.api.key");
            LocationInfo locationInfo = getLocationInfo(zipCode, apiKey);
            
            if (locationInfo != null) {
                String locationName = locationInfo.getLocationName();
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

                return new WeatherData(locationName, Date.valueOf(formattedDate),
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
