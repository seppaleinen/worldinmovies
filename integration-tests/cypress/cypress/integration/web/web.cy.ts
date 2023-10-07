/// <reference types="Cypress" />

const url_address = Cypress.env('URL') !== undefined ? Cypress.env('URL') : "http://localhost:80";

context('World Map', () => {
    beforeEach(() => {
        cy.visit(`${url_address}/map`)
    });

    it('Title should be world in movies', () => {
        cy.title().should('include', 'World in Movies');
    });

    it('US Country should be clickable', () => {
        cy.get('path[data-code="US"]').click({force: true})
            .url().should('include', '/country/US');
    });

    it('US Country should have content', () => {
        cy.get('path[data-code="US"]').click({force: true})
            .get("h1").should('have.text', 'United States');
    });

    it('SE Country should be clickable', () => {
        cy.get('path[data-code="SE"]').click({force: true})
            .url().should('include', '/country/SE');

    });

    it('SE Country should have content', () => {
        cy.get('path[data-code="SE"]').click({force: true})
            .get("h1").should('have.text', 'Sweden');
    });

});

context('Imports', () => {
    beforeEach(() => {
        cy.visit(`${url_address}/import`)
    });

    it('IMDB Ratings', () => {
        cy.get('h2').should('contain', 'Import');
        cy.get('img[alt="IMDB"]').should('exist');
    })
});
