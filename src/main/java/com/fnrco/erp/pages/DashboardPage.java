package com.fnrco.erp.pages;

import org.openqa.selenium.By;

public class DashboardPage extends BasePage {

    private final By welcomeHeading = By.xpath("//div[contains(@class,'identity-info')]/h2");

    public boolean isLoaded() {
        return isVisible(welcomeHeading);
    }
    public String getWelcomeText() {
        return textOf(welcomeHeading).trim();
    }
}