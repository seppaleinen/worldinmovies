const imdbUrl = 'http://localhost:8000'
const expectedImdbStatus = {
    total: Cypress._.isNumber,
    fetched: Cypress._.isNumber,
    percentageDone: Cypress._.isNumber,
}

const expectedRatingsResponse = {
    found: Cypress._.isArray,
    not_found: Cypress._.isArray
}

const expectedFound = {
    country_code: Cypress._.isString,
    id: Cypress._.isNumber,
    imdb_id: Cypress._.isString,
    original_title: Cypress._.isString,
    poster_path: Cypress._.isString,
    release_date: Cypress._.isString,
    vote_average: Cypress._.isString,
    vote_count: Cypress._.isNumber
}

const expectedNotFound = {
    year: Cypress._.isString,
    imdb_id: Cypress._.isString,
    title: Cypress._.isString
}


describe('IMDB Service endpoints', () => {
    it('Status response', () => {
        cy.request(`${imdbUrl}/status`)
            .then((resp) => {
                expect(resp.status).to.eq(200);
                expect(resp.body).to.have.all.keys(expectedImdbStatus);
            })
    });

    it('Posting ratings.csv should be parseable', () => {
        cy.fixture('ratings', 'base64')
            .then(ratings => Cypress.Blob.base64StringToBlob(ratings, "text/csv"))
            .then(blob => {
                const formData = new FormData();
                formData.append('file', blob, "ratings.csv");

                cy.request({
                    url: `${imdbUrl}/ratings`,
                    method: "POST",
                    headers: {
                        'content-type': 'multipart/form-data'
                    },
                    body: formData
                })
                    .then(response => {
                        expect(response.status).to.eq(200);
                        const bodyString = Cypress.Blob.arrayBufferToBinaryString(response.body);
                        const body = JSON.parse(bodyString);

                        expect(body).to.have.all.keys(expectedRatingsResponse);
                        expect(body.found.US.length).to.be.greaterThan(0);
                        body.found.US.forEach(movie => {
                            expect(movie).to.have.all.keys(expectedFound);
                        })

                        expect(body.not_found.length).to.be.greaterThan(0);

                        body.not_found.forEach(nonfound => {
                            expect(nonfound).to.have.all.keys(expectedNotFound);
                        })

                    })
            })
    });

    it('Get Votes For Movie Endpoint', () => {
        const expectedResponse = {
            id: Cypress._.isNumber,
            imdb_vote_average: Cypress._.isNumber,
            imdb_vote_count: Cypress._.isNumber,
            vote_average: Cypress._.isNumber,
            vote_count: Cypress._.isNumber,
            weighted_rating: Cypress._.isNumber
        }
        cy.request(`${imdbUrl}/votes/9322,1700`)
            .then((resp) => {
                expect(resp.status).to.eq(200);
                expect(resp.body.length).to.be.greaterThan(0);
                resp.body.forEach(movie => {
                    expect(movie).to.have.all.keys(expectedResponse);
                })
            })
    });


    it('Get Best Movies From Country Endpoint', () => {
        const expectedResponse = {
            result: Cypress._.isArray,
            total_result: Cypress._.isNumber
        }
        const expectedMovie = {
            en_title: Cypress._.isString,
            id: Cypress._.isNumber,
            imdb_id: Cypress._.isString,
            original_title: Cypress._.isString,
            poster_path: Cypress._.isString,
            release_date: Cypress._.isString,
            vote_average: Cypress._.isNumber,
            vote_count: Cypress._.isNumber
        }
        cy.request(`${imdbUrl}/view/best/SE`)
            .then((resp) => {
                expect(resp.status).to.eq(200);
                expect(resp.body).to.have.all.keys(expectedResponse);
                expect(resp.body.result.length).to.be.greaterThan(0);
                resp.body.result.forEach(movie => {
                    expect(movie).to.have.all.keys(expectedMovie);
                })
            })
    });

})