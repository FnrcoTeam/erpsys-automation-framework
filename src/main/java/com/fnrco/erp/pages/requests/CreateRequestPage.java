package com.fnrco.erp.pages.requests;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;

import com.fnrco.erp.pages.BasePage;

// Create Request form (/en/general/requests/create).
// Covered so far: Filter by Group, Request Type, Start Date, Select Whom, Client,
// Save Request, validation popup.
public class CreateRequestPage extends BasePage {

    private static final String PATH = "/en/general/requests/create";
    static final String URL_PART = "/general/requests/create";

    // ---- Form fields (ids of the hidden <select>s that Select2 is built on) ----
    private static final String GROUP_FILTER_ID = "requestTypeGroupFilter"; // optional
    private static final String REQUEST_TYPE_ID = "requestTypeSelect";      // required

    // Start Date is a flatpickr: a hidden input holds the value sent to the server,
    // and the visible read-only input right after it shows it (e.g. "24-11-2026 15:53")
    private final By startDateHidden  = By.id("start_date");
    private final By startDateVisible = By.cssSelector("#start_date + input.flatpickr-input");

    // Select Whom: a normal (native) <select name="for_whom">, not Select2.
    // Options: Client, Employee, Candidate Application, Internal Department.
    // Each choice shows its own section (#client_section, #employee_section, ...).
    private final By whomSelect = By.id("type_selector");

    // Client (shown after Select Whom = Client): a Select2 with a search box
    private static final String CLIENT_ID = "client";

    // id only: "@type='submit'" could also match other submit buttons on the page
    private final By saveRequestBtn = By.id("saveRequestBtn");
    private final By loader = By.id("loader2");

    // ---- Validation popup (SweetAlert2) ----
    private final By popupTitle    = By.id("swal2-title");
    private final By popupMessages = By.cssSelector("#swal2-html-container li");
    private final By popupOkButton = By.cssSelector("button.swal2-confirm");

    public CreateRequestPage open() {
        openPath(PATH);
        return waitUntilLoaded();
    }

    // Wait for the URL first (after a click the old page is still on screen), then the form
    public CreateRequestPage waitUntilLoaded() {
        waitForUrl(URL_PART);
        waitVisible(saveRequestBtn);
        return this;
    }

    // ---------- Filter by Group (optional: only narrows the Request Type list) ----------

    public CreateRequestPage selectGroup(String group) {
        selectSelect2Option(GROUP_FILTER_ID, group);
        return this;
    }

    public String getSelectedGroup() {
        return getSelect2Selected(GROUP_FILTER_ID);
    }

    // ---------- Request Type (required) ----------

    // Choosing a type loads extra form sections; a loader covers the page until it's done
    public CreateRequestPage selectRequestType(String type) {
        selectSelect2Option(REQUEST_TYPE_ID, type);
        wait.until(ExpectedConditions.invisibilityOfElementLocated(loader));
        return this;
    }

    public String getSelectedRequestType() {
        return getSelect2Selected(REQUEST_TYPE_ID);
    }

    // ---------- Start Date (required) ----------

    public String getStartDateText() {
        return waitVisible(startDateVisible).getAttribute("value").trim();
    }

    // The input is read-only (calendar only), so we use flatpickr's own API, which
    // updates both inputs exactly like picking in the calendar does.
    public CreateRequestPage clearStartDate() {
        log.info("Clearing Start Date");
        js().executeScript("arguments[0]._flatpickr.clear();", driver.findElement(startDateHidden));
        return this;
    }

    public CreateRequestPage setStartDate(LocalDateTime dateTime) {
        log.info("Setting Start Date to {}", dateTime);
        long epochMillis = dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        js().executeScript("arguments[0]._flatpickr.setDate(new Date(arguments[1]), true);",
                driver.findElement(startDateHidden), epochMillis);
        return this;
    }

    // ---------- Select Whom (required) ----------

    public CreateRequestPage selectWhom(String whom) {
        log.info("Selecting whom: {}", whom);
        new Select(waitVisible(whomSelect)).selectByVisibleText(whom);
        return this;
    }

    public String getSelectedWhom() {
        return new Select(waitVisible(whomSelect)).getFirstSelectedOption().getText().trim();
    }

    // ---------- Client (after Select Whom = Client) ----------

    public boolean isClientFieldVisible() {
        return isVisible(By.id("select2-" + CLIENT_ID + "-container"));
    }

    public CreateRequestPage selectClient(String client) {
        selectSelect2Option(CLIENT_ID, client);
        return this;
    }

    public String getSelectedClient() {
        return getSelect2Selected(CLIENT_ID);
    }

    // ---------- Save + validation popup ----------

    public CreateRequestPage clickSave() {
        log.info("Clicking 'Save Request'");
        WebElement save = driver.findElement(saveRequestBtn);
        // 'instant': the ERP uses smooth scrolling, and a click during the scroll misses the button
        js().executeScript("arguments[0].scrollIntoView({block: 'center', behavior: 'instant'});", save);
        click(saveRequestBtn);
        return this;
    }

    public boolean isValidationPopupShown() {
        return isVisible(popupTitle);
    }

    public String getPopupTitle() {
        return textOf(popupTitle).trim();
    }

    public List<String> getPopupMessages() {
        waitVisible(popupTitle);
        return driver.findElements(popupMessages).stream()
                .map(li -> li.getText().trim())
                .toList();
    }

    public CreateRequestPage closePopup() {
        click(popupOkButton);
        wait.until(ExpectedConditions.invisibilityOfElementLocated(popupTitle));
        return this;
    }

    private JavascriptExecutor js() {
        return (JavascriptExecutor) driver;
    }
}
