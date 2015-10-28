package io.parallec.core.filter;

import io.parallec.core.FilterRegex;
import io.parallec.core.TestBase;
import io.parallec.core.util.PcConstants;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;

public class FilterRegexTest extends TestBase {

    @Test
    public void testFilterRegex() {
        String completeRegex = ".*\"progress\"\\s*:\\s*(100).*}";
        FilterRegex.stringMatcherByPattern(null, completeRegex);

        FilterRegex.stringMatcherByPattern(completeRegex, null);
    }

    @Test
    public void testRegex() {

        String completeRegex = ".*\"progress\"\\s*:\\s*(100).*}";
        Pattern patternMetric = Pattern.compile(completeRegex,
                Pattern.MULTILINE);
        String response = "{\"status\": \"/status/e40c0f1e-ddc2-4987-aaa7-b638a9978782\", \"progress\": 100,  \"error\": 300}";

        final Matcher matcher = patternMetric.matcher(response);
        String matchStr = PcConstants.NA;
        if (matcher.matches()) {
            matchStr = matcher.group(1);
        }
        logger.info(matchStr + "");
    }

}
