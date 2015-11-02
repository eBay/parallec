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
package io.parallec.core;

import io.parallec.core.util.PcStringUtils;

import java.util.LinkedHashMap;
import java.util.Map;


/**
 * Easy to use HTTP Header builder to add header key/value pair.
 *
 * @author Yuanteng (Jeff) Pei
 */
public class ParallecHeader {

    /** The header map. */
    // will never be null
    private final Map<String, String> headerMap = new LinkedHashMap<String, String>();

    /**
     * Instantiates a new parallec header.
     */
    public ParallecHeader() {

    }

    /**
     * Adds the pair.
     *
     * @param key
     *            the key
     * @param value
     *            the value
     * @return the parallec header
     */
    public ParallecHeader addPair(String key, String value) {
        this.headerMap.put(key, value);
        return this;
    }

    /**
     * Gets the header map.
     *
     * @return the header map
     */
    public Map<String, String> getHeaderMap() {
        return headerMap;
    }

    /**
     * Gets the header str.
     *
     * @return the header str
     */
    public String getHeaderStr() {
        return PcStringUtils.strMapToStr(headerMap);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "ParallecHeader [headerMap="
                + PcStringUtils.strMapToStr(headerMap) + "]";
    }

}
