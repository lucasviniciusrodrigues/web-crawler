package com.axr.backend.config;

import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Logger;

public class AppConfig {

    private static final Logger log = Logger.getLogger(AppConfig.class.getName());

    private static final String PROPERTIES_FILE = "application.properties";
    private static Properties properties;

    // The static approach is used cause in that way the application will load the .properties just once
    // but with this we can't change the values of the .properties without restarting the application

    static {
        properties = new Properties();
        try (InputStream inputStream = AppConfig.class.getClassLoader().getResourceAsStream(PROPERTIES_FILE)) {
            if (inputStream != null) {
                properties.load(inputStream);
            } else {
                System.err.println("File " + PROPERTIES_FILE + " not found");
            }
        } catch (Exception e) {
            log.severe("Error reading properties file: " + PROPERTIES_FILE);
        }
    }

    public static String getProperty(String key) {
        return properties.getProperty(key);
    }
}
