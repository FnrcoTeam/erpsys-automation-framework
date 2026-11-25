package com.fnrco.erp.pages.requests;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.fnrco.erp.pages.BasePage;

// My Related Requests: Main menu > Requests > My Related Requests
// The table (#request-table) lists the newest request first, and each row's id is
// its request number: <tr id="29612">. So a row is found directly by its number.
public class MyRelatedRequestsPage extends BasePage {

    private static final String PATH = "/en/general/requests/my-related-requests";

    private final By table   = By.id("request-table");
    private final By headers = By.cssSelector("#request-table thead th");

    public MyRelatedRequestsPage open() {
        openPath(PATH);
        waitVisible(table);
        return this;
    }

    public boolean hasRequest(String requestNumber) {
        return isVisible(row(requestNumber));
    }

    // Reads a cell by its column header ("Request Type", "Client"...), not by position:
    // columns can be hidden or moved with "Columns & Settings".
    public String getCell(String requestNumber, String columnTitle) {
        List<WebElement> ths = driver.findElements(headers);
        int index = -1;
        for (int i = 0; i < ths.size(); i++) {
            if (columnTitle.equals(ths.get(i).getAttribute("title"))) {
                index = i;
                break;
            }
        }
        if (index < 0) {
            throw new IllegalArgumentException("No column titled '" + columnTitle + "'");
        }
        List<WebElement> cells = waitVisible(row(requestNumber)).findElements(By.tagName("td"));
        return cells.get(index).getText().trim();
    }

    private By row(String requestNumber) {
        return By.cssSelector("#request-table tbody tr[id='" + requestNumber + "']");
    }
}
