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

package org.apache.guacamole.auth.docker.connection;

import com.google.inject.Inject;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.guacamole.GuacamoleException;
import org.apache.guacamole.auth.docker.conf.ConfigurationService;
import org.apache.guacamole.net.auth.Connection;
import org.apache.guacamole.net.auth.ConnectionGroup;
import org.apache.guacamole.net.auth.DecoratingDirectory;
import org.apache.guacamole.net.auth.Directory;

/**
 * A directory containing a single connection that is started in a Docker
 * container.
 */
public class DockerStartupConnectionDirectory 
        extends DecoratingDirectory<Connection> {
    
    /**
     * The configuration service for this module.
     */
    @Inject
    private ConfigurationService confService;
    
    private final Map<String, Connection> connections;
    
    // private final ConnectionGroup rootGroup;
    
    public DockerStartupConnectionDirectory(Directory<Connection> directory) throws GuacamoleException {
        super(directory);
        this.connections = new ConcurrentHashMap<>();
        // this.rootGroup = rootGroup;
    }
    
    @Override
    public Connection decorate(Connection object) {
        return object;
    }
    
    @Override
    public Connection undecorate(Connection object) {
        return object;
    }
    
}
