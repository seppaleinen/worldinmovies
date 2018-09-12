Feature: Importer should download file

  Scenario: Download stuff
    Given new.json.gz is in daily file response
    When starting import
