// GSP-4101 Tests
describe('GSP-4101 mission_id runtime tests', () => {

    let missionIDs, features
    it('Grab Mission IDs', () => {
        cy.request({method: "GET", url: "omar-stager/dataManager/getDistinctValues?property=missionId"})
            .then((response) => {
                missionIDs = response.body
        })
    })

    it('Delete Support Files in Features', () => {
        features = []
        missionIDs.forEach(missionID => {
            cy.request({method: "GET", url: "omar-wfs/wfs?maxFeatures=3&filter=mission_id%20LIKE%20%27%25"+missionID+"%25%27&outputFormat=JSON&request=GetFeature&service=WFS&sortBy=acquisition_date+D&startIndex=0&typeName=omar:raster_entry&version=1.1.0"})
                .then((response) => {
                    response.body.features.forEach(feature => {
                        features.push(feature)
                        cy.request({
                            method: "POST",
                            url: "omar-stager/dataManager/removeRaster?deleteSupportFiles=true&filename="+feature.properties.filename,
                            form: true,
                            body: {
                                //username: 'postgres',
                                //password: 'vME7B9RWivNqBkgfz5N9'
                                username: 'omar',
                                password: 'omarftw123'
                            }
                        })
                    })
                })
        })
    })

    it('Restaging Support Files in Features', () => {
        features.forEach(feature => {
            cy.request({method: "POST", url: "omar-stager/dataManager/addRaster?filename="+feature.properties.filename})
        })
    })

    it('Checking addRaster Features', () => {
        features.forEach(feature => {
            cy.request({method: "GET", url: "omar-wfs/wfs?maxFeatures=3&filter=filename%20IS%20LIKE%20%27%25"+feature.properties.filename+"%25%27&outputFormat=JSON&request=GetFeature&service=WFS&sortBy=acquisition_date+D&startIndex=0&typeName=omar:raster_entry&version=1.1.0"})
                .then((response) => {
                    expect(response.body.totalFeatures).to.eq(1)
                })
        })
    })

})