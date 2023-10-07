import { defineConfig } from 'cypress'


module.exports = defineConfig({
    e2e: {
        baseUrl: 'https://localhost',
        specPattern: "cypress/integration/**/*.cy.{js,jsx,ts,tsx}",
        experimentalRunAllSpecs: true
    }
})