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
import org.apache.guacamole.docker.DockerStartupException;
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
    
    /**
     * The identifier of the container associated with this Connection.
     */
    private final String containerId;
    
    /**
     * The object that describes the Guacamole Configuration associated with
     * this Connection.
     */
    private final GuacamoleConfiguration config;
    
    /**
     * Create a new Docker Startup Connection, with the given client, image
     * name, port, container name, command, and protocol.
     * 
     * @param client
     *     The DockerStartupClient that will be used to start this container.
     * 
     * @param imageName
     *     The name of the image that will be used as the base for the container.
     * 
     * @param imagePort
     *     The port on which the container will listen.
     * 
     * @param containerName
     *     The name to assign the container.
     * 
     * @param imageCmd
     *     The command to run to start the container.
     * 
     * @param imageProtocol
     *     The protocol to use to access the container.
     * 
     * @throws GuacamoleException 
     *     If an error occurs starting the container.
     */
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
        super.setName(containerName);
        
    }
    
    public DockerStartupConnection(DockerStartupClient client, String imageName,
            int imagePort, String containerName, GuacamoleProtocol imageProtocol)
            throws GuacamoleException {
        this(client, imageName, imagePort, containerName, null, imageProtocol);
    }
    
    public String getContainerId() {
        return containerId;
    }
    
    public String stopContainer() throws GuacamoleException {
        return client.stopContainer(containerId);
    }
    
    
    
}
