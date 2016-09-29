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

import java.util.List;


// TODO: Auto-generated Javadoc
/**
 * The class used to represent the metadata for getting the response headers.
 * Which keys are needed to get from response header.
 * 
 * @author Yuanteng (Jeff) Pei
 */
/**
 * @author ypei
 *
 */
public class ResponseHeaderMeta {


    /**  If true, will get all the entries in the response headers. */
    private boolean getAll;

    /** The key set for retrieving the http response headers. */
    private List<String> keys;

    
    /**
     * Instantiates a new response header meta.
     *
     * @param keys the keys would like to save
     * @param getAll when true: will get all the key value pair, regardless of the keys list provided.
     */
    public ResponseHeaderMeta(List<String> keys, boolean getAll) {
        super();
        this.keys = keys;
        this.getAll = getAll;
    }


    /**
     * Checks if is gets the all.
     *
     * @return true, if is gets the all
     */
    public boolean isGetAll() {
        return getAll;
    }

    /**
     * Sets the gets the all.
     *
     * @param getAll the new gets the all
     */
    public void setGetAll(boolean getAll) {
        this.getAll = getAll;
    }

    /**
     * Gets the keys.
     *
     * @return the keys
     */
    public List<String> getKeys() {
        return keys;
    }

    /**
     * Sets the keys.
     *
     * @param keys the new keys
     */
    public void setKeys(List<String> keys) {
        this.keys = keys;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "ResponseHeaderMeta [getAll=" + getAll + ", keys=" + keys + "]";
    }


}
