package com.edwinurrea.weathernotifier;

import java.io.IOException;
import java.io.InputStream;

import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatabaseConnector {
    private static final Logger logger = LoggerFactory.getLogger(WeatherNotifier.class);
    protected static final Properties config = new Properties();
    
    static {
        loadConfiguration();
    }
    
    public static void loadConfiguration() {
        try (InputStream inputStream = DatabaseConnector.class.getResourceAsStream("/config.properties")) {
            config.load(inputStream);
        } catch (IOException e) {
            logger.error("Error: Failed to load the configuration file. Please make sure it exists.", e);
        }
    }
    
    public static String getProperty(String key) {
        return config.getProperty(key);
    }
}


