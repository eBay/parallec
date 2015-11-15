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
package io.parallec.core.main.tcp.sampleserver;


import io.parallec.core.util.PcStringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Sample TCP Echo Server on port 10081. 
 * will echo back 3 lines of response that include the request string, 
 * then auto close the connection
 * 
 * @author Yuanteng (Jeff) Pei
 */
public class TcpEchoServer {

    /** The Constant logger. */
    protected static final Logger logger = LoggerFactory
            .getLogger(TcpEchoServer.class);

    /** The server socket. */
    private ServerSocket serverSocket;
    

    
    /** The port. */
    private int port = 10081;

    /** The idle. */
    private boolean idle;
    
    /**
     * Instantiates a new tcp server.
     *
     * @param idle the idle
     */
    public TcpEchoServer(boolean idle) {
        try {
            
            this.idle = idle;
            
            setServer(new ServerSocket(port));
        } catch (Exception ex) {
            System.err.println("Could not listen on port: "
                    + port + " " + PcStringUtils.printStackTrace(ex));
        }
    }


    /**
     * The main method.
     *
     * @param args the arguments
     */
    public static void main(String[] args) {
        try {
            TcpEchoServer server = new TcpEchoServer(false);
            server.serve();
            
            
        } catch (Exception e) {
            System.err.println("Couldn't start server:\n" + e);
        }
    }
    
    /**
     * Stop.
     */
    public void stop() {
        try{
            
            getServerSocket().close();
        }catch(Exception e){
            logger.error("error in stop server socket ", e);
        }
    }

    /**
     * Serve.
     *
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public void serve() throws IOException {

        Socket clientSocket = null;
        try {
            while (true) {

                System.out.println("TCP Echo Server Started on port " 
                        +port
                        + " . \nWaiting for connection.....");
                clientSocket = getServerSocket().accept();
                logger.info("Client Connection successful");
                logger.info("Waiting for input.....");

                PrintWriter out = new PrintWriter(
                        clientSocket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(
                        clientSocket.getInputStream()));

                String inputLine;
                //just read 1 single line then auto close
                if ((inputLine = in.readLine()) != null) {
                    logger.info("Server: " + inputLine);
                  
                    for(int i=1; i<=3; i++){
                        String msg ="L: "+i+ " " +inputLine +" AT_TCP_SERVER  ";
                        out.println(msg);
                        logger.info(msg);
                    }
                    
                }
                //whether to close after a single request.
                // the interrupt is critical otherwise cannot easily shutdown
                if(!this.idle ) {
                    out.close();
                    clientSocket.close();
                    in.close();
                }else{
                    Thread.sleep(10L);
                }

            }//end for loop
        } catch (IOException  e) {
            logger.error("Exception in echo server. "
                    + "\nExpected when shutdown. {}", e.getLocalizedMessage());
        } catch ( InterruptedException e) {
            logger.error("Exception in echo server. "
                    + "\nExpected when shutdown. {}", e.getLocalizedMessage());
        } finally{
            if(getServerSocket()!=null && !getServerSocket().isClosed())
                getServerSocket().close();
            
        }

    }

    /**
     * Gets the server.
     *
     * @return the server
     */
    public ServerSocket getServer() {
        return getServerSocket();
    }

    /**
     * Sets the server.
     *
     * @param server the new server
     */
    public void setServer(ServerSocket server) {
        this.setServerSocket(server);
    }


    public ServerSocket getServerSocket() {
        return serverSocket;
    }


    public void setServerSocket(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }
}
