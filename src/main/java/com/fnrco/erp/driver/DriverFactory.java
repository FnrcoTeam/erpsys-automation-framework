package com.fnrco.erp.driver;

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

import com.fnrco.erp.config.ConfigReader;

// create , manage and quit driver
public final class DriverFactory {
    
    private static final ThreadLocal<WebDriver> DRIVER = new ThreadLocal<>();
    private static final Logger log = LogManager.getLogger(DriverFactory.class);
    
    private DriverFactory(){}

    // create and init driver
    public static void init(){
        if(DRIVER.get() != null){
            // a browser opened for that driver
            throw new IllegalStateException("A browser is already open for this thread. quit the driver first !");
        }
        WebDriver driver;
        String browser = ConfigReader.getBrowser();
        boolean headless = ConfigReader.isHeadless();
        log.info("Starting {} (headless={})",browser,headless);

        switch (browser.toLowerCase()) {
            case "chrome":
            driver = new ChromeDriver(chromeOptions(headless));    
            break;
            case "firefox":
            driver = new FirefoxDriver(firefoxOptions(headless));    
            break;
            default:
            // not supported browser
                throw new IllegalArgumentException("Unsupported browser: " + browser);
        }
        if(!headless){
            driver.manage().window().maximize();
        }
        DRIVER.set(driver);

    }
    // get driver object
    public static WebDriver get(){
        WebDriver driver = DRIVER.get();
        if(driver == null){
            throw new IllegalStateException("No browser for this thread.");
        }
        return driver;
    }
    // quit and remove driver
    public static void remove(){
        WebDriver driver = DRIVER.get();
        if(driver != null){
            log.info("closing browser");
            try {
                driver.quit();
            } finally {
                DRIVER.remove();
            }
        }     
    }
    private static ChromeOptions chromeOptions(boolean headless){
        ChromeOptions options = new ChromeOptions(); 
        if(headless){
            options.addArguments("--headless=new","--window-size=1920,1080","--no-sandbox", "--disable-dev-shm-usage");
        }
        Map<String, Object> prefs = new HashMap<>();
        prefs.put("credentials_enable_service", false);
        prefs.put("profile.password_manager_enabled", false);
        prefs.put("profile.password_manager_leak_detection", false);
        options.setExperimentalOption("prefs", prefs);
        options.addArguments("--disable-notifications");

        return options;
    }
    private static FirefoxOptions firefoxOptions(boolean headless){
        FirefoxOptions options = new FirefoxOptions(); 
        if(headless){
            options.addArguments("-headless","--width=1920","--height=1080");
        }
        return options;
    }
}
