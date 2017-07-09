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
package io.parallec.core;

import com.ning.http.client.AsyncHttpClient;
import io.parallec.core.actor.ActorConfig;
import io.parallec.core.monitor.MonitorProvider;
import io.parallec.core.resources.HttpClientStore;
import io.parallec.core.resources.HttpClientType;
import io.parallec.core.resources.HttpMethod;
import io.parallec.core.resources.TcpUdpSshPingResourceStore;
import io.parallec.core.task.ParallelTaskManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 
 * This is the starting point of starting a parallel client, which create a
 * {@link ParallelTaskBuilder} after a prepareGet functions. The Parallel
 * Task builder is to build a parallel task. 
 * 
 * This is more a wrapper class and does not hold object specific resources. You
 * may create a single or multiple instances.
 * 
 * Only call {@link ParallelClient#releaseExternalResources()} before your app
 * shutdown and when you no longer need to use it. Will mark as closed after it is closed
 * {@link ParallelClient#isClosed}
 * 
 * Will auto reinitialize if is closed.
 * 
 * <h3>Maven Import</h3>
 * 
 * <pre>
 * <code class="xml">    &lt;dependency&gt;
 *         &lt;groupId&gt;io.parallec&lt;/groupId&gt;
 *         &lt;artifactId&gt;parallec-core&lt;/artifactId&gt;
 *         &lt;version&gt;0.9.0&lt;/version&gt;
 *     &lt;/dependency&gt;
 * </code>
 * </pre>
 * 
 * <h3 id="maven-import">Code Example</h3>
 * 
 * <p>
 * Here is a complete runnable code.
 * </p>
 * 
 * <pre>
 * <code class="java">import io.parallec.core.*;
 * import java.util.Map;
 * 
 * ParallelClient pc= new ParallelClient();
 * pc.prepareHttpGet(&quot;/validateInternals.html&quot;)
 *     .setConcurrency(1000)
 *     .setTargetHostsFromString(&quot;www.parallec.io www.jeffpei.com www.restcommander.com&quot;)
 *     .execute(new ParallecResponseHandler() {
 *         public void onCompleted(ResponseOnSingleTask res,
 *                 Map&lt;String, Object&gt; responseContext) {
 *             String cpu = new FilterRegex(&quot;.*&lt;td&gt;CPU-Usage-Percent&lt;/td&gt;\\s*&lt;td&gt;(.*?)&lt;/td&gt;.*&quot;)
 *             .filter(res.getResponseContent());
 *             System.out.println(&quot;cpu:&quot; + cpu + &quot; host: &quot; + res.getHost() );
 *         }
 *     });
 * pc.releaseExternalResources();
 * </code>
 * </pre>
 * 
 * @author Yuanteng (Jeff) Pei
 */

public class ParallelClient {

    /** The logger. must move this logger to the very front. */
    private static Logger logger = LoggerFactory
            .getLogger(ParallelClient.class);

    /** The task manager. */
    public ParallelTaskManager taskManager = ParallelTaskManager.getInstance();

    /** The http client store. */
    public HttpClientStore httpClientStore = HttpClientStore.getInstance();

    /** The tcp client store. */
    public TcpUdpSshPingResourceStore tcpSshPingResourceStore = TcpUdpSshPingResourceStore.getInstance();

    /** The is closed is marked when all resources are released/not initialized. */
    public static AtomicBoolean isClosed = new AtomicBoolean(true);

    /**
     * Instantiates a new parallel client. call initialize();
     */
    public ParallelClient() {
        initialize();
    }

    /**
     * Initialize. create the httpClientStore, tcpClientStore
     */
    public void initialize() {
        if (isClosed.get()) {
            logger.info("Initialing Parallel Client Resources: actor system, HttpClientStore, Task Manager ....");
            ActorConfig.createAndGetActorSystem();
            httpClientStore.init();
            tcpSshPingResourceStore.init();
            isClosed.set(false);
            logger.info("Parallel Client Resources has been initialized.");
        } else {
            logger.debug("NO OP. Parallel Client Resources has already been initialized.");
        }
    }

    /**
     * Releases the external resources that this object depends on. You should
     * not call this method if you still want to use the external resources
     * (e.g. akka system, async http client store, thread pool for
     * SSH/TCP) are in use by other objects.
     * 
     * 
     * 
     */

    public void releaseExternalResources() {

        if (!isClosed.get()) {
            logger.info("Releasing all ParallelClient resources... ");
            ActorConfig.shutDownActorSystemForce();
            httpClientStore.shutdown();
            tcpSshPingResourceStore.shutdown();
            cleanWaitTaskQueue();
            cleanInprogressJobMap();
            isClosed.set(true);
            logger.info("Have released all ParallelClient resources "
                    + "(actor system + async+sync http client + task queue)"
                    + "\nNow safe to stop your application.");

        } else {
            logger.debug("NO OP. ParallelClient resources have already been released.");
        }

    }

    /**
     * Auto re-initialize external resourced 
     * if resources have been already released.
     */
    public void reinitIfClosed() {
        if (isClosed.get()) {
            logger.info("External Resource was released. Now Re-initializing resources ...");

            ActorConfig.createAndGetActorSystem();
            httpClientStore.reinit();
            tcpSshPingResourceStore.reinit();
            try {
                Thread.sleep(1000l);
            } catch (InterruptedException e) {
                logger.error("error reinit httpClientStore", e);
            }
            isClosed.set(false);
            logger.info("Parallel Client Resources has been reinitialized.");
        } else {
            logger.debug("NO OP. Resource was not released.");
        }
    }

    /**
     * Prepare a parallel SSH Task.
     *
     * @return the parallel task builder
     */
    public ParallelTaskBuilder prepareSsh() {
        reinitIfClosed();
        ParallelTaskBuilder cb = new ParallelTaskBuilder();
        cb.setProtocol(RequestProtocol.SSH);
        return cb;
    }

    /**
     * Prepare a parallel PING Task.
     *
     * @return the parallel task builder
     */
    public ParallelTaskBuilder preparePing() {
        reinitIfClosed();
        ParallelTaskBuilder cb = new ParallelTaskBuilder();
        cb.setProtocol(RequestProtocol.PING);
        return cb;
    }

    /**
     * Prepare a parallel TCP Task.
     *
     * @param command
     *            the command
     * @return the parallel task builder
     */
    public ParallelTaskBuilder prepareTcp(String command) {
        reinitIfClosed();
        ParallelTaskBuilder cb = new ParallelTaskBuilder();
        cb.setProtocol(RequestProtocol.TCP);
        cb.getTcpMeta().setCommand(command);
        return cb;
    }
    
    /**
     * Prepare a parallel UDP Task.
     *
     * @param command
     *            the command
     * @return the parallel task builder
     */
    public ParallelTaskBuilder prepareUdp(String command) {
        reinitIfClosed();
        ParallelTaskBuilder cb = new ParallelTaskBuilder();
        cb.setProtocol(RequestProtocol.UDP);
        cb.getUdpMeta().setCommand(command);
        return cb;
    }

    /**
     * Prepare a parallel HTTP GET Task.
     *
     * @param url
     *            the UrlPostfix: e.g. in http://localhost:8080/index.html.,the url is "/index.html"
     * @return the parallel task builder
     */
    public ParallelTaskBuilder prepareHttpGet(String url) {
        reinitIfClosed();
        ParallelTaskBuilder cb = new ParallelTaskBuilder();
        
        cb.getHttpMeta().setHttpMethod(HttpMethod.GET);
        cb.getHttpMeta().setRequestUrlPostfix(url);
        
        return cb;
    }

    /**
     * Prepare a parallel HTTP POST Task.
     *
     * @param url
     *            the UrlPostfix: e.g. in http://localhost:8080/index.html.,the url is "/index.html"
     * @return the parallel task builder
     */
    public ParallelTaskBuilder prepareHttpPost(String url) {
        reinitIfClosed();
        ParallelTaskBuilder cb = new ParallelTaskBuilder();
        cb.getHttpMeta().setHttpMethod(HttpMethod.POST);
        cb.getHttpMeta().setRequestUrlPostfix(url);
        return cb;
    }

    /**
     * Prepare a parallel HTTP DELETE Task.
     *
     * @param url
     *            the UrlPostfix: e.g. in http://localhost:8080/index.html.,the url is "/index.html"
     * @return the parallel task builder
     */
    public ParallelTaskBuilder prepareHttpDelete(String url) {
        reinitIfClosed();
        ParallelTaskBuilder cb = new ParallelTaskBuilder();

        cb.getHttpMeta().setHttpMethod(HttpMethod.DELETE);
        cb.getHttpMeta().setRequestUrlPostfix(url);
        return cb;
    }

    /**
     * Prepare a parallel HTTP PUT Task.
     *
     * @param url
     *            the UrlPostfix: e.g. in http://localhost:8080/index.html.,the url is "/index.html"
     * @return the parallel task builder
     */
    public ParallelTaskBuilder prepareHttpPut(String url) {
        reinitIfClosed();
        ParallelTaskBuilder cb = new ParallelTaskBuilder();
        cb.getHttpMeta().setHttpMethod(HttpMethod.PUT);
        cb.getHttpMeta().setRequestUrlPostfix(url);
        return cb;

    }

    /**
     * Prepare a parallel HTTP HEAD Task.
     *
     * @param url
     *            the UrlPostfix: e.g. in http://localhost:8080/index.html.,the url is "/index.html"
     * @return the parallel task builder
     */
    public ParallelTaskBuilder prepareHttpHead(String url) {
        reinitIfClosed();
        ParallelTaskBuilder cb = new ParallelTaskBuilder();
        cb.getHttpMeta().setHttpMethod(HttpMethod.HEAD);
        cb.getHttpMeta().setRequestUrlPostfix(url);
        return cb;
    }

    /**
     * Prepare a parallel HTTP OPTION Task.
     *
     * @param url
     *            the UrlPostfix: e.g. in http://localhost:8080/index.html.,the url is "/index.html"
     * @return the parallel task builder
     */
    public ParallelTaskBuilder prepareHttpOptions(String url) {
        reinitIfClosed();
        ParallelTaskBuilder cb = new ParallelTaskBuilder();
        cb.getHttpMeta().setHttpMethod(HttpMethod.OPTIONS);
        cb.getHttpMeta().setRequestUrlPostfix(url);
        return cb;

    }

    /**
     * Sets the custom fast client in the httpClientStore.
     *
     * @param client
     *            the new custom client fast
     */
    public void setCustomClientFast(AsyncHttpClient client) {
        httpClientStore.setCustomClientFast(client);
    }

    /**
     * Sets the custom fast client in the httpClientStore.
     *
     * @param client
     *            the new custom client slow
     */
    public void setCustomClientSlow(AsyncHttpClient client) {
        httpClientStore.setCustomClientSlow(client);
    }

    /**
     * Sets the default client type that the httpClientStore.
     *
     * @param type
     *            the new http client type current default
     */
    public void setHttpClientTypeCurrentDefault(HttpClientType type) {
        httpClientStore.setHttpClientTypeCurrentDefault(type);
    }

    /**
     * Reinit http clients.
     */
    public void reinitHttpClients() {
        httpClientStore.reinit();
    }

    /**
     * Clean inprogress job map.
     */
    public void cleanInprogressJobMap() {
        taskManager.cleanInprogressJobMap();
    }

    /**
     * Clean wait task queue.
     */
    public void cleanWaitTaskQueue() {
        taskManager.cleanWaitTaskQueue();
    }

    /**
     * Gets the http client store.
     *
     * @return the http client store
     */
    public HttpClientStore getHttpClientStore() {
        return httpClientStore;
    }

    /**
     * Log memory health.
     * 
     * {@link MonitorProvider}} class (singleton) provider JVM memory, thread information.
     * 
     * You may directly call MonitorProvider.getInstance() to access JVM memory / thread objects
     *
     * @return the string
     */
    public String logHealth() {
        return MonitorProvider.getInstance().getHealthMemory();
    }

    /**
     * Gets the running job count.
     *
     * @return the running job count
     */
    public int getRunningJobCount() {

        return ParallelTaskManager.getInstance().getInprogressTaskMap()
                .entrySet().size();

    }

}
