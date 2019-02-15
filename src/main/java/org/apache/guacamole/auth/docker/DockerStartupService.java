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
import org.apache.guacamole.net.auth.UserContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A class that handles services related to the Docker Startup service.
 */
public class DockerStartupService {
    
    private static final Logger logger = LoggerFactory.getLogger(DockerStartupService.class);
    
    @Inject
    private ConfigurationService confService;
    
    public UserContext decorate(UserContext userContext)
            throws GuacamoleException {
        
        logger.debug(">>>DOCKER<<< Setting up decoration with docker host {}", confService.getDockerHost());
        
        return new DockerStartupUserContext(userContext);
    }
    
}
