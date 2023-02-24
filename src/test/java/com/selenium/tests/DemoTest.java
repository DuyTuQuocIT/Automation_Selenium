package com.selenium.tests;

import com.selenium.core.BaseTest;
import com.selenium.pages.DemoPage;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;

import java.io.IOException;

public class DemoTest extends BaseTest {
    DemoPage demoPage;
    public DemoTest() throws IOException {
        demoPage = new DemoPage(driver);
    }

    @Given("Login demo page")
    public void loginDemoPage() {
        driver.get(systemInfo.url);
    }

    @And("Enter username and password")
    public void enterUsernameAndPassword() {
        demoPage.loginToDemoPage(systemInfo.user,systemInfo.pass);
    }
}
