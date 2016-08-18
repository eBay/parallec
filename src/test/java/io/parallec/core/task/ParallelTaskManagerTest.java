package io.parallec.core.task;

import io.parallec.core.ParallelClient;
import io.parallec.core.TestBase;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class ParallelTaskManagerTest extends TestBase {

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
    public void testDirectorForException() {
        try {
            
            ParallelTaskManager.getInstance().getRemainingCapacity();
            ParallelTaskManager.getInstance().getTaskFromInProgressMap("1");
            ParallelTaskManager.getInstance().getInprogressTaskMap();
            
            ParallelTaskManager.getInstance().sendTaskToExecutionManager(null);

        } catch (Exception ex) {
            logger.error("Expected Exception : " + ex.getLocalizedMessage());
        }
    }// end func

}
