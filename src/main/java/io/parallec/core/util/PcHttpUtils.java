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

import io.parallec.core.actor.message.NodeReqResponse;

import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ning.http.client.AsyncHttpClient.BoundRequestBuilder;


/**
 * Adding http header logic TODO; should finally be data driven and specific for
 * each command. Now it is defined here and user can easily change.
 * 
 * @author Yuanteng (Jeff) Pei
 * 
 */
public class PcHttpUtils {

    /** The logger. */
    private static Logger logger = LoggerFactory.getLogger(PcHttpUtils.class);

    /**
     * currently not to use MalformedURLException or MalformedURLException as
     * our logic includes add HTTP :// etc Assuming not null .
     *
     * @param url
     *            the url
     * @return true, if is url valid
     */
    public static boolean isUrlValid(String url) {

        return (!url.trim().contains(" "));

    }

    /**
     * !!!! ASSUMPTION: all VAR exists in HTTP Header must of type:
     * APIVARREPLACE_NAME_PREFIX_HTTP_HEADER
     * 
     * 20140310 This may be costly (O(n^2)) of the updated related # of headers;
     * # of parameters in the requests.
     * 
     * Better to only do it when there are some replacement in the request
     * Parameters. a prefix :
     * 
     * TOBE tested
     *
     * @param httpHeaderMap
     *            the http header map
     * @param requestParameters
     *            the request parameters
     */

    public static void replaceHttpHeaderMapNodeSpecific(
            Map<String, String> httpHeaderMap,
            Map<String, String> requestParameters) {

        boolean needToReplaceVarInHttpHeader = false;
        for (String parameter : requestParameters.keySet()) {
            if (parameter.contains(PcConstants.NODE_REQUEST_PREFIX_REPLACE_VAR)) {
                needToReplaceVarInHttpHeader = true;
                break;
            }
        }

        if (!needToReplaceVarInHttpHeader) {
            logger.debug("No need to replace. Since there are no HTTP header variables. ");
            return;
        }
        // replace all the values in the (not the keys) in the header map.
        for (Entry<String, String> entry : httpHeaderMap.entrySet()) {
            String key = entry.getKey();
            String valueOriginal = entry.getValue();
            String valueUpdated = NodeReqResponse.replaceStrByMap(
                    requestParameters, valueOriginal);
            httpHeaderMap.put(key, valueUpdated);
        }
    }

    /**
     * Adds the headers.
     *
     * @param builder
     *            the builder
     * @param headerMap
     *            the header map
     */
    public static void addHeaders(BoundRequestBuilder builder,
            Map<String, String> headerMap) {
        for (Entry<String, String> entry : headerMap.entrySet()) {
            String name = entry.getKey();
            String value = entry.getValue();
            builder.addHeader(name, value);
        }

    }

}
