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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import org.apache.guacamole.GuacamoleException;
import org.apache.guacamole.GuacamoleServerException;
import org.apache.guacamole.properties.GuacamoleProperty;

/**
 * A Guacamole property that represents a list of ports seprated by commas.
 */
public abstract class PortListProperty implements GuacamoleProperty<List<Integer>> {
    
    /**
     * A regular expression pattern that matches to delimit values within
     * the list.  Currently this is a command plus trailing whitespace.
     */
    private static final Pattern DELIMITER_PATTERN = Pattern.compile(",\\s*");
    
    @Override
    public List<Integer> parseValue(String values) throws GuacamoleException {
        
        // Nothing in, nothing out
        if (values == null)
            return null;
        
        List<String> portStrings = Arrays.asList(DELIMITER_PATTERN.split(values));
        List<Integer> ports = new ArrayList<>();
        for (String portStr : portStrings) {
            try {
                int thisPort = Integer.parseInt(portStr);
                if (thisPort < 1 || thisPort > 65535)
                    throw new GuacamoleServerException("Invalid port specified: " + portStr);
                ports.add(thisPort);
            }
            catch (NumberFormatException e) {
                throw new GuacamoleServerException("Port specified is not a number: " + portStr);
            }
        }
        
        if (ports.isEmpty())
            return null;
        
        return ports;
        
    }
    
}
