package io.parallec.core.actor;

import io.parallec.core.ParallelClient;
import io.parallec.core.TestBase;
import io.parallec.core.actor.message.ResponseOnSingeRequest;
import io.parallec.core.actor.message.type.RequestWorkerMsgType;
import io.parallec.core.resources.HttpClientStore;
import io.parallec.core.resources.HttpMethod;

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

public class HttpWorkerTest extends TestBase {

    private static ParallelClient pc;

    @BeforeClass
    public static void setUp() throws Exception {
        pc = new ParallelClient();
    }

    @AfterClass
    public static void shutdown() throws Exception {
        pc.releaseExternalResources();
    }

    /**
     * fake a bad request
     */
    @Test
    public void testHttpWorkerCreateRequestException() {
        ActorRef asyncWorker = null;
        try {
            // Start new job
            
            int actorMaxOperationTimeoutSec = 15;
            String urlComplete = "http://www.parallec.io/v**``\"..,++08alidateInternals.html";
            asyncWorker = ActorConfig.createAndGetActorSystem().actorOf(
                    Props.create(HttpWorker.class, actorMaxOperationTimeoutSec,
                            HttpClientStore.getInstance()
                                    .getCurrentDefaultClient(), urlComplete,
                            HttpMethod.GET, "", null,null));
            ;

            final FiniteDuration duration = Duration.create(20,
                    TimeUnit.SECONDS);
            Future<Object> future = Patterns
                    .ask(asyncWorker, RequestWorkerMsgType.PROCESS_REQUEST,
                            new Timeout(duration));

            ResponseOnSingeRequest response = (ResponseOnSingeRequest) Await
                    .result(future, duration);

            logger.info("\nWorker response:" + response.toString());
        } catch (Throwable ex) {
            logger.error("Exception in test: " + ex);
        }
    }// end func

    @Test
    public void testHttpWorkerNormalCheckComplete() {
        ActorRef asyncWorker = null;
        try {
            // Start new job
            
            int actorMaxOperationTimeoutSec = 15;
            String urlComplete = "http://www.parallec.io/validateInternals.html";
            pc.getHttpClientStore();
            asyncWorker = ActorConfig.createAndGetActorSystem().actorOf(
                    Props.create(HttpWorker.class, actorMaxOperationTimeoutSec,
                            HttpClientStore.getInstance()
                                    .getCurrentDefaultClient(), urlComplete,
                            HttpMethod.GET, "", null,null));

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
    public void testHttpWorkerDupAndCancel() {
        ActorRef asyncWorker = null;
        try {
            // Start new job
            

            int actorMaxOperationTimeoutSec = 15;
            String urlComplete = "http://www.parallec.io/validateInternals.html";
            pc.getHttpClientStore();
            asyncWorker = ActorConfig.createAndGetActorSystem().actorOf(
                    Props.create(HttpWorker.class, actorMaxOperationTimeoutSec,
                            HttpClientStore.getInstance()
                                    .getCurrentDefaultClient(), urlComplete,
                            HttpMethod.GET, "", null,null));

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
    public void testHttpWorkerException() {
        ActorRef asyncWorker = null;
        try {
            // Start new job
            
            int actorMaxOperationTimeoutSec = 15;
            HttpWorker.setLogger(null);
            String urlComplete = "http://www.parallec.io/validateInternals.html";
            asyncWorker = ActorConfig.createAndGetActorSystem().actorOf(
                    Props.create(HttpWorker.class, actorMaxOperationTimeoutSec,
                            HttpClientStore.getInstance()
                                    .getCurrentDefaultClient(), urlComplete,
                            HttpMethod.GET, "", null,null));
            ;

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
        HttpWorker.setLogger(LoggerFactory.getLogger(HttpWorker.class));
    }// end func

    @Test
    public void testHttpWorkerTimeoutException() {
        ActorRef asyncWorker = null;
        try {
            // Start new job
            
            // made a timeout
            int actorMaxOperationTimeoutSec = 0;
            String urlComplete = "http://www.parallec.io/validateInternals.html";
            pc.getHttpClientStore();
            asyncWorker = ActorConfig.createAndGetActorSystem().actorOf(
                    Props.create(HttpWorker.class, actorMaxOperationTimeoutSec,
                            HttpClientStore.getInstance()
                                    .getCurrentDefaultClient(), urlComplete,
                            HttpMethod.GET, "", null,null));

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
    public void testHttpWorkerBadMsgType() {
        ActorRef asyncWorker = null;
        try {
            String urlComplete = "http://www.parallec.io/validateInternals.html";
            pc.getHttpClientStore();
            int actorMaxOperationTimeoutSec = 0;
            asyncWorker = ActorConfig.createAndGetActorSystem().actorOf(
                    Props.create(HttpWorker.class, actorMaxOperationTimeoutSec,
                            HttpClientStore.getInstance()
                                    .getCurrentDefaultClient(), urlComplete,
                            HttpMethod.GET, "", null,null));

            final FiniteDuration duration = Duration.create(20,
                    TimeUnit.SECONDS);
            Future<Object> future = Patterns
                    .ask(asyncWorker, RequestWorkerMsgType.PROCESS_REQUEST,
                            new Timeout(duration));

            // test invalid type
            asyncWorker.tell(new Integer(0), asyncWorker);
            asyncWorker.tell(RequestWorkerMsgType.CHECK_FUTURE_STATE,
                    asyncWorker);
            ResponseOnSingeRequest response = (ResponseOnSingeRequest) Await
                    .result(future, duration);

            logger.info("\nWorker response:" + response.toString());
        } catch (Throwable ex) {

            logger.error("Exception in test : " + ex);
        }
        // made a timeout
    }// end func

}
