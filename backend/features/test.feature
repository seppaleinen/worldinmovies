Feature: Live server
    In order to prove that the live server works
    As the Maintainer
    I want to send an HTTP request

    Scenario: First visit to page
        When I visit "/"
        Then I should see "No movies fetched yet"


    #Scenario: Import of daily file
    #    Given TMDB Daily File is mocked
    #    When I visit "/movies"
    #    Then I should see "Amount of movies imported: 740"
