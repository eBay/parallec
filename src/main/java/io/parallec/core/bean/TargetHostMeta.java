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

import io.parallec.core.util.PcDateUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * The metadata about the targetHost, which is mainly a list of hostnames (IP or FQDN)
 *  the primary key is the targetHostId, generated with timestamp
 * 
 * @author Yuanteng (Jeff) Pei
 */
public class TargetHostMeta {

    /** The target host id. */
    private String targetHostId;

    /** The target host list. */
    private final List<String> hosts = new ArrayList<String>();

    /**
     * Instantiates a new target host meta.
     *
     * @param hosts the hosts
     */
    public TargetHostMeta(List<String> hosts) {
        super();

        final String uuid = UUID.randomUUID().toString().substring(0, 12);
        this.hosts.addAll(hosts);

        this.setTargetHostId("THM_" + hosts.size() + "_"
                + PcDateUtils.getNowDateTimeStrConciseNoZone() + "_" + uuid);

    }

    /**
     * Instantiates a new target host meta.
     */
    public TargetHostMeta() {
    };

    /**
     * Gets the node list.
     *
     * @return the node list
     */
    public List<String> getHosts() {
        return hosts;
    }
    public String getTargetHostId() {
        return targetHostId;
    }

    public void setTargetHostId(String targetHostId) {
        this.targetHostId = targetHostId;
    }

    @Override
    public String toString() {
        return "TargetHostMeta [targetHostId=" + targetHostId + ", hosts size:"
                + hosts.size() + "]";
    }
    
    

}
