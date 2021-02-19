// Declare the variables
let fullCollection = [];
let allMissionIds;

describe('Tests for omar-stager', () => {

    it('Grab all mission ids', () => {
        cy.request({
            method: "GET",
            url: "/omar-stager/dataManager/getDistinctValues?property=missionId"
        }).then((response) =>{
            console.log('all mission ids: ', response.body)
            allMissionIds = response.body;
        })
    })

    it('Loop through and grab 3 of each mission id', ()=> {
        allMissionIds.forEach((id) => {
            cy.request({method: "GET", url: "/omar-wfs/wfs?maxFeatures=3&filter=mission_id%20LIKE%20%27%25"+id+"%25%27&outputFormat=JSON&request=GetFeature&service=WFS&sortBy=acquisition_date%2BD&startIndex=0&typeName=omar%3Araster_entry&version=1.1.0"})
                .then((response) => {
                    let temp
                    console.log(response.body)
                    for(var i = 0; i < response.body.totalFeatures; i++) {
                        temp = response.body.features[i].properties['mission_id']
                        fullCollection.push(response.body.features[i])
                        expect(temp).to.eq(id)
                    }
                    console.log('full collection', fullCollection)
                })
        })
    })

    it('Remove, Add, & Check everything', () => {
        fullCollection.forEach((image) => {
                let filename = image.properties.filename
                    console.log(filename)
                    cy.request({method: "POST",
                        url: "/omar-stager/dataManager/removeRaster?deleteFiles=false&deleteSupportFiles=true&filename="+ filename,
                        failOnStatusCode: false
                    }).then((response) => {
                        cy.request({method: "POST",
                            url: "/omar-stager/dataManager/addRaster?filename="+filename+"&background=true&buildThumbnails=true&buildOverviews=true&buildHistograms=true&buildHistogramsWithR0=false&useFastHistogramStaging=false",
                            failOnStatusCode: false
                        }).then((response) => {
                            cy.wait(20000)
                            cy.request({method: "GET",
                                url: "/omar-wfs/wfs?filter=filename%20LIKE%20%27%25"+filename+"%25%27&outputFormat=JSON&request=GetFeature&service=WFS&sortBy=acquisition_date%2BD&startIndex=0&typeName=omar%3Araster_entry&version=1.1.0",
                                failOnStatusCode: false
                            }).then((response) => {
                                expect(response.body.totalFeatures).to.be.gte(1)
                                let actual = response.body.features[0].properties
                                let expected = image.properties
                                let keys = Object.keys(expected)
                                let forDeletion = ["id", "ingest_date", "raster_data_set_id", "receive_date"]
                                keys = keys.filter(item => !forDeletion.includes(item))
                                keys.forEach((key) => {
                                    expect(actual[key]).to.eq(expected[key])
                                })
                            })
                        })
                    })
                })
        })
})
