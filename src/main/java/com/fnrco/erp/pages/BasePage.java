package com.fnrco.erp.pages;

import java.time.Duration;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.fnrco.erp.config.ConfigReader;
import com.fnrco.erp.driver.DriverFactory;

public abstract class BasePage {
    protected final WebDriver driver;
    protected final WebDriverWait wait;
    protected final Logger log = LogManager.getLogger(getClass());

    protected BasePage() {
        this.driver = DriverFactory.get();
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(ConfigReader.getTimeout()));
    }
    protected void openPath(String path) {
        String url = ConfigReader.getBaseUrl() + path;
        log.info("Opening {}", url);
        driver.get(url);
    }
    protected WebElement waitVisible(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }
    protected void click(By locator) {
        wait.until(ExpectedConditions.elementToBeClickable(locator)).click();
    }
    protected void type(By locator, String text) {
        WebElement field = waitVisible(locator);
        field.clear();
        field.sendKeys(text);
    }
    protected String textOf(By locator) {
        return waitVisible(locator).getText();
    }
    protected boolean isVisible(By locator) {
        try {
            waitVisible(locator);
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }
    
}
