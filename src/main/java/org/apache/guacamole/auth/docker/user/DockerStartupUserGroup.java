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
import org.apache.guacamole.net.auth.DelegatingUserGroup;
import org.apache.guacamole.net.auth.UserGroup;

/**
 *
 * @author nick_couchman
 */
public class DockerStartupUserGroup extends DelegatingUserGroup {
    
    /**
     * The attributes of this delegating user group.
     */
    private final Map<String, String> attributes;
    
    /**
     * The original UserGroup that this delegating object is decorating.
     */
    private final UserGroup undecorated;
    
    /**
     * Whether or not the currently logged-in user has rights to update this
     * user group.
     */
    private final Boolean canUpdate;
    
    /**
     * Create a new DockerStartupUserGroup that decorates a UserGroup from
     * another extension, specifying the UserGroup to decorate and whether or
     * not the currently logged-in user has rights to update this group.
     * 
     * @param userGroup
     *     The UserGroup that this group will decorate.
     * 
     * @param canUpdate 
     *     True if the currently logged-in user has rights to update this group,
     *     otherwise false.
     */
    public DockerStartupUserGroup(UserGroup userGroup, Boolean canUpdate) {
        super(userGroup);
        this.attributes = super.getAttributes();
        this.undecorated = userGroup;
        this.canUpdate = canUpdate;
    }
    
    /**
     * Get the original UserGroup that this group decorates.
     * 
     * @return 
     *     The original UserGroup that this group decorates.
     */
    public UserGroup getUndecorated() {
        return undecorated;
    }
    
    @Override
    public Map<String, String> getAttributes() {
        
        // Get a mutable copy of the attributes.
        Map<String, String> effectiveAttributes = new HashMap<>(attributes);
        
        // Add in missing attributes or filter out if the user does not have rights
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
        
        // Create a mutable copy of the attributes.
        setAttributes = new HashMap<>(setAttributes);
        
        // If no rights, remove the decorating attributes
        if (!canUpdate)
            for (String attr : DockerStartupConnection.DOCKER_IMAGE_ATTRIBUTES)
                setAttributes.remove(attr);
        
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
