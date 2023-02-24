package com.selenium.driver;

import java.io.*;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.selenium.log.LoggingListener;
import com.selenium.objects.SystemProperties;
import io.cucumber.core.backend.TestCaseState;
import io.cucumber.java.*;
import io.cucumber.plugin.event.PickleStepTestStep;
import io.cucumber.plugin.event.TestCase;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.events.EventFiringDecorator;
import io.github.bonigarcia.wdm.WebDriverManager;

public class DriverManager {

    // Variable used for multi-thread
    private static ThreadLocal<WebDriver> webDriver = new ThreadLocal<>();
    public static ThreadLocal<SystemProperties> credentials = new ThreadLocal<>();
    public static ThreadLocal<Scenario> scenarioLog = new ThreadLocal<>();

    public static int implicitWaitTimeInSeconds = 15;
    public static int pageLoadTimeOutInSeconds = 40;
    public static int currentImplicitWaitTime = implicitWaitTimeInSeconds;

    private static ThreadLocal<Properties> loadSystemConfig() {
        ThreadLocal<Properties> property = new ThreadLocal<>();
        try {

            Properties pr = new Properties();
            pr.load( new FileInputStream("./src/test/conf/application.properties") );
            property.set(pr);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return property;
    }

    @Before(order = 0)
    public static void loadConfig() {
        setSystemCredentials();

        String homeDir = System.getProperty("user.home");
        Path windowsDownloadPath = Paths.get(homeDir,"Downloads");

        String linuxDownloadPath = System.getProperty("user.dir");

        // Set windows default download folder
        System.setProperty("windowsDownload",windowsDownloadPath.toString());

        // Set linux default download folder
        System.setProperty("linuxDownload",linuxDownloadPath);
    }

    @Before(order = 1)
    public static void createDriver(Scenario scenario) {
        scenarioLog.set(scenario);


        // Setup web driver
        String browserName = System.getProperty("browser");
        String headless = System.getProperty("headless");
        String os = System.getProperty("os.name");

        DesiredCapabilities capabilities = new DesiredCapabilities();

        if (browserName == null) {
            browserName = "chrome";
        }

        if (browserName.equalsIgnoreCase("Firefox")) {
            //capabilities = DesiredCapabilities.firefox();

            FirefoxOptions options = new FirefoxOptions();
            // configuration to run headless mode
            if(headless != null && headless.equalsIgnoreCase("yes")) {
                options.addArguments("--window-size=1920,1080");
                options.addArguments("--start-maximized");
                options.addArguments("--headless");
            }

            String downloadPath = "";
            // Get download path
            if(os.contains("Windows")) {
                downloadPath = System.getProperty("windowsDownload");
            }
            else if(os.contains("Linux")){
                downloadPath = System.getProperty("linuxDownload");
            }
            FirefoxProfile firefoxProfile = new FirefoxProfile();
            firefoxProfile.setPreference("browser.download.folderList", 2);
            firefoxProfile.setPreference("browser.download.manager.showWhenStarting", false);
            firefoxProfile.setPreference("browser.download.dir", downloadPath);
            firefoxProfile.setPreference("browser.helperApps.neverAsk.saveToDisk", "text/csv,application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            firefoxProfile.setPreference("browser.helperApps.neverAsk.openFile","text/csv,application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            firefoxProfile.setPreference("browser.helperApps.alwaysAsk.force", false);
            firefoxProfile.setPreference("browser.download.manager.alertOnEXEOpen", false);
            firefoxProfile.setPreference("browser.download.manager.focusWhenStarting", false);
            firefoxProfile.setPreference("browser.download.manager.useWindow", false);
            firefoxProfile.setPreference("browser.download.manager.showAlertOnComplete", false);
            firefoxProfile.setPreference("browser.download.manager.closeWhenDone", false);


            options.setProfile(firefoxProfile);
            WebDriverManager.firefoxdriver().setup();
            capabilities.setBrowserName("Firefox");
            webDriver.set(new FirefoxDriver(options));
        } else if (browserName.equalsIgnoreCase("chrome")) {
            //capabilities = DesiredCapabilities.chrome();

            ChromeOptions options = new ChromeOptions();
            // configuration to run headless mode
            if(headless != null && headless.equalsIgnoreCase("yes")) {
                options.addArguments("--window-size=1920,1080");
                options.addArguments("--start-maximized");
                options.addArguments("--headless");
            }

            WebDriverManager.chromedriver().setup();
            capabilities.setBrowserName("Chrome");

            if (os.contains("Windows")) {
                capabilities.setPlatform(Platform.WIN8_1);
            } else if (os.contains("Linux")) {
                capabilities.setPlatform(Platform.LINUX);
            } else if (os.contains("Mac")) {
                capabilities.setPlatform(Platform.MAC);
            }

            webDriver.set(new ChromeDriver(options));
        }

        LoggingListener listener = new LoggingListener();
        EventFiringDecorator eventHandler = new EventFiringDecorator(listener);

        webDriver.set(eventHandler.decorate(getWebDriver()));

        getWebDriver().manage().window().maximize();
        getWebDriver().manage().deleteAllCookies();
        getWebDriver().manage().timeouts().implicitlyWait(Duration.ofSeconds(implicitWaitTimeInSeconds));
        getWebDriver().manage().timeouts().pageLoadTimeout(Duration.ofSeconds(pageLoadTimeOutInSeconds));
    }

    /**
     * @return the web driver for the current thread
     */
    public static WebDriver getWebDriver() {
        System.out.println("WebDriver: " + webDriver.get());
        return webDriver.get();
    }

    /**
     * @return the system info for the current thread
     */
    public static SystemProperties getSystemInfo() {
        return credentials.get();
    }

    /**
     * Method executes at the end of each scenario and takes screenshot in case of scenario failure.
     * Also, quit the web driver.
     *
     * @param scenario to verify if scenarios has passed or failed
     */
    @After
    public void teardown(Scenario scenario) {
        String timestamp = new SimpleDateFormat("MM-dd-yyyy HH-mm-ss").format(new GregorianCalendar().getTime());
        if (scenario.isFailed() || scenario.getStatus().name().equals("PENDING")) {
            try {
                TakesScreenshot ts = (TakesScreenshot) getWebDriver();
                // Attach screenshot on failed scenario
                scenario.attach(ts.getScreenshotAs(OutputType.BYTES),"image/png", "Failed at: " + timestamp);
                System.out.println("Scenario failed and screenshot saved in screenshot folder");
                //Ping test on failed scenario
                //pingTest(scenario,1);
            } catch (Exception e) {
                System.out.println("Exception while taking screenshot " + e.getMessage());
            }
        }

        scenario.log("Finished test case at: " + timestamp);

        System.out.println("Shutting down driver" + "\n" + "----------------------------------------------");
        System.out.println("\n");

        getWebDriver().quit();
    }

    /**
     * Author: Duy Tu
     * Hook after each step
     * Take screenshot for Then steps
     */
    int stepIndex = 0;
    @AfterStep
    public void afterStep(Scenario scenario){
//        try {
//            Field f = scenario.getClass().getDeclaredField("delegate");
//            f.setAccessible(true);
//            TestCaseState tcs = (TestCaseState) f.get(scenario);
//
//            Field f2 = tcs.getClass().getDeclaredField("testCase");
//            f2.setAccessible(true);
//            TestCase r = (TestCase) f2.get(tcs);
//
//            List<PickleStepTestStep> stepDefs = r.getTestSteps()
//                    .stream()
//                    .filter(x -> x instanceof PickleStepTestStep)
//                    .map(x -> (PickleStepTestStep) x)
//                    .collect(Collectors.toList());
//
//            PickleStepTestStep currentStepDef = stepDefs.get(stepIndex);
//
//            String currentStepKw = currentStepDef.getStep().getKeyword();
//
//            if(currentStepKw.trim().equalsIgnoreCase("then")) {
//                // Take screenshot for Then steps
//                scenario.attach(((TakesScreenshot) getWebDriver()).getScreenshotAs(OutputType.BYTES),"image/png", "Verification Image");
//            }
//            stepIndex++;
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
    }

    /**
     *  Method to load system information (url, user, pass)
     */
    private static void setSystemCredentials() {
        ThreadLocal<Properties> property = loadSystemConfig();

        // get system info from systemProperties first
        String url = System.getProperty("url") != null ? System.getProperty("url") : property.get().getProperty("url");
        String user = (System.getProperty("user") != null) ? System.getProperty("user") : property.get().getProperty("user");
        String pass = (System.getProperty("pass") != null) ? System.getProperty("pass") : property.get().getProperty("pass");

        if(!url.endsWith("/")) {
            url += "/";
        }

        credentials.set(new SystemProperties(url,user,pass));
    }


}
