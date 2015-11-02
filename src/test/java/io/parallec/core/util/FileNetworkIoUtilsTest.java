package io.parallec.core.util;

import io.parallec.core.TestBase;

import java.io.FileNotFoundException;

import org.junit.Before;
import org.junit.Test;

public class FileNetworkIoUtilsTest extends TestBase {

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void test() throws FileNotFoundException {
        PcFileNetworkIoUtils.isFileExist(FILEPATH_TOP_100);
        PcFileNetworkIoUtils.readFileToInputStream(FILEPATH_TOP_100);
        PcFileNetworkIoUtils.readFileToInputStream(FILEPATH_TOP_100+"badpath");
        PcFileNetworkIoUtils.getListFromLineByLineText(FILEPATH_TOP_100,
                SOURCE_LOCAL);
        PcFileNetworkIoUtils.getListFromLineByLineText(FILEPATH_TOP_100 + "a",
                SOURCE_LOCAL);

    }

}
