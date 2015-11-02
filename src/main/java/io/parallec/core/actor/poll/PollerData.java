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
package io.parallec.core.actor.poll;

import io.parallec.core.util.PcConstants;

import java.util.LinkedHashMap;
import java.util.Map;


/**
 * the currentProgress is just for display.
 * 
 * @author Yuanteng (Jeff) Pei
 *
 */
public class PollerData {

    /** The current progress. */
    private double currentProgress = 0.0;

    /** The is error. */
    private boolean isError = false;

    /** The is complete. */
    private boolean isComplete = false;

    /** The is stuck progress. */
    private boolean isStuckProgress = false;

    /** The job id. */
    private String jobId = PcConstants.NA;

    /** The uuid has been set. */
    private boolean uuidHasBeenSet = false;

    /** The metadata. */
    private String metadata;

    /** The polling history map. */
    final private Map<String, String> pollingHistoryMap = new LinkedHashMap<String, String>();

    /**
     * Instantiates a new poller data.
     */
    public PollerData() {
        currentProgress = 0.0;
        isError = false;
        isComplete = false;
        isStuckProgress = false;
        jobId = PcConstants.NA;
        uuidHasBeenSet = false;
    }

    /**
     * Gets the current progress.
     *
     * @return the current progress
     */
    public double getCurrentProgress() {
        return currentProgress;
    }

    /**
     * Sets the current progress.
     *
     * @param currentProgress
     *            the new current progress
     */
    public void setCurrentProgress(double currentProgress) {
        this.currentProgress = currentProgress;
    }

    /**
     * Checks if is error.
     *
     * @return true, if is error
     */
    public boolean isError() {
        return isError;
    }

    /**
     * Sets the error.
     *
     * @param isError
     *            the new error
     */
    public void setError(boolean isError) {
        this.isError = isError;
    }

    /**
     * Checks if is complete.
     *
     * @return true, if is complete
     */
    public boolean isComplete() {
        return isComplete;
    }

    /**
     * Sets the complete.
     *
     * @param isComplete
     *            the new complete
     */
    public void setComplete(boolean isComplete) {
        this.isComplete = isComplete;
    }

    /**
     * Checks if is stuck progress.
     *
     * @return true, if is stuck progress
     */
    public boolean isStuckProgress() {
        return isStuckProgress;
    }

    /**
     * Sets the stuck progress.
     *
     * @param isStuckProgress
     *            the new stuck progress
     */
    public void setStuckProgress(boolean isStuckProgress) {
        this.isStuckProgress = isStuckProgress;
    }

    /**
     * Gets the job id.
     *
     * @return the job id
     */
    public String getJobId() {
        return jobId;
    }

    /**
     * also mark as set.
     *
     * @param uuid
     *            the new job id and mark has been set
     */
    public void setJobIdAndMarkHasBeenSet(String uuid) {
        this.jobId = uuid;
        this.uuidHasBeenSet = true;
    }

    /**
     * Gets the metadata.
     *
     * @return the metadata
     */
    public String getMetadata() {
        return metadata;
    }

    /**
     * Sets the metadata.
     *
     * @param metadata
     *            the new metadata
     */
    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    /**
     * Checks if is uuid has been set.
     *
     * @return true, if is uuid has been set
     */
    public boolean isUuidHasBeenSet() {
        return uuidHasBeenSet;
    }

    /**
     * Sets the uuid has been set.
     *
     * @param uuidHasBeenSet
     *            the new uuid has been set
     */
    public void setUuidHasBeenSet(boolean uuidHasBeenSet) {
        this.uuidHasBeenSet = uuidHasBeenSet;
    }

    /**
     * Sets the uuid.
     *
     * @param uuid
     *            the new uuid
     */
    public void setUuid(String uuid) {
        this.jobId = uuid;
    }

    /**
     * Gets the polling history map.
     *
     * @return the polling history map
     */
    public Map<String, String> getPollingHistoryMap() {
        return pollingHistoryMap;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "PollerData [currentProgress=" + currentProgress + ", isError="
                + isError + ", isComplete=" + isComplete + ", isStuckProgress="
                + isStuckProgress + ", uuid=" + jobId + ", uuidHasBeenSet="
                + uuidHasBeenSet + ", metadata=" + metadata
                + ", pollingHistoryMap=" + pollingHistoryMap + "]";
    }

}
