package com.edwinurrea.weathernotifier;

import org.junit.Test;
import static org.junit.Assert.*;

import java.sql.Date;

public class WeatherDataTest {   
    @Test
    public void testWeatherDataConstructor() {
        String locationName = "Los Angeles, California";
        Date date = new Date(System.currentTimeMillis());
        int maxTemperature = 75;
        int minTemperature = 60;
        String weatherCondition = "Sunny";
        int chanceOfRain = 20;
        String windSpeed = "10 mph";
        String windDirection = "NW";
        String sunriseTime = "6:00 AM";
        String sunsetTime = "6:00 PM";
        String zipCode = "12345";

        WeatherData weatherData = new WeatherData(locationName, date, maxTemperature, minTemperature,
                                                weatherCondition, chanceOfRain, windSpeed, windDirection,
                                                sunriseTime, sunsetTime, zipCode);

        assertEquals(locationName, weatherData.getLocationName());
        assertEquals(date, weatherData.getDate());
        assertEquals(maxTemperature, weatherData.getMaxTemperature());
        assertEquals(minTemperature, weatherData.getMinTemperature());
        assertEquals(weatherCondition, weatherData.getWeatherCondition());
        assertEquals(chanceOfRain, weatherData.getChanceOfRain());
        assertEquals(windSpeed, weatherData.getWindSpeed());
        assertEquals(windDirection, weatherData.getWindDirection());
        assertEquals(sunriseTime, weatherData.getSunriseTime());
        assertEquals(sunsetTime, weatherData.getSunsetTime());
        assertEquals(zipCode, weatherData.getZipCode());
    }   
}
