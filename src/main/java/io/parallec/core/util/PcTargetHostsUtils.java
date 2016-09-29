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
package io.parallec.core.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * The Class PcTargetHostsUtils.
 *
 * @author Yuanteng (Jeff) Pei
 */
public class PcTargetHostsUtils {

    /** The logger. */
    private static Logger logger = LoggerFactory
            .getLogger(PcTargetHostsUtils.class);

    /**
     * Gets the node list from string line seperate or space seperate.
     *
     * @param listStr
     *            the list str
     * @param removeDuplicate
     *            the remove duplicate
     * @return the node list from string line seperate or space seperate
     */
    public static List<String> getNodeListFromStringLineSeperateOrSpaceSeperate(
            String listStr, boolean removeDuplicate) {

        List<String> nodes = new ArrayList<String>();

        for (String token : listStr.split("[\\r?\\n| +]+")) {

            // 20131025: fix if fqdn has space in the end.
            if (token != null && !token.trim().isEmpty()) {
                nodes.add(token.trim());

            }
        }

        if (removeDuplicate) {
            removeDuplicateNodeList(nodes);
        }
        logger.info("Target hosts size : " + nodes.size());

        return nodes;

    }

    /**
     * Removes the duplicate node list.
     *
     * @param list
     *            the list
     * @return the int
     */
    public static int removeDuplicateNodeList(List<String> list) {

        int originCount = list.size();
        // add elements to all, including duplicates
        HashSet<String> hs = new LinkedHashSet<String>();
        hs.addAll(list);
        list.clear();
        list.addAll(hs);

        return originCount - list.size();
    }

}
