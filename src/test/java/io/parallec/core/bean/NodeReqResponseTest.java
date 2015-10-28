package io.parallec.core.bean;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import io.parallec.core.TestBase;
import io.parallec.core.actor.message.NodeReqResponse;

public class NodeReqResponseTest extends TestBase {

    @Test
    public void testReplaceVar() {
        Map<String, String> requestParameters = new HashMap<String, String>();
        requestParameters.put("REPLACE-VAR_AGENT_VERSION", "0.1.911");

        requestParameters.put("REPLACE-VAR_updateWisb", "WWWWWWWII");

        // String sourceContent =
        // "{\"manifest\": \"agent_selfupdate-$AGENT_VERSION\", \"updateWisb\":\"True\"}";
        String sourceContent = "{\"manifest\": \"agent_selfupdate-$AGENT_VERSION\", \"$updateWisb\":\"True\"}";

        String afterReplacement = NodeReqResponse.replaceStrByMap(
                requestParameters, sourceContent);

        logger.info(afterReplacement);
    }

}
