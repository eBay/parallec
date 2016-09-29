package io.parallec.core.actor;

import io.parallec.core.ParallelClient;
import io.parallec.core.RequestProtocol;
import io.parallec.core.TestBase;
import io.parallec.core.actor.message.type.RequestWorkerMsgType;
import io.parallec.core.bean.TaskRequest;
import io.parallec.core.resources.HttpClientStore;
import io.parallec.core.resources.HttpMethod;

import java.util.HashMap;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import akka.actor.ActorRef;
import akka.actor.Props;

public class OperationWorkerTest extends TestBase {

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
     * a request. expected to
     */
    @Test
    public void testOperationWorkerWrongMsgType() {
        ActorRef asyncWorker = null;
        try {
            // Start new job
            

            int actorMaxOperationTimeoutSec = 15;

            asyncWorker = ActorConfig.createAndGetActorSystem().actorOf(
                    Props.create(
                            OperationWorker.class,
                            new TaskRequest(actorMaxOperationTimeoutSec,
                                    RequestProtocol.valueOf("HTTP"
                                            .toUpperCase()),
                                    "www.parallec.io", null, 80, "", "",
                                    HttpMethod.GET, false, null, 
                                    null,
                                    new HashMap<String, Object>(),
                                    null,null, null,null,null), HttpClientStore
                                    .getInstance().getEmbedClientFast(), null

                    ));
            // bad type
            asyncWorker.tell(RequestWorkerMsgType.CANCEL, asyncWorker);

        } catch (Throwable ex) {
            logger.error("Exception in test : " + ex);
            ex.printStackTrace();
        }
    }// end func

}
