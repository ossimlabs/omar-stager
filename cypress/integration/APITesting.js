
// Declare the variables
let allImages;

// Get the json here, store in variable

// McKenzie - We got our collection ;)
describe('yet another test', () => {
    it('here we go again', () => {
       // const url = https://omar-dev.ossim.io/omar-wfs/wfs?service=WFS&version=1.1.0&request=GetFeature&typeName=omar:raster_entry&filter=&outputFormat=JSON&sortBy=mission_id+D&startIndex=0;
        cy.request({
            method: 'Get',
            url: `https://omar-dev.ossim.io/omar-wfs/wfs?service=WFS&version=1.1.0&request=GetFeature&typeName=omar:raster_entry&filter=&outputFormat=JSON&sortBy=mission_id+D&startIndex=0`,
        }).then((response) => {
            console.log('response', response)
            console.log('responseBody', response.body)
            // expect(response).property('status').to.equal(200)
            // expect(response.body).property('id').to.not.be.oneOf([null, ""])
            const body = (response.body)

        })
    })
})

// iterate through and grab the ones with mission id and filename


// Make a call to the endpoint to get JSON data
