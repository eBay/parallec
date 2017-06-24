Parallec Change Log
==========

## Version 0.10.6

_2017-06-23_

* Pull [#46](https://github.com/eBay/parallec/issues/46).  Enable http response body charset encoding to be a static value of "UTF-8" from `ParallecGlobalConfig.httpResponseBodyDefaultCharset`; or dynamically derived from the response header's content type. User may overwrite the static value when overloading the config. 

## Version 0.10.5

_2017-06-18_

* Pull [#60](https://github.com/eBay/parallec/pull/60).  Fix the param setup issue in reinitializing ParallelClient, thanks to [djKooks](https://github.com/djKooks).


## Version 0.10.4

_2017-01-29_

* Issue [#56](https://github.com/eBay/parallec/issues/56).  For loading target hosts from CMS (YiDB), add support for authorization token.


## Version 0.10.3

_2016-11-03_

* Fixed the ping status check in linux with process mode.
* Improved unit tests coverage.

## Version 0.10.2

_2016-10-27_

* Issue [#48](https://github.com/eBay/parallec/issues/48). 
* Fixed not setting context in some tests. Code refactoring. 

## Version 0.10.1

_2016-10-18_

* Issue [#47](https://github.com/eBay/parallec/issues/47). Created the response with proper error msg for the host when the whole parallel task [timeouts](http://www.parallec.io/docs/configurations/#long-running-jobs), added unit tests on this case. 
* Fixed not setting context in some tests. Code refactoring. 

## Version 0.10.1-beta


_2016-09-30_

* Issue [#24](https://github.com/eBay/parallec/issues/24). Examples are [here](https://github.com/eBay/parallec/blob/master/src/test/java/io/parallec/core/main/http/ParallelClientHttpResponseHeaderTest.java). Save Response Headers into the ResponseOnSingleTask. Note that the key set provided in  esponseHeaderMeta in request can be case insensitive. However in the response headers map returned in the ResponseOnSingleTask, all keys have been **lower case for easy access**. The key line is as below to enable the save response headers.
* Erased the response headers in the ResponseOnSingleTask if `task.getConfig().isSaveResponseToTask()` is false (default).
* Minor refactored multiple places.
* Fixed javadoc.
* Fixed the test cases with HttpWorker constructor.

```java

//save all the keys. 
 .saveResponseHeaders(new ResponseHeaderMeta(null, true))
 
 
//save a subset of keys
 .saveResponseHeaders(
                        new ResponseHeaderMeta(Arrays.asList("Content-Type",
                                "server", "x-github-request-id"), false))
```                                




## Version 0.10.0


_2016-08-18_

* Issue [#41](https://github.com/eBay/parallec/issues/41). Added main feature of parallel UDP based on async Netty, with `prepareUdp(String command)` API. Use `setUdpPort()` and `setUdpIdleTimeoutSec()` to set the port and read timeout.  Added tests. 
* In udp/tcp/udp/http/ping/ssh worker, to check context before kill itself, as sometimes the msg is passed faster to the Op Worker which send msg to kill the worker already.  
* Improved test coverage. 

## Version 0.9.4-beta


_2016-07-22_

* Fix Issue [#25](https://github.com/eBay/parallec/issues/25), Enabled different ports replacements with different target hosts. Passed tests. Example check [here](https://github.com/eBay/parallec/issues/25). Key is ` .setHttpPortReplaceable("$PORT")`
* Minor log refinement for ssh sudo user. 

## Version 0.9.3


_2016-06-23_

* SSH: Allow run as super user for ssh: handle SSH commands where password is needed to sudo.  Auto apply the password. Thanks [billzwu](https://github.com/billzwu)
* Test: add coverage: for get target hosts duplicate/empty for CMS.
* Test/Build: update CI setting to enable test on process based Ping. 

## Version 0.9.2


_2015-11-27_

* Build/Maven/Logback: exclude logback.xml in the built jar, thanks to [xmpp](http://stackoverflow.com/users/5178636/xmpp) for raising the [issue](http://stackoverflow.com/questions/33897196/how-to-disable-inherited-logback/33900287#33900287), close [#21](https://github.com/eBay/parallec/issues/21).
* Test: add sleep to make sure server starts first; should fix build issue related to [#7](https://github.com/eBay/parallec/issues/7)
* Test: add coverage: updateRequestByAddingReplaceVarPairNodeSpecific() check null, add some other minor coverage
* Test: fix TcpServerThread (tcp sample server) not closed in test, thus skip idle TCP tests. 

## Version 0.9.1

_2015-11-10_

* Change: maven pom: maven-compiler-plugin version to 1.7.
* Fix: aggregateResultMap change to concurrentHashMap: Issue #11 


## Version 0.9.0

_2015-11-01_

* Add: Late initialize CapacityAwareTaskScheduler only when it is used. Added shutdown for the scheduler.
* Test: Update some tests on variable replacements.
* Doc: Update javadoc.

## Version 0.8.12-beta

_2015-10-29_

* Javadoc: refine javadoc. Fixed errors in javadoc.  
* Coverage: add code coverage.
* Maven: setup with maven central deployment. Added codecov and travis CI.


## Version 0.8.11-beta 
## Version 0.8.10 

_2015-10-27_

* Change: refactor and removed command director. Renamed command manager to execution manager. 
* Change: remove duplicated functions of prepareHttp* in builder and client classes.   Refactor internal type of http method.
* Add: ParallelTask add getAggregatedResultHumanStr() to display human readable results.
* Change: enforce safeguard of concurrency limit for SSH as 400.

## Version 0.8.9

_2015-10-25_

* Remove: Remove apache http client

## Version 0.8.8

_2015-10-24_


* Change: Refactor validation for httpMeta and move the async http client to httpMeta. Refactor Various package structures. Passed test with 93.6 coverage.


## Version 0.8.7

_2015-10-20_

* Add: Option to execute response handler in either Manager thread (after aggregation) or operation worker thread (before aggregation; in parallel). 
* Change: Refactor poller information into httpMeta for better composition.


## Version 0.8.6

_2015-10-15_

* Add: Added Parallel Ping feature. able to due 2 types. added retries and unit tests
* Add  Added Parallel TCP feature based on netty. able to handle idle connections. Added the unit testing with sample TCP server. 
* Change: added the log interval to give options to trim logs


## Version 0.8.5

_2015-10-15_

* Fix: change from single thread executor to pool executor in SSH/Ping worker to significantly reduce the thread size.
* Add: Add scalable PING with InetAddress and process based. 
* Fix: replace all tab by spaces for consistency.
* Fix: Fix when response received in operation worker, timeoutFuture is not canceled. 
* Change: remove unnecessary response from manager. Remove redundant sent 


## Version 0.8.4

_2015-10-13_

* Add: Change cancel ont target hosts from single to a list.
* Fix: Fix 3 sonar critical ones.


## Version 0.8.3

_2015-10-12_

* Add: several unit tests case for corner cases.
* Add: parallel task api to cancel task on single target. 

## Version 0.8.2

_2015-10-11b_

* Add: status code 
* Change: when not save response: will still save metadata about the task. 
* Add: map of completeness of each. add function to cancel each worker.
* Add: parallel task submit/execution start/end time and duration in seconds:view in logs.
* Fix: parallel task cancel in command manager now wait for all op workers to come back. Fix issues when the op sender is not set. Cleaned up 3 duplicated data in response. 
* Add: response map now properly show canceled single host response status; and the PTask status. 

## Version 0.8.1

_2015-10-11a_

 * Add: Add status aggregation
 * Add: Add the monitoring on memory/java thread APIs
 * Add: Sample 10K, 2K websites to hit tests
 * Fix: Fixed the auto save log not working issue
 * Fix: Removed the structure of SSH Meta when it is not SSH.
 * Enhance: Refined the logic to get error message summary.
 * Add: Add Aggregation on return status code with list and count.
 * Understand: socket connection exception. Change to google DNS will resolve the issue.
 

## Version 0.8.0

_2015-10-04_

 * Add: Add log ability and pretty print log
 * Add: Now config can be changed on each task, rather than the global level.
 * Add: Option to save the log, save the response into results, enable scheduler.
 * Fix: change the visibility of the config/parallel task.
 * Fix: reduced solar critical from 9 to 2.
 * Fix: change HTTP Store to be singleton from static functions
 * Test: Test passed JDK 1.7 and JDK 1.8.0_60