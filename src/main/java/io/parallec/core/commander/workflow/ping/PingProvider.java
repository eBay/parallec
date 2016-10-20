package io.parallec.core.commander.workflow.ping;

import io.parallec.core.bean.ping.PingMeta;
import io.parallec.core.bean.ping.PingMode;

import java.net.InetAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PingProvider {
    /** The logger. */
    private static Logger logger = LoggerFactory.getLogger(PingProvider.class);

    private static PingProvider instance = new PingProvider();
    
    private PingProvider(){
    }
    
    public static PingProvider getInstance(){
        return instance;
    }
    
    public boolean isReachableByPing(String targetHost, PingMeta pingMeta) {
        
        boolean live = isReachableByPingHelper(targetHost, pingMeta);
        int retryLeft = pingMeta.getNumRetries();
        while(!live && retryLeft>0){
            live = isReachableByPingHelper(targetHost, pingMeta);
            retryLeft--;
        }
        return live;
        
    }
    
    public boolean isReachableByPingHelper(String targetHost, PingMeta pingMeta) {
        try {
            
            if(pingMeta.getMode()==PingMode.INET_ADDRESS_REACHABLE_NEED_ROOT){
                InetAddress address = InetAddress.getByName(targetHost);
                return address.isReachable(pingMeta.getPingTimeoutMillis());
            }else{
                String cmd;
                if (System.getProperty("os.name").startsWith("Windows")) {
                    // For Windows
                    cmd = "ping -n 1 " + targetHost;
                } else {
                    // For Linux and OSX
                    cmd = "ping -c 1 " + targetHost;
                }
                
                Process myProcess = Runtime.getRuntime().exec(cmd);
                myProcess.waitFor();
                if (myProcess.exitValue() == 0) {
                    return true;
                } else {
                    return false;
                }
                
            }
            
        } catch (Exception e) {
            logger.info("Bad hostname {} with err {} ", targetHost, e.getLocalizedMessage());
            return false;
        }
    }
}
