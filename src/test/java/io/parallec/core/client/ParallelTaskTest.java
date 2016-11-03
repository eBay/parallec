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
package io.parallec.core.client;

import io.parallec.core.ParallelClient;
import io.parallec.core.ParallelTask;
import io.parallec.core.TestBase;
import io.parallec.core.actor.ExecutionManagerTest;
import io.parallec.core.exception.ParallelTaskInvalidException;
import io.parallec.core.resources.HttpClientStore;
import io.parallec.core.task.ParallelTaskState;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * The most basic test with hitting the same URL at 3 different websites.
 * require Internet access for testing.
 * 
 * <p>
 * This example shows 1. Basic request construction 2. how to use response
 * context to pass value during the response handler out to a global space
 * </p>
 */
public class ParallelTaskTest extends TestBase {

    /** The pc. */
    private static ParallelClient pc;

    /**
     * Sets the up.
     * 
     * @throws Exception
     *             the exception
     */
    @BeforeClass
    public static void setUp() throws Exception {
        pc = new ParallelClient();
    }

    /**
     * Shutdown.
     *
     * @throws Exception
     *             the exception
     */
    @AfterClass
    public static void shutdown() throws Exception {
        pc.releaseExternalResources();
    }
    
    @Test
    public void testCancelException() {
        ParallelTask task = ExecutionManagerTest.genParallelTask();
        task.setState(null);
        task.cancel(true);
    }

    @Test
    public void testGetProgress() {
        ParallelTask task = ExecutionManagerTest.genParallelTask();
        task.setState(ParallelTaskState.IN_PROGRESS);
        task.setRequestNum(0);
        logger.info("progress: {}",task.getProgress());
        task.setState(ParallelTaskState.COMPLETED_WITHOUT_ERROR);
        logger.info("progress: {}",task.getProgress());
        
    }

    
    @Test
    public void testCancelNullManager() {
        ParallelTask task = ExecutionManagerTest.genParallelTask();
        task.setState(ParallelTaskState.IN_PROGRESS);
        task.executionManager = null;
        task.cancel(true);
    }
    
    
    @Test
    public void testValidation() {
        ParallelTask task = ExecutionManagerTest.genParallelTask();
        logger.info(task.toString());
        task.setConfig(null);
        task.setAsyncHttpClient( HttpClientStore.getInstance()
                    .getCurrentDefaultClient());
        task.getHttpMeta().setHttpMethod(null);
        try {

            task.validateWithFillDefault();
        } catch (ParallelTaskInvalidException e) {

            logger.info("EXPECTED Exception {}", e.getLocalizedMessage());
        }
    }

    @Test
    public void testValidationPoller() {
        ParallelTask task = ExecutionManagerTest.genParallelTask();
        task.getHttpMeta().setRequestUrlPostfix(null);
        task.getHttpMeta().setPollable(true);
        task.getHttpMeta().setHttpPollerProcessor(null);
        try {
            task.validateWithFillDefault();
        } catch (ParallelTaskInvalidException e) {

            logger.info("EXPECTED Exception {}", e.getLocalizedMessage());
        }
    }

}
