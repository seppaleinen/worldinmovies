/// <reference types="Cypress" />

context('World Map', () => {
    beforeEach(() => {
        const url = Cypress.env('URL') !== undefined ? Cypress.env('URL') : "http://localhost:80"
        cy.visit(url)
    });

    it('Title should be world in movies', () => {
        cy.title().should('include', 'World in Movies');
    });

    it('US Country should be clickable', () => {
        cy.get('path[data-code="US"]').click({force: true});
        cy.get('#myModal')
            .should('be.visible');
    });

    it('US Country should have content', () => {
        cy.get('path[data-code="US"]').click({force: true});
        const modal = cy.get('#myModal', {timeout: 10000})
            .should('be.visible')
            .should('contain.text', 'Top ranked movies from United States')


        modal.get('table>thead>tr>th').eq(0).should('have.text', '#');
        modal.get('table>thead>tr>th').eq(1).should('have.text', 'Title');
        modal.get('table>thead>tr>th').eq(2).should('have.text', 'Rating');
        //modal.get('table>tbody>tr').eq(0).get('td').eq(0).should('match', /[0-9]*/)
        //modal.get('table>tbody>tr').eq(0).get('td').eq(1).should('match', /^\w+$/)
        //modal.get('table>tbody>tr').eq(0).get('td').eq(2).should('match', /^[0-9.]*$/)
    });

    it('SE Country should be clickable', () => {
        cy.get('path[data-code="SE"]').click({force: true});
        cy.get('#myModal')
            .should('be.visible');
    });

    it('SE Country should have content', () => {
        cy.get('path[data-code="SE"]').click({force: true});
        cy.get('#myModal')
            .should('be.visible')
            .should('contain.text', 'Top ranked movies from Sweden');

        /**
         modal.get('tbody>tr>th').eq(0).should('have.text', '#');
         modal.get('tbody>tr>th').eq(1).should('have.text', 'Title');
         modal.get('tbody>tr>th').eq(2).should('have.text', 'Rating');
         modal.get('tbody>tr').eq(1).get('td').eq(1).should('have.text', 'The Girl with the Dragon Tattoo')
         **/
    });

});

context('Imports', () => {
    beforeEach(() => {
        const url = Cypress.env('URL') !== undefined ? Cypress.env('URL') : "http://localhost:80"
        cy.visit(url)
    });

    it('IMDB Ratings import should color code countries', () => {
        cy.get('button:contains("Import")').click({force: true});
        cy.get('#importModal')
            .should('be.visible')
            .should('contain.text', 'Choose how you want to import your data')
            .get('img[alt="IMDB"]').click({force: true});

        cy.get('.import').should('contain.text', 'Import IMDB data');

        cy.get('input[type=file]').selectFile('cypress/fixtures/ratings.csv', {force: true});
        let gray = 'rgb(168, 168, 168)';
        let lightGreen = 'rgb(201, 223, 175)';
        cy.get('path[data-code="SE"]').should('have.css', 'fill', gray);
        cy.get('path[data-code="US"]').should('have.css', 'fill', lightGreen);
    })
});
