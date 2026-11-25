package com.fnrco.erp.tests.requests;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
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
import com.fnrco.erp.pages.components.SideMenu;
import com.fnrco.erp.pages.requests.CreateRequestPage;
import com.fnrco.erp.pages.requests.MyRelatedRequestsPage;
import com.fnrco.erp.pages.requests.RequestsPage;

// Requests module: one browser, logged in once, then all request scenarios.
public class CreateRequestTest extends BaseTest {

    // example test data from the dropdowns
    private static final String REQUEST_TYPE = "Add Joining Date";
    private static final String GROUP = "HR Request";
    private static final String CLIENT = "6CATS International";
    private static final String EMPLOYEE_FILE_NO = "2101";
    private static final String WRONG_FILE_NO = "20741677";

    // Filled by the create tests, read by the "appears in My Related Requests" tests
    private String clientRequestNumber;
    private String employeeRequestNumber;

    private static final String WHOM_DETAILS_REQUIRED =
            "Please specify who the request is for (Client, Employee, Candidate, or Department).";

    // Start Date is shown as "24-11-2026 15:53", in Saudi time
    private static final DateTimeFormatter START_DATE_FORMAT = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
    private static final ZoneId SAUDI_TIME = ZoneId.of("Asia/Riyadh");

    // Files in src/test/resources/testdata (copied to target/test-classes by Maven)
    private Path testFile(String name) {
        try {
            return Path.of(getClass().getResource("/testdata/" + name).toURI());
        } catch (URISyntaxException | NullPointerException e) {
            throw new IllegalStateException("Test file not found: testdata/" + name, e);
        }
    }

    // Written in Description of every request the tests create, so testers can spot them
    private static String automationMarker() {
        return "AUTOMATION TEST - " + LocalDateTime.now(SAUDI_TIME).format(START_DATE_FORMAT);
    }

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

    // ---------- Employee ----------

    @Test(priority = 12, groups = {TestGroups.REGRESSION, TestGroups.REQUESTS},
          description = "A wrong File No. shows 'Not Found'")
    public void wrongEmployeeFileNoShowsNotFound() {
        CreateRequestPage create = new CreateRequestPage().open()
                .selectWhom("Employee")
                .searchEmployee(WRONG_FILE_NO);
        Assert.assertEquals(create.getPopupTitle(), "Not Found");
        Assert.assertEquals(create.getPopupText(), "Employee not found or server error");
        create.closePopup();
    }

    @Test(priority = 13, groups = {TestGroups.REGRESSION, TestGroups.REQUESTS},
          description = "A correct File No. fills in the Employee Name")
    public void employeeSearchFillsName() {
        CreateRequestPage create = new CreateRequestPage().open()
                .selectWhom("Employee")
                .searchEmployee(EMPLOYEE_FILE_NO);
        Assert.assertEquals(create.getPopupTitle(), "Success");
        Assert.assertEquals(create.getPopupText(), "Employee data loaded successfully!");
        Assert.assertFalse(create.getEmployeeName().isEmpty(), "Employee Name stayed empty");
    }

    @Test(priority = 14, groups = {TestGroups.REGRESSION, TestGroups.REQUESTS},
          description = "Select Whom = Employee without searching shows only the 'specify who' error")
    public void saveWithoutEmployeeShowsSpecifyWhoError() {
        CreateRequestPage create = new CreateRequestPage().open()
                .selectRequestType(REQUEST_TYPE)
                .selectWhom("Employee")
                .clickSave();
        Assert.assertEquals(create.getPopupTitle(), "Validation Error");
        Assert.assertEquals(create.getPopupMessages(), List.of(WHOM_DETAILS_REQUIRED));
        create.closePopup();
    }

    // ---------- Description & Attachments (optional; nothing is saved here) ----------

    @Test(priority = 20, groups = {TestGroups.REGRESSION, TestGroups.REQUESTS},
          description = "Text can be typed in Description")
    public void descriptionCanBeTyped() {
        CreateRequestPage create = new CreateRequestPage().open().typeDescription("Description check");
        Assert.assertEquals(create.getDescriptionText(), "Description check");
    }

    @Test(priority = 21, groups = {TestGroups.REGRESSION, TestGroups.REQUESTS},
          description = "Several allowed files can be attached and are previewed")
    public void allowedFilesAreAttached() {
        CreateRequestPage create = new CreateRequestPage().open()
                .attachFiles(testFile("sample.pdf"), testFile("sample.txt"));
        Assert.assertEquals(create.getAttachedFileNames(), List.of("sample.pdf", "sample.txt"));
    }

    @Test(priority = 22, groups = {TestGroups.REGRESSION, TestGroups.REQUESTS},
          description = "An attached file can be removed with ×")
    public void attachmentCanBeRemoved() {
        CreateRequestPage create = new CreateRequestPage().open()
                .attachFiles(testFile("sample.pdf"), testFile("sample.txt"))
                .removeAttachment("sample.pdf");
        Assert.assertEquals(create.getAttachedFileNames(), List.of("sample.txt"));
    }

    @Test(priority = 23, groups = {TestGroups.REGRESSION, TestGroups.REQUESTS},
          description = "A file type that is not allowed is refused")
    public void notAllowedFileTypeIsRejected() {
        CreateRequestPage create = new CreateRequestPage().open().attachFiles(testFile("not-allowed.exe"));
        Assert.assertEquals(create.getPopupTitle(), "Invalid File Type");
        Assert.assertTrue(create.getPopupText().startsWith("File \"not-allowed.exe\" is not supported."),
                "Message was: " + create.getPopupText());
        create.closePopup();
        Assert.assertTrue(create.getAttachedFileNames().isEmpty(), "The refused file was still attached");
    }

    @Test(priority = 24, groups = {TestGroups.REGRESSION, TestGroups.REQUESTS},
          description = "A file over 20MB is refused")
    public void fileOver20MbIsRejected() throws IOException {
        // Created on the fly (21MB of zeros) so the repo doesn't store a big file
        Path big = Files.createTempFile("too-large-", ".pdf");
        try (RandomAccessFile f = new RandomAccessFile(big.toFile(), "rw")) {
            f.setLength(21L * 1024 * 1024);
        }
        try {
            CreateRequestPage create = new CreateRequestPage().open().attachFiles(big);
            Assert.assertEquals(create.getPopupTitle(), "File Too Large");
            Assert.assertTrue(create.getPopupText().endsWith("Maximum size per file is 20MB."),
                    "Message was: " + create.getPopupText());
            create.closePopup();
            Assert.assertTrue(create.getAttachedFileNames().isEmpty(), "The refused file was still attached");
        } finally {
            Files.deleteIfExists(big);
        }
    }

    // ---------- Happy path: these CREATE real requests on staging ----------
    // Each create test saves the request number from the green success message; the
    // follow-up test (dependsOnMethods) finds that exact row in My Related Requests.

    @Test(priority = 15, groups = {TestGroups.REGRESSION, TestGroups.REQUESTS},
          description = "Sidebar: Requests > Create Request opens the Create Request page")
    public void sidebarOpensCreateRequestPage() {
        CreateRequestPage create = new SideMenu().openCreateRequest();
        Assert.assertEquals(create.getPageTitle(), "Create Request");
    }

    @Test(priority = 16, groups = {TestGroups.REGRESSION, TestGroups.REQUESTS},
          description = "Create a request for a Client (opened from 'Add new')")
    public void createRequestForClient() {
        CreateRequestPage create = new RequestsPage().open().clickAddNew()
                .selectRequestType(REQUEST_TYPE)
                .selectWhom("Client")
                .selectClient(CLIENT)
                .typeDescription(automationMarker())
                .attachFiles(testFile("sample.pdf"))
                .clickSave();
        clientRequestNumber = create.waitForSuccessAndGetRequestNumber();
        log.info("Created client request {}", clientRequestNumber);
    }

    @Test(priority = 17, dependsOnMethods = "createRequestForClient",
          groups = {TestGroups.REGRESSION, TestGroups.REQUESTS},
          description = "The client request appears in My Related Requests")
    public void clientRequestAppearsInMyRelatedRequests() {
        MyRelatedRequestsPage list = new MyRelatedRequestsPage().open();
        Assert.assertTrue(list.hasRequest(clientRequestNumber),
                "Request " + clientRequestNumber + " not found in My Related Requests");
        Assert.assertEquals(list.getCell(clientRequestNumber, "Request Type"), REQUEST_TYPE);
        Assert.assertEquals(list.getCell(clientRequestNumber, "Beneficiary Type"), "Client");
        Assert.assertEquals(list.getCell(clientRequestNumber, "Client"), CLIENT);
    }

    @Test(priority = 18, groups = {TestGroups.REGRESSION, TestGroups.REQUESTS},
          description = "Create a request for an Employee (opened from the sidebar)")
    public void createRequestForEmployee() {
        CreateRequestPage create = new SideMenu().openCreateRequest()
                .selectRequestType(REQUEST_TYPE)
                .selectWhom("Employee")
                .searchEmployee(EMPLOYEE_FILE_NO);
        create.getEmployeeName();   // waits until the search filled the employee in
        create.typeDescription(automationMarker()).clickSave();
        employeeRequestNumber = create.waitForSuccessAndGetRequestNumber();
        log.info("Created employee request {}", employeeRequestNumber);
    }

    @Test(priority = 19, dependsOnMethods = "createRequestForEmployee",
          groups = {TestGroups.REGRESSION, TestGroups.REQUESTS},
          description = "The employee request appears in My Related Requests")
    public void employeeRequestAppearsInMyRelatedRequests() {
        MyRelatedRequestsPage list = new MyRelatedRequestsPage().open();
        Assert.assertTrue(list.hasRequest(employeeRequestNumber),
                "Request " + employeeRequestNumber + " not found in My Related Requests");
        Assert.assertEquals(list.getCell(employeeRequestNumber, "Request Type"), REQUEST_TYPE);
        Assert.assertEquals(list.getCell(employeeRequestNumber, "Beneficiary Type"), "Employee");
        Assert.assertEquals(list.getCell(employeeRequestNumber, "File No"), EMPLOYEE_FILE_NO);
    }
}
