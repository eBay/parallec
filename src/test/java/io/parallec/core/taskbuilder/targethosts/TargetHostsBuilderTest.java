package io.parallec.core.taskbuilder.targethosts;

import io.parallec.core.ParallelClient;
import io.parallec.core.TestBase;
import io.parallec.core.exception.TargetHostsLoadException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.http.util.Asserts;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class TargetHostsBuilderTest extends TestBase {

    private ITargetHostsBuilder thb;
    private static ParallelClient pc;

    @BeforeClass
    public static void beforeClass() throws Exception {
        pc = new ParallelClient();
    }

    @AfterClass
    public static void shutdown() throws Exception {
        pc.releaseExternalResources();
    }

    @Before
    public void setUp() throws Exception {
        thb = new TargetHostsBuilder();
    }

    @Test
    public void setTargetHostsFromStringTest() {
        thb.setTargetHostsFromString("www.jeffpei.com www.restcommander.com");
    }

    @Test
    public void setTargetHostsFromListTest() {
        List<String> targetHosts = thb.setTargetHostsFromList(Arrays.asList("www.jeffpei.com", "www.restcommander.com"));
        logger.info("Get list " + targetHosts.size()
                + " setTargetHostsFromListTest ");
        Asserts.check(targetHosts.size()>0, "fail test");
    }

    @Test
    public void setTargetHostsFromListTestDup() {
        List<String> targetHostsOrg = new ArrayList<String>();
        targetHostsOrg.add("www.restcommander.com");
        targetHostsOrg.add("www.restcommander.com");
        List<String> targetHosts = thb.setTargetHostsFromList(targetHostsOrg);
        logger.info("Get list " + targetHosts.size()
                + " setTargetHostsFromListTest ");
        Asserts.check(targetHosts.size()==1, "fail test");
    }

    @Test
    public void setTargetHostsFromListBadHostTest() {
        List<String> targetHostsOrg = new ArrayList<String>();
        targetHostsOrg.add("www.restco mmander.com");
        List<String> targetHosts = thb.setTargetHostsFromList(targetHostsOrg);
        logger.info("Get list " + targetHosts.size()
                + " setTargetHostsFromListTest ");
        Asserts.check(targetHosts.size()>0, "fail test");
    }

    @Test
    public void setTargetHostsFromLineByLineText() {
        List<String> targetHosts = thb.setTargetHostsFromLineByLineText(
                FILEPATH_TOP_100, SOURCE_LOCAL);
        logger.info("Get list " + targetHosts.size() + " from "
                + FILEPATH_TOP_100);
        Asserts.check(targetHosts.size()>0, "fail test");

        // bad path
        try {
            thb.setTargetHostsFromLineByLineText("/badpath", SOURCE_LOCAL);
        } catch (TargetHostsLoadException e) {
            logger.info("expected exception: " + e);
        }
    }

    @Test
    public void setTargetHostsFromUrl() {
        List<String> targetHosts = thb.setTargetHostsFromLineByLineText(
                URL_TOP_100, SOURCE_URL);
        logger.info("Get list " + targetHosts.size() + " from " + URL_TOP_100);

        Asserts.check(targetHosts.size() > 0,
                "fail setTargetHostsFromLineByLineText setTargetHostsFromUrl");

    }

    // http://www.parallec.io/userdata/sample_target_hosts_json_path.json
    @Test
    public void setTargetHostsFromJsonPathTest() {
        String jsonPath = "$.sample.small-target-hosts[*].hostName";
        List<String> targetHosts = thb.setTargetHostsFromJsonPath(jsonPath,
                URL_JSON_PATH, SOURCE_URL);
        logger.info("Get list " + targetHosts.size() + " from json path  "
                + jsonPath + " from file " + URL_JSON_PATH);

        Asserts.check(targetHosts.size() > 0,
                "fail setTargetHostsFromJsonPathTest");
        // try bad
        try {
            thb.setTargetHostsFromJsonPath(jsonPath,
                    FILEPATH_JSON_PATH + "bad", SOURCE_LOCAL);
        } catch (TargetHostsLoadException e) {
            logger.info("expected error. Get bad list " + " from json path  "
                    + jsonPath + " from file " + URL_JSON_PATH);
        }

    }

    @Test
    public void setTargetHostsFromCmsQueryUrTest() {

        List<String> targetHosts = thb
                .setTargetHostsFromCmsQueryUrl(URL_CMS_QUERY_MULTI_PAGE);
        logger.info("Get list " + targetHosts.size() + " from "
                + URL_CMS_QUERY_MULTI_PAGE);

        
        List<String> targetHostsSg = thb
                .setTargetHostsFromCmsQueryUrl(URL_CMS_QUERY_SINGLE_PAGE);
        logger.info("Get list " + targetHostsSg.size() + " from "
                + URL_CMS_QUERY_SINGLE_PAGE);

        Asserts.check(targetHosts.size() > 0,
                "fail targetHosts setTargetHostsFromCmsQueryUrTest");
        Asserts.check(targetHostsSg.size() > 0,
                "fail targetHostsSg setTargetHostsFromCmsQueryUrTest");

        // null query
        try {
            thb.setTargetHostsFromCmsQueryUrl(null);
        } catch (TargetHostsLoadException e) {
            logger.info("expected exception: " + e);
        }
        
        //  duplicate hosts
        thb.setTargetHostsFromCmsQueryUrl(
                "http://www.parallec.io/cms/repositories/cmsdb/branches"
                        + "/main/query/sample_cms_query_results_single_page_duplicate.json");

        //  empty hosts
        thb.setTargetHostsFromCmsQueryUrl(
                "http://www.parallec.io/cms/repositories/cmsdb/branches"
                        + "/main/query/sample_cms_query_results_single_page_duplicate_empty.json");

        // bad query
        try {

            thb.setTargetHostsFromCmsQueryUrl("http://1www.parallec.io/cms/repositories/cmsdb/branches"
                    + "/main/query/sample_cms_query_results_single_page.json");
        } catch (TargetHostsLoadException e) {
            logger.info("expected exception: " + e);
        }

        // with projection null/empty/wrong projection
        thb.setTargetHostsFromCmsQueryUrl(
                "http://www.parallec.io/cms/repositories/cmsdb/branches"
                        + "/main/query/sample_cms_query_results_single_page.json",
                null);
        thb.setTargetHostsFromCmsQueryUrl(
                "http://www.parallec.io/cms/repositories/cmsdb/branches"
                        + "/main/query/sample_cms_query_results_single_page.json",
                "");
        thb.setTargetHostsFromCmsQueryUrl(
                "http://www.parallec.io/cms/repositories/cmsdb/branches"
                        + "/main/query/sample_cms_query_results_single_page.json",
                "labelbad");

    }

}
