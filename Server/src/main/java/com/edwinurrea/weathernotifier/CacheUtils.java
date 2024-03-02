package com.edwinurrea.weathernotifier;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.*;

import java.util.Calendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CacheUtils extends WeatherNotifier{
    private static final Logger logger = LoggerFactory.getLogger(WeatherNotifier.class);
    
    public static String generateCacheKey(String zipCode) {
        return "location:" + zipCode;
    }
    
    public static WeatherData retrieveCachedWeatherData(String cacheKey, String zipCode) {
        String dbUrl = System.getenv("database.url");
        String dbUser = System.getenv("database.username");
        String dbPassword = System.getenv("database.password");

        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword)) {
        String cleanupQuery = "DELETE FROM weather_cache WHERE expiration < CURRENT_TIMESTAMP";
        try (PreparedStatement cleanupStmt = conn.prepareStatement(cleanupQuery)) {
            int cleanupRowsAffected = cleanupStmt.executeUpdate();
            if (cleanupRowsAffected > 0) {
                logger.info(cleanupRowsAffected + " row(s) of expired data deleted.");
            } else {
                    logger.info("No expired data found.");
                }
            } catch (SQLException e) {
                logger.error("SQL Error occurred during retrieval.", e);
                logger.info("Error: Failed to clear expired weather cache.");
            }
    
            String query = "SELECT wc.location_name, wc.date, wc.max_temperature, wc.min_temperature, wc.weather_condition, "
                        +  "wc.chance_of_rain, wc.wind_speed, wc.wind_direction, wc.sunrise_time, wc.sunset_time "
                        +  "FROM weather_cache wc "
                        +  "WHERE wc.cache_key = ? AND wc.expiration >= CURRENT_TIMESTAMP";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, cacheKey);
                ResultSet resultSet = stmt.executeQuery();
                if (resultSet.next()) {
                    String locationName = resultSet.getString("location_name");
                    Date date = resultSet.getDate("date");
                    int maxTemperature = resultSet.getInt("max_temperature");
                    int minTemperature = resultSet.getInt("min_temperature");
                    String weatherCondition = resultSet.getString("weather_condition");
                    int chanceOfRain = resultSet.getInt("chance_of_rain");
                    String windSpeed = resultSet.getString("wind_speed");
                    String windDirection = resultSet.getString("wind_direction");
                    String sunriseTime = resultSet.getString("sunrise_time");
                    String sunsetTime = resultSet.getString("sunset_time");
 
                    // WeatherData object to hold the retrieved data
                    WeatherData weatherData = new WeatherData(locationName, date, maxTemperature, minTemperature,
                            weatherCondition, chanceOfRain, windSpeed, windDirection, sunriseTime, sunsetTime, zipCode);

                    logger.info("Data Retrieved.");

                    return weatherData;
                }
            }
        } catch (SQLException e) {
            logger.error("Error: Failed to retrieve weather data.", e);
        }

        return null;
    }
    
    public static void storeWeatherDataInCache(String cacheKey, String locationName, Date date, int maxTemperature, int minTemperature,
        String weatherCondition, int chanceOfRain, String windSpeed, String windDirection,
        String sunrise, String sunset) throws SQLException {
        String dbUrl = System.getenv("database.url");
        String dbUser = System.getenv("database.username");
        String dbPassword = System.getenv("database.password");
        
        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword)) {
            String cleanupQuery = "DELETE FROM weather_cache WHERE expiration < CURRENT_TIMESTAMP";
            try (PreparedStatement cleanupStmt = conn.prepareStatement(cleanupQuery)) {
                int cleanupRowsAffected = cleanupStmt.executeUpdate();
                if (cleanupRowsAffected > 0) {
                    logger.info(cleanupRowsAffected + " row(s) of expired data deleted.");
                } else {
                    logger.info("No expired data found.");
                }
            } catch (SQLException e) {
                logger.error("Error: Failed to clear expired weather cache.", e);
            }
            
                    // If the weather cache already exists for the subscriber, data updates
                    String query = "INSERT INTO weather_cache (cache_key, location_name, date, max_temperature, min_temperature, "
                            + "weather_condition, chance_of_rain, wind_speed, wind_direction, sunrise_time, sunset_time, expiration) "
                            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) "
                            + "ON CONFLICT (cache_key) DO UPDATE SET "
                            + "max_temperature = EXCLUDED.max_temperature, min_temperature = EXCLUDED.min_temperature, weather_condition = EXCLUDED.weather_condition, "
                            + "chance_of_rain = EXCLUDED.chance_of_rain, wind_speed = EXCLUDED.wind_speed, wind_direction = EXCLUDED.wind_direction, sunrise_time = EXCLUDED.sunrise_time, sunset_time = EXCLUDED.sunset_time, expiration = EXCLUDED.expiration";
                    try (PreparedStatement insertOrUpdateStmt = conn.prepareStatement(query)) {
                        insertOrUpdateStmt.setString(1, cacheKey);
                        insertOrUpdateStmt.setString(2, locationName);
                        insertOrUpdateStmt.setDate(3, date);
                        insertOrUpdateStmt.setInt(4, maxTemperature);
                        insertOrUpdateStmt.setInt(5, minTemperature);
                        insertOrUpdateStmt.setString(6, weatherCondition);
                        insertOrUpdateStmt.setInt(7, chanceOfRain);
                        insertOrUpdateStmt.setString(8, windSpeed);
                        insertOrUpdateStmt.setString(9, windDirection);
                        insertOrUpdateStmt.setString(10, sunrise);
                        insertOrUpdateStmt.setString(11, sunset);
                        Calendar expiration = Calendar.getInstance();
                        expiration.setTime(new Date(System.currentTimeMillis()));
                        expiration.set(Calendar.HOUR_OF_DAY, 23);
                        expiration.set(Calendar.MINUTE, 59);
                        expiration.set(Calendar.SECOND, 59);
                        expiration.set(Calendar.MILLISECOND, 999);
                        insertOrUpdateStmt.setTimestamp(12, new Timestamp(expiration.getTimeInMillis()));
                        insertOrUpdateStmt.executeUpdate();
                        logger.info("Update Executed.");        
            }
        } catch (SQLException e) {
                    logger.error("SQL Error occurred while storing data in cache: {}" + e);
                    logger.info("Error: Failed to insert or update weather cache.");
        }
    }
}
