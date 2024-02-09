package com.edwinurrea.weathernotifier;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import org.json.simple.JSONObject;

public class DateTimeUtils extends WeatherNotifier {
    protected static long convertToEpochTimestamp(String iso8601DateTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
        LocalDateTime dateTime = LocalDateTime.parse(iso8601DateTime, formatter);
        return dateTime.atZone(ZoneId.systemDefault()).toEpochSecond();
    }

    protected static String formatTimestampToTime(long epochTimestamp) {
        Instant instant = Instant.ofEpochSecond(epochTimestamp);
        LocalDateTime localDateTime = instant.atZone(ZoneId.systemDefault()).toLocalDateTime();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("h:mm a");
        return localDateTime.format(formatter);
    }

    protected static int extractChanceOfRain(JSONObject dayForecast) {
        JSONObject dayDetails = (JSONObject) dayForecast.get("Day");
        if (dayDetails.containsKey("RainProbability")) {
            Object rainProbability = dayDetails.get("RainProbability");
            if (rainProbability instanceof Number number) {
                return number.intValue();
            } else {
                return -1;
            }
        } else {
            return -1;
        }
    }

    protected static String extractWindSpeed(JSONObject dayForecast) {
        JSONObject dayDetails = (JSONObject) dayForecast.get("Day");
        JSONObject wind = (JSONObject) dayDetails.get("Wind");
        return wind != null && wind.containsKey("Speed") ? ((JSONObject) wind.get("Speed")).get("Value").toString() : null;
    }

    protected static String extractWindDirection(JSONObject dayForecast) {
        JSONObject dayDetails = (JSONObject) dayForecast.get("Day");
        JSONObject wind = (JSONObject) dayDetails.get("Wind");
        return wind != null && wind.containsKey("Direction") ? ((JSONObject) wind.get("Direction")).get("Localized").toString() : null;
    }

    protected static String extractSunrise(JSONObject dayForecast) {
        JSONObject sunInfo = (JSONObject) dayForecast.get("Sun");
        if (sunInfo != null && sunInfo.containsKey("Rise")) {
            String sunriseISO = sunInfo.get("Rise").toString();
            return formatTimestampToTime(convertToEpochTimestamp(sunriseISO));
        }
        return null;
    }

    protected static String extractSunset(JSONObject dayForecast) {
        JSONObject sunInfo = (JSONObject) dayForecast.get("Sun");
        if (sunInfo != null && sunInfo.containsKey("Set")) {
            String sunsetISO = sunInfo.get("Set").toString();
            return formatTimestampToTime(convertToEpochTimestamp(sunsetISO));
        }
        return null;
    }   
}
