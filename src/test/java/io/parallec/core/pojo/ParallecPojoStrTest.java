package io.parallec.core.pojo;

import io.parallec.core.ParallelTaskBuilder;
import io.parallec.core.TestBase;
import io.parallec.core.actor.message.NodeReqResponse;
import io.parallec.core.actor.poll.PollerData;
import io.parallec.core.app.ParallecAppMin;
import io.parallec.core.bean.HttpMeta;
import io.parallec.core.bean.tcp.TcpMeta;
import io.parallec.core.resources.HttpMethod;
import io.parallec.core.task.TaskErrorMeta;
import io.parallec.core.task.TaskErrorMeta.TaskErrorType;
import io.parallec.core.util.BeanMapper;
import io.parallec.core.util.PcConstants;
import io.parallec.core.util.PcDateUtils;
import io.parallec.core.util.PcErrorMsgUtils;
import io.parallec.core.util.PcFileNetworkIoUtils;
import io.parallec.core.util.PcHttpUtils;
import io.parallec.core.util.PcNumberUtils;
import io.parallec.core.util.PcStringUtils;
import io.parallec.core.util.PcTargetHostsUtils;

import org.junit.Test;

public class ParallecPojoStrTest extends TestBase {

    @Test
    public void testToString() {

        PollerData pollerData = new PollerData();
        logger.info(pollerData.toString());

        ParallelTaskBuilder pTaskBuilder = new ParallelTaskBuilder();
        logger.info(pTaskBuilder.toString());

        NodeReqResponse nodeReqResponse = new NodeReqResponse("hostname1");
        logger.info(nodeReqResponse.toString());

        HttpMeta httpMeta = new HttpMeta();
        logger.info(httpMeta.toString());
        
        HttpMeta httpMeta2 = new HttpMeta(HttpMethod.GET, "", "", "", null);
        httpMeta2.toString();
        
        TcpMeta tcpMeta = new TcpMeta("", 80, 5, 1000, null);
        
        tcpMeta.toString();
        

    }

    @Test
    public void testStaticFuncAndUtilsClass() {

        new ParallecAppMin();
        ParallecAppMin.main(null);

        new BeanMapper();
        new PcConstants();
        new PcDateUtils();
        new PcErrorMsgUtils();
        new PcHttpUtils();
        new PcStringUtils();
        new PcTargetHostsUtils();
        new PcFileNetworkIoUtils();
        new PcNumberUtils();
        new TaskErrorMeta(TaskErrorType.COMMAND_MANAGER_ERROR, "", null);
    }

}
