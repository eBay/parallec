<a href="http://www.parallec.io"><img alt="Parallec-logo" src="http://www.parallec.io/images/parallec-logo.png" width="325"></a>


![build status](https://img.shields.io/badge/build-info=>-green.svg) [![Build Status](https://travis-ci.org/eBay/parallec.svg?branch=master)](https://travis-ci.org/eBay/parallec) [![Coverage Status](https://img.shields.io/codecov/c/github/eBay/parallec.svg)](https://codecov.io/github/eBay/parallec) [![Apache V2.0 License](http://www.parallec.io/images/apache2.svg) ](https://github.com/eBay/parallec/blob/master/LICENSE)


![latest 0.9.x](http://img.shields.io/badge/latest_stable-0.9.x=>-green.svg) [ ![latest beta  maven central](https://maven-badges.herokuapp.com/maven-central/io.parallec/parallec-core/badge.svg?style=flat)](http://search.maven.org/#artifactdetails|io.parallec|parallec-core|0.9.0|) 

[![Javadoc](http://www.parallec.io/images/parallec-javadoc-blue.svg)](http://www.parallec.io/javadoc/index.html?io/parallec/core/ParallelClient.html) [![Documentation](http://www.parallec.io/images/parallec-documentation-red.svg)](http://www.parallec.io/docs/) [![Samples](http://www.parallec.io/images/parallec-samples-brightgreen.svg)](https://github.com/eBay/parallec-samples) 


Parallec is a performant parallel async http/ssh/tcp/ping client java library. Scalably aggregate and handle API responses **anyway** and send it **anywhere** by writing [20 lines](https://www.youtube.com/watch?v=QcavegPMDms) of code. Response handler with context enables you conduct scalable API calls, then pass aggregated data anywhere to elastic search, kafka, MongoDB, graphite, memcached, etc. 

Parallec means **Paralle**l **C**lient, and is pronounced as "Para-like". Parallec is built on Akka actors and Async HTTP Client / Netty / Jsch.  The library focuses on HTTP while also enables scalable communication over SSH/Ping/TCP.

![Workflow Overview](http://www.parallec.io/images/parallec-flow.svg)


####Motivation
- Flexible response handling and immediate processing embedded in other applications.
- Handle async APIs with auto progress polling for task level concurrency control.
- Support of other protocols, and [more](#features)..

With the feedbacks, lessons, and improvements from the past year of internal usage and open source of **[REST Commander](http://www.restcommander.com)**, we now made the core of REST Commander as an easy to use standalone library. We added [**15+ new**](#compare) features, rewritten 70%+ of the code, with [**90%+ test coverage**](https://codecov.io/github/eBay/parallec) for confident usage and contribution. This time we also structure it better so that most internal development can be directly made here.

###Use Cases

1. Scalable web server monitoring, management, and configuration push, ping check.
1. Asset / server status discovery, remote task execution in agent-less(parallel SSH) or agent based (parallel HTTP/TCP) method.
1. Scalable API aggregation and processing with flexible destination with your favorate message queue / storage / alert engine.
1. Orchestration and work flows on multiple web servers. 
1. Parallel different requests with controlled concurrency to a single server. 


## Features<a name="features"></a>

**90%+ Test coverage** assures you always find an example of each of feature.

1. **Exceedingly intuitive** interface with builder pattern similar to that in [Async HTTP Client](https://github.com/AsyncHttpClient/async-http-client), but handles concurrency behind the scenes.
1. **Generic response handler with context**. Enable total freedom of processing each response your way. Process and aggregate data **anywhere** to Kafka, Redis, Elastic Search, mongoDB, CMS and etc.  
1. **Flexible on when to invoke the handler**:  before (in worker thread) or after the aggregation (in master/manager thread).
1. **Flexible Input of target hosts**: Input target hosts from a list, string, JSON Path from local files or a remote URL
1. **Scalable and fast**, **infinitely scalable** with built-in **Concurrency control**.
1. **Auto-progress polling** to enable task level concurrency with **Async API** for long jobs and  orchestrations.
1. **Request template** to handle non-uniform requests.
1. **Convenient single place handling success and failure cases**. Handle in a single function where you can get the response including the actual response if success; or stacktrace and error details if failures.
1. **Capacity aware task scheduler** helps you to auto queue up and fire tasks when capacity is insufficient. (e.g. submit consecutively 5 tasks each hitting 100K websites with default concurrency will result in a queue up)
1. **Fine-grained task progress tracking** helps you track the the progress each individual task status. Of a parallel task on 1000 target hosts, you may check status on any single host task, and percentage progress on how many are completed.
1. **Fine-grained task cancelation** on whole/individual request level. Of a parallel task on 1000 target hosts, you may cancel a subset of target hosts or cancel the whole parallel task anytime.
1. **Status-code-aggregation** is provided out of the box.
1. **Parallel Ping** supports both InetAddress.reachable ICMP (requires root) and Process based ping with retries.  Performance testing shows it is ** 67% faster** than best-effort tuned FPing on pinging on 1500 targets. (2.7 vs 4.5 sec)
1. **Parallel SSH** supports both key and password based login and task cancellation.
1. **Parallel TCP** supports idle timeout based channel closes.


### Get Started

#### Maven / Gradle Import

Please replace the version with the latest version available.

######Maven

```xml
<dependency>
	<groupId>io.parallec</groupId>
	<artifactId>parallec-core</artifactId>
	<version>0.9.0</version>
</dependency>
```

######Gradle

```xml
compile 'io.parallec:parallec-core:0.9.0'
```

####Examples 

- **List of Code Examples** please check [here](https://github.com/ebay/parallec-samples#http).

In the example below,  simply changing **prepareHttpGet()** to **prepareSsh()**, **prepareTcp()**, **preparePing()** enables you to conduct parallel SSH/TCP/Ping. Details please refer to [Java Doc](http://www.parallec.io/javadoc/index.html?io/parallec/core/ParallelClient.html).

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
pc.releaseExternalResources();
```	

Here is another example with response parsing and a little more parameters.

```java
import io.parallec.core.*;
import java.util.Map;

ParallelClient pc= new ParallelClient();
pc.prepareGet("/validateInternals.html")
    .setConcurrency(1000)
    .setTargetHostsFromString("parallec.github.io www.jeffpei.com www.restcommander.com")
    .execute(new ParallecResponseHandler() {
        @Override
        public void onCompleted(ResponseOnSingleTask res,
				Map<String, Object> responseContext) {
        	String cpu = new FilterRegex(".*<td>CPU-Usage-Percent</td>\\s*<td>(.*?)</td>.*")
			.filter(res.getResponseContent());
            System.out.println("cpu:" + cpu + " host: " + res.getHost() );
        }
    });
pc.releaseExternalResources();
```	


## Watch Parallec in Action

[**Watch Demo**](https://www.youtube.com/watch?v=QcavegPMDms"Parallec demo - Click to Watch!"): Parallec Aggregates 100 websites status with 20 lines of code.

<a title="Click to Watch HD version in Youtube" href="https://www.youtube.com/watch?v=QcavegPMDms"><img alt="20 lines parallec to elastic search demo" src="http://www.parallec.io/demos/elastic-web100-v3.gif" /></a>



## Performance

Note that speed varies based on network speed, API response time, the slowest servers, and concurrency settings.

#####HTTP

We conducted remote task execution API on 3,000 servers with response aggregated to elastic search, visualized within 15 seconds, by writing 25 lines of code.

With another faster API, calls to 8,000 servers in the same datacenter with response aggregated in memory in 12 seconds. 

#####Ping
Parallec 2.7 seconds vs FPing 4.5 seconds on 1500 servers. Parallec is 67% faster than [FPing](http://fping.org/) (after best-effort tuning : -i 1 -r 0 v3.12)  of pinging 1500 servers while getting the same ping results.  While FPing consistently crashing (seg fault) when it pings 2000 or more servers,  Parallec pings 8000 servers within 11.8 seconds with breeze.

As usual, don't rely on these numbers and perform your own benchmarks.





## Compare Parallec vs REST Commander vs ThreadPools+Async Client<a name="compare"></a>


|                                                        Features                                                       | Parallec | REST Commander | Thread Pools + Async Client |
|:---------------------------------------------------------------------------------------------------------------------:|:--------:|:--------------:|:---------------------------:|
|                               Embedded library with intuitive builder pattern interface                               |    Yes   |       No       |              No             |
|               Ready to use application with GUI wizard based request submission and response aggregation              |    No    |       Yes      |              No             |
|                                 Simple concurrency control not limited by thread size                                 |    Yes   |       Yes      |              No             |
|                             Immediate response handler without waiting all response return                            |    Yes   |       No       |             Yes             |
|                               Capacity aware task scheduler and global capacity control                               |    Yes   |       No       |              No             |
| Total freedom of response processing and API aggregation: Pluggable and generic response handler and response context |    Yes   |       No       |             No*             |
|                                        1 line plugin to enable SSL Client auth                                        |    Yes   |       No       |              No             |
|                                                   90% Test Coverage                                                   |    Yes   |       No       |              No             |
|                     Load target hosts from CMS query, JSON Path, text, list, string from URL/local                    |    Yes   |       No       |              No             |
|                  Task level concurrency and orchestration for Async APIs: auto polling task progress                  |    Yes   |       No       |              No             |
|                                          Task level configuration on timeout and replacing Async HTTP Client                                          |    Yes   |       No       |              No             |
|                           Async and sync task control with progress polling and cancellation                          |    Yes   |       No       |              No             |
|                                Scalable Parallel SSH with password and key based login                                |    Yes   |       No       |              No             |
|                    Proven scalability and speed on 100,000+ target hosts in Production environment                    |    Yes   |       Yes      |              No             |
|   Generic request template with variable replacement for sending different requests to same/different target hosts    |    Yes   |       Yes      |              No             |
|   Scalable Ping with Retries    |    Yes   |       No      |              No             |
|   Scalable TCP with idle timeout    |    Yes   |       No      |              No             |
|   Flexible handler location at either worker (in parallel) or manager thread    |    Yes   |       No      |              No             |
|   Out-of-the-box two-level response aggregation on status code|    Yes   |       Yes      |              No             |
|  Configurable response log trimming on intervals|    Yes   |       No      |              No             |
|  Cancel task on a list of target hosts |    Yes   |       No      |              No             |

#### Actor Based Concurrency Control

![System Design](http://www.parallec.io/images/parallec-actors-v2.svg)

This is similar to the [model](www.ebaytechblog.com/2014/03/11/rest-commander-scalable-web-server-management-and-monitoring/#akka) in REST Commander, except that:

- The user defined response handler can be triggerred in either the manager after aggregation, or in parallel when each operation worker receives the response. 
- When handling async APIs, a single task may contains one job submission request, plus *1-n* progress polling requests.
- Worker now have more protocols such as SSH/Ping/TCP to support.


## [Plugins](https://github.com/eBay/parallec-plugins)

#### [SSL Client Auth Plugin](https://github.com/eBay/parallec-plugins)

## Authors

Parallec is served to you by [Yuanteng (Jeff) Pei](https://www.linkedin.com/in/peiyuant) and [Teng Song](https://www.linkedin.com/pub/teng-song/49/763/713), [Cloud Infrastructure & Platform Services (CIPS)](https://helpusbuild.ebayc3.com/) at eBay Inc. 

## Credits & Acknowledgement

- We thanks our manager [Andy Santosa](https://www.linkedin.com/pub/andy-santosa/0/230/305), project manager [Marco Rotelli](https://www.linkedin.com/pub/marco-rotelli/2/25/54), [Cloud Infrastructure & Platform Services (CIPS)](https://helpusbuild.ebayc3.com/) and legal for the big support on this project and the open source effort.
- The auto-progress polling is inspired by [lightflow](https://github.com/yubin154/lightflow).
- We thank [openpojo](https://github.com/oshoukry/openpojo) and the author Osman Shoukry for his help on making the openpojo more accessible for us to use in our project.
- We thank [AsyncHttpClient](https://github.com/AsyncHttpClient/async-http-client) and Stephane Landelle for guidance.



##Contributions

Any helpful feedback is more than welcome. This includes feature requests, bug reports, pull requests, constructive feedback, and etc.


## Licenses

Code licensed under Apache License v2.0

© 2015 eBay Software Foundation

