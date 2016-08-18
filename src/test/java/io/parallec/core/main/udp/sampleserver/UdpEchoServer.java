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
package io.parallec.core.main.udp.sampleserver;


import io.parallec.core.util.PcStringUtils;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Sample TCP Echo Server on port 10081. 
 * will echo back 3 lines of response that include the request string, 
 * then auto close the connection
 * 
 * @author Yuanteng (Jeff) Pei
 */
public class UdpEchoServer {

    /** The Constant logger. */
    protected static final Logger logger = LoggerFactory
            .getLogger(UdpEchoServer.class);

    /** The server socket. */
    private DatagramSocket serverSocket;
    

    
    /** The port. */
    private int port = 10091;

    /** The similateSlowResponse. */
    private boolean similateSlowResponse;
    
    /**
     * Instantiates a new tcp server.
     *
     * @param similateSlowResponse the similateSlowResponse
     */
    public UdpEchoServer(boolean similateSlowResponse) {
        try {
            
            this.similateSlowResponse = similateSlowResponse;
            
            setServer(new DatagramSocket(port));
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
            UdpEchoServer server = new UdpEchoServer(false);
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
        
        try {
            logger.info("UDP Echo Server Started on port " 
                    +port
                    + " . \nWaiting for connection.....");
            while (true) {
                byte[] receiveData = new byte[512];
                byte[] sendData = new byte[512];
                
                if(this.similateSlowResponse ) {
                    logger.info("similateSlowResponse is on. start to sleep 4 sec");
                    Thread.sleep(1000*4L);
                }
                
                
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                serverSocket.receive(receivePacket);

                logger.info("Waiting for input.....");
                String inputLine = new String( receivePacket.getData());
                inputLine = inputLine.replaceAll("[\n\r]", "");
                StringBuilder sendContentSb = new StringBuilder();
                if(inputLine!=null){
                    
                    logger.info("Server: " + inputLine);
                    for(int i=1; i<=1; i++){
                        String msg ="L: "+i+ " " +inputLine +" AT_UDP_SERVER  ";
                        sendContentSb.append(msg);
                        logger.info(msg);
                    }
                }
                
                int senderPort = receivePacket.getPort();
                
                InetAddress senderAddress = receivePacket.getAddress();
                
                sendData = sendContentSb.toString().getBytes();
                        
                DatagramPacket sendPacket =
                new DatagramPacket(sendData, sendData.length, senderAddress, senderPort);
                serverSocket.send(sendPacket);
                
                

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
    public DatagramSocket getServer() {
        return getServerSocket();
    }

    /**
     * Sets the server.
     *
     * @param server the new server
     */
    public void setServer(DatagramSocket server) {
        this.setServerSocket(server);
    }


    public DatagramSocket getServerSocket() {
        return serverSocket;
    }


    public void setServerSocket(DatagramSocket serverSocket) {
        this.serverSocket = serverSocket;
    }
}
