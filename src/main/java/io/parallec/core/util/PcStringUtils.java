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

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * The Class PcStringUtils.
 */
public class PcStringUtils {

    /** The logger. */
    @SuppressWarnings("unused")
    private static Logger logger = LoggerFactory.getLogger(PcStringUtils.class);

    /**
     * Prints the stack trace.
     *
     * @param t
     *            the throwable
     * @return the string
     */
    public static String printStackTrace(Throwable t) {
        return t == null ? PcConstants.NA : ExceptionUtils.getStackTrace(t);

    }

    /**
     * Str map to str.
     *
     * @param map
     *            the map
     * @return the string
     */
    public static String strMapToStr(Map<String, String> map) {

        StringBuilder sb = new StringBuilder();

        if (map == null || map.isEmpty())
            return sb.toString();

        for (Entry<String, String> entry : map.entrySet()) {

            sb.append("< " + entry.getKey() + ", " + entry.getValue() + "> ");
        }
        return sb.toString();

    }

    /**
     * Get the aggregated result human readable string for easy display.
     * 
     *
     * @param aggregateResultMap the aggregate result map
     * @return the aggregated result human
     */
    public static String getAggregatedResultHuman(Map<String, LinkedHashSet<String>> aggregateResultMap){

        StringBuilder res = new StringBuilder();

        for (Entry<String, LinkedHashSet<String>> entry : aggregateResultMap
                .entrySet()) {
            LinkedHashSet<String> valueSet = entry.getValue(); 
            res.append("[" + entry.getKey() + " COUNT: " +valueSet.size() + " ]:\n");
            for(String str: valueSet){
                res.append("\t" + str + "\n");
            }
            res.append("###################################\n\n");
        }
        
        return res.toString();
        
    }

    /**
     * Render json.
     *
     * @param o
     *            the o
     * @return the string
     */
    public static String renderJson(Object o) {

        Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting()
                .create();
        return gson.toJson(o);
    }

}
