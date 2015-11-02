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

import io.parallec.core.util.PcConstants;
import io.parallec.core.util.PcDateUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A simple class to apply a regular expression to .
 * @author Yuanteng (Jeff) Pei
 */
public class FilterRegex {

    /** The logger. */
    private static Logger logger = LoggerFactory.getLogger(FilterRegex.class);

    /** The regex. */
    private String regex;

    /**
     * Instantiates a new filter regex.
     */
    public FilterRegex() {
    };

    /**
     * Instantiates a new filter regex.
     *
     * @param regex1
     *            the regex1
     */
    public FilterRegex(String regex1) {
        setRegex(regex1);
    }

    public String filter(String input) {
        return stringMatcherByPattern(input, regex);
    }

    /**
     * this remove the linebreak.
     *
     * @param input
     *            the input
     * @param patternStr
     *            the pattern str
     * @return the string
     */
    public static String stringMatcherByPattern(String input, String patternStr) {

        String output = PcConstants.SYSTEM_FAIL_MATCH_REGEX;

        // 20140105: fix the NPE issue
        if (patternStr == null) {
            logger.error("patternStr is NULL! (Expected when the aggregation rule is not defined at "
                    + PcDateUtils.getNowDateTimeStrStandard());
            return output;
        }

        if (input == null) {
            logger.error("input (Expected when the response is null and now try to match on response) is NULL in stringMatcherByPattern() at "
                    + PcDateUtils.getNowDateTimeStrStandard());
            return output;
        } else {
            input = input.replace("\n", "").replace("\r", "");
        }

        logger.debug("input: " + input);
        logger.debug("patternStr: " + patternStr);

        Pattern patternMetric = Pattern.compile(patternStr, Pattern.MULTILINE);

        final Matcher matcher = patternMetric.matcher(input);
        if (matcher.matches()) {
            output = matcher.group(1);
        }
        return output;
    }

    /**
     * Gets the regex.
     *
     * @return the regex
     */
    public String getRegex() {
        return regex;
    }

    /**
     * Sets the regex.
     *
     * @param regex
     *            the new regex
     */
    public void setRegex(String regex) {
        this.regex = regex;
    }

}
