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

/**
 * Static variable names...
 * 
 * @author Yuanteng (Jeff) Pei
 * 
 */
public class PcConstants {

    public static final String API_PREFIX = "API_";

    /** The Constant ACTOR_SYSTEM. */
    public static final String ACTOR_SYSTEM = "ParallecActorSystem";

    public static final String REQUEST_CANCELED = "REQUEST_CANCELED";
    public static final String OPERATION_TIMEOUT = "OPERATION_TIMEOUT";

    public static final String NOT_SAVED = "NOT_SAVED";

    /** The Constant NODE_REQUEST_PREFIX_REPLACE_VAR. */
    public static final String NODE_REQUEST_PREFIX_REPLACE_VAR = "REPLACE-VAR_";

    /** The Constant NODE_REQUEST_TRUE_CONTENT1. */
    public static final String NODE_REQUEST_TRUE_CONTENT = "TRUE_CONTENT";

    public static final String NODE_REQUEST_STATUS = "STATUS";

    /** The Constant NODE_REQUEST_TRUE_URL. */
    public static final String NODE_REQUEST_TRUE_URL = "TRUE_URL";

    /** The Constant NODE_REQUEST_TRUE_PORT. */
    public static final String NODE_REQUEST_TRUE_PORT = "TRUE_PORT";

    /** The Constant NODE_REQUEST_HTTP_METHOD. */
    public static final String NODE_REQUEST_HTTP_METHOD = "HTTP_METHOD";

    /** The Constant NODE_REQUEST_HTTP_HEADER_META. */
    public static final String NODE_REQUEST_HTTP_HEADER_META = "HEADER_META";

    /** The Constant NODE_REQUEST_PREPARE_TIME. */
    public static final String NODE_REQUEST_PREPARE_TIME = "PREPARE_TIME";

    /** The Constant NODE_REQUEST_TRUE_TARGET_NODE. */
    public static final String NODE_REQUEST_TRUE_TARGET_NODE = "TRUE_TARGET_NODE";

    /** The Constant NODE_REQUEST_NEED_POLLER. */
    public static final String NODE_REQUEST_NEED_POLLER = "NEED_POLLER";

    /** The Constant UNIFORM_TARGET_HOST_VAR. */
    public static final String UNIFORM_TARGET_HOST_VAR = "PARALLEC_UNIFORM_TARGET_HOST";

    /** The Constant UNIFORM_TARGET_HOST_VAR_WHEN_CHECK. */
    public static final String UNIFORM_TARGET_HOST_VAR_WHEN_CHECK = "REPLACE-VAR_"
            + UNIFORM_TARGET_HOST_VAR;

    /** The request parameter http header prefix. */
    public static String REQUEST_PARAMETER_HTTP_HEADER_PREFIX = "TRUE_HEADER_";

    /** The var name apivarreplace httpheader auth token. */
    public static String VAR_NAME_APIVARREPLACE_HTTPHEADER_AUTH_TOKEN = "APIVARREPLACE_HTTPHEADER_AUTH_TOKEN";

    /** The Constant SYSTEM_FAIL_MATCH_REGEX. */
    public static final String SYSTEM_FAIL_MATCH_REGEX = "SYSTEM_FAIL_MATCH_REGEX";

    /** The Constant OPERATION_SUCCESSFUL. */
    public static final String OPERATION_SUCCESSFUL = "OPERATION_SUCCESSFUL";

    /** The Constant NA. */
    public static final String NA = "NA";

    /** The Constant NA_INT. */
    public static final int NA_INT = -1;

    /** The Constant NULL_STR. */
    public static final String NULL_STR = "NULL";

    /** The Constant NODE_RESPONSE_INIT. */
    public static final String NODE_RESPONSE_INIT = "UNKNOWN_RESONSE_NOT_RECEIVED_YET";

    /** The Constant REQUEST_BODY_PLACE_HOLDER. */
    public static final String REQUEST_BODY_PLACE_HOLDER = "PARALLEC_EMPTY_REQUEST_BODY";

    /** The Constant COMMAND_VAR_DEFAULT_REQUEST_CONTENT. */
    public static final String COMMAND_VAR_DEFAULT_REQUEST_CONTENT = "$"
            + REQUEST_BODY_PLACE_HOLDER;

    /** The Constant STR_EMPTY. */
    public static final String STR_EMPTY = "";

    /** The Constant NODE_REQUEST_WILL_EXECUTE. */
    // whether or not will execute this command
    public static final String NODE_REQUEST_WILL_EXECUTE = "NODE_REQUEST_WILL_EXECUTE";

    /** The Constant NODE_REQUEST_EXECUTE_MSG. */
    public static final String NODE_REQUEST_EXECUTE_MSG = "NODE_REQUEST_EXECUTE_MSG";

    /** The Constant NODE_REQUEST_EXECUTE_MSG_DETAIL_REPLACEMENT_VAR_VALUE_NA. */
    public static final String NODE_REQUEST_EXECUTE_MSG_DETAIL_REPLACEMENT_VAR_VALUE_NA = "!!! SAFEGUARD: NOTE: REQUEST WILL NOT EXECUTE: because NODE_REQUEST_EXECUTE_MSG_DETAIL_REPLACEMENT_VAR_VALUE_NA.";

    /**
     * The Constant
     * NODE_REQUEST_EXECUTE_MSG_DETAIL_REPLACEMENT_VAR_KEY_OR_VALUE_NULL.
     */
    public static final String NODE_REQUEST_EXECUTE_MSG_DETAIL_REPLACEMENT_VAR_KEY_OR_VALUE_NULL = "!!! SAFEGUARD: NOTE: REQUEST WILL NOT EXECUTE: because NODE_REQUEST_EXECUTE_MSG_DETAIL_REPLACEMENT_VAR_KEY_OR_VALUE_NULL.";

    /**
     * The Constant AGNET_RESPONSE_MAX_RESPONSE_DISPLAY_BYTE. this will record
     * in log
     */
    public static int AGNET_RESPONSE_MAX_RESPONSE_DISPLAY_BYTE = 32;

    /** to get the errorSummary */
    public static String REGEX_ERROR_SUMMARY = "(.*?)\\sDetails.*";
    

}
