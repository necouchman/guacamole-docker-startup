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

package org.apache.guacamole.auth.docker.conf;

import org.apache.guacamole.GuacamoleException;
import org.apache.guacamole.GuacamoleServerException;
import org.apache.guacamole.properties.GuacamoleProperty;

/**
 * A Guacamole property whose value is a valid TCP port.
 */
public abstract class PortGuacamoleProperty implements GuacamoleProperty<Integer> {
    
    @Override
    public Integer parseValue(String value) throws GuacamoleException {
        
        // Zero in, zero out
        if (value == null || value.isEmpty())
            return null;
        
        try {
            
            // Parse the integer
            int port = Integer.parseInt(value);
            
            // Check that the port is in the range of valid ports.
            if (port < 0 || port > 65536)
                throw new GuacamoleServerException("Port value out of range: " + value);
            
            // Return the value
            return port;
        }
        // Catch situations where something other than a number is specified.
        catch (NumberFormatException e) {
            throw new GuacamoleServerException("Invalid number specified for port: " + value, e);
        }
    }
    
}
