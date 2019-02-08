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

package org.apache.guacamole.docker;

import org.apache.guacamole.GuacamoleException;

/**
 * Exception specific to the DockerStartup extension.
 */
public class DockerStartupException extends GuacamoleException {
    
    /**
     * @see GuacamoleException(Throwable)
     * @param cause
     *     The Throwable cause of this exception.
     */
    public DockerStartupException(Throwable cause) {
        super(cause);
    }
    
    /**
     * @see GuacamoleException(String)
     * @param message
     *     The human-readable error associated with this exception.
     */
    public DockerStartupException(String message) {
        super(message);
    }
    
    /**
     * @see GuacamoleException(String, Throwable)
     * @param message
     *     The human-readable error associated with this exception.
     * 
     * @param cause 
     *     The Throwable cause of this exception.
     */
    public DockerStartupException(String message, Throwable cause) {
        super(message, cause);
    }
    
}
