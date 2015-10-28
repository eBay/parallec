package io.parallec.core.main.tcp;

import io.parallec.core.ParallelClient;
import io.parallec.core.TestBase;
import io.parallec.core.actor.ActorConfig;
import io.parallec.core.actor.TcpWorker;
import io.parallec.core.actor.message.ResponseOnSingeRequest;
import io.parallec.core.actor.message.type.RequestWorkerMsgType;
import io.parallec.core.bean.tcp.TcpMeta;
import io.parallec.core.main.tcp.sampleserver.TcpServerThread;
import io.parallec.core.resources.TcpSshPingResourceStore;

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

public class TcpWorkerTest extends TestBase {

    private static ParallelClient pc;
    private static TcpServerThread serverThread;
    
    @BeforeClass
    public static void setUp() throws Exception {
        pc = new ParallelClient();

        serverThread = new TcpServerThread(false);
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
            Thread.sleep(500L);
        } catch (Exception e) {
            ;
        }
    }

    public TcpMeta getTcpMetaSample(){
        TcpMeta tcpMeta = new TcpMeta("hadoop", 10081,1000, 2 
                , TcpSshPingResourceStore.getInstance().getChannelFactory() );
        return tcpMeta;
    }
    
    @Test
    public void testTcpWorkerNormalCheckComplete() {
        ActorRef asyncWorker = null;
        logger.info("IN testTcpWorkerNormalCheckComplete");
        try {
            // Start new job
            

            int actorMaxOperationTimeoutSec = 15;
            asyncWorker = ActorConfig.createAndGetActorSystem().actorOf(
                    Props.create(TcpWorker.class, actorMaxOperationTimeoutSec,
                            getTcpMetaSample(), LOCALHOST));

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
    public void testTcpWorkerDupAndCancel() {
        ActorRef asyncWorker = null;
        logger.info("IN testTcpWorkerDupAndCancel");
        try {
            
            // Start new job
            

            int actorMaxOperationTimeoutSec = 15;
            asyncWorker = ActorConfig.createAndGetActorSystem().actorOf(
                    Props.create(TcpWorker.class, actorMaxOperationTimeoutSec,
                            getTcpMetaSample(), LOCALHOST));

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
    public void testTcpWorkerException() {
        logger.info("IN testTcpWorkerException");
        ActorRef asyncWorker = null;
        try {
            TcpWorker.setLogger(null);
            
            // Start new job
            int actorMaxOperationTimeoutSec = 15;
            asyncWorker = ActorConfig.createAndGetActorSystem().actorOf(
                    Props.create(TcpWorker.class, actorMaxOperationTimeoutSec,
                            getTcpMetaSample(), LOCALHOST));
            
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
        TcpWorker.setLogger(LoggerFactory.getLogger(TcpWorker.class));
    }// end func



    @Test
    public void testTcpWorkerBadMsgType() {
        
        logger.info("IN testTcpWorkerBadMsgType");
        ActorRef asyncWorker = null;
        try {
            // made a timeout
            int actorMaxOperationTimeoutSec = 15;
            asyncWorker = ActorConfig.createAndGetActorSystem().actorOf(
                    Props.create(TcpWorker.class, actorMaxOperationTimeoutSec,
                            getTcpMetaSample(), LOCALHOST));
            
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
