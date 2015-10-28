package io.parallec.core.util;

import io.parallec.core.TestBase;

import java.io.FileNotFoundException;

import org.junit.Before;
import org.junit.Test;

public class PcErrorMsgUtilsTest extends TestBase {

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void test() throws FileNotFoundException {
        PcErrorMsgUtils.replaceErrorMsg(null);

        PcErrorMsgUtils.replaceErrorMsg("java.net.ConnectException abc");

    }

}
