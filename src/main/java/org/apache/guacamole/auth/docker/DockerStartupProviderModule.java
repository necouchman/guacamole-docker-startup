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

import com.google.inject.AbstractModule;
import org.apache.guacamole.GuacamoleException;
import org.apache.guacamole.auth.docker.conf.ConfigurationService;
import org.apache.guacamole.auth.docker.user.DockerStartupUserContext;
import org.apache.guacamole.environment.Environment;
import org.apache.guacamole.environment.LocalEnvironment;
import org.apache.guacamole.net.auth.AuthenticationProvider;

/**
 * A module that handles injection of various classes in the Docker Startup
 * Authentication provider.
 */
public class DockerStartupProviderModule extends AbstractModule {
    
    /**
     * The environment of the Guacamole server.
     */
    private final Environment environment;
    
    /**
     * The authentication provider that instantiated this class.
     */
    private final AuthenticationProvider authProvider;
    
    /**
     * Create a new instance of the DockerStartupAuthenticationProviderModule
     * with the specified calling AuthenticationProvider.
     * 
     * @param authProvider
     *     The AuthenticationProvider that called for this module.
     * 
     * @throws GuacamoleException 
     *     If guacamole.properties cannot be parsed.
     */
    public DockerStartupProviderModule(AuthenticationProvider authProvider)
        throws GuacamoleException {
        this.environment = new LocalEnvironment();
        this.authProvider = authProvider;
    }
    
    @Override
    public void configure() {
        
        // Bind core implementations of guacamole-ext classes
        bind(AuthenticationProvider.class).toInstance(authProvider);
        bind(Environment.class).toInstance(environment);
        
        // Bind DockerStartup-specific classes.
        bind(ConfigurationService.class);
    }
    
}
