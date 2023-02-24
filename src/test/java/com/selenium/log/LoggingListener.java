package com.selenium.log;

import com.selenium.driver.DriverManager;
import org.openqa.selenium.*;
import org.openqa.selenium.support.events.WebDriverListener;

public class LoggingListener implements WebDriverListener {

	@Override
	public void afterClick(WebElement element) {
		System.out.println("Clicked on Web Element");
		WebDriver driver = DriverManager.getWebDriver();
		waitForPageToLoad(driver);
	}

	@Override
	public void afterTo(WebDriver.Navigation navigation, String url) {
		WebDriver driver = DriverManager.getWebDriver();
		waitForPageToLoad(driver);
		System.out.println("Navigated to : " + url);
	}
	/**
	 * Method to wait until page is loaded completely
	 *
	 * @param driver: selenium web driver
	 */
	private void waitForPageToLoad(WebDriver driver) {
		String pageLoadStatus;
		JavascriptExecutor js;

		do {
			js = (JavascriptExecutor) driver;
			pageLoadStatus = (String) js.executeScript("return document.readyState");
			System.out.println(".");
		} while (!pageLoadStatus.equals("complete"));
		System.out.println("------- Page loaded successfully -------");
	}

}