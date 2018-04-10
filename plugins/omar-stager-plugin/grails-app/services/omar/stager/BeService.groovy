package omar.stager


import com.vividsolutions.jts.io.WKTReader
import grails.transaction.Transactional
import groovy.json.JsonSlurper


@Transactional
class BeService {

	def grailsApplication


	def getBeInfo( be ) {
		def url = new URL( "${ grailsApplication.config.beInfoUrl }&BE=${ be }" )
		println "Looking for BE info: ${ url }"
		def text = url.getText()
		def json = new JsonSlurper().parseText( text )


		return json
	}

	def saveBeInfo( be, info ) {
		be.activity = info.activity
		be.affiliation = info.affiliation
		be.be = info.be
		be.category = info.category
		be.cc = info.cc
		be.classification = info.classification
		be.codeWord = info.codeword
		be.condition = info.condition
		be.dateLastChanged = info.dateLastChanged
		be.lastUpdated = info.lastUpdated
		be.location = info.location
		be.msnPrimary = info.msnPrimary
		be.name = info.name
		be.polSubdivision = info.pol_subdiv
		be.recordStatus = info.record_status
		be.releaseMark = info.releaseMark
		be.reviewDate = info.reviewDate
		be.sk = info.SK
		be.suffix = info.suffix
		be.symbolId = info.symbolid

		be.save()
		if ( be.hasErrors() ) {
			be.errors.allErrors.each { println it }
		}


		return
	}

	def updateBeTable( beNumber, overwrite ) {
		def bes = BasicEncyclopedia.findAllByBeLike( "%${ beNumber }%" )

		// if no BEs exist, make a new entry
		if ( bes.size() < 1 ) {
			def beInfo = getBeInfo( beNumber )
			beInfo.features.each {
				def be = it
				def info = be.properties

				def location = be.geometry.coordinates
				info.location = new WKTReader().read( "POINT( ${ location.join( " " ) } )" )
				info.location.setSRID( 4326 )

				saveBeInfo( new BasicEncyclopedia(), info )
			}
		}
		// if a force overwrite is requested
		else if ( bes.size() > 0 && overwrite ) {
			def beInfo = getBeInfo( beNumber )
			beInfo.features.each {
				def suffix = it.suffix
				def be = bes.find { it.suffix = suffix }
				def info = be.properties

				saveBeInfo( be, info )
			}
		}


		return
	}
}
