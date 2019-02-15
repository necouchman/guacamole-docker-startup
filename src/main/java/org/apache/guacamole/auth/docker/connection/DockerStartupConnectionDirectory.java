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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.guacamole.GuacamoleException;
import org.apache.guacamole.net.auth.Connection;
import org.apache.guacamole.net.auth.DecoratingDirectory;
import org.apache.guacamole.net.auth.Directory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A directory containing a single connection that is started in a Docker
 * container.
 */
public class DockerStartupConnectionDirectory 
        extends DecoratingDirectory<Connection> {
    
    private final static Logger logger = LoggerFactory.getLogger(DockerStartupConnectionDirectory.class);
    
    private final Map<String, Connection> connections;
    
    public DockerStartupConnectionDirectory(Directory<Connection> directory)
            throws GuacamoleException {
        super(directory);
        this.connections = new ConcurrentHashMap<>();
    }
    
    @Override
    public void add(Connection connection) {
        logger.debug(">>>DOCKER<<< Adding connection {} to directory.", connection.getIdentifier());
        connections.put(connection.getIdentifier(), connection);
    }
    
    @Override
    public Connection get(String identifier) throws GuacamoleException {
        if (!connections.containsKey(identifier))
            return super.get(identifier);
        return connections.get(identifier);
    }
    
    @Override
    public Collection<Connection> getAll(Collection<String> identifiers) 
            throws GuacamoleException {
        Collection<Connection> allConnections = new ArrayList<>();
        for (String identifier : identifiers) {
            if (!connections.containsKey(identifier))
                allConnections.add(super.get(identifier));
            else
                allConnections.add(connections.get(identifier));
        }
        
        return allConnections;
    }
    
    @Override
    public Set<String> getIdentifiers() throws GuacamoleException {
        Set<String> allIds = new HashSet<>(super.getIdentifiers());
        allIds.addAll(connections.keySet());
        return allIds;
    }
    
    @Override
    public Connection decorate(Connection object) throws GuacamoleException {
        return object;
    }
    
    @Override
    public Connection undecorate(Connection object) throws GuacamoleException {
        return object;
    }
    
}
