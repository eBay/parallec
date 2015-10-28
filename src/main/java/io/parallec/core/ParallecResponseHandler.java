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

import java.util.Map;

// TODO: Auto-generated Javadoc
/**
 * The Interface ParallecResponseHandler.
 * 
 * Consider make it serializable if we need to pass this for akka clustering.
 */
public interface ParallecResponseHandler {

    /**
     * When a response coming back or a timeout received at the Command Manager,
     * apply this response handler.
     * 
     * this gap includes the request content inside
     *
     * @param res
     *            the res
     * @param responseContext
     *            the response context
     */

    public void onCompleted(ResponseOnSingleTask res,
            Map<String, Object> responseContext);

}
