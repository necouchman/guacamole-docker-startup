/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.guacamole.auth.docker.conf;

/**
 * An enum that represents the protocols currently supported by Guacamole,
 * and contains their default ports.
 */
public enum GuacamoleProtocol {
    
    /**
     * Remote Desktop Protocol (RDP), with a default port of 3389.
     */
    rdp(3389),
    
    /**
     * Secure Shell (SSH), with a default port of 22.
     */
    ssh(22),
    
    /**
     * Telnet, with a default port of 23.
     */
    telnet(23),
    
    /**
     * Virtual Network Computing (VNC), with a default port of 5901.
     */
    vnc(5901);
    
    /**
     * The default TCP port to use to connect.
     */
    private int defaultPort;
    
    /**
     * Create a new instance of this enum with the specified default port.
     * 
     * @param defaultPort 
     *     The default TCP port to use for connecting with this protocol.
     */
    GuacamoleProtocol(int defaultPort) {
        this.defaultPort = defaultPort;
    }
    
    /**
     * Return the default TCP port to use to connect to this protocol.
     * 
     * @return 
     *     The default TCP port to use to connect to this protocol.
     */
    public int getDefaultPort() {
        return defaultPort;
    }
    
}
