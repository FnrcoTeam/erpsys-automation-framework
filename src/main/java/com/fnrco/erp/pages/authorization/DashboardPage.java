package com.fnrco.erp.pages.authorization;

import java.time.Duration;

import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.fnrco.erp.pages.BasePage;

public class DashboardPage extends BasePage {

    private final By welcomeHeading = By.xpath("//div[contains(@class,'identity-info')]/h2");

    // "Important announcements" popup that can open on the dashboard after login.
    // While it is open it covers the page (e.g. the sidebar cannot be clicked).
    private final By announcementModal = By.id("announcementDetailsModal");
    private final By announcementClose =
            By.cssSelector("#announcementDetailsModal [data-bs-dismiss='modal']");

    public boolean isLoaded() {
        return isVisible(welcomeHeading);
    }

    public String getWelcomeText() {
        return textOf(welcomeHeading).trim();
    }

    // The popup opens a few seconds AFTER the dashboard loads, with a fade-in animation.
    // A click on × during the animation is ignored, so we keep clicking until it is gone.
    // Does nothing when no announcement opens.
    public DashboardPage closeAnnouncementIfShown() {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(8))
                    .until(ExpectedConditions.visibilityOfElementLocated(announcementModal));
        } catch (TimeoutException noAnnouncement) {
            return this;
        }
        log.info("Closing the announcement popup");
        wait.until(d -> {
            if (d.findElements(announcementModal).stream().noneMatch(m -> m.isDisplayed())) {
                return true;
            }
            try {
                d.findElement(announcementClose).click();
            } catch (RuntimeException stillAnimating) {
                // not clickable yet: try again on the next poll
            }
            return false;
        });
        return this;
    }
}
