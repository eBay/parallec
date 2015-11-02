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


/**
 * The Interface ParallecResponseHandler.
 * 
 */
public interface ParallecResponseHandler {

    /**
     * When timeout / error occurred or response received for the target host,
     * will trigger execution of onCompleted(). 
     * 
     * This can be run at the execution manager (default) or the worker thread 
     * based on the config {@link ParallelTaskBuilder#handleInWorker()}
     * 
     * <br> 
     * When {@link ResponseOnSingleTask#isError()} is true: Fail to receive resposne 
     *
     * @param res
     *            the response
     * @param responseContext
     *            the response context 
     */

    public void onCompleted(ResponseOnSingleTask res,
            Map<String, Object> responseContext);

}
