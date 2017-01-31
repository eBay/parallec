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
import io.parallec.core.util.PcFileNetworkIoUtils;
import io.parallec.core.util.PcTargetHostsUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jayway.jsonpath.JsonPath;


/**
 * The Class TargetHostsBuilder.
 */
public class TargetHostsBuilder implements ITargetHostsBuilder {

    /** The logger. */
    private static Logger logger = LoggerFactory
            .getLogger(TargetHostsBuilder.class);

    /**
     * Instantiates a new target hosts builder.
     */
    public TargetHostsBuilder() {
    }

    /**
     * note that for read from file, this will just load all to memory. not fit
     * if need to read a very large file. However for getting the host name.
     * normally it is fine.
     * 
     * for reading large file, should use iostream.
     *
     * @param sourcePath
     *            the source path
     * @param sourceType
     *            the source type
     * @return the content from path
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private String getContentFromPath(String sourcePath,
            HostsSourceType sourceType) throws IOException {

        String res = "";

        if (sourceType == HostsSourceType.LOCAL_FILE) {
            res = PcFileNetworkIoUtils.readFileContentToString(sourcePath);
        } else if (sourceType == HostsSourceType.URL) {
            res = PcFileNetworkIoUtils.readStringFromUrlGeneric(sourcePath);
        }
        return res;

    }

    /*
     * (non-Javadoc)
     * 
     * @see io.parallec.core.taskbuilder.targethosts.ITargetHostsBuilder#
     * setTargetHostsFromList(java.util.List)
     */
    @Override
    public List<String> setTargetHostsFromList(List<String> targetHosts) {

        List<String> targetHostsNew = new ArrayList<String>();
        targetHostsNew.addAll(targetHosts);
        int dupSize = PcTargetHostsUtils.removeDuplicateNodeList(targetHostsNew);
        if (dupSize > 0) {
            logger.info("get target hosts with duplicated hosts of " + dupSize
                    + " with new total size of " + targetHosts.size());
        }
        return targetHostsNew;
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.parallec.core.taskbuilder.targethosts.ITargetHostsBuilder#
     * setTargetHostsFromString(java.lang.String)
     */
    @Override
    public List<String> setTargetHostsFromString(String targetHostsStr) {

        List<String> targetHosts = new ArrayList<String>();
        if (targetHostsStr != null) {
            boolean removeDuplicate = true;
            targetHosts.addAll(PcTargetHostsUtils
                    .getNodeListFromStringLineSeperateOrSpaceSeperate(
                            targetHostsStr, removeDuplicate));
        }

        return targetHosts;
    }

    /**
     * TODO https://github.com/jayway/JsonPath
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
    @Override
    public List<String> setTargetHostsFromJsonPath(String jsonPath,
            String sourcePath, HostsSourceType sourceType)
            throws TargetHostsLoadException {

        List<String> targetHosts = new ArrayList<String>();
        try {
            String content = getContentFromPath(sourcePath, sourceType);
            targetHosts = JsonPath.read(content, jsonPath);

        } catch (IOException e) {
            throw new TargetHostsLoadException("IEException when reading  "
                    + sourcePath, e);
        }

        return targetHosts;
    }

    /**
     * get target hosts from line by line.
     *
     * @param sourcePath
     *            the source path
     * @param sourceType
     *            the source type
     * @return the list
     * @throws TargetHostsLoadException
     *             the target hosts load exception
     */
    @Override
    public List<String> setTargetHostsFromLineByLineText(String sourcePath,
            HostsSourceType sourceType) throws TargetHostsLoadException {

        List<String> targetHosts = new ArrayList<String>();
        try {
            String content = getContentFromPath(sourcePath, sourceType);

            targetHosts = setTargetHostsFromString(content);

        } catch (IOException e) {
            throw new TargetHostsLoadException("IEException when reading  "
                    + sourcePath, e);
        }

        return targetHosts;

    }

    /*
     * (non-Javadoc)
     * 
     * @see io.parallec.core.taskbuilder.targethosts.ITargetHostsBuilder#
     * setTargetHostsFromCmsQueryUrl(java.lang.String)
     */
    @Override
    public List<String> setTargetHostsFromCmsQueryUrl(String cmsQueryUrl)
            throws TargetHostsLoadException {
        List<String> targetHosts = new ArrayList<String>();
        try {

            logger.info("will use default project label");
            targetHosts = setTargetHostsFromCmsQueryUrl(cmsQueryUrl, "label");
        } catch (Exception e) {
            throw new TargetHostsLoadException("error when reading  "
                    + cmsQueryUrl, e);
        }

        return targetHosts;

    }
    
    

    /*
     * (non-Javadoc)
     * 
     * @see io.parallec.core.taskbuilder.targethosts.ITargetHostsBuilder#
     * setTargetHostsFromCmsQueryUrl(java.lang.String, java.lang.String)
     */
    @Override
    public List<String> setTargetHostsFromCmsQueryUrl(String cmsQueryUrl,
            String projection) {
        return setTargetHostsFromCmsQueryUrl(cmsQueryUrl, projection, null);
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.parallec.core.taskbuilder.targethosts.ITargetHostsBuilder#
     * setTargetHostsFromCmsQueryUrl(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public List<String> setTargetHostsFromCmsQueryUrl(String cmsQueryUrl,
            String projection, String token) {

        List<String> targetHosts = new ArrayList<String>();

        try {

            if (projection == null || projection.isEmpty()) {
                logger.info("will use default project label");
                projection = "label";
            }
            TargetHostsBuilderHelperCms helper = new TargetHostsBuilderHelperCms();
            targetHosts = helper.getNodeListCompleteURLForCMS(cmsQueryUrl,
                    projection, token);

        } catch (Exception e) {
            throw new TargetHostsLoadException("error when reading  "
                    + cmsQueryUrl, e);
        }

        return targetHosts;

    }    
    
}
