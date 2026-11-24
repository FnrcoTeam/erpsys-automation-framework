package com.fnrco.erp.pages.requests;

import org.openqa.selenium.By;

import com.fnrco.erp.pages.BasePage;

// Requests list: Main menu > Requests > Requests  (/en/general/requests)
public class RequestsPage extends BasePage {

    private static final String PATH = "/en/general/requests";

    // "Add new" is a link to the create page (not a popup). Matching the end of the href
    // keeps it working in any language (/en/... or /ar/...).
    private final By addNewButton = By.cssSelector("a.btn[href*='/general/requests/create']");

    public RequestsPage open() {
        openPath(PATH);
        waitVisible(addNewButton);
        return this;
    }

    public boolean isAddNewVisible() {
        return isVisible(addNewButton);
    }

    public CreateRequestPage clickAddNew() {
        log.info("Clicking 'Add new' request");
        click(addNewButton);
        return new CreateRequestPage().waitUntilLoaded();
    }
}
