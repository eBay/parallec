package io.parallec.core.main.http.pollable.sampleserver;

import java.io.IOException;
import java.util.logging.Logger;

public class HttpServerThread extends Thread {
    private static Logger logger = Logger.getLogger(HttpServerThread.class
            .getName());
    private volatile boolean shutdown = false;

    @Override
    public void run() {

        try {
            ServerWithPollableJobs server = new ServerWithPollableJobs();
            while (!this.isShutdown()) {
                ;
            }
            server.stop();
            logger.info("Server Stopped..");
        } catch (IOException ioe) {
            System.err.println("Couldn't start server:\n" + ioe);
        }

    }

    public boolean isShutdown() {
        return shutdown;
    }

    public void setShutdown(boolean shutdown) {
        this.shutdown = shutdown;
    }

}
