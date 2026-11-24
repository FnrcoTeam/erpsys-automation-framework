package com.fnrco.erp.tests.requests;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.fnrco.erp.base.BaseTest;
import com.fnrco.erp.base.TestGroups;
import com.fnrco.erp.pages.requests.CreateRequestPage;
import com.fnrco.erp.pages.requests.RequestsPage;

// Requests module: one browser, logged in once, then all request scenarios.
public class CreateRequestTest extends BaseTest {

    // Test data seen on staging (names exactly as shown in the dropdowns)
    private static final String REQUEST_TYPE = "Add Joining Date";
    private static final String GROUP = "HR Request";
    private static final String CLIENT = "6CATS International";

    private static final String WHOM_DETAILS_REQUIRED =
            "Please specify who the request is for (Client, Employee, Candidate, or Department).";

    // Start Date is shown as "24-11-2026 15:53", in Saudi time
    private static final DateTimeFormatter START_DATE_FORMAT = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
    private static final ZoneId SAUDI_TIME = ZoneId.of("Asia/Riyadh");

    // Runs after BaseTest.setUp() (parent @BeforeClass first), so the browser is ready
    @BeforeClass(alwaysRun = true)
    public void login() {
        loginAsDefaultUser();
    }

    @Test(priority = 1, groups = {TestGroups.SMOKE, TestGroups.REGRESSION, TestGroups.REQUESTS},
          description = "Requests page opens with the title 'Requests'")
    public void requestsPageShowsTitle() {
        RequestsPage requests = new RequestsPage().open();
        Assert.assertEquals(requests.getPageTitle(), "Requests");
    }

    @Test(priority = 2, groups = {TestGroups.SMOKE, TestGroups.REGRESSION, TestGroups.REQUESTS},
          description = "'Add new' opens the Create Request page")
    public void addNewOpensCreateRequestPage() {
        CreateRequestPage create = new RequestsPage().open().clickAddNew();
        Assert.assertTrue(create.getCurrentUrl().contains("/general/requests/create"),
                "Unexpected URL: " + create.getCurrentUrl());
        Assert.assertEquals(create.getPageTitle(), "Create Request");
    }

    @Test(priority = 3, groups = {TestGroups.SMOKE, TestGroups.REGRESSION, TestGroups.REQUESTS},
          description = "Create Request page opens with its title")
    public void createRequestPageShowsTitle() {
        CreateRequestPage create = new CreateRequestPage().open();
        Assert.assertEquals(create.getPageTitle(), "Create Request");
    }

    // ---------- Required fields: Request Type, Start Date ----------
    // None of these tests fill "Select Whom", so the form can never be saved: no data is created.

    @Test(priority = 4, groups = {TestGroups.REGRESSION, TestGroups.REQUESTS},
          description = "Start Date defaults to the current Saudi time")
    public void startDateDefaultsToCurrentSaudiTime() {
        CreateRequestPage create = new CreateRequestPage().open();
        LocalDateTime shown = LocalDateTime.parse(create.getStartDateText(), START_DATE_FORMAT);
        LocalDateTime saudiNow = LocalDateTime.now(SAUDI_TIME);
        long minutesApart = Math.abs(Duration.between(saudiNow, shown).toMinutes());
        Assert.assertTrue(minutesApart <= 5,
                "Start Date " + shown + " is not the current Saudi time " + saudiNow);
    }

    @Test(priority = 5, groups = {TestGroups.REGRESSION, TestGroups.REQUESTS},
          description = "Empty Start Date shows 'The start date is required.'")
    public void saveWithoutStartDateShowsRequiredError() {
        CreateRequestPage create = new CreateRequestPage().open()
                .selectRequestType(REQUEST_TYPE)
                .clearStartDate()
                .clickSave();
        Assert.assertEquals(create.getPopupTitle(), "Validation Error");
        Assert.assertTrue(create.getPopupMessages().contains("The start date is required."),
                "Messages were: " + create.getPopupMessages());
        create.closePopup();
    }

    @Test(priority = 6, groups = {TestGroups.REGRESSION, TestGroups.REQUESTS},
          description = "Start Date does not accept a past day (earlier times today are allowed)")
    public void startDateRejectsPastDate() {
        CreateRequestPage create = new CreateRequestPage().open()
                .setStartDate(LocalDateTime.now().minusDays(1));
        Assert.assertEquals(create.getStartDateText(), "",
                "A past Start Date was accepted");
    }

    // ---------- Select Whom (required) and Client ----------

    @Test(priority = 8, groups = {TestGroups.REGRESSION, TestGroups.REQUESTS},
          description = "Empty Select Whom shows the 'for whom' required errors")
    public void saveWithoutWhomShowsRequiredError() {
        CreateRequestPage create = new CreateRequestPage().open()
                .selectRequestType(REQUEST_TYPE)
                .clickSave();
        Assert.assertEquals(create.getPopupTitle(), "Validation Error");
        List<String> messages = create.getPopupMessages();
        Assert.assertTrue(messages.contains("The for whom is required."), "Messages were: " + messages);
        Assert.assertTrue(messages.contains(WHOM_DETAILS_REQUIRED), "Messages were: " + messages);
        create.closePopup();
    }

    @Test(priority = 11, groups = {TestGroups.REGRESSION, TestGroups.REQUESTS},
          description = "Select Whom = Client without a client shows only the 'specify who' error")
    public void saveWithoutClientShowsSpecifyWhoError() {
        CreateRequestPage create = new CreateRequestPage().open()
                .selectRequestType(REQUEST_TYPE)
                .selectWhom("Client")
                .clickSave();
        Assert.assertEquals(create.getPopupTitle(), "Validation Error");
        Assert.assertEquals(create.getPopupMessages(), List.of(WHOM_DETAILS_REQUIRED));
        create.closePopup();
    }

    @Test(priority = 9, groups = {TestGroups.REGRESSION, TestGroups.REQUESTS},
          description = "Choosing 'Client' in Select Whom shows the Client field")
    public void choosingClientShowsClientField() {
        CreateRequestPage create = new CreateRequestPage().open().selectWhom("Client");
        Assert.assertEquals(create.getSelectedWhom(), "Client");
        Assert.assertTrue(create.isClientFieldVisible(), "Client field did not appear");
    }

    @Test(priority = 10, groups = {TestGroups.REGRESSION, TestGroups.REQUESTS},
          description = "A client can be searched and selected")
    public void clientCanBeSelected() {
        CreateRequestPage create = new CreateRequestPage().open()
                .selectWhom("Client")
                .selectClient(CLIENT);
        Assert.assertEquals(create.getSelectedClient(), CLIENT);
    }

    // ---------- Optional field: Filter by Group ----------

    @Test(priority = 7, groups = {TestGroups.REGRESSION, TestGroups.REQUESTS},
          description = "Filter by Group can be selected")
    public void groupCanBeSelected() {
        CreateRequestPage create = new CreateRequestPage().open().selectGroup(GROUP);
        Assert.assertEquals(create.getSelectedGroup(), GROUP);
    }
}
