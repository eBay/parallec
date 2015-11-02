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

import io.parallec.core.ResponseOnSingleTask;
import io.parallec.core.util.PcConstants;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * To save the request parameter and the task response for this target host.
 * 
 * @author Yuanteng (Jeff) Pei
 * 
 */
public class NodeReqResponse {

    /** The logger. */
    @SuppressWarnings("unused")
    private static Logger logger = LoggerFactory
            .getLogger(NodeReqResponse.class);

    /** The request parameters. */
    private final Map<String, String> requestParameters = new LinkedHashMap<String, String>();

    /** The host name. */
    private String hostName;

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "NodeReqResponse [requestParameters=" + requestParameters
                + ", singleTaskResponse=" + singleTaskResponse + "]";
    }

    /** The single task response. */
    private ResponseOnSingleTask singleTaskResponse = null;

    /**
     * Instantiates a new node req response.
     *
     * @param hostName
     *            the host name
     */
    public NodeReqResponse(String hostName) {
        super();
        this.setHostName(hostName);
    }

    /**
     * 20130507: auto replace part.
     *
     * @param requestParameters
     *            the request parameters
     * @param sourceContent
     *            the source content
     * @return the string
     */
    public static String replaceStrByMap(Map<String, String> requestParameters,
            String sourceContent) {

        String contentAfterReplace = sourceContent;

        for (Entry<String, String> entry : requestParameters.entrySet()) {

            String sourceContentHelperNew = contentAfterReplace;
            String varName = entry.getKey();
            String replacement = entry.getValue();

            if (varName.contains(PcConstants.NODE_REQUEST_PREFIX_REPLACE_VAR)) {

                String varTrueName = "$"
                        + varName
                                .replace(
                                        PcConstants.NODE_REQUEST_PREFIX_REPLACE_VAR,
                                        "");
                sourceContentHelperNew = contentAfterReplace.replace(
                        varTrueName, replacement);
                contentAfterReplace = sourceContentHelperNew;
            }

        }

        return contentAfterReplace;
    }

    /**
     * Sets the default reqest content.
     *
     * @param requestFullContent
     *            the new default reqest content
     */
    public void setDefaultReqestContent(String requestFullContent) {

        requestParameters.put(PcConstants.REQUEST_BODY_PLACE_HOLDER,
                requestFullContent);
    }

    /**
     * Sets the default empty reqest content.
     */
    public void setDefaultEmptyReqestContent() {

        requestParameters.put(PcConstants.REQUEST_BODY_PLACE_HOLDER, "");
    }

    /**
     * Sets the custom reqest content.
     *
     * @param requestVarName
     *            the request var name
     * @param requestVarContent
     *            the request var content
     */
    public void setCustomReqestContent(String requestVarName,
            String requestVarContent) {

        requestParameters.put(requestVarName, requestVarContent);
    }

    /**
     * Gets the request parameters.
     *
     * @return the request parameters
     */
    public Map<String, String> getRequestParameters() {
        return requestParameters;
    }

    /**
     * Gets the single task response.
     *
     * @return the single task response
     */
    public ResponseOnSingleTask getSingleTaskResponse() {
        return singleTaskResponse;
    }

    /**
     * Sets the single task response.
     *
     * @param singleTaskResponse
     *            the new single task response
     */
    public void setSingleTaskResponse(ResponseOnSingleTask singleTaskResponse) {
        this.singleTaskResponse = singleTaskResponse;
    }

    /**
     * Gets the host name.
     *
     * @return the host name
     */
    public String getHostName() {
        return hostName;
    }

    /**
     * Sets the host name.
     *
     * @param hostName
     *            the new host name
     */
    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

}
