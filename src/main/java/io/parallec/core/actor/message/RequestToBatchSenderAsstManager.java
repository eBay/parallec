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
package io.parallec.core.actor.message;

import io.parallec.core.config.ParallecGlobalConfig;

import java.util.List;

import akka.actor.ActorRef;

/**
 * The Class RequestToBatchSenderAsstManager.
 */
public class RequestToBatchSenderAsstManager {

    /** The task id. */
    private final String taskId;

    /** The asst manager retry interval millis. */
    private final long asstManagerRetryIntervalMillis;

    /** The max concurrency. */
    private final int maxConcurrency;

    /**
     * The workers: list of actor ref. cannot be empty because coming from
     * */
    private final List<ActorRef> workers;

    /** The sender. */
    private final ActorRef sender;

    /** used for pojo test only. */
    public RequestToBatchSenderAsstManager() {
        super();
        this.taskId = null;
        this.asstManagerRetryIntervalMillis = 250L;
        this.workers = null;
        this.sender = null;
        this.maxConcurrency = ParallecGlobalConfig.concurrencyDefault;
    }

    /**
     * Instantiates a new request to batch sender asst manager.
     *
     * @param directorJobId            the director job id
     * @param asstManagerRetryIntervalMillis the asst manager retry interval millis
     * @param workers            the workers
     * @param sender            the sender
     * @param maxConcurrency            the max concurrency
     */
    public RequestToBatchSenderAsstManager(String directorJobId,
            long asstManagerRetryIntervalMillis, List<ActorRef> workers,
            ActorRef sender, int maxConcurrency) {
        super();
        this.taskId = directorJobId;
        this.asstManagerRetryIntervalMillis = asstManagerRetryIntervalMillis;
        this.workers = workers;
        this.sender = sender;
        this.maxConcurrency = maxConcurrency;
    }

    /**
     * Gets the max concurrency.
     *
     * @return the max concurrency
     */
    public int getMaxConcurrency() {
        return maxConcurrency;
    }

    /**
     * Gets the task id.
     *
     * @return the task id
     */
    public String getTaskId() {
        return taskId;
    }

    /**
     * Gets the workers.
     *
     * @return the workers
     */
    public List<ActorRef> getWorkers() {
        return workers;
    }

    /**
     * Gets the sender.
     *
     * @return the sender
     */
    public ActorRef getSender() {
        return sender;
    }

    /**
     * Gets the asst manager retry interval millis.
     *
     * @return the asst manager retry interval millis
     */
    public long getAsstManagerRetryIntervalMillis() {
        return asstManagerRetryIntervalMillis;
    }

}
