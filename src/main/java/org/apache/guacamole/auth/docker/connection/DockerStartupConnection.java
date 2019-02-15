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
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.guacamole.GuacamoleException;
import org.apache.guacamole.auth.docker.conf.GuacamoleProtocol;
import org.apache.guacamole.docker.DockerStartupClient;
import org.apache.guacamole.form.EnumField;
import org.apache.guacamole.form.Form;
import org.apache.guacamole.form.NumericField;
import org.apache.guacamole.form.PasswordField;
import org.apache.guacamole.form.TextField;
import org.apache.guacamole.net.auth.simple.SimpleConnection;
import org.apache.guacamole.protocol.GuacamoleConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A connection type use for Docker Startup connections.
 */
public class DockerStartupConnection extends SimpleConnection {
    
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
     * The attribute for a user group that specifies the protocol that will
     * be used for the connection.
     */
    public static final String DOCKER_IMAGE_PROTOCOL_ATTRIBUTE = "docker-image-protocol";
    
    /**
     * The attribute for a user group that specifies the port that the Docker
     * container is listening on, that will be automatically mapped to an
     * available port by the Docker host and published.
     */
    public static final String DOCKER_IMAGE_PORT_ATTRIBUTE = "docker-image-port";
    
    /**
     * The attribute for a user group that specifies what command, if any,
     * should be run when the container starts.  If this is null or empty the
     * container will start with the default command specified within the
     * image.
     */
    public static final String DOCKER_IMAGE_CMD_ATTRIBUTE = "docker-image-cmd";
    
    /**
     * The attribute that defines the username associated with connecting to
     * this image.
     */
    public static final String DOCKER_IMAGE_USER_ATTRIBUTE = "docker-image-user";
    
    /**
     * The name of the attribute that defines the password used when connecting
     * to this image.
     */
    public static final String DOCKER_IMAGE_PASSWORD_ATTRIBUTE = "docker-image-password";
    
    /**
     * The name of the attribute that defines the domain used when connecting
     * to this image.
     */
    public static final String DOCKER_IMAGE_DOMAIN_ATTRIBUTE = "docker-image-domain";
    
    /**
     * The set of all attributes that are available for this delegating user
     * group.
     */
    public static final List<String> DOCKER_IMAGE_ATTRIBUTES = Arrays.asList(
            DOCKER_IMAGE_NAME_ATTRIBUTE,
            DOCKER_IMAGE_PORT_ATTRIBUTE,
            DOCKER_IMAGE_PROTOCOL_ATTRIBUTE,
            DOCKER_IMAGE_CMD_ATTRIBUTE,
            DOCKER_IMAGE_USER_ATTRIBUTE,
            DOCKER_IMAGE_PASSWORD_ATTRIBUTE,
            DOCKER_IMAGE_DOMAIN_ATTRIBUTE
    );
    
    /**
     * The Form that will be used to allow administrators to fill in the
     * attributes for this delegating user group.
     */
    public static final Form DOCKER_IMAGE_FORM = new Form("docker-image",
            Arrays.asList(
                    new TextField(DOCKER_IMAGE_NAME_ATTRIBUTE),
                    new EnumField(DOCKER_IMAGE_PROTOCOL_ATTRIBUTE,
                            Stream.of(GuacamoleProtocol.values())
                               .map(GuacamoleProtocol::name)
                               .collect(Collectors.toList())),
                    new NumericField(DOCKER_IMAGE_PORT_ATTRIBUTE),
                    new TextField(DOCKER_IMAGE_CMD_ATTRIBUTE),
                    new TextField(DOCKER_IMAGE_USER_ATTRIBUTE),
                    new PasswordField(DOCKER_IMAGE_PASSWORD_ATTRIBUTE),
                    new TextField(DOCKER_IMAGE_DOMAIN_ATTRIBUTE)
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
     * @param containerName
     *     The name to assign the container.
     * 
     * @param attributes
     *     The attributes to use for the connection.
     * 
     * @throws GuacamoleException 
     *     If an error occurs starting the container.
     */
    public DockerStartupConnection(DockerStartupClient client, 
            String containerName, Map<String, String> attributes) 
            throws GuacamoleException {
        
        String imageName = attributes.get(DOCKER_IMAGE_NAME_ATTRIBUTE);
        int imagePort = Integer.parseInt(
                attributes.get(DOCKER_IMAGE_PORT_ATTRIBUTE));
        GuacamoleProtocol imageProtocol = GuacamoleProtocol.valueOf(
                attributes.get(DOCKER_IMAGE_PROTOCOL_ATTRIBUTE));
        String imageCmd = attributes.get(DOCKER_IMAGE_CMD_ATTRIBUTE);
        String imageUser = attributes.get(DOCKER_IMAGE_USER_ATTRIBUTE);
        String imagePass = attributes.get(DOCKER_IMAGE_PASSWORD_ATTRIBUTE);
        String imageDomain = attributes.get(DOCKER_IMAGE_DOMAIN_ATTRIBUTE);
        
        logger.debug(">>>DOCKER<<< Image: {}", imageName);
        logger.debug(">>>DOCKER<<< Port: {}", Integer.toString(imagePort));
        logger.debug(">>>DOCKER<<< Cmd: {}", imageCmd);
        logger.debug(">>>DOCKER<<< Protocol: {}", imageProtocol.toString());
        logger.debug(">>>DOCKER<<< User: {}", imageUser);
        logger.debug(">>>DOCKER<<< Password: {}", imagePass);
        logger.debug(">>>DOCKER<<< Domain: {}", imageDomain);
        
        this.client = client;
        if (!client.containerExists(containerName))
            this.containerId = client.createContainer(imageName, imagePort,
                    containerName, imageCmd);
        else
            this.containerId = containerName;
        
        if (!client.containerRunning(containerName))
            client.startContainer(containerName);
        
        // Create the Guacamole configuration
        this.config = new GuacamoleConfiguration();
        config.setProtocol(imageProtocol.toString().toLowerCase());
        config.setParameters(client.getContainerConnection(containerId));
        
        // Set up authentication information
        if (imageUser != null && !imageUser.isEmpty())
            config.setParameter("username", imageUser);
        if (imagePass != null && !imagePass.isEmpty())
            config.setParameter("password", imagePass);
        if (imageDomain != null && !imageDomain.isEmpty())
            config.setParameter("domain", imageDomain);
        
        // Finish up the config
        super.setIdentifier(this.containerId);
        super.setConfiguration(this.config);
        super.setName(containerName);
        
    }
    
    public String getContainerId() {
        return containerId;
    }
    
    public String stopContainer() throws GuacamoleException {
        return client.stopContainer(containerId);
    }
    
    
    
}
