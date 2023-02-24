package com.selenium.core;

import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.*;
import java.util.*;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.selenium.driver.DriverManager;
import com.selenium.objects.*;
import io.cucumber.java.PendingException;
import io.restassured.RestAssured;
import io.restassured.http.Header;
import org.apache.commons.codec.binary.Base64;
import org.jboss.aerogear.security.otp.Totp;
import org.json.simple.JSONObject;
import org.openqa.selenium.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.testcontainers.shaded.org.apache.commons.io.FileUtils;
import org.testng.Assert;
import io.restassured.http.Headers;
import io.restassured.specification.RequestSpecification;
import io.restassured.http.Method;
import io.restassured.response.Response;

import javax.imageio.ImageIO;

public class BaseTest {
	protected WebDriver driver;
	protected SystemProperties systemInfo;
	BasePage basePage;

	public BaseTest() {
		driver = DriverManager.getWebDriver();
		systemInfo = DriverManager.getSystemInfo();
		basePage = new BasePage(driver);
	}

	public void setImplicitTime(int seconds) {
		DriverManager.currentImplicitWaitTime = seconds;
		driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(seconds));
	}

	public void setDefaultImplicitTime() {
		DriverManager.currentImplicitWaitTime = DriverManager.implicitWaitTimeInSeconds;
		driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(DriverManager.implicitWaitTimeInSeconds));
	}

	public int getImplicitWaitTimeInSeconds() {
		return DriverManager.currentImplicitWaitTime;
	}

	public boolean clickElement(WebElement element, boolean stopOnFail) {
		boolean clickResult = true;
		boolean readyToClick = false;
		int maxTry = 4;
		try {
			try {
				for(int i = 1; i <= maxTry; i++) {
					// Loop with max try times to wait element ready to click
					String convertedXpath = convertElementToXpath(element);

					if(convertedXpath.isEmpty()) {
						clickResult = false;
						writeLogToReport("MYT Log Base Test - Element is NULL");
						break;
					}
					readyToClick = basePage.waitForElementToBeClickable(By.xpath(convertedXpath));

					if(readyToClick) {
						System.out.println("MYT Log Base Test - Element is ready to be clicked");
						break;
					} else {
						writeLogToReport("MYT Log Base Test - " + i + ". Loop again to wait for element ready to click");
					}
				}

				if(readyToClick) {
					clickResult = moveAndClickElement(element);
				} else {
					writeLogToReport("MYT Log Base Test - Element is not clickable");
					clickResult = false;
				}
			} catch (StaleElementReferenceException ex) {
				ex.printStackTrace();
				writeLogToReport("MYT Log Base Test - Stale exception occurs, trying to click element....");
				By xpath = By.xpath(convertElementToXpath(element));
				clickElement(basePage.getTargetElement(xpath),true);
			}
		}
		catch (Exception ex) {
			ex.printStackTrace();
			writeLogToReport("MYT Log Base Test - Unexpected issue occurred when clicking element....");
			clickResult = false;
		}

		if(stopOnFail && !clickResult) {
			Assert.fail("MYT Log Base Test - Failed to click element!!");
		}

		return clickResult;
	}

	protected boolean moveAndClickElement(WebElement element) {
		try {
			Actions action = new Actions(driver);
			String convertedXpath = convertElementToXpath(element);
			action.moveToElement(basePage.getTargetElement(By.xpath(convertedXpath))).perform();
			element.click();
			return true;
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}
	}

	public String generateRandomStr() {
		return new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss").format(new Date());
	}

	public boolean compareFloat(float expected, float actual) {
		return (Math.abs(expected - actual) < 0.001);
	}

	public Response get(String baseUrl, String apiUrl, Headers headers) {
		RestAssured.baseURI = baseUrl;
		RequestSpecification httpRequest = RestAssured.given();

		Header h1 = new Header("Content-Type","application/json");
		httpRequest.header(h1);

		httpRequest.headers(headers);

		return httpRequest.request(Method.GET,apiUrl);
	}

	public Response put(String baseUrl, String apiUrl, Headers headers, JSONObject body) {
		RestAssured.baseURI = baseUrl;
		RequestSpecification httpRequest = RestAssured.given();

		Header h1 = new Header("Content-Type","application/json");
		httpRequest.header(h1);

		httpRequest.headers(headers);

		httpRequest.body(body.toJSONString());

		return httpRequest.request(Method.PUT,apiUrl);
	}

	protected Response putRequestRawBody(String baseUrl, String apiUrl, String rawData) {
		RestAssured.baseURI = baseUrl;
		RequestSpecification httpRequest = RestAssured.given();

		httpRequest.headers(buildHeaderWithLoginCookie());

		httpRequest.body(rawData);

		return httpRequest.request(Method.PUT, apiUrl);
	}

	public Response post(String baseUrl, String apiUrl, Headers headers, JSONObject body) {
		RestAssured.baseURI = baseUrl;
		RequestSpecification httpRequest = RestAssured.given();

		Header h1 = new Header("Content-Type","application/json");
		httpRequest.header(h1);

		httpRequest.headers(headers);

		httpRequest.body(body.toJSONString());

		return httpRequest.request(Method.POST, apiUrl);
	}

	protected Response postRequestRawBody(String baseUrl, String apiUrl, String rawData) {
		RestAssured.baseURI = baseUrl;
		RequestSpecification httpRequest = RestAssured.given();

		httpRequest.headers(buildHeaderWithLoginCookie());

		httpRequest.body(rawData);

		return httpRequest.request(Method.POST, apiUrl);
	}

	public Response delete(String baseUrl, String apiUrl, Headers headers) {
		RestAssured.baseURI = baseUrl;
		RequestSpecification httpRequest = RestAssured.given();

		Header h1 = new Header("Content-Type","application/json");
		httpRequest.header(h1);
		httpRequest.headers(headers);

		return httpRequest.request(Method.DELETE,apiUrl);
	}

	public Headers buildBasicAuthHeader(String user, String pass) {
		String auth = user + ":" + pass;
		byte[] encodedAuth = Base64.encodeBase64(
				auth.getBytes(StandardCharsets.US_ASCII) );
		String authHeader = "Basic " + new String( encodedAuth );
		Map<String,String> h = new HashMap<>();
		h.put("Authorization",authHeader);
		return headerBuilder(h);
	}

	public Response sendApiRequest(String method, String apiUri, JSONObject body, boolean useBasicAuth) {
		Response resp = null;
		String baseUri = systemInfo.url.trim();

		if(!baseUri.trim().endsWith("/")) {
			baseUri = baseUri + "/";
		}

		Headers header;
		if(useBasicAuth) {
			header = buildBasicAuthHeader(systemInfo.user,systemInfo.pass);
		} else {
			header = buildHeaderWithLoginCookie();
		}

		if(method.trim().equalsIgnoreCase("get")) {
			resp = get(baseUri,apiUri,header);
		} else if(method.trim().equalsIgnoreCase("post")) {
			resp = post(baseUri,apiUri,header,body);
		} else if(method.trim().equalsIgnoreCase("put")) {
			resp = put(baseUri,apiUri,header,body);
		} else if(method.trim().equalsIgnoreCase("delete")) {
			resp = delete(baseUri,apiUri,header);
		}  // Do nothing

		return resp;
	}

	public Response sendApiRequest(String method, String apiUri, JSONObject body) {
		return sendApiRequest(method,apiUri,body,false);
	}

	public void hoverElement(WebElement element) {
		Actions actions = new Actions(driver);
		try {
			actions.moveToElement(element).perform();
		}
		catch (Exception e) {
			System.out.println("Can not hover to element !");
		}
	}

	public Headers headerBuilder(Map<String,String> headers) {
		List<Header> tmp = new ArrayList<>();
		for (Map.Entry<String, String> entry : headers.entrySet()) {
			String key = entry.getKey();
			String val = entry.getValue();
			tmp.add(new Header(key,val));
		}
		return new Headers(tmp);
	}

	public Headers buildHeaderWithLoginCookie() {
		String authedUsername = driver.manage().getCookieNamed("auth-username").getValue();
		String tokenSecret = driver.manage().getCookieNamed("auth-token-secret").getValue();
		String authedToken = driver.manage().getCookieNamed("auth-token").getValue();
		String authedCookie = String.format("auth-token-secret=%s; auth-username=%s; auth-token=%s",tokenSecret,authedUsername,authedToken);

		Map<String,String> h = new HashMap<>();
		h.put("cookie",authedCookie);
		return headerBuilder(h);
	}




	public void writeLogToReport(String message) {
		if(DriverManager.scenarioLog != null) {
			DriverManager.scenarioLog.get().log(message);
			System.out.println(message);
		}
	}

	public void attachScreenshotToStep() {
		if(DriverManager.scenarioLog != null) {
			// Attach screenshot into step
			DriverManager.scenarioLog.get().attach(((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES),"image/png", "Screenshot");
		} else {
			writeLogToReport("Scenario is NULL. Could not take screenshot");
		}
	}

	protected void attachImageToReport(String name) throws IOException {
		if(DriverManager.scenarioLog != null) {
			Path projectDir = Paths.get(System.getProperty("user.dir"));
			Path filePath = Paths.get(projectDir.toString(), "test-data", name + ".jpg");
			if(!Files.exists(filePath)) {
				writeLogToReport("File is not exists: " + name);
				return;
			}

			BufferedImage bImage = ImageIO.read(new File(filePath.toString()));
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ImageIO.write(bImage, "jpg", bos );
			byte [] data = bos.toByteArray();

			DriverManager.scenarioLog.get().attach(data,"image/png", name);
		} else {
			writeLogToReport("Scenario is NULL. Could not take screenshot");
		}
	}

	public String convertElementToXpath(WebElement element) {
		if(element == null) {
			return "";
		}
		// element must be declared By.xpath

		String regexStr = "^Decorated\\s(.*) -> xpath:(.*)(\\])(\\})$";

		Pattern pattern = Pattern.compile(regexStr);


		String xPathStr = element.toString();

		Matcher m = pattern.matcher(xPathStr);

		if(m.find()) {
			return m.group(2).trim();
		} else {
			return "";
		}
	}

	public String readQRCode(String base64Image) {
		String encodedContent = null;
		String secretPattern = "secret=";
		try {
			// trim base64 string
			base64Image = base64Image.replace("data:image/png;base64,","").trim();

			Base64 base64 = new Base64();
			byte[] imageBytes = base64.decode(base64Image);
			ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(imageBytes);
			BufferedImage bufferedImage = ImageIO.read(byteArrayInputStream);
			encodedContent = readQRCode(bufferedImage);
		} catch (IOException e) {
			e.printStackTrace();
		}

		if(encodedContent != null && encodedContent.indexOf(secretPattern) > 0) {
			encodedContent = encodedContent.substring(encodedContent.indexOf(secretPattern) + secretPattern.length());
		}
		return encodedContent.trim();
	}

	public String readQRCode(File qrCodeFile) {
		String encodedContent = null;
		try {
			BufferedImage bufferedImage = ImageIO.read(qrCodeFile);

			encodedContent = readQRCode(bufferedImage);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return encodedContent;
	}

	public String readQRCode(BufferedImage bufferedImage) {
		String encodedContent = null;
		try {
			BufferedImageLuminanceSource bufferedImageLuminanceSource = new BufferedImageLuminanceSource(bufferedImage);
			HybridBinarizer hybridBinarizer = new HybridBinarizer(bufferedImageLuminanceSource);
			BinaryBitmap binaryBitmap = new BinaryBitmap(hybridBinarizer);
			MultiFormatReader multiFormatReader = new MultiFormatReader();

			Result result = multiFormatReader.decode(binaryBitmap);
			encodedContent = result.getText();
		} catch (NotFoundException | com.google.zxing.NotFoundException e) {
			e.printStackTrace();
		}
		return encodedContent;
	}

	protected String get2FAToken(String secret) {
		Totp totp = new Totp(secret);
		return totp.now();
	}

	protected String convertZipFileToBaseEncodeString(String zipFilePath) {
		final File originalFile = new File(zipFilePath);
		String encodedBase64 = null;
		try {
			final FileInputStream fileInputStreamReader = new FileInputStream(originalFile);
			final byte[] bytes = new byte[(int)originalFile.length()];
			fileInputStreamReader.read(bytes);
			encodedBase64 = new String(Base64.encodeBase64(bytes));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return encodedBase64;
	}

	protected void markKnownIssue(Object expectedValue, Object actualValue, String issueLink) {
		if(!issueLink.toLowerCase().contains("failed by know issue")) {
			issueLink = "Failed by known issue: " + issueLink;
		}
		if(!expectedValue.equals(actualValue)) {
			throw new PendingException(issueLink);
		}
	}

	protected void markKnownIssue(boolean compareResult, String issueLink) {
		if(!issueLink.toLowerCase().contains("failed by know issue")) {
			issueLink = "Failed by known issue: " + issueLink;
		}
		if(!compareResult) {
			throw new PendingException(issueLink);
		}
	}

	protected boolean elementHasAttribute(WebElement element, String attributeName) {
		try {
			Object value = element.getAttribute(attributeName);

			return value != null;
		} catch (Exception ex) {
			ex.printStackTrace();

		}
		return false;
	}

	protected void navigateToUrl(String url) {
		if(driver.getCurrentUrl().contains(url)) {
			// web browser is opening, navigate back to login page.
			driver.navigate().to(url);
		} else {
			// web browser is not opened , open it.
			driver.get(url);
		}
	}

	protected float round(float value, int places) {
		if (places < 0) throw new IllegalArgumentException();

		BigDecimal bd = new BigDecimal(Double.toString(value));
		bd = bd.setScale(places, RoundingMode.HALF_UP);
		return bd.floatValue();
	}

	protected void dragAndDrop(WebElement source, WebElement destination) {
		try {
			Actions actions = new Actions(driver);
			actions.dragAndDrop(source,destination).perform();
			Thread.sleep(500);
		} catch (Exception ex) {
			ex.printStackTrace();
			Assert.fail("Error when dragging and dropping: " + ex.getMessage());
		}
	}

	protected void takeScreenshotTo(WebElement element, String path) throws IOException {
		if(element != null) {
			File s = element.getScreenshotAs(OutputType.FILE);
			FileUtils.copyFile(s,new File(path));
		}
	}

	public static boolean compareImage(File fileA, File fileB, float tolerancePercent) {
		int countDiff = 0;
		int sizeA;
		try {
			// take buffer data from both image files
			BufferedImage biA = ImageIO.read(fileA);
			DataBuffer dbA = biA.getData().getDataBuffer();
			sizeA = dbA.getSize();
			BufferedImage biB = ImageIO.read(fileB);
			DataBuffer dbB = biB.getData().getDataBuffer();
			int sizeB = dbB.getSize();
			// compare data-buffer
			if(sizeA == sizeB) {
				for(int i=0; i<sizeA; i++) {
					if(dbA.getElem(i) != dbB.getElem(i)) {
						countDiff++;
					}
				}
			} else {
				return false;
			}
		}
		catch (Exception e) {
			System.out.println("Failed to compare image files ...");
			return false;
		}

		float diffPercent = (float)countDiff * 100 /(float)sizeA;
		return (diffPercent <= tolerancePercent);
	}

}