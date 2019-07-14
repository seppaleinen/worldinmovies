Feature: DB Integration
    The DB should work

    Scenario: DB stuff
        When I save a movie with basic info
        Then I should be able to find it

    Scenario: DB stuff
        When I save a movie with all info
        Then I should be able to find it with all info