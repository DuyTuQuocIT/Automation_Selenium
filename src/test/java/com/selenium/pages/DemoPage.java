package com.selenium.pages;

import com.selenium.core.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

import java.io.IOException;

public class DemoPage extends BasePage {

    public DemoPage(WebDriver driver) throws IOException {
        super(driver);
        PageFactory.initElements(driver, this);
    }

    @FindBy(xpath = "//input[@id='user-name']")
    private WebElement inputUserName;

    @FindBy(xpath = "//input[@id='password']")
    private WebElement inputPassword;

    @FindBy(xpath = "//input[@id='login-button']")
    private WebElement btnLogin;

    @FindBy(xpath = "//button[@id='react-burger-menu-btn']")
    private WebElement btnMenu;

    @FindBy(xpath = "//a[@class='shopping_cart_link']")
    private WebElement shoppingCart;

    @FindBy(xpath = "//button[@id='checkout']")
    private WebElement btnCheckOut;

    @FindBy(xpath = "//input[@id='first-name']")
    private WebElement inputFirstName;

    @FindBy(xpath = "//input[@id='last-name']")
    private WebElement inputLastName;

    @FindBy(xpath = "//input[@id='postal-code']")
    private WebElement inputPostalCode;

    public void enterUserName(String user) {
        inputUserName.clear();
        inputUserName.sendKeys(user);
    }

    public void enterPassword(String pass) {
        inputPassword.clear();
        inputPassword.sendKeys(pass);
    }

    public void clickBtnLogin() {
        btnLogin.click();
    }

    public boolean loginSuccess() {
        return btnMenu.isDisplayed();
    }

    public WebElement getBtnAddToCard(String productName) {
        return getTargetElement(By.xpath(String.format("//div[normalize-space()='%s']/parent::a/parent::div/following-sibling::div//button",productName)));
    }

    public void clickBtnShoppingCart() {
        shoppingCart.click();
    }

    public void clickBtnCheckOut() {
        btnCheckOut.click();
    }

    public void enterCheckoutInfo() {
        inputFirstName.sendKeys("Duy");
        inputLastName.sendKeys("Tu");
        inputPostalCode.sendKeys("72000");
    }

    public WebElement getBtnContinue() {
        return getTargetElement(By.xpath("//input[@value='Continue']"));
    }

    public WebElement getBtnFinish() {
        return getTargetElement(By.xpath("//button[@id='finish']"));
    }

    public boolean verifyTextPresent(String text) {
        WebElement ele = getTargetElement(By.xpath(String.format("//*[normalize-space()='%s']",text)));
        return ele.isDisplayed();
    }
}
