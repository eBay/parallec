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
package io.parallec.core.resources;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.ning.http.client.AsyncHttpClient;


/**
 * this stores a pair of default embedded fast/slow AsyncHttpClient, and another pair of
 * customized fast/slow AsyncHttpClient. 
 * By default the pair of customized ones are just references (duplicates of) the embedded ones.
 * 
 * For each Parallel Task, will by default load the Embed-fast client in AsyncHttpClientStore 
 *  unless you load your specific async http client.
 * 
 * You may call {@link #setHttpClientTypeCurrentDefault(HttpClientType)} to change the default one 
 * 
 *  (default one is the embedded fast)
 * 
 * @author Yuanteng (Jeff) Pei
 */
public class HttpClientStore {

    /** The http client type current default. */
    private HttpClientType httpClientTypeCurrentDefault;

    /** The Constant map. */
    private final Map<HttpClientType, AsyncHttpClient> map = new HashMap<HttpClientType, AsyncHttpClient>();

    /** The http client factory embed. */
    private AsyncHttpClientFactoryEmbed httpClientFactoryEmbed;


    /** The singleton instance. */
    private final static HttpClientStore instance = new HttpClientStore();

    public static HttpClientStore getInstance() {
        return instance;
    }

    /**
     * Instantiates a new http client store.
     */
    private HttpClientStore() {
        init();
    }

    /**
     * Initialize
     */
    public synchronized void init() {
        httpClientFactoryEmbed = new AsyncHttpClientFactoryEmbed();
        map.put(HttpClientType.EMBED_FAST, httpClientFactoryEmbed.getFastClient());
        map.put(HttpClientType.EMBED_SLOW, httpClientFactoryEmbed.getSlowClient());

        // custom default are also the embed one; unless a change
        map.put(HttpClientType.CUSTOM_FAST, httpClientFactoryEmbed.getFastClient());
        map.put(HttpClientType.CUSTOM_SLOW, httpClientFactoryEmbed.getSlowClient());

        // make sure to use the default fast
        setHttpClientTypeCurrentDefault(HttpClientType.EMBED_FAST);
    }

    /**
     * close and clean up the http client, then create the new ones.
     */
    public synchronized void reinit() {

        // first shutdown existing ones.
        shutdown();
        init();
    }

    /**
     * Shutdown each AHC client in the map.
     */
    public void shutdown() {

        for (Entry<HttpClientType, AsyncHttpClient> entry : map.entrySet()) {
            AsyncHttpClient client = entry.getValue();
            if (client != null)
                client.close();
        }

    }

    /**
     * default fast is the default.
     *
     * @return the embed client fast
     */
    public AsyncHttpClient getEmbedClientFast() {
        return map.get(HttpClientType.EMBED_FAST);
    }

    /**
     * Gets the embed client slow.
     *
     * @return the embed client slow
     */
    public AsyncHttpClient getEmbedClientSlow() {
        return map.get(HttpClientType.EMBED_SLOW);
    }

    /**
     * Gets the custom client fast.
     *
     * @return the custom client fast
     */
    public AsyncHttpClient getCustomClientFast() {
        return map.get(HttpClientType.CUSTOM_FAST);
    }

    /**
     * Gets the custom client slow.
     *
     * @return the custom client slow
     */
    public AsyncHttpClient getCustomClientSlow() {
        return map.get(HttpClientType.CUSTOM_SLOW);
    }

    /**
     * Gets the client by type.
     *
     * @param type
     *            the type
     * @return the client by type
     */
    public AsyncHttpClient getClientByType(HttpClientType type) {
        return map.get(type);

    }

    /**
     * Gets the current default client.
     *
     * @return the current default client
     */
    public AsyncHttpClient getCurrentDefaultClient() {
        return map.get(httpClientTypeCurrentDefault);

    }

    /**
     * Sets the custom client fast.
     *
     * @param client
     *            the new custom client fast
     */
    public void setCustomClientFast(AsyncHttpClient client) {
        map.put(HttpClientType.CUSTOM_FAST, client);
    }

    /**
     * Sets the custom client slow.
     *
     * @param client
     *            the new custom client slow
     */
    public void setCustomClientSlow(AsyncHttpClient client) {
        map.put(HttpClientType.CUSTOM_SLOW, client);
    }

    /**
     * Gets the http client type current default.
     *
     * @return the http client type current default
     */
    public HttpClientType getHttpClientTypeCurrentDefault() {
        return httpClientTypeCurrentDefault;
    }

    /**
     * this is a key function, and all new command builder will use this one .
     *
     * @param httpClientTypeCurrentDefault
     *            the new http client type current default
     */
    public void setHttpClientTypeCurrentDefault(
            HttpClientType httpClientTypeCurrentDefault) {
        this.httpClientTypeCurrentDefault = httpClientTypeCurrentDefault;
    }

    /**
     * Gets the map.
     *
     * @return the map
     */
    public Map<HttpClientType, AsyncHttpClient> getMap() {
        return map;
    }

    /**
     * Gets the http client factory embed.
     *
     * @return the http client factory embed
     */
    public AsyncHttpClientFactoryEmbed getHttpClientFactoryEmbed() {
        return httpClientFactoryEmbed;
    }

    /**
     * Sets the http client factory embed.
     *
     * @param httpClientFactoryEmbed
     *            the new http client factory embed
     */
    public void setHttpClientFactoryEmbed(
            AsyncHttpClientFactoryEmbed httpClientFactoryEmbed) {
        this.httpClientFactoryEmbed = httpClientFactoryEmbed;
    }


}
