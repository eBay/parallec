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

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * The class used to represent the target hosts and a count when aggregation
 * 
 * @author Yuanteng (Jeff) Pei
 */
public class SetAndCount {


    /** The count. */
    private int count;

    /** The set. */
    private final Set<String> set = new LinkedHashSet<String>();

    /**
     * Instantiates a new sets the and count.
     *
     * @param set
     *            the set
     */
    public SetAndCount(Set<String> set) {
        super();
        this.set.addAll(set);
        this.count = set.size();
    }

    /**
     * Gets the count.
     *
     * @return the count
     */
    public int getCount() {
        return count;
    }

    /**
     * Sets the count.
     *
     * @param count
     *            the new count
     */
    public void setCount(int count) {
        this.count = count;
    }

    /**
     * Gets the sets the.
     *
     * @return the sets the
     */
    public Set<String> getSet() {
        return set;
    }

    /**
     * "SetAndCount [count=" + count + ", set=" + set + "]";
     */
    @Override
    public String toString() {
        return "SetAndCount [count=" + count + ", set=" + set + "]";
    }

}
