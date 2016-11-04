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

import io.parallec.core.actor.message.ResponseOnSingeRequest;
import io.parallec.core.util.PcConstants;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


// TODO: Auto-generated Javadoc
/**
 * Define how to poll progress 1. how to get the job id from the 1st response.
 * 2. what is the URL template to poll progress (assuming it is a HTTP GET) 3.
 * the regex to get the progress num. Note that this progress regex is not used
 * to check if the job is completed. just for record. 4.
 * 
 * Poll API: assume the HTTP Method for poll: is GET / no body/ and with the
 * same header as the 1st reqwuest
 * 
 * @author Yuanteng (Jeff) Pei
 * 
 */
public class HttpPollerProcessor {

    /** The logger. */
    private static Logger logger = LoggerFactory
            .getLogger(HttpPollerProcessor.class);

    /** The poller type. */
    private String pollerType;

    /** The success regex. */
    private String successRegex;

    /** The failure regex. */
    private String failureRegex;

    /** The job id regex. */
    // matching pattern "status": "/status/uuid"
    private String jobIdRegex;

    /** The progress regex. */
    // optional for checking stuck
    private String progressRegex;

    /** The progress stuck timeout seconds. */
    private int progressStuckTimeoutSeconds;

    /** The max poll error. */
    private int maxPollError;

    /** The poll interval millis. */
    private long pollIntervalMillis;

    /** The poller request template. */
    private String pollerRequestTemplate;

    /** The job id place holder. */
    private String jobIdPlaceHolder;

    /**
     * default as the cronus agent poller
     * 
     * Assuming the polling API is By HTTP GET; without any postbody (for GET, no
     * post body for sure).
     */

    public HttpPollerProcessor() {
        super();

    }

    /**
     * Instantiates a new http poller processor.
     *
     * @param pollerType
     *            the poller type
     * @param successRegex
     *            the success regex
     * @param failureRegex
     *            the failure regex
     * @param uuidRegex
     *            the uuid regex
     * @param progressRegex
     *            the progress regex
     * @param progressStuckTimeoutSeconds
     *            the progress stuck timeout seconds
     * @param pollIntervalMillis
     *            the poll interval millis
     * @param pollerRequestTemplate
     *            the poller request template
     * @param jobIdPlaceHolder
     *            the job id place holder
     * @param maxPollError
     *            the max poll error
     */
    public HttpPollerProcessor(String pollerType, String successRegex,
            String failureRegex, String uuidRegex, String progressRegex,
            int progressStuckTimeoutSeconds, long pollIntervalMillis,
            String pollerRequestTemplate, String jobIdPlaceHolder,
            int maxPollError) {
        super();
        this.pollerType = pollerType;
        this.successRegex = successRegex;
        this.failureRegex = failureRegex;
        this.setJobIdRegex(uuidRegex);
        this.progressRegex = progressRegex;
        this.progressStuckTimeoutSeconds = progressStuckTimeoutSeconds;
        this.pollIntervalMillis = pollIntervalMillis;
        this.pollerRequestTemplate = pollerRequestTemplate;
        this.jobIdPlaceHolder = jobIdPlaceHolder;
        this.setMaxPollError(maxPollError);
    }

    /**
     * Important. get the poller URL
     *
     * @param uuid
     *            the uuid
     * @return the poller request url
     */
    public String getPollerRequestUrl(String uuid) {
        return pollerRequestTemplate.replace(jobIdPlaceHolder, uuid);
    }

    /**
     * Gets the poll interval millis.
     *
     * @return the poll interval millis
     */
    public long getPollIntervalMillis() {
        return pollIntervalMillis;
    }

    /**
     * Sets the poll interval millis.
     *
     * @param pollIntervalMillis
     *            the new poll interval millis
     */
    public void setPollIntervalMillis(long pollIntervalMillis) {
        this.pollIntervalMillis = pollIntervalMillis;
    }

    /**
     * Gets the poller request template.
     *
     * @return the poller request template
     */
    public String getPollerRequestTemplate() {
        return pollerRequestTemplate;
    }

    /**
     * Sets the poller request template.
     *
     * @param pollerRequestTemplate
     *            the new poller request template
     */
    public void setPollerRequestTemplate(String pollerRequestTemplate) {
        this.pollerRequestTemplate = pollerRequestTemplate;
    }

    /**
     * Gets the uuid from response.
     *
     * @param myResponse
     *            the my response
     * @return the uuid from response
     */
    public String getUuidFromResponse(ResponseOnSingeRequest myResponse) {

        String uuid = PcConstants.NA;
        String responseBody = myResponse.getResponseBody();
        Pattern regex = Pattern.compile(getJobIdRegex());
        Matcher matcher = regex.matcher(responseBody);
        if (matcher.matches()) {
            uuid = matcher.group(1);
        }

        return uuid;
    }

    /**
     * Gets the progress from response.
     *
     * @param myResponse
     *            the my response
     * @return the progress from response
     */
    public double getProgressFromResponse(ResponseOnSingeRequest myResponse) {

        double progress = 0.0;
        try {

            if (myResponse == null || myResponse.isFailObtainResponse()) {
                return progress;
            }

            String responseBody = myResponse.getResponseBody();
            Pattern regex = Pattern.compile(progressRegex);
            Matcher matcher = regex.matcher(responseBody);
            if (matcher.matches()) {
                String progressStr = matcher.group(1);
                progress = Double.parseDouble(progressStr);
            }

        } catch (Exception t) {
            logger.error("fail " + t);

        }

        return progress;
    }

    /**
     * If there is error in response.
     *
     * @param myResponse
     *            the my response
     * @return true, if successful
     */
    public boolean ifThereIsErrorInResponse(ResponseOnSingeRequest myResponse) {

        return myResponse == null ? true : myResponse.isFailObtainResponse();
    }

    /**
     * If task completed success or failure from response.
     *
     * @param myResponse
     *            the my response
     * @return true, if successful
     */
    public boolean ifTaskCompletedSuccessOrFailureFromResponse(
            ResponseOnSingeRequest myResponse) {

        boolean isCompleted = false;
        try {

            if (myResponse == null || myResponse.isFailObtainResponse()) {
                return isCompleted;
            }

            String responseBody = myResponse.getResponseBody();
            if (responseBody.matches(successRegex)
                    || responseBody.matches(failureRegex)) {
                isCompleted = true;
            }

        } catch (Exception t) {
            logger.error("fail " + t);

        }
        return isCompleted;
    }

    /**
     * Gets the poller type.
     *
     * @return the poller type
     */
    public String getPollerType() {
        return pollerType;
    }

    /**
     * Sets the poller type.
     *
     * @param pollerType
     *            the new poller type
     */
    public void setPollerType(String pollerType) {
        this.pollerType = pollerType;
    }

    /**
     * Gets the failure regex.
     *
     * @return the failure regex
     */
    public String getFailureRegex() {
        return failureRegex;
    }

    /**
     * Sets the failure regex.
     *
     * @param failureRegex
     *            the new failure regex
     */
    public void setFailureRegex(String failureRegex) {
        this.failureRegex = failureRegex;
    }

    /**
     * Gets the progress regex.
     *
     * @return the progress regex
     */
    public String getProgressRegex() {
        return progressRegex;
    }

    /**
     * Sets the progress regex.
     *
     * @param progressRegex
     *            the new progress regex
     */
    public void setProgressRegex(String progressRegex) {
        this.progressRegex = progressRegex;
    }

    /**
     * Gets the progress stuck timeout seconds.
     *
     * @return the progress stuck timeout seconds
     */
    public int getProgressStuckTimeoutSeconds() {
        return progressStuckTimeoutSeconds;
    }

    /**
     * Sets the progress stuck timeout seconds.
     *
     * @param progressStuckTimeoutSeconds
     *            the new progress stuck timeout seconds
     */
    public void setProgressStuckTimeoutSeconds(int progressStuckTimeoutSeconds) {
        this.progressStuckTimeoutSeconds = progressStuckTimeoutSeconds;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "HttpPollerProcessor [pollerType=" + pollerType
                + ", successRegex=" + successRegex + ", failureRegex="
                + failureRegex + ", uuidRegex=" + getJobIdRegex()
                + ", progressRegex=" + progressRegex
                + ", progressStuckTimeoutSeconds="
                + progressStuckTimeoutSeconds + ", pollIntervalMillis="
                + pollIntervalMillis + ", pollerRequestTemplate="
                + pollerRequestTemplate + ", jobIdPlaceHolder="
                + jobIdPlaceHolder + "]";
    }

    /**
     * Gets the job id place holder.
     *
     * @return the job id place holder
     */
    public String getJobIdPlaceHolder() {
        return jobIdPlaceHolder;
    }

    /**
     * Sets the job id place holder.
     *
     * @param jobIdPlaceHolder
     *            the new job id place holder
     */
    public void setJobIdPlaceHolder(String jobIdPlaceHolder) {
        this.jobIdPlaceHolder = jobIdPlaceHolder;
    }

    /**
     * Gets the job id regex.
     *
     * @return the job id regex
     */
    public String getJobIdRegex() {
        return jobIdRegex;
    }

    /**
     * Sets the job id regex.
     *
     * @param jobIdRegex
     *            the new job id regex
     */
    public void setJobIdRegex(String jobIdRegex) {
        this.jobIdRegex = jobIdRegex;
    }

    /**
     * Gets the max poll error.
     *
     * @return the max poll error
     */
    public int getMaxPollError() {
        return maxPollError;
    }

    /**
     * Sets the max poll error.
     *
     * @param maxPollError
     *            the new max poll error
     */
    public void setMaxPollError(int maxPollError) {
        this.maxPollError = maxPollError;
    }

    /**
     * Gets the success regex.
     *
     * @return the success regex
     */
    public String getSuccessRegex() {
        return successRegex;
    }

    /**
     * Sets the success regex.
     *
     * @param successRegex the new success regex
     */
    public void setSuccessRegex(String successRegex) {
        this.successRegex = successRegex;
    }
    

}
