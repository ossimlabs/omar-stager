
// Declare the variables
let fullCollection;
let allMissionIds;
// Get the json here, store in variable




// McKenzie - We got our collection ;)
describe('yet another test', () => {

    it('Grab all mission ids', () => {
        cy.request({
            method: "GET",
            url: `https://omar-dev.ossim.io/omar-stager/dataManager/getDistinctValues?property=missionId`
        }).then((response) =>{
            console.log('all mission ids: ', response.body)
            allMissionIds = response.body;
        })
    })

    it('Loop through and grab 3 of each mission ids', ()=> {
        fullCollection = [];
        allMissionIds.forEach((id) => {
            cy.request({method: "GET", url: "https://omar-dev.ossim.io/omar-wfs/wfs?maxFeatures=3&filter=mission_id%20LIKE%20%27%25"+id+"%25%27&outputFormat=JSON&request=GetFeature&service=WFS&sortBy=acquisition_date%2BD&startIndex=0&typeName=omar%3Araster_entry&version=1.1.0"})
                .then((response) => {
                    let temp
                    console.log(response.body)
                    for(var i = 0; i < response.body.totalFeatures; i++) {
                        temp = response.body.features[i].properties['mission_id']
                        fullCollection.push(response.body.features[i])
                        console.log('full collection', fullCollection)
                        expect(temp).to.eq(id)
                    }
                })
        })
    })

//     it('Remove images', ()=> {
//         // allMissionIds.forEach((id) => {
//             cy.request({method: "POST",
//                 url: "https://omar-dev.ossim.io/omar-stager/dataManager/removeRaster?deleteFiles=false&deleteSupportFiles=true&filename="+ fullCollection[0].properties.filename,
//                 auth: {username: 'radiantcibot', password: 'lhLvXspFyX9wraf6jB1I'}
//             })
//                 .then((response) => {
//                     let temp
//
//                 })
//         // })
//     })

    it('Add images back', ()=> {
        // allMissionIds.forEach((id) => {
        cy.request({method: "POST",
            url: "https://omar-dev.ossim.io/omar-stager/dataManager/addRaster?filename="+fullCollection[0].properties.filename+"&background=true&buildThumbnails=true&buildOverviews=true&buildHistograms=true&buildHistogramsWithR0=false&useFastHistogramStaging=false",
            auth: {username: 'radiantcibot', password: 'lhLvXspFyX9wraf6jB1I'}
        })
            .then((response) => {
                let temp

            })
        // })
    })
})

// iterate through and grab the ones with mission id and filename


// Make a call to the endpoint to get JSON data
