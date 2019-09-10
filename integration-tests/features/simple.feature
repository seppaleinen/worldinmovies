Feature: Getting data
  Scenario: when calling get status
    When calling '/status'
    Then status code should be '200'
    And response should be utf-8