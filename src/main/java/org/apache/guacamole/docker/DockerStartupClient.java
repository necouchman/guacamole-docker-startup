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
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.command.InspectContainerResponse.ContainerState;
import com.github.dockerjava.api.exception.NotFoundException;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.Ports;
import com.github.dockerjava.api.model.Ports.Binding;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import org.apache.guacamole.GuacamoleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A utility class that handles the required Docker commands for interfacing
 * with Guacamole.
 */
public class DockerStartupClient {
    
    /**
     * The logger for this class.
     */
    private final static Logger logger = LoggerFactory.getLogger(DockerStartupClient.class);
    
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
     * @param config
     *     The configuration to use for the client
     * 
     * @throws GuacamoleException
     *     If an error occurs retrieving the configuration
     */
    public DockerStartupClient(DockerClientConfig config) throws GuacamoleException {
        
        // Retrieve and store configuration
        this.config = config;
        
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
    public synchronized String createContainer(String imageName, int imagePort,
            String containerName, String imageCmd) throws DockerStartupException {
        
        logger.debug(">>>DOCKER<<< Creating container {} from image {}",
                containerName, imageName);
        
        if (containerExists(imageName))
            throw new DockerStartupException("Container already exists.");
        
        // Set up the port bindings
        ExposedPort containerPort = ExposedPort.tcp(imagePort);
        Ports portBindings = new Ports();
        portBindings.bind(containerPort, Ports.Binding.bindPortSpec(null));
        HostConfig hostConfig = new HostConfig()
                .withPortBindings(portBindings)
                .withPublishAllPorts(false);
        
        // Create the command to start the container
        CreateContainerCmd containerCmd = client.createContainerCmd(imageName)
                .withName(containerName)
                .withExposedPorts(containerPort)
                .withHostConfig(hostConfig);
        if (imageCmd != null && !imageCmd.isEmpty())
            containerCmd.withCmd(imageCmd);
        CreateContainerResponse createResponse = containerCmd.exec();
        
        try {
            createResponse.wait();
            return createResponse.getId();
        }
        catch (InterruptedException e) {
            throw new DockerStartupException("Container creation interrupted.", e);
        }
    }
    
    /**
     * Star the container with the specified container identifier or name.
     * 
     * @param cid
     *     The identifier or name of the container to start.
     * 
     * @throws DockerStartupException
     *     If an error occurs starting the container.
     */
    public synchronized void startContainer(String cid)
            throws DockerStartupException {
        
        logger.debug(">>>DOCKER<<< Starting container {}", cid);
        
        // Start the container and wait, returning the container ID
        try {
            if (containerExists(cid))
                client.startContainerCmd(cid).exec().wait();
            else
                throw new DockerStartupException("Container does not exist.");
        }
        catch (InterruptedException e) {
            throw new DockerStartupException("Startup interrupted.", e);
        }
        
        
    }
    
    /**
     * Check wither the container exists, return true if it exists or false
     * if not.
     * 
     * @param cid
     *     The identifier or name of the container to check for existence.
     * 
     * @return
     *     True if the container exists, otherwise false.
     * 
     * @throws DockerStartupException
     *     If an error occurs get the Docker Client information.
     */
    public Boolean containerExists(String cid) throws DockerStartupException {
        logger.debug(">>>DOCKER<<< Checking if container {} exists.", cid);
        try {
            InspectContainerResponse findContainer = client.inspectContainerCmd(cid).exec();
            return (findContainer.getId() != null);
        }
        catch (NotFoundException e) {
            logger.debug(">>>DOCKER<<< Container {} not found.", cid);
            return false;
        }
    }
    
    /**
     * Check and see if the specified container is running, returning true
     * if the container exists and is running, otherwise false.
     * 
     * @param cid
     *     The identifier or name of the container.
     * 
     * @return
     *     True if the container exists and is running, otherwise false.
     * 
     * @throws DockerStartupException 
     *     If the Docker client cannot be retrieved.
     */
    public Boolean containerRunning(String cid) throws DockerStartupException {
        logger.debug(">>>DOCKER<<< Checking if container {} is running", cid);
        if (!containerExists(cid))
            return false;
        ContainerState containerState = client.inspectContainerCmd(cid).exec().getState();
        logger.debug(">>>DOCKER<<< Container {} in state {}", containerState.getStatus());
        return containerState.getRunning();
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
        
        logger.debug(">>>DOCKER<<< Retrieving parameters for container {}", containerId);
        
        try {
            
            String host = config.getDockerHost().getHost();
            logger.debug(">>>DOCKER<<< Container host: {}", host);
            
            Map<String, String> connectionParameters = new HashMap<>();
            InetAddress hostAddr = InetAddress.getByName(config.getDockerHost().getHost());
            Map<ExposedPort, Binding[]> portBindings = client
                    .inspectContainerCmd(containerId).exec()
                    .getNetworkSettings().getPorts().getBindings();
            
            logger.debug(">>>DOCKER<<< Adding hostname parameter: {}", hostAddr.getHostName());
            connectionParameters.put("hostname", hostAddr.getHostName());
            for (ExposedPort port : portBindings.keySet()) {
                Binding[] bindings = portBindings.get(port);
                if (bindings != null
                        && bindings.length > 0
                        && bindings[0] != null) {
                    String hostPort = bindings[0].getHostPortSpec();
                    logger.debug(">>>DOCKER<<< Adding port parameter: {}", hostPort);
                    connectionParameters.put("port", hostPort);
                    break;
                }
            }

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
        
        logger.debug(">>>DOCKER<<< Stopping container {}", containerId);
        
        if (!containerExists(containerId))
            throw new DockerStartupException("Container " + containerId + " does not exist.");
        
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
            // Close the Docker client.
            client.close();
        }
        // Ignore any exceptions.
        catch (IOException e) {}
        
        super.finalize();
        
    }
    
}
