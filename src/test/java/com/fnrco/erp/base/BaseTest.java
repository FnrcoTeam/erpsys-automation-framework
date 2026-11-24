package com.fnrco.erp.base;

import java.io.ByteArrayInputStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.testng.ITestResult;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;

import com.fnrco.erp.config.ConfigReader;
import com.fnrco.erp.driver.DriverFactory;
import com.fnrco.erp.pages.authorization.DashboardPage;
import com.fnrco.erp.pages.authorization.LoginPage;

import io.qameta.allure.Allure;

// Parent of every test class: one browser per test class, shared by all its tests.
public abstract class BaseTest {

    protected final Logger log = LogManager.getLogger(getClass());

    // Runs once, before the first test of each class that extends BaseTest
    @BeforeClass(alwaysRun = true)
    public void setUp() {
        DriverFactory.init();
    }

    // Runs after every test: logs the result and takes a screenshot if it failed
    @AfterMethod(alwaysRun = true)
    public void afterEachTest(ITestResult result) {
        String name = result.getMethod().getMethodName();
        switch (result.getStatus()) {
            case ITestResult.SUCCESS -> log.info("PASSED: {}", name);
            case ITestResult.SKIP    -> log.warn("SKIPPED: {}", name);
            case ITestResult.FAILURE -> {
                log.error("FAILED: {}", name, result.getThrowable());
                attachScreenshot(name);
            }
            default -> { }
        }
    }

    // Runs once, after the last test of the class: closes the browser
    @AfterClass(alwaysRun = true)
    public void tearDown() {
        DriverFactory.remove();
    }

    // For module test classes (requests, MPR...) that need a logged-in user before their tests.
    // Call it from the subclass's own @BeforeClass: TestNG runs the parent's setUp() first.
    protected void loginAsDefaultUser() {
        new LoginPage().open().loginAs(ConfigReader.getUsername(), ConfigReader.getPassword());
        if (!new DashboardPage().isLoaded()) {
            throw new IllegalStateException("Login failed for " + ConfigReader.getUsername()
                    + ": dashboard did not load");
        }
    }

    private void attachScreenshot(String testName) {
        try {
            byte[] png = ((TakesScreenshot) DriverFactory.get()).getScreenshotAs(OutputType.BYTES);
            Allure.addAttachment(testName + " - screenshot on failure", "image/png",
                    new ByteArrayInputStream(png), "png");
        } catch (RuntimeException e) {
            // no browser (setUp failed) or it crashed: nothing to capture
            log.warn("Could not take screenshot: {}", e.getMessage());
        }
    }
}
