// GSP-4101 Tests
describe('GSP-4101 mission_id runtime tests', () => {
    /*context('context'), () => {
        expect(add(1, 2)).to.eq(3)*/
/*
    it('DESCRIPTION', () => {
        cy
        .request('GET', 'http://localhost:8888/users/admin')
        .then((response) => {
            // response.body is automatically serialized into JSON
            expect(response.body).to.have.property('name', 'Jane') // true
        })    
    })
*/
    let features
it('Grab up to 1000 images', () => {
    cy.request({method: "GET", url: "/wfs?filter=&outputFormat=JSON&request=GetFeature&service=WFS&sortBy=acquisition_date+D&startIndex=0&typeName=omar:raster_entry&version=1.1.0"})
        .then((response) => {
            expect(response.body).to.have.property('type', 'FeatureCollection')
            features = response.body.features
        })
})
    
})