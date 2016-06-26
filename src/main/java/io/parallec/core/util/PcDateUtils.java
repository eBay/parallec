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

import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * The Class PcDateUtils.
 */
public class PcDateUtils {

    /** The logger. */
    private static Logger logger = LoggerFactory.getLogger(PcDateUtils.class);

    /**
     * Gets the date time str.
     *
     * @param d
     *            the d
     * @return the date time str
     */
    public static String getDateTimeStr(Date d) {
        if (d == null)
            return "";

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSZ");
        // 20140315 test will problem +0000
        return sdf.format(d);
    }

    /**
     * Gets the date time str standard.
     *
     * @param d
     *            the d
     * @return the date time str standard
     */
    public static String getDateTimeStrStandard(Date d) {
        if (d == null)
            return "";

        if (d.getTime() == 0L)
            return "Never";

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss.SSSZ");

        return sdf.format(d);
    }

    /**
     * Gets the date time str concise.
     *
     * @param d
     *            the d
     * @return the date time str concise
     */
    public static String getDateTimeStrConcise(Date d) {
        if (d == null)
            return "";

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSSZ");
        return sdf.format(d);
    }

    /**
     * 20141027: same as getDateTimeStrConcise but no timezone.
     *
     * @param d
     *            the d
     * @return the date time str concise no zone
     */
    public static String getDateTimeStrConciseNoZone(Date d) {
        if (d == null)
            return "";

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        return sdf.format(d);
    }

    /**
     * 20130512 Converts the sdsm string generated above to Date format.
     *
     * @param str
     *            the str
     * @return the date from concise str
     */
    public static Date getDateFromConciseStr(String str) {

        Date d = null;
        if (str == null || str.isEmpty())
            return null;

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSSZ");

            d = sdf.parse(str);
        } catch (Exception ex) {
            logger.error(ex + "Exception while converting string to date : "
                    + str);
        }

        return d;
    }

    /**
     * Gets the now date time str.
     *
     * @return the now date time str
     */
    public static String getNowDateTimeStr() {

        return getDateTimeStr(new Date());
    }

    /**
     * Gets the now date time str standard.
     *
     * @return the now date time str standard
     */
    public static String getNowDateTimeStrStandard() {

        return getDateTimeStrStandard(new Date());
    }

    /**
     * Gets the now date time str concise.
     *
     * @return the now date time str concise
     */
    public static String getNowDateTimeStrConcise() {

        return getDateTimeStrConcise(new Date());
    }

    /**
     * Gets the now date time str concise no zone.
     *
     * @return the now date time str concise no zone
     */
    public static String getNowDateTimeStrConciseNoZone() {

        return getDateTimeStrConciseNoZone(new Date());
    }

}
