package com.fnrco.erp.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;


public final class ConfigReader {
    // reads which environment to use (from -Denv,default staging)
    private static final String ENV = System.getProperty("env", "staging");
    // loads environment's properties file
    private static final Properties PROPS = load(ENV);

    //utility class: no objects
    private ConfigReader() {}

    private static Properties load(String env) {
        String path = "config/" + env + ".properties";
        Properties props = new Properties();
        try (InputStream in = ConfigReader.class.getClassLoader().getResourceAsStream(path)) {
            if (in == null) {
                throw new IllegalStateException("Config file not found: " + path);
            }
            props.load(in);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot read " + path, e);
        }
        return props;
    }

    // gets key value of a property
    public static String get(String key) {
        String value = System.getProperty(key);
        if (value == null) value = System.getenv(key.toUpperCase().replace('.', '_'));
        if (value == null) value = PROPS.getProperty(key);
        if (value == null) throw new IllegalStateException("Missing setting: " + key);
        
        return value.trim();
    }

    public static String getEnv(){ 
        return ENV; 
    }
    public static String getBaseUrl(){ 
        return get("baseUrl"); 
    }
    public static String getBrowser() { 
        return get("browser"); 
    }
    public static boolean isHeadless(){ 
        return Boolean.parseBoolean(get("headless")); 
    }
    public static int getTimeout(){
        return Integer.parseInt(get("timeout"));
    }
    public static String getUsername(){
        return get("erp.user");
    }
    public static String getPassword(){
        return get("erp.pass");
    }
}

