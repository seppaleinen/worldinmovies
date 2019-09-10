const { Given, When, Then } = require("cucumber");
const { expect } = require("chai");


When('calling {string}', function (string) {
  console.log(string);
});

Then("status code should be '{int}'", function(expected_status_code) {
  console.log(expected_status_code);
});

Then("response should be utf-8", function() {
});
