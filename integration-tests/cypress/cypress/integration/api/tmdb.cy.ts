const tmdbUrl = 'http://localhost:8020'


describe('TMDB Service endpoints', () => {
    it('Status Endpoint', () => {
        const expectedTmdbStatus = {
            total: Cypress._.isNumber,
            fetched: Cypress._.isNumber,
            percentageDone: Cypress._.isNumber,
        }
        cy.request(`${tmdbUrl}/status`)
            .then((resp) => {
                expect(resp.status).to.eq(200);
                expect(resp.body).to.have.all.keys(expectedTmdbStatus);
            })
    });

    it('Get Movie Details Endpoint', () => {
        const expectedMovie = {
            id: Cypress._.isNumber,
            title: Cypress._.isString,
            overview: Cypress._.isString,
            vote_count: Cypress._.isNumber,
        }
        cy.request(`${tmdbUrl}/movie/9322,1700`)
            .then((resp) => {
                expect(resp.status).to.eq(200);
                expect(resp.body.length).to.be.eq(2);
                resp.body.forEach(movie => {
                    expect(movie).to.have.any.keys(expectedMovie);
                })
            })
    });
})