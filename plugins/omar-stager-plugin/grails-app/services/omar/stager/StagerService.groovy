package omar.stager

import groovy.util.logging.Slf4j
import omar.core.Repository
import omar.core.ProcessStatus
import omar.core.HttpStatus
import joms.oms.ImageStager
import grails.transaction.Transactional
import groovy.json.JsonBuilder


@Slf4j
@Transactional
class StagerService
{
	static transactional = true
	//def dataManagerService
	def grailsApplication
	def sessionFactory


	def parserPool
	def ingestService
	def ingestMetricsService
	def dataInfoService

	enum Action {
		BUILD_OVRS,
		INDEX_ONLY
	}

	def runStager( Repository repository )
	{

		repository.scanStartDate = new Date()
		repository.scanEndDate = null
		repository.save()

		StagerJob.triggerNow( [baseDir: repository.baseDir] )
	}

	def cleanUpGorm()
	{
		def session = sessionFactory.currentSession
		session.flush()
		session.clear()
	}


	def stageFileJni( HashMap params, String baseDir = '/' )
	{
		def results = [status: HttpStatus.OK, message: ""]
		ImageStager imageStager = new ImageStager()
		String filename = params.filename

		def requestType = "GET"
		def requestMethod = "stageFileJni"

		def ingestdate
		def stager_logs
		def responseTime

		try
		{
			ingestMetricsService.startStaging( filename )
			ingestdate = new Date()

			if ( imageStager.open( params.filename ) )
			{
				URI uri = new URI( params.filename )

				String scheme = uri.scheme
				if ( ! scheme ) scheme = "file"
				if ( scheme != "file" )
				{
					params.buildHistograms = false
					params.buildOverviews = false
				}
				//imageStager.setDefaults()
				//imageStager.stageAll()

				Integer nEntries = imageStager.getNumberOfEntries()
				( 0..<nEntries ).each
						{
							imageStager.setEntry( it )
							imageStager.setDefaults()
							imageStager.setHistogramStagingFlag( params.buildHistograms )
							imageStager.setOverviewStagingFlag( params.buildOverviews )
							imageStager.setCompressionType( params.overviewCompressionType )
							imageStager.setOverviewType( params.overviewType )
							imageStager.setUseFastHistogramStagingFlag( params.useFastHistogramStaging )
							imageStager.setQuietFlag( true )

							if ( params.buildHistograms && params.buildOverviews
									&& imageStager.hasOverviews() && params.buildHistogramsWithR0 )
							{

								imageStager.setHistogramStagingFlag( false )
								imageStager.stage()

								imageStager.setHistogramStagingFlag( true )
								imageStager.setOverviewStagingFlag( false )
							}
							imageStager.stage()
						}
				//imageStager.stageAll()
				imageStager.delete()
				imageStager = null

				String xml = dataInfoService.getInfo( filename )
				if ( xml )
				{
					def parser = parserPool.borrowObject()
					def oms
					try
					{
						oms = new XmlSlurper( parser ).parseText( xml )
					}
					catch ( e )
					{
						log.error e.toString()
						results.status = HttpStatus.UNSUPPORTED_MEDIA_TYPE
						results.message = "XML is in incorrect format for file ${params.filename}"
					}

					parserPool.returnObject( parser )
					def (status, message) = ingestService.ingest( oms, baseDir )
					results = [status: status, message: message.toString()]
				}
				else
				{
					results.status = HttpStatus.UNSUPPORTED_MEDIA_TYPE
					results.message = "Unable to open file ${params.filename}"
					ingestMetricsService.setStatus( filename, ProcessStatus.FAILED, "Unable to open file ${params.filename}" )
				}
			}

			Date endTime = new Date()
			responseTime = Math.abs(ingestdate.getTime() - endTime.getTime())

			stager_logs = new JsonBuilder(timestampAAAA: ingestdate.format("yyyy-MM-dd hh:mm:ss.ms"), requestType: requestType,
					requestMethod: requestMethod, status: results.status, message: results.message, filename: filename,
					endTime: endTime.format("yyyy-MM-dd hh:mm:ss.ms"), responseTime: responseTime)

			log.info stager_logs.toString()

			ingestMetricsService.endStaging( filename )
		}
		catch ( e )
		{
			results.status = HttpStatus.UNSUPPORTED_MEDIA_TYPE
			results.message = "Unable to process file ${params.filename} with ERROR: ${e}"
			log.error "${e.toString()}"
			ingestMetricsService.setStatus( filename, ProcessStatus.FAILED, "Unable to process file ${params.filename} with ERROR: ${e}" )
		}

		imageStager?.delete()
		results
	}

	def stageFile( String filename, String baseDir = '/' )
	{
		def results
		try
		{
			//filesLog.append("${file.absolutePath}\n")
			def start = System.currentTimeMillis()
			def xml = null
			def action = Action.BUILD_OVRS

			switch ( action )
			{
				case Action.BUILD_OVRS:
					xml = StagerUtil.getInfo( filename.toString() )
					break
				case Action.INDEX_ONLY:
					xml = dataInfoService.getInfo( filename.toString() )
					break
			}
			if ( xml )
			{
				def parser = parserPool.borrowObject()
				def oms = new XmlSlurper( parser ).parseText( xml )

				parserPool.returnObject( parser )

				def (status, message) = ingestService.ingest( oms, baseDir )

				results = message

//				switch ( status )
//				{
//				case 200:
//					//          filesLog.println file.absolutePath
//					break
//				case 500:
//					//          rejectsLog.println "${file.absolutePath} ${message}"
//					break
//				}
			}
			else
			{
//				rejectsLog.println file.absolutePath
			}

			//      if ( index?.incrementAndGet() % batchSize == 0 )
			//      {
			//        cleanUpGorm()
			//      }
			//
			def end = System.currentTimeMillis()
		}
		catch ( Exception e )
		{
			log.error e.toString()
			//println "ERROR: ${ filename } ${ e.message }"
			e.printStackTrace()
		}

		return results
	}

	private String getNewFileStageProcessId()
	{
		String result
		Boolean found = true

		while ( found )
		{
			result = UUID.randomUUID().toString()
			if ( OmarStageFile.findByProcessId( result ) )
			{
				found = true
			}
			else
			{
				found = false
			}
		}

		result
	}

	synchronized HashMap addFileToStage( String filename, HashMap params = null )
	{
		HashMap result = [status : HttpStatus.OK,
						  message: "",
						  results: []

		]
		def fileRecord = OmarStageFile.findByFilename( filename )
		if ( fileRecord )
		{
			if ( fileRecord.status == ProcessStatus.FAILED )
			{
				fileRecord.status = ProcessStatus.READY
				fileRecord.save( flush: true )
			}
		}
		else
		{
			String processId = getNewFileStageProcessId()
			Boolean buildOverviews = params?.buildOverviews
			Boolean buildHistograms = params?.buildHistograms
			Boolean buildHistogramsWithR0 = params?.buildHistogramsWithR0
			Boolean useFastHistogramStaging = params?.useFastHistogramStaging
			String overviewCompressionType = params?.overviewCompressionType
			String overviewType = params?.overviewType

			fileRecord = new OmarStageFile( processId: processId,
					filename: filename,
					buildOverviews: buildOverviews,
					buildHistograms: buildHistograms,
					buildHistogramsWithR0: buildHistogramsWithR0,
					useFastHistogramStaging: useFastHistogramStaging,
					overviewCompressionType: overviewCompressionType,
					overviewType: overviewType,
					status: ProcessStatus.READY,
					statusMessage: "Ready to stage file: ${filename}"
			)
			fileRecord.save( flush: true )

		}

		result.results << fileRecord.properties
		result
	}

	synchronized def nextFileToStage()
	{
		def firstObject = OmarStageFile.find( "FROM OmarStageFile where status = 'READY' ORDER BY id asc" )
		def result = [:]

		firstObject?.status = "RUNNING"
		firstObject?.statusMessage = ""
		firstObject?.save( flush: true )
		result = firstObject?.properties

		result
	}

	HashMap updateFileStatus( String processId, ProcessStatus status, String statusMessage )
	{
		HashMap result = [statusCode: HttpStatus.OK,
						  status    : HttpStatus.SUCCESS,
						  message   : ""
		]

		OmarStageFile stageFileRecord = OmarStageFile.findByProcessId( processId )

		if ( stageFileRecord )
		{
			stageFileRecord.status = status
			if ( statusMessage != null ) stageFileRecord.statusMessage = statusMessage

			// for now, until we support archiving, ... etc.  once the status goes to finished
			// we will remove the file from the table
			if ( stageFileRecord.status == ProcessStatus.FINISHED )
			{
				if ( ! stageFileRecord.delete( flush: true ) )
				{
					result.statusCode = HttpStatus.INTERNAL_SERVER_ERROR
					result.status = HttpStatus.ERROR
					result.message = "Unable to delete record for id: ${processId}"
				}
			}
			else
			{
				if ( ! stageFileRecord.save( flush: true ) )
				{
					result.statusCode = HttpStatus.INTERNAL_SERVER_ERROR
					result.status = HttpStatus.ERROR
					result.message = "Unable to update record for id: ${processId}"
				}
			}
		}
		else
		{
			result.message = "Unable to update status for id: ${processId}"
			result.statusCode = HttpStatus.NOT_FOUND
			result.status = HttpStatus.ERROR
		}
		result
	}
}
