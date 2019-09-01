/// <reference types="Cypress" />

context('Actions', () => {
  beforeEach(() => {
    const url = Cypress.env('URL') !== undefined ? Cypress.env('URL') : "http://localhost:3000"
    cy.visit(url)
  });

  it('Title should be world in movies', () => {
    cy.title().should('include', 'World in Movies');
  });

  it('US Country should be clickable', () => {
    cy.get('#jqvmap1_us').click({ force: true });
    var modal = cy.get('#myModal')
        .should('be.visible')
        .get('#modal-text>h2')
        .should('have.text', 'Top ranked movies from United States of America');

    modal.get('tbody>tr>th').eq(0).should('have.text', '#');
    modal.get('tbody>tr>th').eq(1).should('have.text', 'Title');
    modal.get('tbody>tr>th').eq(2).should('have.text', 'Rating');
  });

  it('US Country should have content', () => {
    cy.get('#jqvmap1_us').click({ force: true });
    var modal = cy.get('#myModal')
        .should('be.visible')
        .get('#modal-text>h2')
        .should('have.text', 'Top ranked movies from United States of America');

    modal.get('tbody>tr>th').eq(0).should('have.text', '#');
    modal.get('tbody>tr>th').eq(1).should('have.text', 'Title');
    modal.get('tbody>tr>th').eq(2).should('have.text', 'Rating');
    modal.get('tbody>tr').eq(1).get('td').eq(1).should('have.text', 'Inception')
  });

  it('SE Country should be clickable', () => {
    cy.get('#jqvmap1_se').click({ force: true });
    var modal = cy.get('#myModal')
        .should('be.visible')
        .get('#modal-text>h2')
        .should('have.text', 'Top ranked movies from Sweden');

    modal.get('tbody>tr>th').eq(0).should('have.text', '#');
    modal.get('tbody>tr>th').eq(1).should('have.text', 'Title');
    modal.get('tbody>tr>th').eq(2).should('have.text', 'Rating');
  });

  it('SE Country should have content', () => {
    cy.get('#jqvmap1_se').click({ force: true });
    var modal = cy.get('#myModal')
        .should('be.visible')
        .get('#modal-text>h2')
        .should('have.text', 'Top ranked movies from Sweden');

    modal.get('tbody>tr>th').eq(0).should('have.text', '#');
    modal.get('tbody>tr>th').eq(1).should('have.text', 'Title');
    modal.get('tbody>tr>th').eq(2).should('have.text', 'Rating');
    modal.get('tbody>tr').eq(1).get('td').eq(1).should('have.text', 'The Girl with the Dragon Tattoo')
  });
});
