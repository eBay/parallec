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
package io.parallec.core.bean.ping;

import io.parallec.core.config.ParallecGlobalConfig;
import io.parallec.core.exception.ParallelTaskInvalidException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * The Ping metadata on mode,timeout and retries. 
 * 
 * @author Yuanteng (Jeff) Pei
 *
 */
public class PingMeta {
    /** The logger. */
    private static Logger logger = LoggerFactory.getLogger(PingMeta.class);

    private PingMode mode;
    private Integer pingTimeoutMillis;
    private Integer numRetries;
    

    public PingMeta() {
        super();
        this.mode = null;
        this.pingTimeoutMillis = null;
        this.numRetries = null;
    } 
    
    public boolean validation() throws ParallelTaskInvalidException {
        
        if (this.mode == null) {
            logger.info("SET DEFAULT PING MODE: INET_ADDRESS_REACHABLE_NEED_ROOT."
                    + "WARNING. MUST run as ROOT for accuracy."
                    + " ");
            this.mode= ParallecGlobalConfig.pingModeDefault;
        }
 
        if (this.pingTimeoutMillis == null) {
            logger.info("SET DEFAULT PING TIMEOUT: 500ms ");
            this.pingTimeoutMillis= ParallecGlobalConfig.pingTimeoutMillisDefault;
        }
        
        if (this.numRetries == null) {
            logger.info("SET DEFAULT PING NUM OF RETRIES: 1 ");
            this.numRetries= ParallecGlobalConfig.pingNumRetriesDefault;
        }
        return true;

    }
   

    public PingMode getMode() {
        return mode;
    }

    public void setMode(PingMode mode) {
        this.mode = mode;
    }

    public Integer getPingTimeoutMillis() {
        return pingTimeoutMillis;
    }

    public void setPingTimeoutMillis(Integer pingTimeoutMillis) {
        this.pingTimeoutMillis = pingTimeoutMillis;
    }

    public Integer getNumRetries() {
        return numRetries;
    }

    public void setNumRetries(Integer numRetries) {
        this.numRetries = numRetries;
    }
}
