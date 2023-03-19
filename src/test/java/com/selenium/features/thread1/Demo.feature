Feature: Demo Feature

  @TestDemo
  Scenario: Purchase a product in sauce demo page
    Given I navigate to url "https://www.saucedemo.com/"
    And I enter user name "standard_user"
    And I enter password "secret_sauce"
    And I click button Login
    Then I verify login success
    # purchase product
    And I add product "Sauce Labs Backpack" to shopping cart
    # checkout
    And I go to shopping cart
    And I check out added items
    And I enter my information
    And I click button Continue
    And I click button Finish
    # verify checkout success
    Then I verify message "Thank you for your order!" is displayed