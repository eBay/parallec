/*  
Copyright [2013-2015] eBay Software Foundation
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at
    http://www.apache.org/licenses/LICENSE-2.0
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */
package io.parallec.core.pojo;

import io.parallec.core.FilterRegex;
import io.parallec.core.ParallelClient;
import io.parallec.core.ParallelTask;
import io.parallec.core.ParallelTaskBuilder;
import io.parallec.core.ResponseOnSingleTask;
import io.parallec.core.actor.ActorConfig;
import io.parallec.core.actor.message.CancelTaskOnHostRequest;
import io.parallec.core.actor.message.ContinueToSendToBatchSenderAsstManager;
import io.parallec.core.actor.message.InitialRequestToManager;
import io.parallec.core.actor.message.NodeReqResponse;
import io.parallec.core.actor.message.RequestToBatchSenderAsstManager;
import io.parallec.core.actor.message.ResponseCountToBatchSenderAsstManager;
import io.parallec.core.actor.message.ResponseFromManager;
import io.parallec.core.actor.message.ResponseOnSingeRequest;
import io.parallec.core.actor.poll.HttpPollerProcessor;
import io.parallec.core.actor.poll.PollerData;
import io.parallec.core.bean.HttpMeta;
import io.parallec.core.bean.ResponseHeaderMeta;
import io.parallec.core.bean.SetAndCount;
import io.parallec.core.bean.StrStrMap;
import io.parallec.core.bean.TargetHostMeta;
import io.parallec.core.bean.TaskRequest;
import io.parallec.core.bean.ping.PingMeta;
import io.parallec.core.bean.ssh.SshMeta;
import io.parallec.core.bean.tcp.TcpMeta;
import io.parallec.core.bean.udp.UdpMeta;
import io.parallec.core.commander.workflow.InternalDataProvider;
import io.parallec.core.commander.workflow.VarReplacementProvider;
import io.parallec.core.commander.workflow.ssh.SshProvider;
import io.parallec.core.config.ParallelTaskConfig;
import io.parallec.core.exception.ExecutionManagerExecutionException;
import io.parallec.core.exception.TcpUdpRequestCreateException;
import io.parallec.core.main.http.pollable.sampleserver.HttpServerThread;
import io.parallec.core.main.http.pollable.sampleserver.ServerWithPollableJobs.NanoJob;
import io.parallec.core.resources.HttpClientStore;
import io.parallec.core.resources.TcpUdpSshPingResourceStore;
import io.parallec.core.task.ParallelTaskBean;
import io.parallec.core.task.TaskErrorMeta;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.openpojo.reflection.PojoClass;
import com.openpojo.reflection.impl.PojoClassFactory;
import com.openpojo.validation.PojoValidator;
import com.openpojo.validation.rule.Rule;
import com.openpojo.validation.test.Tester;
import com.openpojo.validation.test.impl.GetterTester;
import com.openpojo.validation.test.impl.SetterTester;

/**
 * The Class ParallecPojoClassTest. build on top of library OpenPojo
 * and enhancement by Murthy, Sudheendra
 */
@SuppressWarnings("deprecation")
public class ParallecPojoClassTest {

    /**
     * Unit Test the POJO classes.
     *
     * @throws ClassNotFoundException
     *             the class not found exception
     * @throws InstantiationException
     *             the instantiation exception
     * @throws IllegalAccessException
     *             the illegal access exception
     */

    @Test
    public void testPojoStructureAndBehavior() throws ClassNotFoundException,
            InstantiationException, IllegalAccessException {
        final PojoValidator pojoValidator = new PojoValidator();
        for (final Class<? extends Rule> ruleClass : getValidationRules()) {
            final Rule rule = ruleClass.newInstance();
            pojoValidator.addRule(rule);
        }

        // Load tester classes
        for (Class<? extends Tester> testerClass : getTesters()) {
            final Tester testerInstance = testerClass.newInstance();
            pojoValidator.addTester(testerInstance);
        }

        for (final Class<?> c : getPOJOClasses()) {
            final PojoClass pojoClass = PojoClassFactory.getPojoClass(c);

            pojoValidator.runValidation(pojoClass);
        }
    }

    /**
     * Gets the POJO classes.
     *
     * @return the POJO classes
     * @throws ClassNotFoundException
     *             the class not found exception
     */
    public List<Class<?>> getPOJOClasses() throws ClassNotFoundException {
        List<Class<?>> pojoClasses = new ArrayList<Class<?>>();
        pojoClasses.add(HttpMeta.class);
        pojoClasses.add(TargetHostMeta.class);
        pojoClasses.add(NodeReqResponse.class);
        pojoClasses.add(StrStrMap.class);
        pojoClasses.add(TaskRequest.class);

        pojoClasses.add(ResponseOnSingleTask.class);
        pojoClasses.add(PollerData.class);
        pojoClasses.add(ActorConfig.class);

        pojoClasses.add(ResponseFromManager.class);
        pojoClasses.add(ContinueToSendToBatchSenderAsstManager.class);
        pojoClasses.add(InitialRequestToManager.class);

        pojoClasses.add(RequestToBatchSenderAsstManager.class);

        pojoClasses.add(ResponseCountToBatchSenderAsstManager.class);
        pojoClasses.add(InternalDataProvider.class);
        pojoClasses.add(VarReplacementProvider.class);
        pojoClasses.add(ParallelTaskBuilder.class);

        pojoClasses.add(TaskRequest.class);
        pojoClasses.add(ResponseOnSingleTask.class);
        pojoClasses.add(ParallelTask.class);
        pojoClasses.add(ParallelClient.class);
        pojoClasses.add(HttpPollerProcessor.class);
        pojoClasses.add(TaskErrorMeta.class);
        pojoClasses.add(FilterRegex.class);
        
        pojoClasses.add(SshProvider.class);
        pojoClasses.add(NanoJob.class);
        pojoClasses.add(HttpServerThread.class);

        pojoClasses.add(TaskErrorMeta.class);
        pojoClasses.add(ParallelTaskBean.class);
        pojoClasses.add(ParallelTaskConfig.class);
        pojoClasses.add(CancelTaskOnHostRequest.class);
        
        pojoClasses.add(TcpMeta.class);
        pojoClasses.add(SshMeta.class);
        pojoClasses.add(PingMeta.class);
        pojoClasses.add(UdpMeta.class);
        
        pojoClasses.add(SetAndCount.class);
        
        pojoClasses.add(ExecutionManagerExecutionException.class);
        pojoClasses.add(TcpUdpRequestCreateException.class);
        pojoClasses.add(TcpUdpSshPingResourceStore.class);
        
        pojoClasses.add(ResponseHeaderMeta.class);
        
        pojoClasses.add(HttpClientStore.class);
        pojoClasses.add(ResponseOnSingeRequest.class);
        
        return pojoClasses;
    }

    /**
     * Gets the validation rules.
     *
     * @return the validation rules
     */
    public List<Class<? extends Rule>> getValidationRules() {
        List<Class<? extends Rule>> ruleClasses = new ArrayList<Class<? extends Rule>>();

        return ruleClasses;
    }

    /**
     * Gets the testers.
     *
     * @return the testers
     */
    public List<Class<? extends Tester>> getTesters() {
        // Testers to validate behavior for POJO_PACKAGE
        List<Class<? extends Tester>> testers = new ArrayList<Class<? extends Tester>>();
        testers.add(SetterTester.class);
        testers.add(GetterTester.class);

        return testers;
    }

}
