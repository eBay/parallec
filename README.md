<a href="http://www.parallec.io"><img alt="Parallec-logo" src="http://www.parallec.io/images/parallec-logo.png" width="325"></a>


![build status](https://img.shields.io/badge/build-info=>-green.svg) [![Build Status](https://travis-ci.org/eBay/parallec.svg?branch=master)](https://travis-ci.org/eBay/parallec) [![Coverage Status](https://img.shields.io/codecov/c/github/eBay/parallec.svg)](https://codecov.io/github/eBay/parallec) [![Apache V2.0 License](http://www.parallec.io/images/apache2.svg) ](https://github.com/eBay/parallec/blob/master/LICENSE)


![latest 0.10.x](http://img.shields.io/badge/latest_stable-0.10.x=>-green.svg) [ ![latest beta  maven central](https://maven-badges.herokuapp.com/maven-central/io.parallec/parallec-core/badge.svg?style=flat)](http://search.maven.org/#artifactdetails|io.parallec|parallec-core|0.10.6|) [![Gitter](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/eBay/parallec?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge)

[![Javadoc](http://www.parallec.io/images/parallec-javadoc-blue.svg)](http://www.parallec.io/javadoc/index.html?io/parallec/core/ParallelClient.html) [![Documentation](http://www.parallec.io/images/parallec-documentation-red.svg)](http://www.parallec.io/docs/) [![Samples](http://www.parallec.io/images/parallec-samples-brightgreen.svg)](https://github.com/eBay/parallec-samples) [![Chinese](http://www.parallec.io/images/parallec-cnbrief-blue.svg)](https://github.com/eBay/parallec/blob/master/README-brief-cn.md) 

[ [Get-Started](https://github.com/eBay/parallec/blob/master/README.md#get-started) | [Features](https://github.com/eBay/parallec/blob/master/README.md#features) | [Use Cases](https://github.com/eBay/parallec/blob/master/README.md#use-cases) | [Samples](https://github.com/eBay/parallec-samples) | [Change Log](https://github.com/eBay/parallec/blob/master/CHANGELOG.md) / [What's New](https://github.com/eBay/parallec/blob/master/README.md#whats-new) / [Versions](https://github.com/eBay/parallec/blob/master/README.md#versions) |  [User Group](https://github.com/eBay/parallec/blob/master/README.md#user-group) |  [Motivation](https://github.com/eBay/parallec/blob/master/README.md#motivation) | [Demos](https://github.com/eBay/parallec/blob/master/README.md#demos) | [Performance](https://github.com/eBay/parallec/blob/master/README.md#performance) | [Compare](https://github.com/eBay/parallec/blob/master/README.md#compare) | [Contributors](https://github.com/eBay/parallec/blob/master/README.md#contributors) | [About](https://github.com/eBay/parallec/blob/master/README.md#authors) | [News](http://www.parallec.io/blog/) | [Plugin](https://github.com/eBay/parallec-plugins) | [中文介绍](https://github.com/eBay/parallec/blob/master/README-brief-cn.md) ]


[ [API Overview](http://www.parallec.io/docs/api-overview/) | [Generate & Submit Task](http://www.parallec.io/docs/submit-task/) | [Track Status & Examine Responses](http://www.parallec.io/docs/track-status/) | [Configurations](http://www.parallec.io/docs/configurations/) ]

[Tweeted](https://twitter.com/jboner/status/663618652063813632) by the Creator of Akka & Featured in [ [This Week in #Scala](http://www.cakesolutions.net/teamblogs/this-week-in-scala-16/11/2015) | [OSChina](http://www.oschina.net/p/parallec) - [2015 Top 100](http://www.oschina.net/news/69808/2015-annual-ranking-top-100-new-open-source-software) ]

### Overview

Parallec is a fast parallel async HTTP(S)/SSH/TCP/UDP/Ping client java library based on [Akka](http://akka.io). Scalably aggregate and handle API responses **anyway** and send it **anywhere** by writing [20 lines](https://www.youtube.com/watch?v=QcavegPMDms) of code. A super convenient **response context** let you pass in/out any object when handling the responses. Now you can conduct scalable API calls, then effortlessly pass aggregated data anywhere to elastic search, kafka, MongoDB, graphite, memcached, etc. Flexible task level concurrency control without creating a 1,000 threads thread pool. Parallec means  **Paralle**l **C**lient (pronounced as "Para-like"). Visit [www.parallec.io](http://www.parallec.io)

**[Watch Demo](https://github.com/eBay/parallec/wiki/Parallec-Aggregates-HTTP-Responses-from-8000-Servers)**: **8,000** web server HTTP response aggregation to memory in **12** seconds / to ElasticSearch in **16** seconds.

**Aggregated error messages - Debug friendly with full visibility**: Having trouble debugging in concurrent environment? Not any more! All exceptions, timeout, stack traces, request sent and response received time are **captured and aggregated** in the [response](http://www.parallec.io/javadoc/index.html?io/parallec/core/ResponseOnSingleTask.html) map. It is available in [ParallelTask](http://www.parallec.io/javadoc/index.html?io/parallec/core/ParallelTask.html) for polling right after you execute a task asynchronously.  Multi-level (worker/manager) timeout guarantees tasks return even for 100,000s of requests.  


**[Production Use Cases](http://www.parallec.io/#testimonial-ebay)**: widely used in infrastructure software as [the polling and aggregation engine](https://medium.com/@jeffpeiyt/a-reusable-part-in-many-cloud-projects-2151f42572e9#.31x5rt6j2)

1. **Application Deployment / PaaS**: Parallec has been [integrated](http://www.parallec.io/#testimonial-ebay) in eBay main production application deployment system (PaaS). Parallec orchestrates 10+ API tasks, with each task targeting 10s to 1,000s servers over 1,000+ application pools in production. Parallec has been used with work flow engine [Winder](https://github.com/eBay/Winder) to handle work flows more complex but similar to [this one](http://stackoverflow.com/questions/39081350/parallel-ssh-with-executorservice/39671381#39671381).
2. **Data Extraction / ETL**: Parallec has been [used](http://www.parallec.io/#testimonial-ebay) by eBay Israel's web intelligence team for executing 10k-100k API parallel calls to a single 3rd party server with dramatic improved performance and reduced resources. 
3. **Network Troubleshooting via Probing**:  In eBay's network / cloud team, Parallec is instrumental to ensure an extremely low false alert rates to accurately detect switch soft failures. Parallec serves as the core polling engine in the master component to check agent healths and mark down agents to effectively and timely eliminate noises.
4. **[Agent Management / Agent Master](https://medium.com/@jeffpeiyt/a-reusable-part-in-many-cloud-projects-2151f42572e9#.31x5rt6j2)**: In eBay's site operation / tools team, Parallec serves as the core engine to manage and monitor a puppet agent/salt minion/kubernetes kubelet like agent on 100,000+ production servers to ensure scalable operations.

![Workflow Overview](http://www.parallec.io/images/parallec-flow.svg)



### Get Started
Donwload [the latest JAR](https://search.maven.org/remote_content?g=io.parallec&a=parallec-core&v=LATEST) or grab from Maven:

```xml
<dependency>
	<groupId>io.parallec</groupId>
	<artifactId>parallec-core</artifactId>
	<version>0.10.6</version>
</dependency>
```
Snapshots of the development version are available in [Sonatype's `snapshots` repository](https://oss.sonatype.org/content/repositories/snapshots/io/parallec/parallec-core/).

or Gradle:
```xml
compile 'io.parallec:parallec-core:0.10.6'
```


**6 Line Example**


In the example below,  simply changing **prepareHttpGet()** to **prepareSsh()**, **prepareTcp()**, **prepareUdp()**, **preparePing()** enables you to conduct parallel SSH/TCP/Ping. Details please refer to the [Java Doc](http://www.parallec.io/javadoc/index.html?io/parallec/core/ParallelClient.html) and [Example Code](https://github.com/ebay/parallec-samples).


```java
import io.parallec.core.*;
import java.util.Map;

ParallelClient pc = new ParallelClient(); 
pc.prepareHttpGet("").setTargetHostsFromString("www.google.com www.ebay.com www.yahoo.com")
.execute(new ParallecResponseHandler() {
    public void onCompleted(ResponseOnSingleTask res,
        Map<String, Object> responseContext) {
        System.out.println( res.toString() );  }
});
```	
**20 Line Example**

Now that you have learned the basics, check out how easy to pass an elastic search client using the convenient **response context** to aggregate data anywhere you like. You can also pass a hash map to the `responseContext`, save the processed results to the map during `onCompleted`, and use the map outside for further work. 

```java
...
import org.elasticsearch.client.Client;
import static org.elasticsearch.node.NodeBuilder.*;

ParallelClient pc = new ParallelClient();
org.elasticsearch.node.Node node = nodeBuilder().node(); //elastic client initialize
HashMap<String, Object> responseContext = new HashMap<String, Object>();
responseContext.put("Client", node.client());
pc.prepareHttpGet("")
        .setConcurrency(1000).setResponseContext(responseContext)
        .setTargetHostsFromLineByLineText("http://www.parallec.io/userdata/sample_target_hosts_top100_old.txt", HostsSourceType.URL)
        .execute( new ParallecResponseHandler() {
            public void onCompleted(ResponseOnSingleTask res,
                    Map<String, Object> responseContext) {
                Map<String, Object> metricMap = new HashMap<String, Object>();
                metricMap.put("StatusCode", res.getStatusCode().replaceAll(" ", "_"));
                metricMap.put("LastUpdated",PcDateUtils.getNowDateTimeStrStandard());
                metricMap.put("NodeGroupType", "Web100");
                Client client = (Client) responseContext.get("Client");
                client.prepareIndex("local", "parallec", res.getHost()).setSource(metricMap).execute();
            }
        });
node.close(); pc.releaseExternalResources();
```

**Different Requests to the Same Target**

Now see **how easy** to use the request template to send multiple different requests to the same target. Variable replacement is allowed in post body, url and headers. [Read more..](http://www.parallec.io/docs/submit-task/#apis-on-variable-replacement-for-heterogeneous-requests)

```java
pc.prepareHttpGet("/userdata/sample_weather_$ZIP.txt")
    .setReplaceVarMapToSingleTargetSingleVar("ZIP",
        Arrays.asList("95037","48824"), "www.parallec.io")
    .execute(new ParallecResponseHandler() {...}...
```

- [http://www.parallec.io/userdata/sample_weather_48824.txt](http://www.parallec.io/userdata/sample_weather_48824.txt)
- [http://www.parallec.io/userdata/sample_weather_95037.txt](http://www.parallec.io/userdata/sample_weather_95037.txt)


### What's New

* 06/2017 Add dynamic response encoding according to response content type.
* 09/2016 Add option to save response headers in HTTP [#24](https://github.com/eBay/parallec/issues/24).
* 08/2016 Support Parallel async UDP (via Netty) [#41](https://github.com/eBay/parallec/issues/41).
* 07/2016 Support replacing different ports in different requests. 
* 06/2016 Parallel SSH add run sudo with password for commands.

More details please check the [Change Log](https://github.com/eBay/parallec/blob/master/CHANGELOG.md).

### Versions

* The latest production-ready version is `0.10.x`, where we use in production.
* **On async-http-client 2.x** The Parallec.io version using more up-to-date `async-http-client` (currently using AHC version `2.0.15`) is `0.20.0-SNAPSHOT`. This version has passed comprehensive unit tests but has not been used yet in production. This version **requires JDK8** due to AHC 2.x and can be used with the parallec-plugins with the same version `0.20.0-SNAPSHOT`, details please check [#37](https://github.com/eBay/parallec/issues/37).


### More Readings

- [**More Examples**](https://github.com/ebay/parallec-samples#http) on setting context, send to Elastic Search / Kafka, async running, auto progress polling, track progress, TCP/SSH/Ping.  UDP example is [here](https://github.com/eBay/parallec/blob/master/src/test/java/io/parallec/core/main/udp/ParallelClientUdpBasicTest.java), with more to come. 
- [**Set Target Hosts**](http://www.parallec.io/docs/submit-task/#set-target-hosts) from list, string, line by line text, json path, from local or remote URLs.
- [**Full Documentation**](http://www.parallec.io/docs/)
- [**Javadoc**](http://www.parallec.io/javadoc/index.html?io/parallec/core/package-summary.html)
- [**Ping Demo**](https://github.com/eBay/parallec/blob/master/README.md#demos) Ping 8000 Servers within 11.1 Seconds, performance test vs. [FPing](http://fping.org/).


### User Group

* Ask a question, and keep up to date on the library development by joining the discussion group / forum: [Parallec.io Google Group](https://groups.google.com/forum/#!forum/parallec). 
* Feel free to submit a [Github Issue](https://github.com/eBay/parallec/issues/new) for any questions and suggestions too.
* Check [FAQ](http://www.parallec.io/docs/faq/).


### Use Cases

1. Scalable web server monitoring, management, and configuration push, ping check.
1. Asset / server status discovery, remote task execution in agent-less(parallel SSH) or agent based (parallel HTTP/TCP) method.
1. Scalable API aggregation and processing with flexible destination with your favorate message queue / storage / alert engine.
1. Orchestration and work flows on multiple web servers. 
1. Parallel different requests with controlled concurrency to a single server: as a parallec client for REST API enabled Database / Web Server CRUD operations. Variable replacement allowed in post body, url and headers.
1. Load testing with request template. 
1. Network monitoring with active probing via UDP/Ping etc. 



## Features<a name="features"></a>

Parallec is built on Akka actors and [Async HTTP Client](https://github.com/AsyncHttpClient/async-http-client) / [Netty](http://netty.io/) / [Jsch](http://www.jcraft.com/jsch/).  The library focuses on HTTP while also enables scalable communication over SSH/Ping/TCP.

**90%+ Test coverage** assures you always find an example of each of feature.

1. **Exceedingly intuitive** interface with builder pattern similar to that in [Async HTTP Client](https://github.com/AsyncHttpClient/async-http-client), but handles concurrency behind the scenes.
1. **Generic response handler with context**. Special response context enables total freedom and convenience of processing each response your way. Process and aggregate data **anywhere** to Kafka, Redis, Elastic Search, mongoDB, CMS and etc.  
1. **Flexible on when to invoke the handler**:  before (in worker thread) or after the aggregation (in master/manager thread).
1. **Flexible Input of target hosts**: Input target hosts from a list, string, JSON Path from local files or a remote URL
1. **Scalable and fast**, **infinitely scalable** with built-in [**Concurrency control**](http://www.ebaytechblog.com/2014/03/11/rest-commander-scalable-web-server-management-and-monitoring/#akka).
1. **Auto-progress polling** to enable task level concurrency with **Async API** for long jobs and  orchestrations.
1. **Request template** to handle non-uniform requests.
1. **Convenient single place handling success and failure cases**. Handle in a single function where you can get the response including the actual response if success; or stacktrace and error details if failures.
1. **Capacity aware task scheduler** helps you to auto queue up and fire tasks when capacity is insufficient. (e.g. submit consecutively 5 tasks each hitting 100K websites with default concurrency will result in a queue up)
1. **Fine-grained task progress tracking** helps you track the the progress each individual task status. Of a parallel task on 1000 target hosts, you may check status on any single host task, and percentage progress on how many are completed.
1. **Fine-grained task cancelation** on whole/individual request level. Of a parallel task on 1000 target hosts, you may cancel a subset of target hosts or cancel the whole parallel task anytime.
1. **Status-code-aggregation** is provided out of the box.
1. **Parallel Ping** supports both InetAddress.reachable ICMP (requires root) and Process based ping with retries.  Performance testing shows it is **2x the speed of** than best-effort tuned FPing on pinging on 1500 targets. (2.2 vs 4.5 sec)
1. **Parallel SSH** supports both key and password based login and task cancellation.
1. **Parallel TCP/UDP** supports idle timeout based channel closes.



## Motivation
- Flexible response handling and immediate processing embedded in other applications.
- Handle async APIs with auto progress polling for task level concurrency control.
- Support of other protocols, and [more](https://github.com/eBay/parallec/blob/master/README.md#features)..
- Anyone can write 20 lines to make his/her application become [REST Commander](http://www.restcommander.com/).

With the feedbacks, lessons, and improvements from the past year of internal usage and open source of **[REST Commander](http://www.restcommander.com)**, we now made the core of REST Commander as an easy to use standalone library. We added [**15+ new**](https://github.com/eBay/parallec/blob/master/README.md#compare) features, rewritten 70%+ of the code, with [**90%+ test coverage**](https://codecov.io/github/eBay/parallec) for confident usage and contribution. This time we also structure it better so that most internal development can be directly made here.


## Watch Parallec in Action<a name="demos"></a>

[**Watch Demo**](https://www.youtube.com/watch?v=QcavegPMDms"Parallec demo - Click to Watch!"): Parallec Aggregates 100 websites status to elastic search and visualized with [20 lines of code](https://github.com/eBay/parallec-samples/blob/master/sample-apps/src/main/java/io/parallec/sample/app/http/Http100WebAggregateToElasticSearchApp.java).

<a title="Click to Watch HD version in Youtube" href="https://www.youtube.com/watch?v=QcavegPMDms"><img alt="20 lines parallec to elastic search demo" src="http://www.parallec.io/demos/elastic-web100-v3.gif" /></a>

**[Watch Demo on HTTP Calls on 8000 Servers](https://github.com/eBay/parallec/wiki/Parallec-Aggregates-HTTP-Responses-from-8000-Servers)**: **8,000** web server HTTP response aggregation to memory in **12** seconds / to ElasticSearch in **16** seconds.

[**Watch Ping Demo**](https://www.youtube.com/watch?v=9m1TFuO1Mys"Parallec Ping vs FPing demo - Click to Watch!"): Parallec is **2x Speed** of best-efforted tuned [FPing](http://fping.org) with same accurate results and pings 8000 servers within 11.1 seconds, details check [here](https://github.com/eBay/parallec/wiki/Parallec-pings-8000-servers-in-11.1-seconds).

<a title="Click to Watch HD version in Youtube" href="https://www.youtube.com/watch?v=9m1TFuO1Mys"><img alt="parallec pings 8000 servers in 11.1 seconds" src="http://www.parallec.io/demos/parallec-vs-fping-v1.gif" /></a>


## Performance

Note that speed varies based on network speed, API response time, the slowest servers, timeout, and concurrency settings.

##### HTTP

We conducted remote task execution API on 3,000 servers with response aggregated to elastic search, visualized within 15 seconds, by writing 25 lines of code.

With another faster API, calls to 8,000 servers in the same datacenter with response aggregated in memory in 12 seconds. 

##### Ping

Parallec 2.2 seconds vs FPing 4.5 seconds on 1500 servers. Parallec is 2x the speed of [FPing](http://fping.org/) (after best-effort tuning : -i 1 -r 0 v3.12)  of pinging 1500 servers while getting the same ping results.  Parallec pings 8000 servers within 11.1 seconds with breeze.

As usual, don't rely on these numbers and perform your own benchmarks.

## Compare Parallec vs REST Commander vs ThreadPools+Async Client<a name="compare"></a>

- Compared with java thread pool based solution, parallec gives you worry free concurrency control without constraints on thread size. Thread pools do not fit well when need to have a concurrency of 1000 (1000 threads..)  or need a different concurrency setting for each request.
- Compared with single-threaded Node.js solutions, Parallec enables parallel computation-intensive response handling with multiple-cores. 
- Similar issues with Python's global interpreter lock, and to use multiple CPU you will need to use costly multi-process. These are more suited for I/O only but no cpu intensive response processing.  

In Parallec, you may handle response either in Worker (before aggregation: in parallel) or in Manager (after aggregation: single thread). [Read More..](http://www.parallec.io/docs/submit-task/#apis-on-response-handling)


For more related work review, please visit [here](http://www.ebaytechblog.com/2014/03/11/rest-commander-scalable-web-server-management-and-monitoring/#relatedwork).

|                                                        Features                                                       | Parallec | REST Commander | Thread Pools + Async Client |
|:---------------------------------------------------------------------------------------------------------------------:|:--------:|:--------------:|:---------------------------:|
|                               Embedded library with intuitive builder pattern interface                               |    <img alt="Parallec-logo" src="http://www.parallec.io/images/yes.png" >   |       <img alt="Parallec-logo" src="http://www.parallec.io/images/no.png" >       |              <img alt="Parallec-logo" src="http://www.parallec.io/images/no.png" >             |
|               Ready to use application with GUI wizard based request submission and response aggregation              |    <img alt="Parallec-logo" src="http://www.parallec.io/images/no.png" >    |       <img alt="Parallec-logo" src="http://www.parallec.io/images/yes.png" >      |              <img alt="Parallec-logo" src="http://www.parallec.io/images/no.png" >             |
|                                 Simple concurrency control not limited by thread size                                 |    <img alt="Parallec-logo" src="http://www.parallec.io/images/yes.png" >   |       <img alt="Parallec-logo" src="http://www.parallec.io/images/yes.png" >      |              <img alt="Parallec-logo" src="http://www.parallec.io/images/no.png" >             |
|                             Immediate response handler without waiting all response return                            |    <img alt="Parallec-logo" src="http://www.parallec.io/images/yes.png" >   |       <img alt="Parallec-logo" src="http://www.parallec.io/images/no.png" >       |             <img alt="Parallec-logo" src="http://www.parallec.io/images/yes.png" >             |
|                               Capacity aware task scheduler and global capacity control                               |    <img alt="Parallec-logo" src="http://www.parallec.io/images/yes.png" >   |       <img alt="Parallec-logo" src="http://www.parallec.io/images/no.png" >       |              <img alt="Parallec-logo" src="http://www.parallec.io/images/no.png" >             |
| Total freedom of response processing and API aggregation: Pluggable and generic response handler and response context |    <img alt="Parallec-logo" src="http://www.parallec.io/images/yes.png" >   |       <img alt="Parallec-logo" src="http://www.parallec.io/images/no.png" >       |             <img alt="Parallec-logo" src="http://www.parallec.io/images/no.png" >             |
|                                        1 line plugin to enable SSL Client auth                                        |    <img alt="Parallec-logo" src="http://www.parallec.io/images/yes.png" >   |       <img alt="Parallec-logo" src="http://www.parallec.io/images/no.png" >       |              <img alt="Parallec-logo" src="http://www.parallec.io/images/no.png" >             |
|                                                   90% Test Coverage                                                   |    <img alt="Parallec-logo" src="http://www.parallec.io/images/yes.png" >   |       <img alt="Parallec-logo" src="http://www.parallec.io/images/no.png" >       |              <img alt="Parallec-logo" src="http://www.parallec.io/images/no.png" >             |
|                     Load target hosts from CMS query, JSON Path, text, list, string from URL/local                    |    <img alt="Parallec-logo" src="http://www.parallec.io/images/yes.png" >   |       <img alt="Parallec-logo" src="http://www.parallec.io/images/no.png" >       |              <img alt="Parallec-logo" src="http://www.parallec.io/images/no.png" >             |
|                  Task level concurrency and orchestration for Async APIs: auto polling task progress                  |    <img alt="Parallec-logo" src="http://www.parallec.io/images/yes.png" >   |       <img alt="Parallec-logo" src="http://www.parallec.io/images/no.png" >       |              <img alt="Parallec-logo" src="http://www.parallec.io/images/no.png" >             |
|                                          Task level configuration on timeout and replacing Async HTTP Client                                          |    <img alt="Parallec-logo" src="http://www.parallec.io/images/yes.png" >   |       <img alt="Parallec-logo" src="http://www.parallec.io/images/no.png" >       |              <img alt="Parallec-logo" src="http://www.parallec.io/images/no.png" >             |
|                           Async and sync task control with progress polling and cancellation                          |    <img alt="Parallec-logo" src="http://www.parallec.io/images/yes.png" >   |       <img alt="Parallec-logo" src="http://www.parallec.io/images/no.png" >       |              <img alt="Parallec-logo" src="http://www.parallec.io/images/no.png" >             |
|                                Scalable Parallel SSH with password and key based login                                |    <img alt="Parallec-logo" src="http://www.parallec.io/images/yes.png" >   |       <img alt="Parallec-logo" src="http://www.parallec.io/images/no.png" >       |              <img alt="Parallec-logo" src="http://www.parallec.io/images/no.png" >             |
|                    Proven scalability and speed on 100,000+ target hosts in Production environment                    |    <img alt="Parallec-logo" src="http://www.parallec.io/images/yes.png" >   |       <img alt="Parallec-logo" src="http://www.parallec.io/images/yes.png" >      |              <img alt="Parallec-logo" src="http://www.parallec.io/images/no.png" >             |
|   Generic request template with variable replacement for sending different requests to same/different target hosts    |    <img alt="Parallec-logo" src="http://www.parallec.io/images/yes.png" >   |       <img alt="Parallec-logo" src="http://www.parallec.io/images/yes.png" >      |              <img alt="Parallec-logo" src="http://www.parallec.io/images/no.png" >             |
|   Scalable Ping with Retries    |    <img alt="Parallec-logo" src="http://www.parallec.io/images/yes.png" >   |       <img alt="Parallec-logo" src="http://www.parallec.io/images/no.png" >      |              <img alt="Parallec-logo" src="http://www.parallec.io/images/no.png" >             |
|   Scalable TCP/UDP with idle timeout    |    <img alt="Parallec-logo" src="http://www.parallec.io/images/yes.png" >   |       <img alt="Parallec-logo" src="http://www.parallec.io/images/no.png" >      |              <img alt="Parallec-logo" src="http://www.parallec.io/images/no.png" >             |
|   Flexible handler location at either worker (in parallel) or manager thread    |    <img alt="Parallec-logo" src="http://www.parallec.io/images/yes.png" >   |       <img alt="Parallec-logo" src="http://www.parallec.io/images/no.png" >      |              <img alt="Parallec-logo" src="http://www.parallec.io/images/no.png" >             |
|   Out-of-the-box two-level response aggregation on status code|    <img alt="Parallec-logo" src="http://www.parallec.io/images/yes.png" >   |       <img alt="Parallec-logo" src="http://www.parallec.io/images/yes.png" >      |              <img alt="Parallec-logo" src="http://www.parallec.io/images/no.png" >             |
|  Configurable response log trimming on intervals|    <img alt="Parallec-logo" src="http://www.parallec.io/images/yes.png" >   |       <img alt="Parallec-logo" src="http://www.parallec.io/images/no.png" >      |              <img alt="Parallec-logo" src="http://www.parallec.io/images/no.png" >             |
|  Cancel task on a list of target hosts |    <img alt="Parallec-logo" src="http://www.parallec.io/images/yes.png" >   |       <img alt="Parallec-logo" src="http://www.parallec.io/images/no.png" >      |              <img alt="Parallec-logo" src="http://www.parallec.io/images/no.png" >             |


## Plugins

#### [SSL Client Auth Plugin](https://github.com/eBay/parallec-plugins)

## [Change Log](https://github.com/eBay/parallec/blob/master/CHANGELOG.md)


## [Contributors](https://github.com/eBay/parallec/blob/master/AUTHORS.txt)

We deeply thank all contributors for their effort.

- Lukasz Kryger  [http://stackoverflow.com/users/1240557/kryger](http://stackoverflow.com/users/1240557/kryger)
- [billzwu](https://github.com/billzwu)
- [faisal-hameed](https://github.com/faisal-hameed)
- [fivesmallq](https://github.com/fivesmallq)
- [bryant1410](https://github.com/bryant1410)
- [djKooks](https://github.com/djKooks)

## Authors

Parallec is served to you by [Yuanteng (Jeff) Pei](https://www.linkedin.com/in/peiyuant) and [Teng Song](https://www.linkedin.com/pub/teng-song/49/763/713), [Cloud Infrastructure & Platform Services (CIPS)](https://helpusbuild.ebayc3.com/) at eBay Inc. (original authors)

## Credits & Acknowledgement

- We thanks our manager [Andy Santosa](https://www.linkedin.com/pub/andy-santosa/0/230/305), project manager [Marco Rotelli](https://www.linkedin.com/pub/marco-rotelli/2/25/54), [Cloud Infrastructure & Platform Services (CIPS)](https://helpusbuild.ebayc3.com/) and legal for the big support on this project and the open source effort.
- The auto-progress polling is inspired by [lightflow](https://github.com/yubin154/lightflow).
- We thank [openpojo](https://github.com/oshoukry/openpojo) and the author Osman Shoukry for his help on making the openpojo more accessible for us to use in our project.
- We thank [AsyncHttpClient](https://github.com/AsyncHttpClient/async-http-client) and Stephane Landelle for guidance.



## Contributions
<div style="text-align:right">
  <img src="https://github.com/eBay/Winder/blob/master/docs/ebaysf-open-x.png" width="150px"/>
</div>
Any helpful feedback is more than welcome. This includes feature requests, bug reports, pull requests, constructive feedback, and etc. You must agree on [this](https://github.com/eBay/parallec/blob/master/CONTRIBUTING.md) before submitting a [pull](https://github.com/eBay/parallec/pulls) request.


## Licenses

Code licensed under Apache License v2.0

© 2015-2017 eBay Software Foundation

