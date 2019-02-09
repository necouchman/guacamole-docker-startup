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

import com.google.inject.Inject;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import org.apache.guacamole.GuacamoleException;
import org.apache.guacamole.auth.docker.conf.ConfigurationService;
import org.apache.guacamole.auth.docker.connection.DockerStartupConnectionDirectory;
import org.apache.guacamole.docker.DockerStartupClient;
import org.apache.guacamole.form.Form;
import org.apache.guacamole.net.auth.AuthenticationProvider;
import org.apache.guacamole.net.auth.Connection;
import org.apache.guacamole.net.auth.DecoratingDirectory;
import org.apache.guacamole.net.auth.DelegatingUserContext;
import org.apache.guacamole.net.auth.Directory;
import org.apache.guacamole.net.auth.Permissions;
import org.apache.guacamole.net.auth.User;
import org.apache.guacamole.net.auth.UserContext;
import org.apache.guacamole.net.auth.UserGroup;
import org.apache.guacamole.net.auth.permission.ObjectPermission;
import org.apache.guacamole.net.auth.permission.ObjectPermissionSet;
import org.apache.guacamole.net.auth.permission.SystemPermission;
import org.apache.guacamole.net.auth.permission.SystemPermissionSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A UserContext that delegates authentication and storage of user attributes
 * to another module, and provides for handling startup of and connection to
 * a Docker container.
 */
public class DockerStartupUserContext extends DelegatingUserContext {
    
    private static final Logger logger = LoggerFactory.getLogger(DockerStartupUserContext.class);
    
    @Inject
    private ConfigurationService confService;
    
    /**
     * The authentication provider associated with this user context.
     */
    private final AuthenticationProvider authProvider;
    
    private final Directory<User> userDirectory;
    
    private final Directory<UserGroup> groupDirectory;
    
    private final Directory<Connection> connectionDirectory;
    
    public DockerStartupUserContext(UserContext userContext)
            throws GuacamoleException {
        
        super(userContext);
        this.authProvider = userContext.getAuthenticationProvider();
        this.connectionDirectory = 
                new DockerStartupConnectionDirectory(super.getConnectionDirectory());
        
        logger.debug(">>>DOCKER<<< Initializing docker client.");
        DockerStartupClient dockerClient = new DockerStartupClient(confService.getDockerClientConfig());
        
        logger.debug(">>>DOCKER<<< Building user directory.");
        this.userDirectory = new DecoratingDirectory<User>(super.getUserDirectory()) {
            
            @Override
            protected User decorate(User object) throws GuacamoleException {
                Permissions effective = self().getEffectivePermissions();
                SystemPermissionSet sys = effective.getSystemPermissions();
                ObjectPermissionSet obj = effective.getUserPermissions();
                Boolean canUpdate = false;
                if (sys.hasPermission(SystemPermission.Type.ADMINISTER)
                        || obj.hasPermission(ObjectPermission.Type.UPDATE, object.getIdentifier()))
                    canUpdate = true;
                DockerStartupUser decoratedUser = new DockerStartupUser(object, canUpdate);
                if (decoratedUser.hasDockerConnection())
                    connectionDirectory.add(decoratedUser.getDockerConnection(dockerClient));
                return decoratedUser;
            }
            
            @Override
            protected User undecorate(User object) throws GuacamoleException {
                assert(object instanceof DockerStartupUser);
                return ((DockerStartupUser) object).getUndecorated();
            }
            
            
        };
        
        logger.debug(">>>DOCKER<<< Building group directory.");
        this.groupDirectory = new DecoratingDirectory<UserGroup>(super.getUserGroupDirectory()) {
            
            @Override
            protected UserGroup decorate(UserGroup object) throws GuacamoleException {
                Permissions effective = self().getEffectivePermissions();
                SystemPermissionSet sys = effective.getSystemPermissions();
                ObjectPermissionSet obj = effective.getUserGroupPermissions();
                Boolean canUpdate = false;
                if (sys.hasPermission(SystemPermission.Type.ADMINISTER)
                        || obj.hasPermission(ObjectPermission.Type.UPDATE, object.getIdentifier()))
                    canUpdate = true;
                DockerStartupUserGroup decoratedGroup = new DockerStartupUserGroup(object, canUpdate);
                if (decoratedGroup.hasDockerConnection())
                    connectionDirectory.add(decoratedGroup.getDockerConnection(dockerClient));
                return decoratedGroup;
                    
                
            }
            
            @Override
            protected UserGroup undecorate(UserGroup object) {
                assert(object instanceof DockerStartupUserGroup);
                return ((DockerStartupUserGroup) object).getUndecorated();
            }
            
        };
        
    }
    
    @Override
    public AuthenticationProvider getAuthenticationProvider() {
        return authProvider;
    }
    
    @Override
    public Directory<User> getUserDirectory() throws GuacamoleException {
        return userDirectory;
    }
    
    @Override
    public Directory<UserGroup> getUserGroupDirectory() throws GuacamoleException {
        return groupDirectory;
    }
    
    @Override
    public Directory<Connection> getConnectionDirectory() throws GuacamoleException {
        return connectionDirectory;
    }
    
    @Override
    public Collection<Form> getUserAttributes() {
        Collection<Form> allAttributes = new HashSet<>(super.getUserAttributes());
        allAttributes.addAll(DockerStartupUser.ATTRIBUTES);
        return Collections.unmodifiableCollection(allAttributes);
    }
    
    @Override
    public Collection<Form> getUserGroupAttributes() {
        Collection<Form> allAttributes = new HashSet<>(super.getUserGroupAttributes());
        allAttributes.addAll(DockerStartupUserGroup.ATTRIBUTES);
        return Collections.unmodifiableCollection(allAttributes);
    }
    
}
