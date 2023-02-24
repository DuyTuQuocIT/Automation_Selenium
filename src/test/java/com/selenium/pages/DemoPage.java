package com.selenium.pages;

import com.selenium.core.BasePage;
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

    public void loginToDemoPage(String user, String pass) {
        // Enter username
        this.inputUserName.clear();
        this.inputUserName.sendKeys(user);

        // Enter password
        this.inputPassword.clear();
        this.inputPassword.sendKeys(pass);

        // Press button login
        btnLogin.click();
    }

}
