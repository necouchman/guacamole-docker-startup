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
import org.apache.guacamole.GuacamoleException;
import org.apache.guacamole.GuacamoleServerException;
import org.apache.guacamole.auth.docker.DockerStartupAuthenticationProvider;
import org.apache.guacamole.auth.docker.conf.ConfigurationService;
import org.apache.guacamole.auth.docker.connection.DockerStartupConnectionDirectory;
import org.apache.guacamole.net.auth.AbstractUserContext;
import org.apache.guacamole.net.auth.AuthenticationProvider;
import org.apache.guacamole.net.auth.Connection;
import org.apache.guacamole.net.auth.Directory;
import org.apache.guacamole.net.auth.User;
import org.apache.guacamole.net.auth.permission.ObjectPermissionSet;
import org.apache.guacamole.net.auth.simple.SimpleObjectPermissionSet;
import org.apache.guacamole.net.auth.simple.SimpleUser;

/**
 * A UserContext that delegates authentication and storage of user attributes
 * to another module, and provides for handling startup of and connection to
 * a Docker container.
 */
public class DockerStartupUserContext extends AbstractUserContext {
    
    /**
     * The configuration service for this module.
     */
    @Inject
    private ConfigurationService confService;
    
    /**
     * The authentication provider associated with this user context.
     */
    private final AuthenticationProvider authProvider;
    
    /**
     * The username used to log in to Guacamole.
     */
    private final String username;
    
    public DockerStartupUserContext(AuthenticationProvider authProvider,
            String username) {
        this.authProvider = authProvider;
        this.username = username;
    }
    
    @Override
    public AuthenticationProvider getAuthenticationProvider() {
        return authProvider;
    }
    
    @Override
    public User self() {
                return new SimpleUser(username) {

            @Override
            public ObjectPermissionSet getConnectionGroupPermissions()
                    throws GuacamoleException {
                return new SimpleObjectPermissionSet(
                        getConnectionDirectory().getIdentifiers());
            }

            @Override
            public ObjectPermissionSet getConnectionPermissions()
                    throws GuacamoleException {
                return new SimpleObjectPermissionSet(
                        getConnectionGroupDirectory().getIdentifiers());
            }

        };
    }

    @Override
    public Directory<Connection> getConnectionDirectory() 
            throws GuacamoleException {

        return new DockerStartupConnectionDirectory();
        
    }
    
}
