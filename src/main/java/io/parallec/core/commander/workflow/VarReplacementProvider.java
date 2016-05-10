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
import io.parallec.core.bean.StrStrMap;
import io.parallec.core.util.PcConstants;
import io.parallec.core.util.PcDateUtils;

import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Deal with WISB Var generator and get values
 * 
 * extending from replacing only WISB based. now to also API based.
 * 
 * @author Yuanteng (Jeff) Pei
 * 
 */
public class VarReplacementProvider {

    /** The logger. */
    private static Logger logger = LoggerFactory
            .getLogger(VarReplacementProvider.class);

    /** The Constant instance. */
    private static final VarReplacementProvider instance = new VarReplacementProvider();

    /**
     * Gets the single instance of VarReplacementProvider.
     *
     * @return single instance of VarReplacementProvider
     */
    public static VarReplacementProvider getInstance() {
        return instance;
    }

    /**
     * Instantiates a new var replacement provider.
     */
    private VarReplacementProvider() {

    }
    
    /**
     * 
     * 20130916: add node specific replacement Var Map; replacementVarMap VS.
     * replacementVarMap
     * 
     * replacementVarMap: is for the *uniform * var replacement identical to all
     * nodes replacementVarMapNodeSpecific: is for node specific var
     * replacement; e.g. each node wants a diff id, hwpath etc
     *
     * @param task
     *            the parallel task
     * @param useReplacementVarMap
     *            boolean: use uniform replacement map
     * @param replacementVarMap
     *            the replacement variable map
     * @param useReplacementVarMapNodeSpecific
     *            boolean: use target host specific replacement map
     * @param replacementVarMapNodeSpecific
     *            the replacement variable map node specific
     */
    public void updateRequestWithReplacement(
            ParallelTask task, boolean useReplacementVarMap,
            Map<String, String> replacementVarMap,
            boolean useReplacementVarMapNodeSpecific,
            Map<String, StrStrMap> replacementVarMapNodeSpecific) {

        try {

            /**
             * 
             * ENABLE CRETIRIA: useReplacementVarMap==true
             * 
             */
            if (useReplacementVarMap && replacementVarMap != null) {

                for (Entry<String, String> entry : replacementVarMap.entrySet()) {

                    String replaceVarKey = entry.getKey();
                    String replaceVarValue = entry.getValue();

                    VarReplacementProvider.getInstance()
                            .updateRequestByAddingReplaceVarPair(task,
                                    replaceVarKey, replaceVarValue);

                }// end for loop

            }// end if

            /**
             * ENABLE CRETIRIA: useReplacementVarMap==true
             */
            if (useReplacementVarMapNodeSpecific 
                    && replacementVarMapNodeSpecific != null) {
                VarReplacementProvider.getInstance()
                        .updateRequestByAddingReplaceVarPairNodeSpecific(task,
                                replacementVarMapNodeSpecific);
            }// end if

        } catch (Exception e) {
            logger.error(" exception updateRequestWithReplacement ", e);
        }

    }// end func.
    
    

    /**
     * GENERIC!!! HELPER FUNCION FOR REPLACEMENT
     * 
     * update the var: DYNAMIC REPLACEMENT of VAR.
     * 
     * Every task must have matching command data and task result
     *
     * @param task
     *            the task
     * @param replaceVarKey
     *            the replace var key
     * @param replaceVarValue
     *            the replace var value
     */
    public void updateRequestByAddingReplaceVarPair(
            ParallelTask task, String replaceVarKey, String replaceVarValue) {

        Map<String, NodeReqResponse> taskResult = task.getParallelTaskResult();

        for (Entry<String, NodeReqResponse> entry : taskResult.entrySet()) {
            NodeReqResponse nodeReqResponse = entry.getValue();

            nodeReqResponse.getRequestParameters()
                    .put(PcConstants.NODE_REQUEST_PREFIX_REPLACE_VAR
                            + replaceVarKey, replaceVarValue);
            nodeReqResponse.getRequestParameters().put(
                    PcConstants.NODE_REQUEST_WILL_EXECUTE,
                    Boolean.toString(true));

        }// end for loop

    }// end func

    /**
     * 
     * Will change replacementVarMapNodeSpecific according to each node
     * specifically
     * 
     * With KEY set as NA; will not run the command ONLY if the NA is
     * the last replacement; note in this logic; when it is not NA; will set AS
     * True.
     *
     * @param task
     *            the task
     * @param replacementVarMapNodeSpecific
     *            the replacement var map node specific
     */
    public void updateRequestByAddingReplaceVarPairNodeSpecific(
            ParallelTask task,
            Map<String, StrStrMap> replacementVarMapNodeSpecific

    ) {

        Map<String, NodeReqResponse> taskResult = task.getParallelTaskResult();

        for (Entry<String, NodeReqResponse> entry : taskResult.entrySet()) {

            String fqdn = entry.getKey();
            StrStrMap replacementVarMapForThisNode = replacementVarMapNodeSpecific
                    .get(fqdn);

            if (replacementVarMapForThisNode == null) {
                logger.info("replacementVarMapForThisNode is null in "
                        + " for host "
                        + fqdn );
                continue;
            }

            for (Entry<String, String> entryReplaceMap : replacementVarMapForThisNode
                    .getMap().entrySet()) {

                String replaceVarKey = entryReplaceMap.getKey();
                String replaceVarValue = entryReplaceMap.getValue();

                NodeReqResponse nodeReqResponse = entry.getValue();

                // Safeguard!! When the wisbVarValue is "NA" (e.g. fail to
                // get
                // the wisb) should alert that
                // Safeguard: if NA, then dont run it!
                if (replaceVarKey.equalsIgnoreCase(PcConstants.NA)) {

                    logger.info("Replace NA means to disable this item."
                            + PcDateUtils.getNowDateTimeStrStandard());

                    // 20130731: add error msg
                    nodeReqResponse
                            .getRequestParameters()
                            .put(PcConstants.NODE_REQUEST_EXECUTE_MSG,
                                    PcConstants.NODE_REQUEST_EXECUTE_MSG_DETAIL_REPLACEMENT_VAR_VALUE_NA);

                    nodeReqResponse.getRequestParameters().put(
                            PcConstants.NODE_REQUEST_WILL_EXECUTE,
                            Boolean.toString(false));

                    /**
                     * 20130828: make it generic to check NULL KEY/VALUE
                     */
                } else {
                    nodeReqResponse.getRequestParameters().put(
                            PcConstants.NODE_REQUEST_PREFIX_REPLACE_VAR
                                    + replaceVarKey, replaceVarValue);

                    // CAREFUL! This is added to prevent a last time run
                    // "NOT EXECUTE" to continue to be effective this time.
                    // Since the whole nodeReqResponse will not replaced
                    // everytime

                    /*
                     * 20131205: to prevent this overwrite when there is a NA
                     * field passed in. This will check
                     */
                    if (replacementVarMapForThisNode.getMap().keySet()
                            .contains(PcConstants.NA)) {
                        nodeReqResponse.getRequestParameters().put(
                                PcConstants.NODE_REQUEST_WILL_EXECUTE,
                                Boolean.toString(false));
                    } else {
                        nodeReqResponse.getRequestParameters().put(
                                PcConstants.NODE_REQUEST_WILL_EXECUTE,
                                Boolean.toString(true));
                    }

                }

            }// end loop thru entryReplaceMap

        }// end for loop of nodeData

    }// end func
}
