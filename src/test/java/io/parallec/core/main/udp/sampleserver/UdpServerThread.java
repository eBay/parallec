package io.parallec.core.main.udp.sampleserver;

import io.parallec.core.util.PcStringUtils;

import java.util.logging.Logger;

public class UdpServerThread extends Thread {
    private static Logger logger = Logger.getLogger(UdpServerThread.class
            .getName());
    private volatile boolean shutdown = false;

    private boolean similateSlowResponse;
    public UdpServerThread(boolean similateSlowResponse){
        this.similateSlowResponse = similateSlowResponse;
    }
    private UdpEchoServer server = null;
    @Override
    public void run() {

        try {
            server = new UdpEchoServer(similateSlowResponse);
            server.serve();
            while (!this.isShutdown()) {
                ;
            }
            server.stop();
            logger.info("UDP Server Stopped..");
        } catch (Exception e) {
            logger.info("Couldn't start UDP server: " + PcStringUtils.printStackTrace(e));
        }

    }

    public boolean isShutdown() {
        return shutdown;
    }

    //interrupt is useful
    public void setShutdown(boolean shutdown) {
        this.shutdown = shutdown;
        
        if(shutdown){
            try {
                this.server.getServerSocket().close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        this.interrupt();
    }

}
