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

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import org.apache.guacamole.GuacamoleException;
import org.apache.guacamole.auth.docker.connection.DockerStartupConnection;
import org.apache.guacamole.docker.DockerStartupClient;
import org.apache.guacamole.form.Form;
import org.apache.guacamole.net.auth.AuthenticationProvider;
import org.apache.guacamole.net.auth.Connection;
import org.apache.guacamole.net.auth.DecoratingDirectory;
import org.apache.guacamole.net.auth.DelegatingUserContext;
import org.apache.guacamole.net.auth.Directory;
import org.apache.guacamole.net.auth.Permissions;
import org.apache.guacamole.net.auth.UserContext;
import org.apache.guacamole.net.auth.permission.ObjectPermission;
import org.apache.guacamole.net.auth.permission.ObjectPermissionSet;
import org.apache.guacamole.net.auth.permission.SystemPermission;
import org.apache.guacamole.net.auth.permission.SystemPermissionSet;

/**
 * A UserContext that delegates authentication and storage of user attributes
 * to another module, and provides for handling startup of and connection to
 * a Docker container.
 */
public class DockerStartupUserContext extends DelegatingUserContext {
    
    /**
     * The authentication provider associated with this user context.
     */
    private final AuthenticationProvider authProvider;
    
    /**
     * The Docker client used to manage containers.
     */
    private final DockerStartupClient dockerClient;
    
    /**
     * Initialize a new DockerStartupUserContext, decorating the provided
     * userContext object, and using the provided DockerStartupClient to
     * perform Docker-related operations.
     * 
     * @param userContext
     *     The UserContext to decorate.
     * 
     * @param dockerClient
     *     The DockerStartupClient to use to perform Docker-related operations.
     * 
     * @param authProvider
     *     The authentication provider calling this user context.
     * 
     * @throws GuacamoleException
     *     If errors occur using the DockerStartupClient or initializing
     *     the various directories.
     */
    public DockerStartupUserContext(UserContext userContext,
            DockerStartupClient dockerClient,
            AuthenticationProvider authProvider) throws GuacamoleException {
        
        super(userContext);
        this.dockerClient = dockerClient;
        this.authProvider = authProvider;
        
    }
    
    @Override
    public AuthenticationProvider getAuthenticationProvider() {
        return authProvider;
    }
    
    @Override
    public Directory<Connection> getConnectionDirectory() throws GuacamoleException {
        return new DecoratingDirectory<Connection>(super.getConnectionDirectory()) {
            
            @Override
            public Connection decorate(Connection object) throws GuacamoleException {
                Permissions effective = self().getEffectivePermissions();
                SystemPermissionSet system = effective.getSystemPermissions();
                ObjectPermissionSet objperms = effective.getConnectionPermissions();
                Boolean canUpdate = false;
                if (system.hasPermission(SystemPermission.Type.ADMINISTER)
                        || objperms.hasPermission(ObjectPermission.Type.UPDATE, object.getIdentifier()))
                    canUpdate = true;
                return new DockerStartupConnection(object, dockerClient, self().getIdentifier(), canUpdate);
            }
            
            @Override
            public Connection undecorate(Connection object) {
                assert (object instanceof DockerStartupConnection);
                return ((DockerStartupConnection) object).getUndecorated();
            }
            
        };
    }
    
    @Override
    public Collection<Form> getConnectionAttributes() {
        Collection<Form> allAttributes = new HashSet<>(super.getConnectionAttributes());
        allAttributes.addAll(DockerStartupConnection.ATTRIBUTES);
        return Collections.unmodifiableCollection(allAttributes);
    }
    
}
