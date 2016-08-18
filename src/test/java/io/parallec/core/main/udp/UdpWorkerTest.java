package io.parallec.core.main.udp;

import io.parallec.core.ParallelClient;
import io.parallec.core.TestBase;
import io.parallec.core.actor.ActorConfig;
import io.parallec.core.actor.UdpWorker;
import io.parallec.core.actor.message.ResponseOnSingeRequest;
import io.parallec.core.actor.message.type.RequestWorkerMsgType;
import io.parallec.core.bean.udp.UdpMeta;
import io.parallec.core.main.udp.sampleserver.UdpServerThread;
import io.parallec.core.resources.TcpUdpSshPingResourceStore;

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

public class UdpWorkerTest extends TestBase {

    private static ParallelClient pc;
    private static UdpServerThread serverThread;
    
    @BeforeClass
    public static void setUp() throws Exception {
        pc = new ParallelClient();

        serverThread = new UdpServerThread(false);
        serverThread.start();

        try {
            Thread.sleep(500L);
        } catch (Exception e) {
            ;
        }
    }

    @AfterClass
    public static void shutdown() throws Exception {
        pc.releaseExternalResources();
        serverThread.setShutdown(true);

        try {
            Thread.sleep(2500L);
        } catch (Exception e) {
            ;
        }
    }

    public UdpMeta getUdpMetaSample(){
        UdpMeta udpMeta = new UdpMeta("hadoop", 10091,1000,  
                TcpUdpSshPingResourceStore.getInstance().getDatagramChannelFactory() );
        return udpMeta;
    }
    
    
    @Test
    public void testUdpWorkerNormalCheckComplete() {
        ActorRef asyncWorker = null;
        logger.info("IN testUdpWorkerNormalCheckComplete");
        try {
            // Start new job
            

            int actorMaxOperationTimeoutSec = 15;
            asyncWorker = ActorConfig.createAndGetActorSystem().actorOf(
                    Props.create(UdpWorker.class, actorMaxOperationTimeoutSec,
                            getUdpMetaSample(), LOCALHOST));

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
    public void testUdpWorkerActorTimeout() {
        ActorRef asyncWorker = null;
        logger.info("IN testUdpWorkerConnectionTimeout");
        try {
            // Start new job

            int actorMaxOperationTimeoutSec = 0;
            asyncWorker = ActorConfig.createAndGetActorSystem().actorOf(
                    Props.create(UdpWorker.class, actorMaxOperationTimeoutSec,
                            getUdpMetaSample(), LOCALHOST));
            
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
    public void testUdpWorkerDupAndCancel() {
        ActorRef asyncWorker = null;
        logger.info("IN testUdpWorkerDupAndCancel");
        try {
            
            // Start new job
            

            int actorMaxOperationTimeoutSec = 15;
            asyncWorker = ActorConfig.createAndGetActorSystem().actorOf(
                    Props.create(UdpWorker.class, actorMaxOperationTimeoutSec,
                            getUdpMetaSample(), LOCALHOST));

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
    public void testUdpWorkerException() {
        logger.info("IN testUdpWorkerException");
        ActorRef asyncWorker = null;
        try {
            UdpWorker.setLogger(null);
            
            // Start new job
            int actorMaxOperationTimeoutSec = 15;
            asyncWorker = ActorConfig.createAndGetActorSystem().actorOf(
                    Props.create(UdpWorker.class, actorMaxOperationTimeoutSec,
                            getUdpMetaSample(), LOCALHOST));
            
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
        UdpWorker.setLogger(LoggerFactory.getLogger(UdpWorker.class));
    }// end func

    /**
     * fake a NPE in bootStrapTcpClient
     */
    @Test
    public void testBootStrapUdpClient() {
        logger.info("IN testTcpWorkerException");
        ActorRef asyncWorker = null;
        try {
            
            // Start new job
            UdpMeta meta = getUdpMetaSample();
            meta.setChannelFactory(null);
            int actorMaxOperationTimeoutSec = 15;
            asyncWorker = ActorConfig.createAndGetActorSystem().actorOf(
                    Props.create(UdpWorker.class, actorMaxOperationTimeoutSec,
                            meta, LOCALHOST));
            
            final FiniteDuration duration = Duration.create(20,
                    TimeUnit.SECONDS);
            Future<Object> future = Patterns
                    .ask(asyncWorker, RequestWorkerMsgType.PROCESS_REQUEST,
                            new Timeout(duration));
            ResponseOnSingeRequest response = (ResponseOnSingeRequest) Await
                    .result(future, duration);

            

            logger.info("\nWorker response:" + response.toString());
        } catch (Throwable ex) {
        }
    }// end func

    
    @Test
    public void testUdpWorkerBadMsgTypeDefaultType() {
        
        logger.info("IN testUdpWorkerBadMsgTypeDefaultType");
        ActorRef asyncWorker = null;
        try {
            // made a timeout
            int actorMaxOperationTimeoutSec = 15;
            asyncWorker = ActorConfig.createAndGetActorSystem().actorOf(
                    Props.create(UdpWorker.class, actorMaxOperationTimeoutSec,
                            getUdpMetaSample(), LOCALHOST));
            
            final FiniteDuration duration = Duration.create(20,
                    TimeUnit.SECONDS);
            Future<Object> future = Patterns
                    .ask(asyncWorker, RequestWorkerMsgType.CHECK_FUTURE_STATE,
                            new Timeout(duration));
            ResponseOnSingeRequest response = (ResponseOnSingeRequest) Await
                    .result(future, duration);

            logger.info("\nWorker response:" + response.toString());
        } catch (Throwable ex) {

            logger.error("Exception in test : " + ex);
        }
    }// end func
    
    @Test
    public void testUdpWorkerBadMsgType() {
        
        logger.info("IN testUdpWorkerBadMsgType");
        ActorRef asyncWorker = null;
        try {
            // made a timeout
            int actorMaxOperationTimeoutSec = 15;
            asyncWorker = ActorConfig.createAndGetActorSystem().actorOf(
                    Props.create(UdpWorker.class, actorMaxOperationTimeoutSec,
                            getUdpMetaSample(), LOCALHOST));
            
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
