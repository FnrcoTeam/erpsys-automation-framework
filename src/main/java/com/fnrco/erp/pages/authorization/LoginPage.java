package com.fnrco.erp.pages.authorization;

import org.openqa.selenium.By;

import com.fnrco.erp.pages.BasePage;

public class LoginPage extends BasePage {

    private static final String PATH = "/login";

    private final By username     = By.xpath("//input[@id='email' or @name='email']");
    private final By password     = By.xpath("//input[@id='password' or @name='password']");
    private final By loginButton  = By.xpath("//button[@type='submit' and normalize-space()='Login']");
    private final By errorMessage = By.cssSelector("#email + .invalid-feedback");

    public LoginPage open() {
        openPath(PATH);
        waitVisible(username);
        return this;
    }

    public void loginAs(String user, String pass) {
        log.info("Logging in as {}", user);   // never log the password
        typeText(username, user);
        typeText(password, pass);
        click(loginButton);
    }

    public boolean isErrorShown() {
        return isVisible(errorMessage);
    }
    public String getEmailErrorText() {
        return textOf(errorMessage).trim();
    }
}

