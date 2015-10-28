package io.parallec.core.main.ssh;

import io.parallec.core.ParallelClient;
import io.parallec.core.TestBase;
import io.parallec.core.actor.ActorConfig;
import io.parallec.core.actor.SshWorker;
import io.parallec.core.actor.message.ResponseOnSingeRequest;
import io.parallec.core.actor.message.type.RequestWorkerMsgType;

import java.util.concurrent.TimeUnit;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.pattern.Patterns;
import akka.util.Timeout;

public class SshWorkerTest extends TestBase {

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
    public void testSshWorkerNormalCheckComplete() {
        ActorRef asyncWorker = null;
        try {
            // Start new job
            

            int actorMaxOperationTimeoutSec = 15;
            asyncWorker = ActorConfig.createAndGetActorSystem().actorOf(
                    Props.create(SshWorker.class, actorMaxOperationTimeoutSec,
                            SshProviderMockTest.sshMetaPassword, hostIpSample));

            final FiniteDuration duration = Duration.create(20,
                    TimeUnit.SECONDS);
            Future<Object> future = Patterns
                    .ask(asyncWorker, RequestWorkerMsgType.PROCESS_REQUEST,
                            new Timeout(duration));

            ResponseOnSingeRequest response = (ResponseOnSingeRequest) Await
                    .result(future, duration);

            logger.info("\nWorker response:" + response.toString());
        } catch (Throwable ex) {

            logger.error("Exception in test : " + ex);
        }
    }// end func

    @Test
    public void testSshWorkerDupAndCancel() {
        ActorRef asyncWorker = null;
        try {
            // Start new job
            

            int actorMaxOperationTimeoutSec = 15;
            asyncWorker = ActorConfig.createAndGetActorSystem().actorOf(
                    Props.create(SshWorker.class, actorMaxOperationTimeoutSec,
                            SshProviderMockTest.sshMetaPassword, hostIpSample));

            final FiniteDuration duration = Duration.create(20,
                    TimeUnit.SECONDS);
            Future<Object> future = Patterns
                    .ask(asyncWorker, RequestWorkerMsgType.PROCESS_REQUEST,
                            new Timeout(duration));

            // test dup
            asyncWorker.tell(RequestWorkerMsgType.PROCESS_REQUEST, asyncWorker);

            // test cancel
            asyncWorker.tell(RequestWorkerMsgType.CANCEL, asyncWorker);

            ResponseOnSingeRequest response = (ResponseOnSingeRequest) Await
                    .result(future, duration);

            logger.info("\nWorker response:" + response.toString());
        } catch (Throwable ex) {

            logger.error("Exception in test : " + ex);
        }
    }// end func

    /**
     * fake a NPE of logger; do not forget to reset it or other tests will fail.
     */
    @Test
    public void testSshWorkerException() {
        ActorRef asyncWorker = null;
        try {
            // Start new job
            
            int actorMaxOperationTimeoutSec = 15;
            SshWorker.setLogger(null);
            asyncWorker = ActorConfig.createAndGetActorSystem().actorOf(
                    Props.create(SshWorker.class, actorMaxOperationTimeoutSec,
                            SshProviderMockTest.sshMetaPassword, hostIpSample));

            final FiniteDuration duration = Duration.create(20,
                    TimeUnit.SECONDS);
            Future<Object> future = Patterns
                    .ask(asyncWorker, RequestWorkerMsgType.PROCESS_REQUEST,
                            new Timeout(duration));

            ResponseOnSingeRequest response = (ResponseOnSingeRequest) Await
                    .result(future, duration);

            logger.info("\nWorker response:" + response.toString());
        } catch (Throwable ex) {
            logger.error("Exception in test : " + ex);
        }
        SshWorker.setLogger(LoggerFactory.getLogger(SshWorker.class));
    }// end func

    @Test
    public void testSshWorkerTimeoutException() {
        ActorRef asyncWorker = null;
        try {
            // Start new job
            
            // made a timeout
            int actorMaxOperationTimeoutSec = 0;
            asyncWorker = ActorConfig.createAndGetActorSystem()
                    .actorOf(
                            Props.create(SshWorker.class,
                                    actorMaxOperationTimeoutSec,
                                    SshProviderMockTest.sshMetaPassword,
                                    hostIpSample2));

            final FiniteDuration duration = Duration.create(20,
                    TimeUnit.SECONDS);
            Future<Object> future = Patterns
                    .ask(asyncWorker, RequestWorkerMsgType.PROCESS_REQUEST,
                            new Timeout(duration));
            ResponseOnSingeRequest response = (ResponseOnSingeRequest) Await
                    .result(future, duration);

            logger.info("\nWorker response:" + response.toString());
        } catch (Throwable ex) {

            logger.error("Exception in test : " + ex);
        }
    }// end func

    @Test
    public void testSshWorkerBadMsgType() {
        ActorRef asyncWorker = null;
        try {
            // made a timeout
            int actorMaxOperationTimeoutSec = 15;
            asyncWorker = ActorConfig.createAndGetActorSystem()
                    .actorOf(
                            Props.create(SshWorker.class,
                                    actorMaxOperationTimeoutSec,
                                    SshProviderMockTest.sshMetaPassword,
                                    hostIpSample2));

            final FiniteDuration duration = Duration.create(20,
                    TimeUnit.SECONDS);
            Future<Object> future = Patterns
                    .ask(asyncWorker, RequestWorkerMsgType.PROCESS_REQUEST,
                            new Timeout(duration));

            // test invalid type
            asyncWorker.tell(new Integer(0), asyncWorker);
            ResponseOnSingeRequest response = (ResponseOnSingeRequest) Await
                    .result(future, duration);

            logger.info("\nWorker response:" + response.toString());
        } catch (Throwable ex) {

            logger.error("Exception in test : " + ex);
        }
    }// end func

}
