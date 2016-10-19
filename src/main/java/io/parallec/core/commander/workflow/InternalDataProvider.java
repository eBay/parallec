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
package io.parallec.core.commander.workflow;

import io.parallec.core.ParallelTask;
import io.parallec.core.actor.message.NodeReqResponse;
import io.parallec.core.bean.HttpMeta;
import io.parallec.core.bean.TargetHostMeta;
import io.parallec.core.util.PcConstants;
import io.parallec.core.util.PcDateUtils;

import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * generate the node data; make sure it is not null.
 * 
 * @author Yuanteng (Jeff) Pei
 * 
 */
public class InternalDataProvider {

    /** The logger. */
    private static Logger logger = LoggerFactory
            .getLogger(InternalDataProvider.class);

    /** The Constant instance. */
    private static final InternalDataProvider instance = new InternalDataProvider();

    /**
     * Gets the single instance of InternalDataProvider.
     *
     * @return single instance of InternalDataProvider
     */
    public static InternalDataProvider getInstance() {
        return instance;
    }

    /**
     * Instantiates a new internal data provider.
     */
    private InternalDataProvider() {
    }

    /**
     * Generate node data map.
     *
     * @param task
     *            the job info
     */
    public void genNodeDataMap(ParallelTask task) {

        TargetHostMeta targetHostMeta = task.getTargetHostMeta();
        HttpMeta httpMeta = task.getHttpMeta();

        String entityBody = httpMeta.getEntityBody();
        String requestContent = HttpMeta
                .replaceDefaultFullRequestContent(entityBody);

        Map<String, NodeReqResponse> parallelTaskResult = task
                .getParallelTaskResult();
        for (String fqdn : targetHostMeta.getHosts()) {
            NodeReqResponse nodeReqResponse = new NodeReqResponse(fqdn);
            nodeReqResponse.setDefaultReqestContent(requestContent);
            parallelTaskResult.put(fqdn, nodeReqResponse);
        }
    }// end func.

    /**
     * Filter unsafe or unnecessary request.
     *
     * @param nodeDataMapValidSource
     *            the node data map valid source
     * @param nodeDataMapValidSafe
     *            the node data map valid safe
     */
    public void filterUnsafeOrUnnecessaryRequest(
            Map<String, NodeReqResponse> nodeDataMapValidSource,
            Map<String, NodeReqResponse> nodeDataMapValidSafe) {

        for (Entry<String, NodeReqResponse> entry : nodeDataMapValidSource
                .entrySet()) {

            String hostName = entry.getKey();
            NodeReqResponse nrr = entry.getValue();

            Map<String, String> map = nrr.getRequestParameters();

            /**
             * 20130507: will generally apply to all requests: if have this
             * field and this field is false
             */
            if (map.containsKey(PcConstants.NODE_REQUEST_WILL_EXECUTE)) {
                Boolean willExecute = Boolean.parseBoolean(map
                        .get(PcConstants.NODE_REQUEST_WILL_EXECUTE));

                if (!willExecute) {
                    logger.info("NOT_EXECUTE_COMMAND " + " on target: "
                            + hostName + " at "
                            + PcDateUtils.getNowDateTimeStrStandard());
                    continue;
                }
            }

            // now safely to add this node in.
            nodeDataMapValidSafe.put(hostName, nrr);
        }// end for loop

    }

}
