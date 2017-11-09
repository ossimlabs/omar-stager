import grails.util.BuildSettings
import grails.util.Environment

// See http://logback.qos.ch/manual/groovy.html for details on configuration
// This appender is used by default for all classes and prints the logging level,
// the logger, and the message
appender('STDOUT', ConsoleAppender) {
    encoder(PatternLayoutEncoder) {
        pattern = "%level %logger - %msg%n"
    }
}

// This appender is used to only print the message, such as when printing JSON to
// be parsed by Elasticsearch
appender('JSON', ConsoleAppender) {
    encoder(PatternLayoutEncoder) {
        pattern = "%msg%n"
    }
}

root(ERROR, ['STDOUT'])
logger("omar.stager.StagerService", INFO, ['JSON'], false)
logger("omar.raster.RasterDataSetService", INFO, ['JSON'], false)

def targetDir = BuildSettings.TARGET_DIR
if (Environment.isDevelopmentMode() && targetDir) {
    appender("FULL_STACKTRACE", FileAppender) {
        file = "${targetDir}/stacktrace.log"
        append = true
        encoder(PatternLayoutEncoder) {
            pattern = "%level %logger - %msg%n"
        }
    }
    logger("StackTrace", ERROR, ['FULL_STACKTRACE'], false)
}
