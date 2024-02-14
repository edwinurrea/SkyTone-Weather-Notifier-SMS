package com.edwinurrea.weathernotifier;

import java.sql.Date;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class WeatherData extends WeatherNotifier {
    private final String locationName;
    private final Date date;
    private final int maxTemperature;
    private final int minTemperature;
    private final String weatherCondition;
    private final int chanceOfRain;
    private final String windSpeed;
    private final String windDirection;
    private final String sunriseTime;
    private final String sunsetTime;
    private final String zipCode;
    
    public WeatherData(String locationName, Date date, int maxTemperature, int minTemperature,
                       String weatherCondition, int chanceOfRain, String windSpeed, String windDirection,
                       String sunriseTime, String sunsetTime, String zipCode) {
        this.locationName = locationName;
        this.date = date;
        this.maxTemperature = maxTemperature;
        this.minTemperature = minTemperature;
        this.weatherCondition = weatherCondition;
        this.chanceOfRain = chanceOfRain;
        this.windSpeed = windSpeed;
        this.windDirection = windDirection;
        this.sunriseTime = sunriseTime;
        this.sunsetTime = sunsetTime;
        this.zipCode = zipCode;
    }

    public String getLocationName() {
        return locationName;
    }
    
    public Date getDate() {
        return date;
    }

    public int getMaxTemperature() {
        return maxTemperature;
    }

    public int getMinTemperature() {
        return minTemperature;
    }

    public String getWeatherCondition() {
        return weatherCondition;
    }

    public int getChanceOfRain() {
        return chanceOfRain;
    }

    public String getWindSpeed() {
        return windSpeed;
    }

    public String getWindDirection() {
        return windDirection;
    }

    public String getSunriseTime() {
        return sunriseTime;
    }

    public String getSunsetTime() {
        return sunsetTime;
    }

    public String getZipCode() {
        return zipCode;
    }
    
    protected static JSONObject getDayForecast(JSONArray dailyForecasts) {
        return (JSONObject) dailyForecasts.get(0);
    }
    
    protected static int getMaxTemperature(JSONObject day) {
        JSONObject temperature = (JSONObject) day.get("Temperature");
        JSONObject maxTemperatureObject = (JSONObject) temperature.get("Maximum");
        double maxTemperatureValue = (Double) maxTemperatureObject.get("Value");
        return (int) Math.round(maxTemperatureValue);
    }

    protected static int getMinTemperature(JSONObject day) {
        JSONObject temperature = (JSONObject) day.get("Temperature");
        JSONObject minTemperatureObject = (JSONObject) temperature.get("Minimum");
        double minTemperatureValue = (Double) minTemperatureObject.get("Value");
        return (int) Math.round(minTemperatureValue);
    }

    protected static String getWeatherCondition(JSONObject day) {
        JSONObject dayDetails = (JSONObject) day.get("Day");
        return dayDetails.get("IconPhrase").toString();
    }
}
