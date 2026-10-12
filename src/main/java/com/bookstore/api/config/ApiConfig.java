package com.bookstore.api.config;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Locale;
import java.util.Properties;


public final class ApiConfig {

    private static final String CONFIG_FILE = "config.properties";
    private static final ApiConfig INSTANCE = new ApiConfig(loadDefaults());

    private final Properties defaults;

    private ApiConfig(Properties defaults) {
        this.defaults = defaults;
    }

    public static ApiConfig get() {
        return INSTANCE;
    }

    public String baseUrl() {
        return value("api.base.url");
    }

    public String basePath() {
        return value("api.base.path");
    }

    public int connectTimeoutMs() {
        return intValue("api.connect.timeout.ms");
    }

    public int readTimeoutMs() {
        return intValue("api.read.timeout.ms");
    }

    public long maxResponseTimeMs() {
        return intValue("api.max.response.time.ms");
    }

    public boolean isHttpLoggingEnabled() {
        return Boolean.parseBoolean(value("api.http.logging.enabled"));
    }

    private int intValue(String key) {
        String raw = value(key);
        try {
            return Integer.parseInt(raw);
        } catch (NumberFormatException e) {
            throw new IllegalStateException("Configuration value '" + key + "' must be an integer but was '" + raw + "'", e);
        }
    }

    private String value(String key) {
        String systemProperty = System.getProperty(key);
        if (isPresent(systemProperty)) {
            return systemProperty.trim();
        }
        String environmentVariable = System.getenv(toEnvironmentVariableName(key));
        if (isPresent(environmentVariable)) {
            return environmentVariable.trim();
        }
        String defaultValue = defaults.getProperty(key);
        if (isPresent(defaultValue)) {
            return defaultValue.trim();
        }
        throw new IllegalStateException("Missing configuration value for '" + key + "'");
    }

    private static String toEnvironmentVariableName(String key) {
        return key.toUpperCase(Locale.ROOT).replace('.', '_');
    }

    private static boolean isPresent(String value) {
        return value != null && !value.isBlank();
    }

    private static Properties loadDefaults() {
        Properties properties = new Properties();
        try (InputStream stream = ApiConfig.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (stream == null) {
                throw new IllegalStateException("'" + CONFIG_FILE + "' was not found on the classpath");
            }
            properties.load(stream);
            return properties;
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read '" + CONFIG_FILE + "'", e);
        }
    }
}
