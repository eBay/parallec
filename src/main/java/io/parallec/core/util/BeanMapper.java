package io.parallec.core.util;

import io.parallec.core.ParallelTask;
import io.parallec.core.task.ParallelTaskBean;

import org.springframework.beans.BeanUtils;

/**
 * TODO
 * 
 * @author Yuanteng (Jeff) Pei
 *
 */
public class BeanMapper {

    public static void copy(final ParallelTask source,
            final ParallelTaskBean target) {
        BeanUtils.copyProperties(source, target, new String[] { "state",
                "logger", "responseContext", " handler", " parallelTaskResult",
                "executionManager", "replacementVarMapNodeSpecific",
                "replacementVarMap", "submitTime", "executeStartTime",
                "executionEndTime" });
    }
}
