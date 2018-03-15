package omar.stager


class BeController {

    def BeService


    def updateBe() {
        beService.updateBeTable( params.beNumber, true )

        render "done"
    }
}
