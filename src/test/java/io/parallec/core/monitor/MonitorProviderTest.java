package io.parallec.core.monitor;

import io.parallec.core.ParallelClient;
import io.parallec.core.TestBase;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class MonitorProviderTest extends TestBase {

    private static ParallelClient pc;

    @BeforeClass
    public static void setUp() throws Exception {
        pc = new ParallelClient();
    }

    @AfterClass
    public static void shutdown() throws Exception {
        pc.releaseExternalResources();
    }

    @Test
    public void testMonitorProvider() {

        MonitorProvider.getInstance().getJVMMemoryUsage();
        MonitorProvider.getInstance().getThreadUsage();
        MonitorProvider.THRESHOLD_PERCENT = 0;
        MonitorProvider.getInstance().getHealthMemory();
        MonitorProvider.getInstance().currentJvmPerformUsage.getSummary();
        MonitorProvider.THRESHOLD_PERCENT = 90;
    }

}
