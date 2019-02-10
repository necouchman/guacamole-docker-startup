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

package org.apache.guacamole.auth.docker;

import com.google.inject.Guice;
import com.google.inject.Injector;
import org.apache.guacamole.GuacamoleException;
import org.apache.guacamole.auth.docker.conf.ConfigurationService;
import org.apache.guacamole.auth.docker.user.DockerStartupUserContext;
import org.apache.guacamole.net.auth.AbstractAuthenticationProvider;
import org.apache.guacamole.net.auth.AuthenticatedUser;
import org.apache.guacamole.net.auth.Credentials;
import org.apache.guacamole.net.auth.UserContext;

/**
 * An authentication provider that facilitates the startup of a docker container
 * and connection to that container for the user that has logged in.  This module
 * does not do any authentication, it only provides a connection directory
 * containing the started docker container.
 */
public class DockerStartupProvider extends AbstractAuthenticationProvider {
    
    /**
     * Guice object used for managing dependency injection.
     */
    private final Injector injector;
    
    /**
     * Configuration service
     */
    private final ConfigurationService confService;
    
    /**
     * Configure a new instance of this authentication module.
     * 
     * @throws GuacamoleException
     *     If an error occurs parsing guacamole.properties while standing
     *     up the environment.
     */
    public DockerStartupProvider() throws GuacamoleException {
        this.injector = Guice.createInjector(new DockerStartupProviderModule(this));
        this.confService = injector.getInstance(ConfigurationService.class);
    }
    
    @Override
    public String getIdentifier() {
        return "docker-startup";
    }
    
    @Override
    public UserContext decorate(UserContext context,
            AuthenticatedUser authenticatedUser, Credentials credentials)
            throws GuacamoleException {
        return injector.getInstance(DockerStartupUserContext.class);
    }
    
    @Override
    public UserContext redecorate(UserContext decorated, UserContext context,
            AuthenticatedUser authenticatedUser, Credentials credentials)
            throws GuacamoleException {
        return injector.getInstance(DockerStartupUserContext.class);
    }
    
}
