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

import io.parallec.core.HostsSourceType;
import io.parallec.core.exception.TargetHostsLoadException;

import java.util.List;

/**
 * The Interface to build the target hosts.
 * Will remove duplicates. 
 */
public interface ITargetHostsBuilder {

    /**
     * Sets the target hosts from list. 
     * Will remove duplicate from it.
     * 
     * Will create a new array list because the original one may be unmodifiable.
     *
     * @param targetHosts
     *            the target hosts
     * @return the list
     */
    public List<String> setTargetHostsFromList(List<String> targetHosts);

    /**
     * Sets the target hosts from string.
     *
     * @param targetHostsStr
     *            the target hosts str
     * @return the list
     */
    public List<String> setTargetHostsFromString(String targetHostsStr);

    /**
     * Sets the target hosts from json path.
     *
     * @param jsonPath
     *            the json path
     * @param sourcePath
     *            the source path
     * @param sourceType
     *            the source type
     * @return the list
     * @throws TargetHostsLoadException
     *             the target hosts load exception
     */
    public List<String> setTargetHostsFromJsonPath(String jsonPath,
            String sourcePath, HostsSourceType sourceType)
            throws TargetHostsLoadException;

    /**
     * Sets the target hosts from line by line text.
     *
     * @param sourcePath
     *            the source path
     * @param sourceType
     *            the source type
     * @return the list
     * @throws TargetHostsLoadException
     *             the target hosts load exception
     */
    // read from a URL/File line by line
    public List<String> setTargetHostsFromLineByLineText(String sourcePath,
            HostsSourceType sourceType) throws TargetHostsLoadException;

    /**
     * Sets the target hosts from cms query url.
     *
     * @param cmsQueryUrl
     *            the cms query url
     * @return the list
     * @throws TargetHostsLoadException
     *             the target hosts load exception
     */
    // from query of CMS (CMS is internal name of the project YiDB. aka YiDB
    // http://yidb.org/ );
    public List<String> setTargetHostsFromCmsQueryUrl(String cmsQueryUrl)
            throws TargetHostsLoadException;

    /**
     * Sets the target hosts from cms query url.
     *
     * @param cmsQueryUrl
     *            the cms query url
     * @param projection
     *            the projection
     * @return the list
     * @throws TargetHostsLoadException
     *             the target hosts load exception
     */
    public List<String> setTargetHostsFromCmsQueryUrl(String cmsQueryUrl,
            String projection) throws TargetHostsLoadException;

    /**
     * Sets the target hosts from cms query url with projection and authorization token.
     *
     * @param cmsQueryUrl
     *            the cms query url
     * @param projection
     *            the projection
     * @param token
     *            the cms authorization token
     * @return the list
     * @throws TargetHostsLoadException
     *             the target hosts load exception
     */
    public List<String> setTargetHostsFromCmsQueryUrl(String cmsQueryUrl,
            String projection, String token) throws TargetHostsLoadException;
    
}
