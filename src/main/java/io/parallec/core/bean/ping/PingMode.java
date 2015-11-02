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
package io.parallec.core.bean.ping;

/**
 * The Enum of Ping Mode. Process or INET_ADDRESS_REACHABLE based.
 * Default as InetAddress mode. InetAddress requires Root privilege. 
 * @author Yuanteng (Jeff) Pei
 */
public enum PingMode {

    /** Start a process to run the real ping command from the OS 
     * current did not specify the timeout. 
     * This is slower due to creating process for each request that limits the concurrency.
     * Suggest maximum concurrency less than 400. 
     * if not adjust your ulimit. Only use this when you do not run as Root.*/
    PROCESS,
    
    /** The inet address reachable. please only use in linux model with Root privilege.
     *  This is the way faster approach.
     * This normally needs ROOT privilege to really use ICMP protocol. 
     * Test whether that address is reachable. 
     * Best effort is made by the implementation to try to reach the host, 
     * but firewalls and server configuration may block requests resulting in a unreachable status 
     * while some specific ports may be accessible. 
     * A typical implementation will use ICMP ECHO REQUESTs if the privilege can be obtained, 
     * otherwise it will try to establish a TCP connection on port 7 (Echo) of the destination host.
     * 
     * */
    INET_ADDRESS_REACHABLE_NEED_ROOT 
}