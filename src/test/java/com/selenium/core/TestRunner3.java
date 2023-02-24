package com.selenium.core;

import io.cucumber.junit.Cucumber;
import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
        monochrome = true,
        features = "src/test/java/com/selenium/features/thread3",
        glue = {
                "com.selenium.tests",
                "com.selenium.pages",
                "com.selenium.core",
                "com.selenium.driver"
        },
        plugin = {
                "pretty",
                "json:test-reports/cucumber-report/cucumber3.json",
        },
        tags = ""
)
public class TestRunner3 extends AbstractTestNGCucumberTests {

}
