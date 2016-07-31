/*  
Copyright [2013-2015] eBay Software Foundation
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at
    http://www.apache.org/licenses/LICENSE-2.0
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */
package io.parallec.core.config;

import io.parallec.core.bean.ping.PingMode;

/**
 * this is the global system config. You can replace the values here before executing them. 
 * Different from those settings defined in ParallelTaskConfig, 
 * settings here are effective to all executions, and cannot be overwritten for a particular task.
 * 
 *  @author Yuanteng (Jeff) Pei
 */
public class ParallecGlobalConfig {

    /** The max capacity for the job scheduler. default 2500 */
    public static int maxCapacity = 2500;

    /** The concurrency for each task. default 1000 */
    public static final int concurrencyDefault = 1000;
    
    /** The max concurrency for SSH task. default 400. */
    public static final int concurrencySshLimit= 400;

    /** The scheduler check interval. default 500ms */
    public static final long schedulerCheckInterval = 500L;

    /** The scheduler init delay. default 500 ms */
    public static final long schedulerInitDelay = 500L;

    /** The ning slow client request timeout millis. */
    public static final int ningSlowClientRequestTimeoutMillis = 60000;

    /** The ning slow client connection timeout millis. */
    public static final int ningSlowClientConnectionTimeoutMillis = 15000;

    /** The ning fast client request timeout millis. */
    public static final int ningFastClientRequestTimeoutMillis = 14000;

    /** The ning fast client connection timeout millis. */
    public static final int ningFastClientConnectionTimeoutMillis = 4000;

    /** The ssh connection timeout millis default. */
    public static final int sshConnectionTimeoutMillisDefault = 5000;

    /** The ssh sleep millis btw read buffer. */
    public static final int sshSleepMIllisBtwReadBuffer = 100;

    /** The ssh buffer size. */
    public static final int sshBufferSize = 1024;
    
    /** The tcp connection timeout millis default. */
    public static final int tcpConnectTimeoutMillisDefault = 2000;
    
    /** The tcp idle timeout seconds default. */
    public static final int tcpIdleTimeoutSecDefault = 5;
    
    /** The ping timeout millis default. */
    public static final int pingTimeoutMillisDefault = 500;
    
    /** The ping mode default. */
    public static final PingMode pingModeDefault = PingMode.INET_ADDRESS_REACHABLE_NEED_ROOT;
    
    /** The ping num retries default. */
    public static final int pingNumRetriesDefault = 1;
    
    /** The Constant USERDATA_FOLDER_WITH_SLASH. */
    public static final String userDataFolderWithSlash = "userdata/";

    /** The task log folder with slash. */
    public static final String taskLogFolderWithSlash = userDataFolderWithSlash
            + "tasklogs/";

    /** The task log postfix. */
    public static final String taskLogPostfix = ".jsonlog.txt";

    /** The ssh future check interval sec. */
    public static final double sshFutureCheckIntervalSec = 0.5;
    
    /** The ping future check interval sec. */
    public static final double pingFutureCheckIntervalSec = 0.1;
    
    /** The log response interval. */
    public static final int logResponseInterval = 5;
    
    /** The log all response after percent. */
    public static final double logAllResponseAfterPercent = 95.0;
    
    /** The log all response before percent. */
    public static final double logAllResponseBeforePercent = 5.0;
    
    /** The log all response before init count. */
    public static final int logAllResponseBeforeInitCount = 2;
    
    /** The log all response if total less than. */
    public static final int logAllResponseIfTotalLessThan = 11;
    
    /** The url connection connect timeout millis. Used when load target host from URL/CMS*/
    public static final int urlConnectionConnectTimeoutMillis = 6000;
    
    /** The url connection read timeout millis. Used when load target host from URL/CMS*/
    public static final int urlConnectionReadTimeoutMillis = 15000;
}
