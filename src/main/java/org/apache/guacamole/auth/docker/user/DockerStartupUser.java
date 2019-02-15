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

import java.util.HashMap;
import java.util.Map;
import org.apache.guacamole.GuacamoleException;
import org.apache.guacamole.auth.docker.connection.DockerStartupConnection;
import org.apache.guacamole.docker.DockerStartupClient;
import org.apache.guacamole.net.auth.Connection;
import org.apache.guacamole.net.auth.DelegatingUser;
import org.apache.guacamole.net.auth.User;

/**
 *
 * @author nick_couchman
 */
public class DockerStartupUser extends DelegatingUser {
    
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
        for (String attr : DockerStartupConnection.DOCKER_IMAGE_ATTRIBUTES) {
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
            for (String attr: DockerStartupConnection.DOCKER_IMAGE_ATTRIBUTES)
                setAttributes.remove(attr);
        
        // Pass the buck.
        super.setAttributes(setAttributes);
    }
    
    public Boolean hasDockerConnection() {
        return (attributes.containsKey(DockerStartupConnection.DOCKER_IMAGE_NAME_ATTRIBUTE)
                && attributes.containsKey(DockerStartupConnection.DOCKER_IMAGE_PROTOCOL_ATTRIBUTE)
                && attributes.containsKey(DockerStartupConnection.DOCKER_IMAGE_PORT_ATTRIBUTE));
    }
    
    public Connection getDockerConnection(DockerStartupClient dockerClient)
            throws GuacamoleException {
        
        if (!hasDockerConnection())
            return null;
        
        Map<String, String> connectionAttrs = new HashMap<>();
        for (String attr : DockerStartupConnection.DOCKER_IMAGE_ATTRIBUTES) {
            if (attributes.containsKey(attr))
                connectionAttrs.put(attr, attributes.get(attr));
        }
        
        return new DockerStartupConnection(dockerClient, this.getIdentifier(),
                connectionAttrs);
    }
    
}
