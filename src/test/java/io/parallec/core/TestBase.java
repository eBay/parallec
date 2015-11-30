package io.parallec.core;

import io.parallec.core.config.ParallecGlobalConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestBase {
    public final String FILEPATH_TMP1 = "userdata/target_hosts_tmp1.txt";
    public final String FILEPATH_TMP2 = "userdata/target_hosts_tmp2.txt";
    
    public final String FILEPATH_TOP_100 = "userdata/sample_target_hosts_top100_old.txt";
    
    public final String FILEPATH_TOP_100_NEW = "userdata/sample_target_hosts_top100.txt";

    public final String FILEPATH_TOP_500 = "userdata/sample_target_hosts_top500_old.txt";
    public final String FILEPATH_TOP_500_NEW = "userdata/sample_target_hosts_top500.txt";

    public final String FILEPATH_TOP_1000 = "userdata/sample_target_hosts_top1000.txt";
    public final String FILEPATH_TOP_2000 = "userdata/sample_target_hosts_top2000.txt";
    public final String FILEPATH_TOP_10000 = "userdata/sample_target_hosts_top10k.txt";

    public final String FILEPATH_JSON_PATH = "userdata/sample_target_hosts_json_path.json";

    public final String URL_TOP_100 = "http://www.restcommander.com/docs/sample_target_hosts_top100.txt";
    public final String URL_JSON_PATH = "http://www.parallec.io/userdata/sample_target_hosts_json_path.json";
    public final String URL_CMS_QUERY_SINGLE_PAGE = "http://www.parallec.io/cms/repositories/cmsdb/branches/main/query/sample_cms_query_results_single_page.json";
    public final String URL_CMS_QUERY_MULTI_PAGE = "http://www.parallec.io/cms/repositories/cmsdb/branches/main/query/sample_cms_query_results_multi_page_1.json";

    public final String URL_CMS_QUERY_SINGLE_PAGE_DUPLICATE = "http://www.parallec.io/cms/repositories/cmsdb/branches/main/query/sample_cms_query_results_single_page_duplicate.json";
    public final String URL_CMS_QUERY_SINGLE_PAGE_EMPTY = "http://www.parallec.io/cms/repositories/cmsdb/branches/main/query/sample_cms_query_results_single_page_empty.json";

    
    
    public final HostsSourceType SOURCE_LOCAL = HostsSourceType.LOCAL_FILE;
    public final HostsSourceType SOURCE_URL = HostsSourceType.URL;

    // ssh

    public final static String userName = "someUser";
    public final static int PORT_DEFAULT = 22;
    public final static String passwd = "mypassword";
    public final static String hostIpSample = "192.168.1.155";
    public final static String hostIpSample2 = "192.168.1.156";
    public final static String commandSshLineValid = "date";

    public final static String LOCALHOST="localhost";
    
    public final static int sshConnectionTimeoutMillis = ParallecGlobalConfig.sshConnectionTimeoutMillisDefault;

    protected static final Logger logger = LoggerFactory
            .getLogger(TestBase.class);
}
