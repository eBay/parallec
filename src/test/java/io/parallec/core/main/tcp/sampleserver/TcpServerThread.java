package io.parallec.core.main.tcp.sampleserver;

import io.parallec.core.util.PcStringUtils;

import java.io.IOException;
import java.util.logging.Logger;

public class TcpServerThread extends Thread {
    private static Logger logger = Logger.getLogger(TcpServerThread.class
            .getName());
    private volatile boolean shutdown = false;

    private boolean idle;
    public TcpServerThread(boolean idle){
        this.idle = idle;
    }
    private TcpEchoServer server = null;
    @Override
    public void run() {

        try {
            server = new TcpEchoServer(idle);
            server.serve();
            while (!this.isShutdown()) {
                ;
            }
            server.stop();
            logger.info("TCP Server Stopped..");
        } catch (Exception e) {
            logger.info("Couldn't start TCP server: " + PcStringUtils.printStackTrace(e));
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
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        this.interrupt();
    }

}
