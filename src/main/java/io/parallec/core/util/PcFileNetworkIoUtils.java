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

import io.parallec.core.HostsSourceType;
import io.parallec.core.config.ParallecGlobalConfig;
import io.parallec.core.taskbuilder.targethosts.TargetHostsBuilder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Charsets;
import com.google.common.io.Files;


/**
 * The Class PcFileNetworkIoUtils.
 */
public class PcFileNetworkIoUtils {

    /** The logger. */
    private static Logger logger = LoggerFactory
            .getLogger(PcFileNetworkIoUtils.class);

    /**
     * 20140907: for key management get from file to input stream.
     *
     * @param filePath
     *            the file path
     * @return the input stream
     * @throws FileNotFoundException
     *             the file not found exception
     */
    public static InputStream readFileToInputStream(String filePath)
            throws FileNotFoundException {

        InputStream is = null;

        try {

            is = new FileInputStream(filePath);

            logger.info("Completed read file for input stream " + " Path: "
                    + filePath + " at " + PcDateUtils.getNowDateTimeStr());

        } catch (Exception e) {
            logger.error("Error read file." + e);
        }
        return is;

    } // end func.

    /**
     * Checks if is file exist.
     *
     * @param filePath
     *            the file path
     * @return true, if is file exist
     */
    public static boolean isFileExist(String filePath) {

        File f = new File(filePath);

        return f.exists() && !f.isDirectory();
    }

    /**
     * Read file content to string.
     *
     * @param filePath
     *            the file path
     * @return the string
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public static String readFileContentToString(String filePath)
            throws IOException {
        String content = "";
        content = Files.toString(new File(filePath), Charsets.UTF_8);
        return content;
    }

    /**
     * Read string from url generic.
     *
     * @param url
     *            the url
     * @return the string
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public static String readStringFromUrlGeneric(String url)
            throws IOException {
        InputStream is = null;
        URL urlObj = null;
        String responseString = PcConstants.NA;
        try {
            urlObj = new URL(url);
            URLConnection con = urlObj.openConnection();

            con.setConnectTimeout(ParallecGlobalConfig.urlConnectionConnectTimeoutMillis);
            con.setReadTimeout(ParallecGlobalConfig.urlConnectionReadTimeoutMillis);
            is = con.getInputStream();

            BufferedReader rd = new BufferedReader(new InputStreamReader(is,
                    Charset.forName("UTF-8")));
            responseString = PcFileNetworkIoUtils.readAll(rd);

        } finally {

            if (is != null) {
                is.close();
            }

        }

        return responseString;
    }

    /**
     * Read all.
     *
     * @param rd
     *            the rd
     * @return the string
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public static String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }

    public static List<String> getListFromLineByLineText(String sourcePath,
            HostsSourceType sourceType) {

        TargetHostsBuilder thb = new TargetHostsBuilder();
        List<String> list = new ArrayList<String>();
        try {
            list = thb.setTargetHostsFromLineByLineText(sourcePath, sourceType);
        } catch (Exception e) {
            logger.error("error getListFromLineByLineText " + e);
        }

        return list;

    }

}
