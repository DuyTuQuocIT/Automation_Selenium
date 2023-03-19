package com.selenium.tests;

import com.selenium.core.BaseTest;
import com.selenium.pages.DemoPage;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import org.junit.Assert;

import java.io.IOException;

public class DemoTest extends BaseTest {
    DemoPage demoPage;
    public DemoTest() throws IOException {
        demoPage = new DemoPage(driver);
    }

    @Given("I navigate to url {string}")
    public void iNavigateToUrl(String url) {
        driver.navigate().to(url);
    }

    @And("I enter user name {string}")
    public void iEnterUserName(String userName) {
        demoPage.enterUserName(userName);
    }

    @And("I enter password {string}")
    public void iEnterPassword(String pass) {
        demoPage.enterPassword(pass);
    }

    @And("I click button Login")
    public void clickBtnLogin() {
        demoPage.clickBtnLogin();
    }

    @Then("I verify login success")
    public void iVerifyLoginSuccess() {
        Assert.assertTrue(demoPage.loginSuccess());
    }

    @And("I add product {string} to shopping cart")
    public void iAddProductToShoppingCart(String productName) {
        clickElement(demoPage.getBtnAddToCard(productName), true);
    }

    @And("I go to shopping cart")
    public void iGoToShoppingCart() {
        demoPage.clickBtnShoppingCart();
    }

    @And("I check out added items")
    public void iCheckOutAddedItems() {
        demoPage.clickBtnCheckOut();
    }

    @And("I enter my information")
    public void iEnterMyInformation() {
        demoPage.enterCheckoutInfo();
    }

    @And("I click button Continue")
    public void iClickButton() {
        clickElement(demoPage.getBtnContinue(), true);
    }

    @And("I click button Finish")
    public void iClickButtonFinish() {
        clickElement(demoPage.getBtnFinish(),true);
    }

    @Then("I verify message {string} is displayed")
    public void verifyTextPresent(String text) {
        Assert.assertTrue(demoPage.verifyTextPresent(text));
    }
}
