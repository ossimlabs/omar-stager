package omar.stager
import omar.core.ProcessStatus

class StageFileJob {
   def stagerService
   def ingestMetricsService
   def concurrent = false
   static triggers = {
      simple name: 'StageFileJobTrigger', group: 'StageFileJobGroup', repeatInterval: 5000l
   }

   def execute() {
      log.trace "Entered........."
      def fileRecord
      while(fileRecord = stagerService.nextFileToStage())
      {
         try
         {
//            def result = stagerService.stageFileJni([filename:fileRecord.filename,
//                                                     buildOverviews: fileRecord.buildOverviews,
//                                                     buildHistograms:fileRecord.buildHistograms,
//                                                     buildHistogramsWithR0:fileRecord.buildHistogramsWithR0,
//                                                     useFastHistogramStaging:fileRecord.useFastHistogramStaging,
//                                                     overviewCompressionType: fileRecord.overviewCompressionType,
//                                                     overviewType: fileRecord.overviewType
//            ]
            log.info "Staging File: ${fileRecord.filename}"
            def result =  stagerService.stageFileJni(fileRecord as HashMap)
            ingestMetricsService.endIngest(fileRecord.filename)

            if(result.status>=300)
            {
               if(result.status == 415)
               {
                  log.error  result?.message?:"File ${fileRecord.filename} not added.  We currently do not support updating."
                  stagerService.updateFileStatus(fileRecord.processId, ProcessStatus.FINISHED, result?.message?:"Failed to stage file ${fileRecord.filename}")
                  ingestMetricsService.setStatus(fileRecord.filename, ProcessStatus.FINISHED.toString(), result?.message?:"Failed to stage file ${fileRecord.filename}")
               }
               else
               {
                  log.error  result?.message?:"Failed to stage file ${fileRecord.filename}"
                  stagerService.updateFileStatus(fileRecord.processId, ProcessStatus.FAILED, result?.message?:"Failed to stage file ${fileRecord.filename}")
                  ingestMetricsService.setStatus(fileRecord.filename, ProcessStatus.FINISHED.toString(),  result?.message?:"Failed to stage file ${fileRecord.filename}")
               }
            }
            else
            {
               log.info "File ${fileRecord.filename} successfully staged"
               stagerService.updateFileStatus(fileRecord.processId, ProcessStatus.FINISHED, "File ${fileRecord.filename} successfully staged")
               ingestMetricsService.setStatus(fileRecord.filename, ProcessStatus.FINISHED.toString(),  result?.message?:"File ${fileRecord.filename} successfully staged")
            }
         }
         catch(e)
         {
            log.error e.toString()
            stagerService.updateFileStatus(fileRecord.processId, ProcessStatus.FAILED, "File ${fileRecord.filename} NOT staged with error ${e}")
            ingestMetricsService.endIngest(fileRecord.filename)
            ingestMetricsService.setStatus(fileRecord.filename, ProcessStatus.FAILED.toString(),  "File NOT staged with error ${e}".toString())
         }
      }
      log.trace "Leaving..........."
   }
}
