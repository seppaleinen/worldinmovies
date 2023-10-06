import { defineConfig } from 'cypress'


module.exports = defineConfig({
    e2e: {
        baseUrl: 'https://localhost',
        specPattern: "cypress/integration/api/*.cy.{js,jsx,ts,tsx}"
    }
})