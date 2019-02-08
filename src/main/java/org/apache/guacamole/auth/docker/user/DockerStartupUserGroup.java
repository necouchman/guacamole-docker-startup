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
import org.apache.guacamole.auth.docker.conf.GuacamoleProtocol;
import org.apache.guacamole.form.EnumField;
import org.apache.guacamole.form.Form;
import org.apache.guacamole.form.NumericField;
import org.apache.guacamole.form.TextField;
import org.apache.guacamole.net.auth.DelegatingUserGroup;
import org.apache.guacamole.net.auth.UserGroup;

/**
 *
 * @author nick_couchman
 */
public class DockerStartupUserGroup extends DelegatingUserGroup {
    
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
     * The set of all attributes that are available for this delegating user
     * group.
     */
    public static final List<String> DOCKER_IMAGE_ATTRIBUTES = Arrays.asList(
            DOCKER_IMAGE_NAME_ATTRIBUTE,
            DOCKER_IMAGE_PROTOCOL_ATTRIBUTE,
            DOCKER_IMAGE_CMD_ATTRIBUTE,
            DOCKER_IMAGE_CMD_ATTRIBUTE
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
                    new TextField(DOCKER_IMAGE_NAME_ATTRIBUTE)
            ));
    
    /**
     * The collection of all forms that will be avaiable for this delegating
     * user group.
     */
    public static final Collection<Form> ATTRIBUTES = 
            Collections.unmodifiableCollection(Arrays.asList(DOCKER_IMAGE_FORM));
    
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
        
        // Create a mutable copy of the attributes.
        setAttributes = new HashMap<>(setAttributes);
        
        // If no rights, remove the decorating attributes
        if (!canUpdate)
            for (String attr : DOCKER_IMAGE_ATTRIBUTES)
                setAttributes.remove(attr);
        
        super.setAttributes(setAttributes);
        
    }
    
}
