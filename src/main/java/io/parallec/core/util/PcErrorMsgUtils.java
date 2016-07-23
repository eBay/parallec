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

import java.util.EnumMap;
import java.util.Map;


/**
 * The Class PcErrorMsgUtils.
 */
public class PcErrorMsgUtils {

    /**
     * The Enum ERROR_TYPE.
     */
    public enum ERROR_TYPE {
        /** The connection exception. */
        CONNECTION_EXCEPTION,

    }

    /** The Constant errorMapOrig. */
    public static final Map<ERROR_TYPE, String> errorMapOrig = new EnumMap<ERROR_TYPE, String>(ERROR_TYPE.class);

    /** The Constant errorMapReplace. */
    public static final Map<ERROR_TYPE, String> errorMapReplace = new EnumMap<ERROR_TYPE, String>(ERROR_TYPE.class);

    static {
        errorMapOrig.put(ERROR_TYPE.CONNECTION_EXCEPTION,
                "java.net.ConnectException");
        errorMapReplace.put(ERROR_TYPE.CONNECTION_EXCEPTION,
                "java.net.ConnectException");
    }

    /**
     * Replace error msg.
     *
     * @param origMsg
     *            the orig msg
     * @return the string
     */
    public static String replaceErrorMsg(String origMsg) {

        String replaceMsg = origMsg;
        for (ERROR_TYPE errorType : ERROR_TYPE.values()) {

            if (origMsg == null) {
                replaceMsg = PcConstants.NA;
                return replaceMsg;
            }

            if (origMsg.contains(errorMapOrig.get(errorType))) {
                replaceMsg = errorMapReplace.get(errorType);
                break;
            }

        }

        return replaceMsg;

    }

}
