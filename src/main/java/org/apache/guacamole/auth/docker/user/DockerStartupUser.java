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

package org.apache.guacamole.auth.docker.user;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.guacamole.GuacamoleException;
import org.apache.guacamole.auth.docker.conf.GuacamoleProtocol;
import org.apache.guacamole.auth.docker.connection.DockerStartupConnection;
import org.apache.guacamole.docker.DockerStartupClient;
import org.apache.guacamole.form.EnumField;
import org.apache.guacamole.form.Form;
import org.apache.guacamole.form.NumericField;
import org.apache.guacamole.form.PasswordField;
import org.apache.guacamole.form.TextField;
import org.apache.guacamole.net.auth.Connection;
import org.apache.guacamole.net.auth.DelegatingUser;
import org.apache.guacamole.net.auth.User;

/**
 *
 * @author nick_couchman
 */
public class DockerStartupUser extends DelegatingUser {
    
    /**
     * The attribute name that stores the name of the Docker image that will
     * be used to start a container.
     */
    public static final String DOCKER_IMAGE_NAME_ATTRIBUTE = "docker-image-name";
    
    /**
     * The attribute name that contains the protocol that will be used to
     * connect to the container.
     */
    public static final String DOCKER_IMAGE_PROTOCOL_ATTRIBUTE = "docker-image-protocol";
    
    /**
     * The attribute name that stores the port number that the container will
     * listen on.  This port will be automatically mapped by the Docker host
     * to an available port.
     */
    public static final String DOCKER_IMAGE_PORT_ATTRIBUTE = "docker-image-port";
    
    /**
     * The attribute name that stores the command that will be run at the time
     * the container is started.  If unspecified (null or empty), no specific
     * command will be run and the default specified in the image will be used.
     */
    public static final String DOCKER_IMAGE_CMD_ATTRIBUTE = "docker-image-cmd";
    
    public static final String DOCKER_IMAGE_USER_ATTRIBUTE = "docker-image-user";
    
    public static final String DOCKER_IMAGE_PASSWORD_ATTRIBUTE = "docker-image-password";
    
    public static final String DOCKER_IMAGE_DOMAIN_ATTRIBUTE = "docker-image-domain";
    
    /**
     * The List of all available attributes for this decorating User.
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
     * The Form that contains the Fields used to configure each of the available
     * attributes.
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
            ));
    
    /**
     * The Collection of all forms to configure the available attributes.
     */
    public static final Collection<Form> ATTRIBUTES = 
            Collections.unmodifiableCollection(Arrays.asList(DOCKER_IMAGE_FORM));
    
    /**
     * The attributes associated with this User.
     */
    private final Map<String, String> attributes;
    
    /**
     * The original User that this object decorates.
     */
    private final User undecorated;
    
    /**
     * Whether or not the currently logged-in user can update this user, in
     * particular the attributes associated with the decoration performed by
     * this user.
     */
    private final Boolean canUpdate;
    
    /**
     * Create a new DockerStartupUser, specifying the original User object to
     * decorate and whether or not the currently logged-in user can update this
     * user.
     * 
     * @param user
     *     The original User object to decorate.
     * 
     * @param canUpdate 
     *     Whether or not the currently logged-in user can update this User.
     */
    public DockerStartupUser(User user, Boolean canUpdate) {
        
        // r00t
        super(user);
        this.undecorated = user;
        this.canUpdate = canUpdate;
        this.attributes = super.getAttributes();
    }
    
    /**
     * Return the original User object that this object is decorating.
     * 
     * @return 
     *     The original User object that this object decorates.
     */
    public User getUndecorated() {
        return undecorated;
    }
    
    @Override
    public Map<String, String> getAttributes() {
        
        // Make a mutable copy of the attributes.
        Map<String, String> effectiveAttributes = new HashMap<>(attributes);
        
        // Add attributes not present if we can update
        // Remove attributes if we cannot update
        for (String attr : DOCKER_IMAGE_ATTRIBUTES) {
            if (canUpdate && !effectiveAttributes.containsKey(attr))
                effectiveAttributes.put(attr, null);
            else if (!canUpdate && effectiveAttributes.containsKey(attr))
                effectiveAttributes.remove(attr);
        }
        
        return effectiveAttributes;
        
    }
    
    @Override
    public void setAttributes(Map<String, String> setAttributes) {
        
        // Make a mutable copy of the attributes.
        setAttributes = new HashMap<>(setAttributes);
        
        // If cannot update, remove our attributes.
        if (!canUpdate)
            for (String attr: DOCKER_IMAGE_ATTRIBUTES)
                setAttributes.remove(attr);
        
        // Pass the buck.
        super.setAttributes(setAttributes);
    }
    
    public Boolean hasDockerConnection() {
        return (attributes.containsKey(DOCKER_IMAGE_NAME_ATTRIBUTE)
                && attributes.containsKey(DOCKER_IMAGE_PROTOCOL_ATTRIBUTE)
                && attributes.containsKey(DOCKER_IMAGE_PORT_ATTRIBUTE));
    }
    
    public Connection getDockerConnection(DockerStartupClient dockerClient)
            throws GuacamoleException {
        
        if (!hasDockerConnection())
            return null;
        
        return new DockerStartupConnection(dockerClient,
                attributes.get(DOCKER_IMAGE_NAME_ATTRIBUTE),
                Integer.parseInt(attributes.get(DOCKER_IMAGE_PORT_ATTRIBUTE)),
                this.getIdentifier(),
                attributes.get(DOCKER_IMAGE_CMD_ATTRIBUTE),
                GuacamoleProtocol.valueOf(attributes.get(DOCKER_IMAGE_PROTOCOL_ATTRIBUTE)));
    }
    
}
