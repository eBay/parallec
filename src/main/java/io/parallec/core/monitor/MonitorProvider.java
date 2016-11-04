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
package io.parallec.core.monitor;

import io.parallec.core.util.PcDateUtils;
import io.parallec.core.util.PcNumberUtils;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.GsonBuilder;

/**
 * The Class MonitorProvider.
 */
public class MonitorProvider {

    /** The logger. */
    @SuppressWarnings("unused")
    private static Logger logger = LoggerFactory
            .getLogger(MonitorProvider.class);

    /** The threshold percent. */
    public static int THRESHOLD_PERCENT = 90;

    /** The instance. */
    private static MonitorProvider instance = new MonitorProvider();

    /**
     * Gets the single instance of MonitorProvider.
     *
     * @return single instance of MonitorProvider
     */
    public static MonitorProvider getInstance() {
        return instance;
    }

    /**
     * Instantiates a new monitor provider.
     */
    private MonitorProvider() {
    }

    /** The current jvm perform usage. */
    public PerformUsage currentJvmPerformUsage;

    /**
     * Gets the JVM memory usage.
     *
     * @return the JVM memory usage
     */
    public PerformUsage getJVMMemoryUsage() {
        int mb = 1024 * 1024;
        Runtime rt = Runtime.getRuntime();
        PerformUsage usage = new PerformUsage();
        usage.totalMemory = (double) rt.totalMemory() / mb;
        usage.freeMemory = (double) rt.freeMemory() / mb;
        usage.usedMemory = (double) rt.totalMemory() / mb - rt.freeMemory()
                / mb;
        usage.maxMemory = (double) rt.maxMemory() / mb;
        usage.memoryUsagePercent = usage.usedMemory / usage.maxMemory * 100.0;

        // update current
        currentJvmPerformUsage = usage;
        return usage;
    }

    /**
     * Gets the thread dump.
     *
     * @return the thread dump
     */
    public ThreadInfo[] getThreadDump() {
        ThreadMXBean threadMxBean = ManagementFactory.getThreadMXBean();
        return threadMxBean.dumpAllThreads(true, true);
    }

    /**
     * Gets the live thread count.
     *
     * @return the live thread count
     */
    public int getLiveThreadCount() {
        return ManagementFactory.getThreadMXBean().getThreadCount();
    }

    /**
     * Gets the thread usage.
     *
     * @return the thread usage
     */
    public ThreadUsage getThreadUsage() {
        ThreadMXBean threadMxBean = ManagementFactory.getThreadMXBean();

        ThreadUsage threadUsage = new ThreadUsage();
        long[] threadIds = threadMxBean.getAllThreadIds();
        threadUsage.liveThreadCount = threadIds.length;

        for (long tId : threadIds) {
            ThreadInfo threadInfo = threadMxBean.getThreadInfo(tId);
            threadUsage.threadData.put(Long.toString(tId), new ThreadData(
                    threadInfo.getThreadName(), threadInfo.getThreadState()
                            .name(), threadMxBean.getThreadCpuTime(tId)));

        }
        return threadUsage;
    }

    /**
     * Gets the health memory.
     *
     * @return the health memory
     */
    public String getHealthMemory() {
        StringBuilder sb = new StringBuilder();
        sb.append("Logging JVM Stats\n");
        MonitorProvider mp = MonitorProvider.getInstance();
        PerformUsage perf = mp.getJVMMemoryUsage();
        sb.append(perf.toString());

        if (perf.memoryUsagePercent >= THRESHOLD_PERCENT) {
            sb.append("========= WARNING: MEM USAGE > " + THRESHOLD_PERCENT
                    + "!!");
            sb.append(" !! Live Threads List=============\n");
            sb.append(mp.getThreadUsage().toString());
            sb.append("========================================\n");
            sb.append("========================JVM Thread Dump====================\n");
            ThreadInfo[] threadDump = mp.getThreadDump();
            for (ThreadInfo threadInfo : threadDump) {
                sb.append(threadInfo.toString() + "\n");
            }
            sb.append("===========================================================\n");
        }
        sb.append("Logged JVM Stats\n");

        return sb.toString();
    }

    /**
     * The Class ThreadUsage.
     */
    public static class ThreadUsage extends Jsonable {

        /** The live thread count. */
        public int liveThreadCount;

        /** The thread data. */
        public Map<String, ThreadData> threadData = new HashMap<String, ThreadData>();
    }

    /**
     * The Class ThreadData.
     */
    public static class ThreadData extends Jsonable {

        /** The thread name. */
        public String threadName;

        /** The thread state. */
        public String threadState;

        /** The cpu time in nano seconds. */
        public long cpuTimeInNanoSeconds;

        /**
         * Instantiates a new thread data.
         *
         * @param threadName
         *            the thread name
         * @param threadState
         *            the thread state
         * @param cpuTimeInNanoSeconds
         *            the cpu time in nano seconds
         */
        public ThreadData(String threadName, String threadState,
                long cpuTimeInNanoSeconds) {
            this.threadName = threadName;
            this.threadState = threadState;
            this.cpuTimeInNanoSeconds = cpuTimeInNanoSeconds;
        }

    }

    /**
     * The Class PerformUsage.
     */
    public static class PerformUsage extends Jsonable {

        /** The date. */
        public String date = PcDateUtils.getNowDateTimeStrStandard();

        /** The total memory. */
        public double totalMemory;

        /** The free memory. */
        public double freeMemory;

        /** The used memory. */
        public double usedMemory;

        /** The max memory. */
        public double maxMemory;

        /** The memory usage percent. */
        public double memoryUsagePercent;

        /**
         * Gets the summary.
         *
         * @return the summary
         */
        public String getSummary() {
            return PcNumberUtils.getStringFromDouble(memoryUsagePercent)
                    + "% (" + PcNumberUtils.getStringFromDouble(usedMemory)
                    + "/" + PcNumberUtils.getStringFromDouble(totalMemory)
                    + ") Max " + PcNumberUtils.getStringFromDouble(maxMemory);
        }
    }

    /**
     * The Class Jsonable.
     */
    public abstract static class Jsonable {

        /*
         * (non-Javadoc)
         * 
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            return new GsonBuilder()
                    .excludeFieldsWithModifiers(Modifier.STATIC).create()
                    .toJson(this);
        }
    }

}
