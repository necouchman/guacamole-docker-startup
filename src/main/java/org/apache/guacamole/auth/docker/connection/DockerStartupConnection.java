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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.guacamole.GuacamoleClientException;
import org.apache.guacamole.GuacamoleException;
import org.apache.guacamole.docker.DockerStartupClient;
import org.apache.guacamole.form.Form;
import org.apache.guacamole.form.NumericField;
import org.apache.guacamole.form.TextField;
import org.apache.guacamole.net.GuacamoleTunnel;
import org.apache.guacamole.net.auth.Connection;
import org.apache.guacamole.net.auth.DelegatingConnection;
import org.apache.guacamole.protocol.GuacamoleClientInformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A connection type use for Docker Startup connections.
 */
public class DockerStartupConnection extends DelegatingConnection {
    
    /**
     * The logger for this class.
     */
    private static final Logger logger = LoggerFactory.getLogger(DockerStartupConnection.class);
    
        /**
     * The attribute for a group that specifies the Docker image name that
     * will be started for a connection.
     */
    public static final String DOCKER_IMAGE_NAME_ATTRIBUTE = "docker-image-name";
    
    /**
     * The attribute for a user group that specifies what command, if any,
     * should be run when the container starts.  If this is null or empty the
     * container will start with the default command specified within the
     * image.
     */
    public static final String DOCKER_IMAGE_CMD_ATTRIBUTE = "docker-image-cmd";
    
    /**
     * The name of the attribute used to store environment variables that will
     * be passed to the container when created.
     */
    public static final String DOCKER_IMAGE_ENV_ATTRIBUTE = "docker-image-env";
    
    /**
     * The name of the attribute that contains the port that the container
     * will listen on and that will be forwarded through to the actual
     * connection.
     */
    public static final String DOCKER_IMAGE_PORT_ATTRIBUTE = "docker-image-port";
    
    /**
     * The set of all attributes that are available for this delegating user
     * group.
     */
    public static final List<String> DOCKER_IMAGE_ATTRIBUTES = Arrays.asList(
            DOCKER_IMAGE_NAME_ATTRIBUTE,
            DOCKER_IMAGE_CMD_ATTRIBUTE,
            DOCKER_IMAGE_ENV_ATTRIBUTE,
            DOCKER_IMAGE_PORT_ATTRIBUTE
    );
    
    /**
     * The Form that will be used to allow administrators to fill in the
     * attributes for this delegating user group.
     */
    public static final Form DOCKER_IMAGE_FORM = new Form("docker-image",
            Arrays.asList(
                    new TextField(DOCKER_IMAGE_NAME_ATTRIBUTE),
                    new TextField(DOCKER_IMAGE_CMD_ATTRIBUTE),
                    new TextField(DOCKER_IMAGE_ENV_ATTRIBUTE),
                    new NumericField(DOCKER_IMAGE_PORT_ATTRIBUTE)
            )
    );
    
    /**
     * The collection of all forms that will be available for this delegating
     * user group.
     */
    public static final Collection<Form> ATTRIBUTES = 
            Collections.unmodifiableCollection(Arrays.asList(DOCKER_IMAGE_FORM));
    
    /**
     * The DockerStartupClient to use for starting up this connection.
     */
    private final DockerStartupClient client;
    
    /**
     * The identifier of the container associated with this Connection.
     */
    private final String containerId;
    
    /**
     * Whether or not the current user can update this connection.
     */
    private final Boolean canUpdate;
    
    /**
     * Create a new Docker Startup Connection, with the given client, image
     * name, port, container name, command, and protocol.
     * 
     * @param undecorated
     *     The original Connection object that this decorates.
     * 
     * @param client
     *     The DockerStartupClient that will be used to start this container.
     * 
     * @param username
     *     The user logged in to Guacamole, used to create a container name.
     * 
     * @param canUpdate
     *     If the current user can update this connection.
     * 
     * @throws GuacamoleException 
     *     If an error occurs starting the container.
     */
    public DockerStartupConnection(Connection undecorated,
            DockerStartupClient client, String username, Boolean canUpdate)
            throws GuacamoleException {
        
        super(undecorated);
        
        this.canUpdate = canUpdate;
        this.containerId = super.getName() + "_" + username;
        this.client = client;
        
    }
    
    /**
     * Return the identifier of the container backing this connection.
     * 
     * @return 
     *     The container backing this connection.
     */
    public String getContainerId() {
        return containerId;
    }
    
    /**
     * Return the original, undecorated connection that this connection
     * decorates.
     * 
     * @return 
     *     The original, undecorated connection that this connection decorates.
     */
    public Connection getUndecorated() {
        return super.getDelegateConnection();
    }
    
    @Override
    public Map<String, String> getAttributes() {
        Map<String, String> attributes = super.getAttributes();
        
        for (String attr : DOCKER_IMAGE_ATTRIBUTES) {
            if (!attributes.containsKey(attr) && canUpdate)
                attributes.put(attr, null);
            if (attributes.containsKey(attr) && !canUpdate)
                attributes.remove(attr);
        }
        
        return attributes;
        
    }
    
    @Override
    public void setAttributes(Map<String, String> attributes) {
        attributes = new HashMap<>(attributes);
        
        for (String attr : DOCKER_IMAGE_ATTRIBUTES) {
            if (!canUpdate && attributes.containsKey(attr))
                attributes.remove(attr);
        }
        
        super.setAttributes(attributes);
    }
    
    @Override
    public GuacamoleTunnel connect(GuacamoleClientInformation info,
            Map<String, String> tokens) throws GuacamoleException {
        
        tokens = new HashMap<>(tokens);
        Map<String, String> attributes = super.getAttributes();
        
        String imageName = attributes.get(DOCKER_IMAGE_NAME_ATTRIBUTE);
        String imageCmd = attributes.get(DOCKER_IMAGE_CMD_ATTRIBUTE);
        String imagePort = attributes.get(DOCKER_IMAGE_PORT_ATTRIBUTE);
        String imageEnv = attributes.get(DOCKER_IMAGE_ENV_ATTRIBUTE);
        
        if (imagePort == null || imagePort.isEmpty())
            throw new GuacamoleClientException("Port is not defined.");
        
        if (!client.containerExists(containerId))
            client.createContainer(imageName, Integer.parseInt(imagePort),
                    containerId, imageCmd, imageEnv);
        
        if (!client.containerRunning(containerId))
            client.startContainer(containerId);
        
        Map<String, String> tokenParams = client.getContainerConnection(containerId);
        tokens.put("DOCKER_HOST", tokenParams.get("hostname"));
        tokens.put("DOCKER_PORT", tokenParams.get("port"));
        
        return super.connect(info, tokens);
        
    }
    
    @Override
    public void finalize() throws GuacamoleException {
        client.stopContainer(containerId);
    }
    
}
