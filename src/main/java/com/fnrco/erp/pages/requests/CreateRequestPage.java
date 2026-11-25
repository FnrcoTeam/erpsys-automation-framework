package com.fnrco.erp.pages.requests;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;

import com.fnrco.erp.pages.BasePage;

// Create Request form (/en/general/requests/create).
// Covered so far: Filter by Group, Request Type, Start Date, Select Whom, Client, Employee,
// Description, Attachments, Save Request, validation popup, success message.
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

    // Employee (shown after Select Whom = Employee)
    private final By employeeFileNo    = By.id("lr_id_residency_portal");
    private final By employeeName      = By.id("lr_emp_name");   // read-only, filled by Search
    private final By employeeSearchBtn = By.id("search-btn");

    // Description & Attachments
    private final By descriptionEditor =
            By.cssSelector("#suneditor_smart-description .se-wrapper-wysiwyg");
    private final By attachmentsInput = By.id("attachments");
    private final By attachmentNames  = By.cssSelector("#attachmentsPreview p[title]");

    // id only: "@type='submit'" could also match other submit buttons on the page
    private final By saveRequestBtn = By.id("saveRequestBtn");
    private final By loader = By.id("loader2");

    // ---- Validation popup (SweetAlert2) ----
    private final By popupTitle    = By.id("swal2-title");
    private final By popupMessages = By.cssSelector("#swal2-html-container li");
    private final By popupOkButton = By.cssSelector("button.swal2-confirm");
    private final By popupBody     = By.id("swal2-html-container");
    private final By popupContainer = By.cssSelector(".swal2-container");

    // Green success message (toastr), it fades out after a few seconds:
    // <div class="toast toast-success"><div class="toast-message">Request submitted ...</div></div>
    private final By successMessage = By.cssSelector(".toast-success .toast-message");
    private static final Pattern REQUEST_NUMBER = Pattern.compile("Request Number is (\\d+)");

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

    // ---------- Employee (after Select Whom = Employee) ----------
    // Type the File No. and click Search: the read-only Employee Name fills in by itself.
    // A wrong File No. shows the popup "Not Found" / "Employee not found or server error".

    public CreateRequestPage searchEmployee(String fileNo) {
        log.info("Searching employee by File No. {}", fileNo);
        typeText(employeeFileNo, fileNo);
        click(employeeSearchBtn);
        return this;
    }

    // A successful search shows the popup "Success / Employee data loaded successfully!",
    // which closes by itself, and fills in the name. Waits for both, so the next click
    // (e.g. Save) is not blocked by the popup.
    public String getEmployeeName() {
        wait.until(d -> !d.findElement(employeeName).getAttribute("value").isBlank());
        waitForPopupToClose();
        return driver.findElement(employeeName).getAttribute("value").trim();
    }

    public CreateRequestPage waitForPopupToClose() {
        wait.until(ExpectedConditions.invisibilityOfElementLocated(popupContainer));
        return this;
    }

    // ---------- Description (optional) ----------
    // SunEditor: a contenteditable <div> you type in; it copies the HTML into the hidden
    // <textarea id="smart-description" name="description"> that is sent with the form.

    public CreateRequestPage typeDescription(String text) {
        log.info("Typing description: {}", text);
        click(descriptionEditor);
        driver.findElement(descriptionEditor).sendKeys(text);
        return this;
    }

    public String getDescriptionText() {
        return textOf(descriptionEditor).trim();
    }

    // ---------- Attachments (optional) ----------
    // <input type="file" id="attachments" multiple>. Selenium "types" the file path into it,
    // which is what choosing it in the file dialog does. Each accepted file gets a preview
    // card in #attachmentsPreview with its name and a × button. A wrong type or a file over
    // 20MB is refused with a popup ("Invalid File Type" / "File Too Large").

    public CreateRequestPage attachFiles(Path... files) {
        String paths = Arrays.stream(files)
                .map(f -> f.toAbsolutePath().toString())
                .collect(Collectors.joining("\n"));   // several files: one path per line
        log.info("Attaching {}", paths.replace('\n', ','));
        driver.findElement(attachmentsInput).sendKeys(paths);
        return this;
    }

    // File names shown in the preview cards, e.g. ["sample.pdf", "sample.txt"]
    public List<String> getAttachedFileNames() {
        return driver.findElements(attachmentNames).stream()
                .map(p -> p.getAttribute("title").replaceAll(" \\(.*\\)$", "").trim())
                .toList();
    }

    public CreateRequestPage removeAttachment(String fileName) {
        log.info("Removing attachment {}", fileName);
        click(By.xpath("//div[@id='attachmentsPreview']/div[.//p[starts-with(@title,'"
                + fileName + " (')]]/button"));
        return this;
    }

    // ---------- Save + validation popup ----------

    public CreateRequestPage clickSave() {
        log.info("Clicking 'Save Request'");
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

    // Popup body when it is plain text, not a list (e.g. "Employee not found or server error")
    public String getPopupText() {
        return textOf(popupBody).trim();
    }

    // ---------- Success message (green, disappears after a few seconds) ----------
    // "Request submitted successfully. Your Request Number is 29611"
    // Waits for it right after Save, and returns the request number it shows.
    public String waitForSuccessAndGetRequestNumber() {
        String text = textOf(successMessage).trim();
        log.info("Success message: {}", text);
        Matcher m = REQUEST_NUMBER.matcher(text);
        if (!m.find()) {
            throw new IllegalStateException("No request number in success message: " + text);
        }
        return m.group(1);
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
