package com.selenium.core;

import java.time.Duration;
import com.selenium.driver.DriverManager;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class BasePage extends Assertion {
	public final int timeOut = DriverManager.implicitWaitTimeInSeconds;
	protected WebDriver driver;

	public BasePage(WebDriver driver) {
		super(driver);
		this.driver = driver;
	}
	/**
	 * Method to open specified url
	 * 
	 * @param url to open
	 */
	public void get(String url) {
		driver.get(url);
	}

	/**
	 * Method to navigate to specified page
	 * 
	 * @param url navigation url
	 */
	public void navigate(String url) {
		driver.navigate().to(url);
	}

	/**
	 * Method to click on an element with action class
	 * 
	 * @param element to be clicked
	 */
	public void clickOnElementUsingActions(By element) {
		Actions actions = new Actions(driver);
		actions.moveToElement(getTargetElement(element));
		actions.click().perform();
	}

	/**
	 * Method to click on an element using javascript
	 * 
	 * @param element to be clicked
	 */
	public void clickOnElementUsingJs(By element) {
		JavascriptExecutor js = (JavascriptExecutor) driver;
		WebElement webElement = driver.findElement(element);
		js.executeScript("arguments[0].click();", webElement);
	}

	/**
	 * Method to get title of current webpage
	 * 
	 * @return String name of a webpage
	 */
	public String getTitle() {
		return driver.getTitle();
	}

	/**
	 * Method to wait until page is loaded completely
	 * 
	 * @param PageName String name of current webpage
	 */
	public void waitForPageToLoad(String PageName) {
		String pageLoadStatus;
		JavascriptExecutor js;

		do {
			js = (JavascriptExecutor) driver;
			pageLoadStatus = (String) js.executeScript("return document.readyState");
		} while (!pageLoadStatus.equals("complete"));
		System.out.println(PageName + " page loaded successfully");
	}

	/**
	 * Method verify whether element is present on screen
	 * 
	 * @param targetElement element to be present
	 * @return true if element is present else throws exception
	 */

	public Boolean isElementPresent(By targetElement) {
		return driver.findElements(targetElement).size() > 0;
	}

	/**
	 * Method verify whether element is not present on screen
	 * @param targetElement element not to be present
	 */
	public Boolean isElementNotPresent(By targetElement) {
		return driver.findElements(targetElement).size() == 0;
	}

	/**
	 * Method to wait for an element to be visible
	 * 
	 * @param targetElement element to be visible
	 * @return true if element is visible else throws TimeoutException
	 */
	public boolean waitForVisibility(By targetElement) {
		try {
			WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeOut));
			wait.until(ExpectedConditions.visibilityOfElementLocated(targetElement));
			return true;
		} catch (TimeoutException e) {
			System.out.println("Element is not visible: " + targetElement);
			System.out.println();
			System.out.println(e.getMessage());
			throw new TimeoutException();
		}
	}

	/**
	 * Method to wait for an element to be clickable
	 * 
	 * @param targetElement element to be clickable
	 * @return true if element is clickable else throws TimeoutException
	 */
	public boolean waitForElementToBeClickable(By targetElement) {
		try {
			WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeOut));
			wait.until(ExpectedConditions.elementToBeClickable(targetElement));
			return true;
		} catch (TimeoutException e) {
			e.printStackTrace();
			return false;
		}
	}

	public WebElement getTargetElement(By by) {
		while (true) {
			try {
				return driver.findElement(by);
			} catch (StaleElementReferenceException ex) {
				// Catch stale exception, loop back to get element again
				System.out.println("Stale Element exception occurs");
			} catch (NoSuchElementException e) {
				e.printStackTrace();
				System.out.println("No such element exception occurred!");
				break;
			} catch (Exception ex) {
				ex.printStackTrace();
				System.out.println("Unexpected exception occurred!");
				break;
			}
		}
		return null;
	}
}
