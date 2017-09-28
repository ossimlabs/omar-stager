# OMAR Stager

## Dockerfile
```
FROM omar-ossim-base
EXPOSE 8080
RUN mkdir /usr/share/omar
COPY omar-stager-app-1.0.0-SNAPSHOT.jar /usr/share/omar
RUN chown -R 1001:0 /usr/share/omar
RUN chown 1001:0 /usr/share/omar
RUN chmod -R g+rw /usr/share/omar
RUN find $HOME -type d -exec chmod g+x {} +
USER 1001
WORKDIR /usr/share/omar
CMD java -server -Xms256m -Xmx1024m -Djava.awt.headless=true -XX:+CMSClassUnloadingEnabled -XX:+UseGCOverheadLimit -Djava.security.egd=file:/dev/./urandom -jar omar-stager-app-1.0.0-SNAPSHOT.jar
```
Ref: [omar-ossim-base](../../../omar-ossim-base/docs/install-guide/omar-ossim-base/)

## JAR
[https://artifactory.ossim.io/artifactory/webapp/#/artifacts/browse/tree/General/omar-local/io/ossim/omar/apps/omar-stager-app](https://artifactory.ossim.io/artifactory/webapp/#/artifacts/browse/tree/General/omar-local/io/ossim/omar/apps/omar-stager-app)

## Configuration

You can insert any common YAML tags found at the [Common Config Settings](../../../omar-common/docs/install-guide/omar-common/#common-config-settings).  

* **quartz**
 * **jdbcStore:** This service supports background jobs using the quartz framework.  Just fix this to not use the jdbcStore.   For now the requests are not persistent.
 * **threadPool.threadCount** Quartz allows one to adjust the number of concurrent threads running.  Here we default to 4 threads.  This will allow 4 concurrent stagers to run for this service.
