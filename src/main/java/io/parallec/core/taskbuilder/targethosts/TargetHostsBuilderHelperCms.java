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
package io.parallec.core.taskbuilder.targethosts;

import io.parallec.core.config.ParallecGlobalConfig;
import io.parallec.core.exception.TargetHostsLoadException;
import io.parallec.core.util.PcDateUtils;
import io.parallec.core.util.PcFileNetworkIoUtils;
import io.parallec.core.util.PcStringUtils;
import io.parallec.core.util.PcTargetHostsUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * load node from CMS.
 *
 * @author Yuanteng (Jeff) Pei
 */
public class TargetHostsBuilderHelperCms {

    /** The logger. */
    static Logger logger = LoggerFactory
            .getLogger(TargetHostsBuilderHelperCms.class);

    /** The Constant ADD_QUOTE. */
    public static final boolean ADD_QUOTE = true;

    /**
     * 20141022.
     *
     * @param jObj
     *            the j obj
     * @param projectionStr
     *            the projection str
     * @return the FQDN value list cms
     * @throws JSONException
     *             the JSON exception
     */
    static List<String> getFQDNValueListCMS(JSONObject jObj,
            String projectionStr) throws JSONException {
        final List<String> labelList = new ArrayList<String>();

        if (!jObj.has("result")) {
            logger.error("!!CMS_ERROR! result key is not in jOBJ in getFQDNValueListCMS!!: \njObj:"
                    + PcStringUtils.renderJson(jObj));

            return labelList;
        }
        JSONArray jArr = (JSONArray) jObj.get("result");
        if (jArr == null || jArr.length() == 0) {
            return labelList;
        }
        for (int i = 0; i < jArr.length(); ++i) {
            JSONObject agentObj = jArr.getJSONObject(i);
            // properties can be null

            if (!agentObj.has(projectionStr)) {
                continue;
            }
            String label = (String) agentObj.get(projectionStr);

            if (label != null && !label.trim().isEmpty()) {
                labelList.add(label);
            }
        }

        return labelList;
    }

    /**
     * removed header (only used for authorization for PP) 2015.08
     *
     * @param url
     *            the url
     * @return the JSON object
     * @throws MalformedURLException
     *             the malformed url exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * @throws JSONException
     *             the JSON exception
     */
    static JSONObject readJsonFromUrlWithCmsHeader(String url, String token)
            throws MalformedURLException, IOException, JSONException {
        InputStream is = null;

        JSONObject jObj = new JSONObject();
        try {

            HttpURLConnection.setFollowRedirects(false);
            HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
            con.setRequestMethod("GET");
            if(token!=null){
                con.setRequestProperty("Authorization", token);
            }
            con.setConnectTimeout(ParallecGlobalConfig.urlConnectionConnectTimeoutMillis);
            con.setReadTimeout(ParallecGlobalConfig.urlConnectionReadTimeoutMillis);
            is = con.getInputStream();

            BufferedReader rd = new BufferedReader(new InputStreamReader(is,
                    Charset.forName("UTF-8")));
            String jsonText = PcFileNetworkIoUtils.readAll(rd);
            jObj = new JSONObject(jsonText);
            rd.close();

        } catch (Exception t) {
            logger.error("readJsonFromUrl() exception: "
                    + t.getLocalizedMessage()
                    + PcDateUtils.getNowDateTimeStrStandard());

        } finally {
            if (is != null) {
                is.close();
            }
        }
        return jObj;
    }


    /**
     *
     * @param url
     *            the url
     * @param projectionStr
     *            the projection str
     * @return the node list complete url for cms
     */
    public List<String> getNodeListCompleteURLForCMS(String url,
            String projectionStr, String token) {
        List<String> nodes = new ArrayList<String>();

        try {

            int indexNextUrlStartPos = url.indexOf("/repositories/");
            String cmsQueryUrlPrefix = url.substring(0, indexNextUrlStartPos);

            // add 1st
            // updated 201501 for adding auth header.
            JSONObject jsonObject = readJsonFromUrlWithCmsHeader(url, token);
            nodes.addAll(getFQDNValueListCMS(jsonObject, projectionStr));
            JSONObject jsonObjectNext = jsonObject;
            Boolean hasMore = false;
            String hasMoreNextUrl = null;
            String KEY_HAS_MORE = "hasmore";
            String KEY_NEXT_PARENT = "next";
            String KEY_NEXT_URL = "url";
            do {
                hasMore = false;
                hasMoreNextUrl = null;

                if (jsonObjectNext.has(KEY_HAS_MORE)) {
                    hasMore = jsonObjectNext.getBoolean(KEY_HAS_MORE);
                    if (jsonObjectNext.has(KEY_NEXT_PARENT)
                            && jsonObjectNext.getJSONObject(KEY_NEXT_PARENT)
                                    .has(KEY_NEXT_URL)) {
                        hasMoreNextUrl = jsonObjectNext.getJSONObject(
                                KEY_NEXT_PARENT).getString(KEY_NEXT_URL);
                    }

                    // final check
                    if (hasMoreNextUrl != null) {
                        String nextUrlComplete = cmsQueryUrlPrefix
                                + hasMoreNextUrl;

                        // 201501 add here too.
                        jsonObjectNext = readJsonFromUrlWithCmsHeader(nextUrlComplete, token);
                        nodes.addAll(getFQDNValueListCMS(jsonObjectNext,
                                projectionStr));

                        logger.info("CMS query: hasMore==true. Found next round in query with URL:"
                                + nextUrlComplete);
                    }
                }
            } while (hasMore);


            // filtering duplicated nodes:
            int removedDuplicatedNodeCount = PcTargetHostsUtils
                    .removeDuplicateNodeList(nodes);

            if (removedDuplicatedNodeCount > 0) {

                logger.info(" Removed duplicated node #: "
                        + removedDuplicatedNodeCount);
            }

            logger.info("List size: " + nodes.size());

        } catch (Exception e) {

            throw new TargetHostsLoadException("error when reading  " + url, e);
        }

        return nodes;
    }
}
