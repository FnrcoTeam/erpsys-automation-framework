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

    // Title shown at the top of every ERP page after login (e.g. "Requests", "Create Request")
    private final By pageTitle = By.cssSelector("h1.page-title");

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
    protected void typeText(By locator, String text) {
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
    // Waits until the URL contains the given part: use it after a click that opens another page
    protected void waitForUrl(String urlPart) {
        wait.until(ExpectedConditions.urlContains(urlPart));
    }

    // ---------- Select2 dropdowns (used across the ERP) ----------
    // Select2 hides the real <select id="X"> and draws its own widget:
    //   - the box you click:  <span id="select2-X-container" title="selected text">
    //   - the options list:   <ul id="select2-X-results"><li role="option">...</li></ul>
    //   - a search box inside the open dropdown
    protected void selectSelect2Option(String selectId, String optionText) {
        log.info("Selecting '{}' in {}", optionText, selectId);
        click(By.id("select2-" + selectId + "-container"));
        typeText(By.cssSelector(".select2-container--open .select2-search__field"), optionText);
        click(By.xpath("//ul[@id='select2-" + selectId + "-results']/li[normalize-space()='"
                + optionText + "']"));
    }

    protected String getSelect2Selected(String selectId) {
        String title = waitVisible(By.id("select2-" + selectId + "-container")).getAttribute("title");
        return title == null ? "" : title.trim();
    }

    public String getPageTitle() {
        return textOf(pageTitle).trim();
    }

    public String getCurrentUrl() {
        return driver.getCurrentUrl();
    }
}
