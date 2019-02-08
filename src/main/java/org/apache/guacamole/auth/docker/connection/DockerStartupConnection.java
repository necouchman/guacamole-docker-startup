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

package org.apache.guacamole.auth.docker.connection;

import org.apache.guacamole.GuacamoleException;
import org.apache.guacamole.auth.docker.conf.GuacamoleProtocol;
import org.apache.guacamole.docker.DockerStartupClient;
import org.apache.guacamole.net.auth.simple.SimpleConnection;
import org.apache.guacamole.protocol.GuacamoleConfiguration;

/**
 * A connection type use for Docker Startup connections.
 */
public class DockerStartupConnection extends SimpleConnection {
    
    /**
     * The DockerStartupClient to use for starting up this connection.
     */
    private final DockerStartupClient client;
    
    private final String containerId;
    
    private final GuacamoleConfiguration config;
    
    public DockerStartupConnection(DockerStartupClient client, String imageName,
            int imagePort, String containerName, String imageCmd,
            GuacamoleProtocol imageProtocol) 
            throws GuacamoleException {
        
        this.client = client;
        this.containerId = client.startContainer(imageName, imagePort,
                containerName, imageCmd);
        this.config = new GuacamoleConfiguration();
        config.setProtocol(imageProtocol.toString());
        config.setParameters(client.getContainerConnection(containerId));
        super.setConfiguration(this.config);
        
    }
    
    
    
}
