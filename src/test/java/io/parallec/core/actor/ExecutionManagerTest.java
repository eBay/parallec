package io.parallec.core.actor;

import io.parallec.core.ParallelClient;
import io.parallec.core.ParallelTask;
import io.parallec.core.TestBase;
import io.parallec.core.actor.message.InitialRequestToManager;
import io.parallec.core.actor.message.type.ExecutionManagerMsgType;
import io.parallec.core.bean.HttpMeta;
import io.parallec.core.bean.TargetHostMeta;
import io.parallec.core.commander.workflow.InternalDataProvider;
import io.parallec.core.resources.HttpMethod;
import io.parallec.core.util.PcStringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.pattern.Patterns;
import akka.util.Timeout;

public class ExecutionManagerTest extends TestBase {

    private static ParallelClient pc;

    @BeforeClass
    public static void setUp() throws Exception {
        pc = new ParallelClient();
    }

    @AfterClass
    public static void shutdown() throws Exception {
        pc.releaseExternalResources();
    }

    public static ParallelTask genParallelTask() {
        ParallelTask task = new ParallelTask();

        List<String> list = new ArrayList<String>();
        list.add("restcommander.com");
        list.add("www.restcommander.com");

        task.setTargetHostMeta(new TargetHostMeta(list));
        task.setHttpMeta(new HttpMeta());
        // task.getCommandMeta().setDefaultUnusedValue();
        task.getHttpMeta().setHttpMethod(HttpMethod.GET);
        task.getHttpMeta().setRequestUrlPostfix("/validateInternals.html");
        task.validateWithFillDefault();
        task.setTaskId(task.generateTaskId());
        return task;

    }

    @Test
    public void testHttpWorkerNormalCheckCancel() {
        ActorRef executionManager = null;
        try {
            // Start new job
            

            ParallelTask task = genParallelTask();
            InternalDataProvider adp = InternalDataProvider.getInstance();
            adp.genNodeDataMap(task);

            executionManager = ActorConfig.createAndGetActorSystem().actorOf(
                    Props.create(ExecutionManager.class, task),
                    "executionManager-" + task.getTaskId());

            final FiniteDuration duration = Duration.create(20,
                    TimeUnit.SECONDS);
            Future<Object> future = Patterns.ask(executionManager,
                    new InitialRequestToManager(task), new Timeout(duration));

            executionManager.tell(ExecutionManagerMsgType.CANCEL, executionManager);

            Await.result(future, duration);
            logger.info("\nWorker response header:"
                    + PcStringUtils.renderJson(task.getParallelTaskResult()));
            // logger.info("\nWorker response:" +
            // PcStringUtils.renderJson(task));
        } catch (Exception ex) {
            logger.error("Exception in testHttpWorkerNormalCheckCancel : " + ex);
        }
    }// end func

    @Test
    public void testHttpWorkerNormalCheckTimeout() {
        ActorRef executionManager = null;
        try {
            // Start new job
            

            ParallelTask task = genParallelTask();
            InternalDataProvider adp = InternalDataProvider.getInstance();
            adp.genNodeDataMap(task);

            executionManager = ActorConfig.createAndGetActorSystem().actorOf(
                    Props.create(ExecutionManager.class, task),
                    "ExecutionManager-" + task.getTaskId());

            final FiniteDuration duration = Duration.create(20,
                    TimeUnit.SECONDS);
            Future<Object> future = Patterns.ask(executionManager,
                    new InitialRequestToManager(task), new Timeout(duration));

            executionManager.tell(ExecutionManagerMsgType.OPERATION_TIMEOUT,
                    executionManager);

            Await.result(future, duration);
            logger.info("\nWorker response header:"
                    + PcStringUtils.renderJson(task.getParallelTaskResult()));
            // logger.info("\nWorker response:" +
            // PcStringUtils.renderJson(task));
        } catch (Throwable ex) {
            logger.error("Exception in testHttpWorkerNormalCheckTimeout : "
                    + ex);
        }
    }// end func

    @Test
    public void testBadMsg() {
        ActorRef executionManager = null;
        try {
            // Start new job
            

            ParallelTask task = genParallelTask();
            InternalDataProvider adp = InternalDataProvider.getInstance();
            adp.genNodeDataMap(task);

            executionManager = ActorConfig.createAndGetActorSystem().actorOf(
                    Props.create(ExecutionManager.class, task),
                    "ExecutionManager-" + task.getTaskId());

            executionManager.tell("bad request", executionManager);
        } catch (Exception ex) {
            logger.error("Exception in testBadMsg : " + ex);
        }
    }// end func

    @Test
    public void testException() {
        ActorRef executionManager = null;
        try {
            // Start new job
            

            ParallelTask task = genParallelTask();
            InternalDataProvider adp = InternalDataProvider.getInstance();
            adp.genNodeDataMap(task);

            // fake bad attribute
            task.setTaskId(null);

            executionManager = ActorConfig.createAndGetActorSystem().actorOf(
                    Props.create(ExecutionManager.class, task),
                    "executionManager-" + task.getTaskId());

            final FiniteDuration duration = Duration.create(20,
                    TimeUnit.SECONDS);

            // set task as null

            Future<Object> future = Patterns.ask(executionManager,
                    new InitialRequestToManager(task), new Timeout(duration));

            Await.result(future, duration);
            logger.info("\nWorker response header:"
                    + PcStringUtils.renderJson(task.getParallelTaskResult()));

        } catch (Exception ex) {
            logger.error("Exception in testBadMsg : " + ex);
        }
    }// end func

}
