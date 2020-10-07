package omar.stager.app 
 
import grails.util.Holders
import groovy.util.logging.Slf4j
import omar.core.Repository
import omar.stager.core.StagerJob
import org.quartz.TriggerKey

import grails.util.Environment

@Slf4j
class BootStrap {
    def sessionFactory
    def grailsApplication

    def init = { servletContext ->
     try {
         if (Environment.current == Environment.DEVELOPMENT) {
             [
                     // '/Volumes/Iomega_HDD/data',
                     // '/data/celtic',
                     // '/data1',
                     // '/data/uav'
             ].each {
                 println it
                 def repo = Repository.findOrCreateByBaseDir(it)
                 repo.save()
                 StagerJob.triggerNow(baseDir: repo.baseDir)
             }
             // sessionFactory?.currentSession?.flush()
         }

         grailsApplication = Holders.grailsApplication
         //StagerConfig.application = grailsApplication
         //StagerUtils.resetSqsConfig()
         //StagerUtils.sqsConfig

         def quartzScheduler = grailsApplication.mainContext.getBean('quartzScheduler')

         //if(SqsUtils.sqsConfig.reader.enabled)
         //{
         org.quartz.TriggerKey triggerKey = new TriggerKey("StageFileJobTrigger", "StageFileJobGroup")

         def trigger = quartzScheduler.getTrigger(triggerKey)
         if (trigger) {
             trigger.repeatInterval = 5000l
             // trigger.repeatInterval = StagerUtils.sqsConfig.reader.pollingIntervalSeconds*1000 as Long

             Date nextFireTime = quartzScheduler.rescheduleJob(triggerKey, trigger)
         }
         //}
     }
     catch (final Exception e) {
        log.error("Bootstrap init failure. If the exception is from Hibernate unable to create raster_entry, " +
                "the likely cause is missing postgis extensions/schemas. Omar Stager uses the Omar Raster plugin " +
                "which requires postgis DB schema.")
         log.error(e.message)
     }
    }
    def destroy = {
    }
}
