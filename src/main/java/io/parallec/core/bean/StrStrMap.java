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
package io.parallec.core.bean;

import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This allow you to replace multiple variables with multiple values key: the
 * variable name : e.g. GET /checkProgress/$JOBID value: the actual value string
 * that will replace the JOBID
 * 
 * @author Yuanteng (Jeff) Pei
 */
public class StrStrMap {

    /** The map. */
    private final Map<String, String> map = new LinkedHashMap<String, String>();

    /** The logger. */
    private static Logger logger = LoggerFactory.getLogger(StrStrMap.class);

    /**
     * Gets the map.
     *
     * @return the map
     */
    public Map<String, String> getMap() {
        return map;
    }

    /**
     * Instantiates a new str str map.
     */
    public StrStrMap() {
    }

    /**
     * Instantiates a new str str map.
     *
     * @param map
     *            the map
     */
    public StrStrMap(Map<String, String> map) {
        this.map.putAll(map);
    }

    /**
     * Adds the pair.
     *
     * @param key
     *            the key
     * @param value
     *            the value
     * @return the str str map
     */
    public StrStrMap addPair(String key, String value) {
        if (key == null || value == null) {
            logger.error("invalid key value as null. ignore pair");

        } else {

            this.map.put(key, value);
        }

        return this;
    }
}
