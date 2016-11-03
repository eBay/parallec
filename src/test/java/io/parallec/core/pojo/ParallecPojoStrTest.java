package io.parallec.core.pojo;

import io.parallec.core.ParallelTaskBuilder;
import io.parallec.core.ResponseOnSingleTask;
import io.parallec.core.TestBase;
import io.parallec.core.actor.message.NodeReqResponse;
import io.parallec.core.actor.poll.PollerData;
import io.parallec.core.app.ParallecAppMin;
import io.parallec.core.bean.HttpMeta;
import io.parallec.core.bean.SetAndCount;
import io.parallec.core.bean.StrStrMap;
import io.parallec.core.bean.tcp.TcpMeta;
import io.parallec.core.bean.udp.UdpMeta;
import io.parallec.core.exception.HttpRequestCreateException;
import io.parallec.core.exception.ParallelTaskInvalidException;
import io.parallec.core.monitor.MonitorProvider;
import io.parallec.core.resources.AsyncHttpClientFactoryEmbed.CustomTrustManager;
import io.parallec.core.resources.HttpMethod;
import io.parallec.core.resources.TcpUdpSshPingResourceStore;
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

import java.util.HashMap;
import java.util.HashSet;

import org.junit.Test;

public class ParallecPojoStrTest extends TestBase {

    @Test
    public void testMetaValidationToString() {

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

        TcpMeta tcpMeta = new TcpMeta("", 80, 1000, 5, null);
        tcpMeta.toString();

        // empty and all pass validation: test validation
        TcpMeta tcpMeta2 = new TcpMeta();
        TcpMeta tcpMeta3 = new TcpMeta("", 80, 1000, 5,
                TcpUdpSshPingResourceStore.getInstance().getChannelFactory());

        try {

            tcpMeta2.validation();
        } catch (ParallelTaskInvalidException e) {
            logger.info("expected exception {}", e.getLocalizedMessage());
        }
        tcpMeta2.setCommand("");
        try {

            tcpMeta2.validation();
        } catch (ParallelTaskInvalidException e) {
            logger.info("expected exception {}", e.getLocalizedMessage());
        }

        tcpMeta3.validation();

        SetAndCount sc = new SetAndCount(new HashSet<String>());
        sc.toString();

        // udp meta validation
        UdpMeta udpMeta = new UdpMeta("", 80, 5, null);
        udpMeta.toString();

        // empty and all pass validation: test validation
        UdpMeta udpMeta2 = new UdpMeta();
        UdpMeta udpMeta3 = new UdpMeta("", 80, 5, TcpUdpSshPingResourceStore
                .getInstance().getDatagramChannelFactory());

        // null command
        try {

            udpMeta2.validation();
        } catch (ParallelTaskInvalidException e) {
            logger.info("expected exception {}", e.getLocalizedMessage());
        }
        // now null port
        udpMeta2.setCommand("");
        try {

            udpMeta2.validation();
        } catch (ParallelTaskInvalidException e) {
            logger.info("expected exception {}", e.getLocalizedMessage());
        }
        // now with null idle
        udpMeta2.setUdpPort(40);
        try {
            udpMeta2.validation();
        } catch (ParallelTaskInvalidException e) {
            logger.info("expected exception {}", e.getLocalizedMessage());
        }

        udpMeta3.validation();
    }

    @Test
    public void testStaticFuncUtilsClassAndMisc() {

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
        StrStrMap ssm = new StrStrMap(new HashMap<String, String>());
        ssm.addPair(null, "");
        ssm.addPair("", null);
        ssm.addPair("k", "v");

        // misc
        new HttpRequestCreateException("", new RuntimeException());
        new ParallelTaskInvalidException("", new RuntimeException());

        boolean removeDuplicate = false;
        PcTargetHostsUtils.getNodeListFromStringLineSeperateOrSpaceSeperate(
                "a b", removeDuplicate);

        PcStringUtils.printStackTrace(null);
        ResponseOnSingleTask task = new ResponseOnSingleTask();
        task.getHost();

        logger.info("thread count {}", MonitorProvider.getInstance()
                .getLiveThreadCount());

        CustomTrustManager manager = new CustomTrustManager();
        manager.checkClientTrusted(null, null);
        manager.checkServerTrusted(null, null);
        manager.getAcceptedIssuers();
        
        
        
    }

}
