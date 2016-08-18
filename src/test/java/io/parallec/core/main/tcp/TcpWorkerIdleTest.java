package io.parallec.core.main.tcp;

import io.parallec.core.ParallelClient;
import io.parallec.core.TestBase;
import io.parallec.core.actor.ActorConfig;
import io.parallec.core.actor.TcpWorker;
import io.parallec.core.actor.message.ResponseOnSingeRequest;
import io.parallec.core.actor.message.type.RequestWorkerMsgType;
import io.parallec.core.bean.tcp.TcpMeta;
import io.parallec.core.main.tcp.sampleserver.TcpServerThread;
import io.parallec.core.resources.TcpUdpSshPingResourceStore;

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

public class TcpWorkerIdleTest extends TestBase {

    private static ParallelClient pc;
    private static TcpServerThread serverThread;
    
    @BeforeClass
    public static void setUp() throws Exception {
        pc = new ParallelClient();
        
        try {
            Thread.sleep(500L);
        } catch (Exception e) {
            ;
        }

        serverThread = new TcpServerThread(true);
        serverThread.start();

        try {
            Thread.sleep(2500L);
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
                , TcpUdpSshPingResourceStore.getInstance().getChannelFactory() );
        return tcpMeta;
    }
    
    /**
     * Timeout here: as if in the none idle one, will immediately return and not to timeout.
     */
    @Test
    public void testTcpWorkerTimeoutException() {
        ActorRef asyncWorker = null;
        logger.info("IN testTcpWorkerTimeoutException in idle");
        try {
            // Start new job
            
            // made a timeout
            int actorMaxOperationTimeoutSec = 0;
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
    public void testTcpWorkerNormalCheckCompleteForIdle() {
        ActorRef asyncWorker = null;
        logger.info("IN testTcpWorkerNormalCheckCompleteForIdle");
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

}
