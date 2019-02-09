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

package org.apache.guacamole.docker;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.Ports;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import com.google.inject.Inject;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import org.apache.guacamole.GuacamoleException;
import org.apache.guacamole.auth.docker.conf.ConfigurationService;

/**
 * A utility class that handles the required Docker commands for interfacing
 * with Guacamole.
 */
public class DockerStartupClient {
    
    @Inject
    private ConfigurationService confService;
    
    /**
     * The DockerClient instance used to talk with the Docker server and manage
     * containers.
     */
    private final DockerClient client;
    
    /**
     * The configuration used to connect to Docker.
     */
    private final DockerClientConfig config;
    
    /**
     * Configure a new instance of the DockerStartupClient with the given
     * DockerClientConfig, derived from the options specified in the
     * guacamole.properties file.
     * 
     * @throws GuacamoleException
     *     If an error occurs retrieving the configuration
     */
    public DockerStartupClient() throws GuacamoleException {
        
        // Retrieve and store configuration
        this.config = confService.getDockerClientConfig();
        
        // Build the client from the provided config.
        this.client = DockerClientBuilder.getInstance(config).build();
        
    }
    
    /**
     * Return the DockerClient associated with this instance.
     * 
     * @return 
     *     The DockerClient associated with this instance.
     */
    public DockerClient getClient() {
        return client;
    }
    
    /**
     * Return the DockerClientConfig that generated this client instance.
     * 
     * @return 
     *     The DockerClientConfig used to generate this Docker client instance.
     */
    public DockerClientConfig getConfig() {
        return config;
    }
    
    /**
     * Start a container in Docker using the specified image name, port, and
     * command, and using the container name specified, returning the container
     * identifier upon success, or throwing a DockerStartupException if an
     * error occurs.
     * 
     * @param imageName
     *     The name of the image to start up.
     * 
     * @param imagePort
     *     The port within the image that will be opened and published by
     *     Docker.
     * 
     * @param containerName
     *     The name to give to the container.
     * 
     * @param imageCmd
     *     The command to run within the container at startup, or null if
     *     no additional/specific command should be run.
     * 
     * @return
     *     The identifier of the container.
     * 
     * @throws DockerStartupException 
     *     If startup of the container is interrupted.
     */
    public String startContainer(String imageName, int imagePort,
            String containerName, String imageCmd) throws DockerStartupException {
        
        // Set up the port bindings
        ExposedPort containerPort = ExposedPort.tcp(imagePort);
        Ports portBindings = new Ports();
        portBindings.bind(containerPort, Ports.Binding.bindPortSpec(null));
        HostConfig hostConfig = new HostConfig()
                .withPortBindings(portBindings)
                .withPublishAllPorts(true);
        
        // Create the command to start the container
        CreateContainerCmd containerCmd = client.createContainerCmd(imageName)
                .withName(containerName)
                .withExposedPorts(containerPort)
                .withHostConfig(hostConfig);
        if (imageCmd != null && !imageCmd.isEmpty())
            containerCmd.withCmd(imageCmd);
        String cid = containerCmd.exec().getId();
        
        // Start the container and wait, returning the container ID
        try {
            client.startContainerCmd(cid).exec().wait();
            return cid;
        }
        catch (InterruptedException e) {
            throw new DockerStartupException("Startup interrupted.", e);
        }
        
        
    }
    
    /**
     * Retrieve a Map containing the host address and port that are published
     * for the container specified by the identifier.
     * 
     * @param containerId
     *     The identifier of the container for which to retrieve the
     *     connectivity information.
     * 
     * @return
     *     A Map containing the hostname and port number to use to connect
     *     to the container.
     * 
     * @throws DockerStartupException
     *     If the Docker host cannot be resolved.
     */
    public Map<String, String> getContainerConnection(String containerId)
            throws DockerStartupException {
        
        try {
            InetAddress hostAddr = InetAddress.getByName(config.getDockerHost().getHost());
            Ports publishedPorts = client.inspectContainerCmd(containerId)
                    .exec().getNetworkSettings().getPorts();
            Map<String, String> connectionParameters = new HashMap<>();
            connectionParameters.put("hostname", hostAddr.toString());
            connectionParameters.put("port", publishedPorts.toString());
            return connectionParameters;
        }
        catch (UnknownHostException e) {
            throw new DockerStartupException("Cannot resolve docker host.", e);
        }
        
    }
    
    /**
     * Stop the container specified by the identifier, and return the
     * identifier upon stop.
     * 
     * @param containerId
     *     The identifier of the container to stop.
     * 
     * @return
     *     The identifier of the container that was stopped.
     * 
     * @throws DockerStartupException 
     *     If an error occurs during the container stop operation.
     */
    public String stopContainer(String containerId)
            throws DockerStartupException {
        
        try {
            client.stopContainerCmd(containerId).exec().wait();
            return containerId;
        }
        catch (InterruptedException e) {
            throw new DockerStartupException("Container stop interrupted.", e);
        }
        
    }
    
    @Override
    public void finalize() throws Throwable {
        try {
            client.close();
        }
        catch (IOException e) {}
        
        super.finalize();
        
    }
    
}
