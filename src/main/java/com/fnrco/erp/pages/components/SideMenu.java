package com.fnrco.erp.pages.components;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.fnrco.erp.pages.BasePage;
import com.fnrco.erp.pages.requests.CreateRequestPage;

// The left sidebar, shared by every page after login.
// A group (e.g. "Requests") is <li class="slide has-sub"> with <a href="#">; it gets the
// class "open" when expanded. Its links are hidden until the group is open.
public class SideMenu extends BasePage {

    private final By sidebar = By.id("sidebar");

    // "contains" on href: the real href ends with a space ("...requests/create ")
    private final By createRequestLink =
            By.cssSelector("#sidebar a.side-menu__item[href*='/general/requests/create']");

    public CreateRequestPage openCreateRequest() {
        expandGroup("Requests");
        log.info("Sidebar: Requests > Create Request");
        click(createRequestLink);
        return new CreateRequestPage().waitUntilLoaded();
    }

    // Clicks the group header only when it is collapsed (clicking an open one closes it)
    private void expandGroup(String label) {
        waitVisible(sidebar);
        WebElement group = driver.findElement(By.xpath(
                "//aside[@id='sidebar']//li[contains(@class,'has-sub')]"
                + "[a[contains(@class,'side-menu__item')][normalize-space()='" + label + "']]"));
        if (!group.getAttribute("class").contains("open")) {
            log.info("Sidebar: expanding '{}'", label);
            group.findElement(By.xpath("./a")).click();
        }
    }
}
