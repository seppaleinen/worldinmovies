const { setWorldConstructor } = require("cucumber");

class CustomWorld {
  constructor() {
    this.response = null;
  }

  setResponse(response) {
    this.response = response;
  }
}

setWorldConstructor(CustomWorld);
