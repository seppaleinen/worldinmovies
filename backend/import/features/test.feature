Feature: Importer should download file

  Scenario: Download stuff
    Given new_orig.txt.gz is in daily file response
    When starting import
