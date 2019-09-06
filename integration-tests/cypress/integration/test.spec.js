/// <reference types="Cypress" />

context('Actions', () => {
  beforeEach(() => {
    const url = Cypress.env('URL') !== undefined ? Cypress.env('URL') : "http://localhost:81"
    cy.visit(url)
  });

  it('Title should be world in movies', () => {
    cy.title().should('include', 'World in Movies');
  });

  it('US Country should be clickable', () => {
    cy.get('path[data-code="US"]').click({ force: true });
    var modal = cy.get('#myModal')
        .should('be.visible');
  });

  it('US Country should have content', () => {
    cy.get('path[data-code="US"]').click({ force: true });
    var modal = cy.get('#myModal')
        .should('be.visible')
        .should('contain.text', 'Top ranked movies from United States');

    /**
    modal.get('tbody>tr>th').eq(0).should('have.text', '#');
    modal.get('tbody>tr>th').eq(1).should('have.text', 'Title');
    modal.get('tbody>tr>th').eq(2).should('have.text', 'Rating');
    modal.get('tbody>tr').eq(1).get('td').eq(1).should('have.text', 'Inception')
    **/
  });

  it('SE Country should be clickable', () => {
    cy.get('path[data-code="SE"]').click({ force: true });
    var modal = cy.get('#myModal')
        .should('be.visible');
  });

  it('SE Country should have content', () => {
    cy.get('path[data-code="SE"]').click({ force: true });
    var modal = cy.get('#myModal')
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
