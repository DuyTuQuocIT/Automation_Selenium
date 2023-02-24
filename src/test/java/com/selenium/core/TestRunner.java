package com.selenium.core;

import org.junit.runner.RunWith;

import io.cucumber.junit.Cucumber;
import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;

@RunWith(Cucumber.class)
@CucumberOptions(
		monochrome = true,
		features = "src/test/java/com/selenium/features/thread1",
		glue = {
				"com.selenium.tests",
				"com.selenium.pages",
				"com.selenium.core",
				"com.selenium.driver"
		},
		plugin = {
				"pretty",
				"json:test-reports/cucumber-report/cucumber.json",
		},
		tags = ""
)
public class TestRunner extends AbstractTestNGCucumberTests {

}
