const expectedMovie = {
    imdb_id: Cypress._.isString,
    id: Cypress._.isNumber,
    original_title: Cypress._.isString,
    en_title: Cypress._.isString,
    poster_path: Cypress._.isString,
    release_date: Cypress._.isString,
    vote_average: Cypress._.isNumber,
    vote_count: Cypress._.isNumber,
    weight: Cypress._.isNumber
}

const expectedStatus = {
    total: Cypress._.isNumber,
    fetched: Cypress._.isNumber,
    percentageDone: Cypress._.isNumber,
}

const url = 'http://localhost:8082'

describe('Neo4J Service endpoints', () => {
    it('status response', () => {
        cy.request(`${url}/status`)
            .then((resp) => {
                expect(resp.status).to.eq(200);
                expect(resp.body).to.have.all.keys(expectedStatus);
            })
    });

    it('health response', () => {
        cy.request(`${url}/actuator/health`)
            .then((resp) => {
                expect(resp.status).to.eq(200);
                expect(resp.body.status).to.eq('UP');
            })
    });

    describe('View best country endpoint', () => {
        it('Standard call', () => {
            cy.request(`${url}/view/best/SE`)
                .then((resp) => {
                    expect(resp.status).to.eq(200);
                    expect(resp.body).length.to.be.greaterThan(0);
                    resp.body.forEach(movie => {
                        expect(movie).to.have.all.keys(expectedMovie)
                    })
                })
        })

        it('Call with skip should return different values from regular', () => {
            cy.request(`${url}/view/best/SE`)
                .then((firstResponse) => {
                    expect(firstResponse.status).to.eq(200);
                    expect(firstResponse.body.length).to.be.greaterThan(0);
                    const firstMovie = firstResponse.body[0];

                    cy.request(`${url}/view/best/SE?skip=25`)
                        .then(secondResponse => {
                            expect(secondResponse.status).to.eq(200);
                            expect(secondResponse.body).length.to.be.greaterThan(0);
                            const secondMovie = secondResponse.body[0];
                            expect(secondMovie).to.not.eq(firstMovie);
                        })
                })
        })

        it('Call with limit should return only limited response', () => {
            cy.request(`${url}/view/best/SE?limit=1`    )
                .then((response) => {
                    expect(response.status).to.eq(200);
                    expect(response.body.length).to.be.eq(1);
                })
        })

    })
})