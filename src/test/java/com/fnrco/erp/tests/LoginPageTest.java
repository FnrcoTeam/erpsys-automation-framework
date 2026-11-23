package com.fnrco.erp.tests;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.fnrco.erp.base.BaseTest;
import com.fnrco.erp.base.TestGroups;
import com.fnrco.erp.config.ConfigReader;
import com.fnrco.erp.pages.DashboardPage;
import com.fnrco.erp.pages.LoginPage;

// Login feature + landing on the dashboard, all in ONE browser (see BaseTest).
// Order: logged-out (negative) tests first -> valid login -> dashboard checks.
public class LoginPageTest extends BaseTest {

    private static final String WRONG_PASSWORD = "definitely-wrong-password";

    // ---------- 1. Negative tests: independent, ordered by priority only ----------

    @Test(priority = 1, groups = {TestGroups.REGRESSION, TestGroups.AUTH},
          description = "Empty email shows 'The email field is required.'")
    public void emptyEmailShowsRequiredError() {
        LoginPage login = new LoginPage().open();
        login.loginAs("", ConfigReader.getPassword());
        Assert.assertEquals(login.getEmailErrorText(), "The email field is required.");
    }

    @Test(priority = 2, groups = {TestGroups.REGRESSION, TestGroups.AUTH},
          description = "Wrong password shows 'invalid email or password'")
    public void wrongPasswordShowsError() {
        LoginPage login = new LoginPage().open();
        login.loginAs(ConfigReader.getUsername(), WRONG_PASSWORD);
        Assert.assertEquals(login.getEmailErrorText(), "invalid email or password");
    }

    // ---------- 2. Valid login: the prerequisite for the dashboard tests ----------

    @Test(priority = 3, groups = {TestGroups.SMOKE, TestGroups.REGRESSION, TestGroups.AUTH},
          description = "Valid user logs in and lands on the dashboard")
    public void validUserCanLogIn() {
        new LoginPage().open().loginAs(ConfigReader.getUsername(), ConfigReader.getPassword());
        Assert.assertTrue(new DashboardPage().isLoaded(), "Dashboard did not load after login");
    }

    // ---------- 3. Dashboard: real dependency on the login (skipped if it failed) ----------

    @Test(priority = 4, dependsOnMethods = "validUserCanLogIn",
          groups = {TestGroups.SMOKE, TestGroups.REGRESSION, TestGroups.AUTH},
          description = "Dashboard greets the logged-in user")
    public void dashboardShowsWelcomeMessage() {
        String welcome = new DashboardPage().getWelcomeText();
        Assert.assertTrue(welcome.startsWith("Welcome back"),
                "Unexpected welcome text: " + welcome);
    }
}
