# OMAR Stager

## Configuration

You can insert any common YAML tags found at the [Common Config Settings](../../../omar-common/docs/install-guide/omar-common/#common-config-settings).  

* **quartz**
 * **jdbcStore:** This service supports background jobs using the quartz framework.  Just fix this to not use the jdbcStore.   For now the requests are not persistent.
 * **threadPool.threadCount** Quartz allows one to adjust the number of concurrent threads running.  Here we default to 4 threads.  This will allow 4 concurrent stagers to run for this service.
