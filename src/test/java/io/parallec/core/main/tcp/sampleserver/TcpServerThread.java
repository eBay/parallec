package io.parallec.core.main.tcp.sampleserver;

import java.util.logging.Logger;

public class TcpServerThread extends Thread {
    private static Logger logger = Logger.getLogger(TcpServerThread.class
            .getName());
    private volatile boolean shutdown = false;

    private boolean idle;
    public TcpServerThread(boolean idle){
        this.idle = idle;
    }
    @Override
    public void run() {

        try {
            TcpEchoServer server = new TcpEchoServer(idle);
            server.serve();
            while (!this.isShutdown()) {
                ;
            }
            server.stop();
            logger.info("TCP Server Stopped..");
        } catch (Exception e) {
            System.err.println("Couldn't start TCP server:\n" + e);
        }

    }

    public boolean isShutdown() {
        return shutdown;
    }

    //interrupt is useful
    public void setShutdown(boolean shutdown) {
        this.shutdown = shutdown;
        this.interrupt();
    }

}
