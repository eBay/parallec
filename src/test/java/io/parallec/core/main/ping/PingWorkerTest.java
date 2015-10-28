package io.parallec.core.main.ping;

import io.parallec.core.ParallelClient;
import io.parallec.core.TestBase;
import io.parallec.core.actor.ActorConfig;
import io.parallec.core.actor.PingWorker;
import io.parallec.core.actor.message.ResponseOnSingeRequest;
import io.parallec.core.actor.message.type.RequestWorkerMsgType;
import io.parallec.core.bean.ping.PingMeta;

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

public class PingWorkerTest extends TestBase {

    private static ParallelClient pc;
    
    @BeforeClass
    public static void setUp() throws Exception {
        pc = new ParallelClient();

        try {
            Thread.sleep(500L);
        } catch (Exception e) {
            ;
        }
    }

    @AfterClass
    public static void shutdown() throws Exception {
        pc.releaseExternalResources();
        try {
            Thread.sleep(500L);
        } catch (Exception e) {
            ;
        }
    }

    public PingMeta getPingMetaSample(){
        PingMeta pingMeta = new PingMeta();
        pingMeta.validation();
        return pingMeta;
    }
    
    @Test
    public void testSlowAndPollProgress() {
        ActorRef asyncWorker = null;
        try {
            int actorMaxOperationTimeoutSec = 15;
            asyncWorker = ActorConfig.createAndGetActorSystem().actorOf(
                    Props.create(PingWorker.class, actorMaxOperationTimeoutSec,
                            getPingMetaSample(), "www.google.com"));

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
    public void testDupAndCancel() {
        ActorRef asyncWorker = null;
        logger.info("IN testTcpWorkerDupAndCancel");
        try {
            int actorMaxOperationTimeoutSec = 15;
            asyncWorker = ActorConfig.createAndGetActorSystem().actorOf(
                    Props.create(PingWorker.class, actorMaxOperationTimeoutSec,
                            getPingMetaSample(), "www.google.com"));

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
            // Start new job
            
            
        } catch (Throwable ex) {

            logger.error("Exception in test : " + ex);
        }
    }// end func

    /**
     * fake a NPE of logger; do not forget to reset it or other tests will fail.
     */
    @Test
    public void testException() {
        ActorRef asyncWorker = null;
        try {
            PingWorker.setLogger(null);
            
            int actorMaxOperationTimeoutSec = 15;
            asyncWorker = ActorConfig.createAndGetActorSystem().actorOf(
                    Props.create(PingWorker.class, actorMaxOperationTimeoutSec,
                            getPingMetaSample(), "www.google.com"));

            final FiniteDuration duration = Duration.create(20,
                    TimeUnit.SECONDS);
            Future<Object> future = Patterns
                    .ask(asyncWorker, RequestWorkerMsgType.CANCEL,
                            new Timeout(duration));
            ResponseOnSingeRequest response = (ResponseOnSingeRequest) Await
                    .result(future, duration);


            logger.info("\nWorker response:" + response.toString());
        } catch (Throwable ex) {
            logger.error("Exception in test : " + ex);
        }
        PingWorker.setLogger(LoggerFactory.getLogger(PingWorker.class));
    }// end func

    @Test
    public void testTimeoutException() {
        ActorRef asyncWorker = null;
        try {
            // Start new job
            
            // made a timeout
            int actorMaxOperationTimeoutSec = 0;
            asyncWorker = ActorConfig.createAndGetActorSystem().actorOf(
                    Props.create(PingWorker.class, actorMaxOperationTimeoutSec,
                            getPingMetaSample(), "www.google.com"));

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
    public void testTcpWorkerBadMsgType() {
        
        logger.info("IN testTcpWorkerBadMsgType");
        ActorRef asyncWorker = null;
        try {
            int actorMaxOperationTimeoutSec = 15;
            asyncWorker = ActorConfig.createAndGetActorSystem().actorOf(
                    Props.create(PingWorker.class, actorMaxOperationTimeoutSec,
                            getPingMetaSample(), "www.google.com"));

            final FiniteDuration duration = Duration.create(20,
                    TimeUnit.SECONDS);
            Future<Object> future = Patterns
                    .ask(asyncWorker, new Integer(0),
                            new Timeout(duration));
            ResponseOnSingeRequest response = (ResponseOnSingeRequest) Await
                    .result(future, duration);

            logger.info("\nWorker response:" + response.toString());
        } catch (Throwable ex) {

            logger.error("Exception in test : " + ex);
        }
    }// end func

}
