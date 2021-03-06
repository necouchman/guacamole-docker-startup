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

import com.google.inject.Inject;
import org.apache.guacamole.GuacamoleException;
import org.apache.guacamole.auth.docker.conf.ConfigurationService;
import org.apache.guacamole.auth.docker.user.DockerStartupUserContext;
import org.apache.guacamole.docker.DockerStartupClient;
import org.apache.guacamole.net.auth.UserContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A class that handles services related to the Docker Startup service.
 */
public class DockerStartupService {
    
    /**
     * The logger for this class.
     */
    private static final Logger logger = LoggerFactory.getLogger(DockerStartupService.class);
    
    /**
     * The configuration service that handles guacamole.properties entries
     * for this extension.
     */
    @Inject
    private ConfigurationService confService;
    
    /**
     * Decorate the given user context, returning the decorated user context.
     * 
     * @param userContext
     *     The original user context to decorate.
     * 
     * @return
     *     The decorated user context.
     * 
     * @throws GuacamoleException
     *     If guacamole.properties cannot be parsed.
     */
    public UserContext decorate(UserContext userContext)
            throws GuacamoleException {
        
        return new DockerStartupUserContext(userContext,
                new DockerStartupClient(confService.getDockerClientConfig()));
    }
    
}
