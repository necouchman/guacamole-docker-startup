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

import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.RemoteApiVersion;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import org.apache.guacamole.GuacamoleException;
import org.apache.guacamole.GuacamoleServerException;
import org.apache.guacamole.environment.Environment;
import org.apache.guacamole.properties.BooleanGuacamoleProperty;
import org.apache.guacamole.properties.FileGuacamoleProperty;
import org.apache.guacamole.properties.StringGuacamoleProperty;

/**
 * The Configuration Service for the DockerStartup authentication module,
 * which provides the properties available in guacamole.properties for
 * configuring this module.
 */
@Singleton
public class ConfigurationService {
    
    /**
     * The local Guacamole Server environment.
     */
    @Inject
    private Environment environment;
    
    /**
     * The URI that will connect the Docker Client used in this module to a
     * host system running Docker for starting/stopping containers.
     */
    public final static StringGuacamoleProperty DOCKER_HOST =
            new StringGuacamoleProperty() {
    
        @Override
        public String getName() { return "docker-host"; }
                
    };
    
    /**
     * A property which configures whether or not the Docker client will verify
     * TLS connections to the Docker host.
     */
    public final static BooleanGuacamoleProperty DOCKER_VERIFY_TLS =
            new BooleanGuacamoleProperty() {
    
        @Override
        public String getName() { return "docker-verify-tls"; }
                
    };
    
    /**
     * A property that dictates the path to the location where certificates
     * will be stored to facilitate TLS communication with the Docker host.
     */
    public final static FileGuacamoleProperty DOCKER_CERT_PATH =
            new FileGuacamoleProperty() {
    
        @Override
        public String getName() { return "docker-cert-path"; }
                
    };
    
    /**
     * A property that configures the path to a location on the system that
     * stores configuration information for the Docker Client.
     */
    public final static FileGuacamoleProperty DOCKER_CONFIG_PATH =
            new FileGuacamoleProperty() {
    
        @Override
        public String getName() { return "docker-config-path"; }
                
    };
    
    /**
     * A property that configures the API version used to talk ot the Docker
     * host.
     */
    public final static StringGuacamoleProperty DOCKER_API_VERSION =
            new StringGuacamoleProperty() {
    
        @Override
        public String getName() { return "docker-api-version"; }
                
    };
    
    /**
     * A property that configures the URL of the Docker Registry to use when
     * searching and deploying images to a Docker host.
     */
    public final static StringGuacamoleProperty DOCKER_REGISTRY_URL =
            new StringGuacamoleProperty() {
                
        @Override
        public String getName() { return "docker-registry-url"; }
                
    };
    
    /**
     * A property to configure the username used when accessing the Docker
     * registry.
     */
    public final static StringGuacamoleProperty DOCKER_REGISTRY_USER =
            new StringGuacamoleProperty() {
        
        @Override
        public String getName() { return "docker-registry-user"; }
                
    };
    
    /**
     * A property to configure the password used when accessing the Docker
     * registry.
     */
    public final static StringGuacamoleProperty DOCKER_REGISTRY_PASSWORD =
            new StringGuacamoleProperty() {
    
        @Override
        public String getName() { return "docker-registry-password"; }
                
    };
    
    /**
     * A property that specifies the e-mail address to use with the Docker
     * registry.
     */
    public final static StringGuacamoleProperty DOCKER_REGISTRY_EMAIL =
            new StringGuacamoleProperty() {
    
        @Override
        public String getName() { return "docker-registry-email"; }
                
    };
    
    /**
     * A property that specifies the name of the Docker image that will be
     * started to establish the connection.  This is required.
     */
    public final static StringGuacamoleProperty DOCKER_IMAGE_NAME =
            new StringGuacamoleProperty() {
    
        @Override
        public String getName() { return "docker-image-name"; }
                
    };
    
    /**
     * A property that specifies the protocol that should be used to communicate
     * with the container once it is started.  This is required.
     */
    public final static ProtocolGuacamoleProperty DOCKER_IMAGE_PROTOCOL =
            new ProtocolGuacamoleProperty() {
    
        @Override
        public String getName() { return "docker-image-protocol"; }
                
    };
    
    /**
     * A property to configure the TCP port that the Docker container will
     * listen on that should be published by the Docker host.
     */
    public final static PortGuacamoleProperty DOCKER_IMAGE_PORT =
            new PortGuacamoleProperty() {
    
        @Override
        public String getName() { return "docker-image-port"; }
                
    };
    
    /**
     * A property containing the command to run in the Docker container.  This
     * is optional.
     */
    public final static StringGuacamoleProperty DOCKER_IMAGE_CMD =
            new StringGuacamoleProperty() {
    
        @Override
        public String getName() { return "docker-image-cmd"; }
                
    };
    
    /**
     * Get the name of the image that will be used to start containers.
     * 
     * @return
     *     The name of the image that will be used to start containers.
     * 
     * @throws GuacamoleException 
     *     If guacamole.properties cannot be parsed, or this property is
     *     missing.
     */
    public String getDockerImageName() throws GuacamoleException {
        return environment.getRequiredProperty(DOCKER_IMAGE_NAME);
    }
    
    /**
     * Return the protocol that will be used to communicate with the Docker
     * container and will be set as part of the connection.
     * 
     * @return
     *     The protocol used to communicate with the Docker container.
     * 
     * @throws GuacamoleException 
     *     If guacamole.properties cannot be parsed or this parameter is not
     *     found.
     */
    public GuacamoleProtocol getDockerImageProtocol() throws GuacamoleException {
        return environment.getRequiredProperty(DOCKER_IMAGE_PROTOCOL);
    }
    
    /**
     * Return the TCP port on which the Docker container will listen, and that
     * the Docker host will then publish to a random port of its choosing.
     * 
     * @return
     *     The TCP port on which the Docker container will listen.
     * 
     * @throws GuacamoleException 
     *     If guacamole.properties cannot be parsed or this parameter is not
     *     found.
     */
    public int getDockerImagePort() throws GuacamoleException {
        return environment.getRequiredProperty(DOCKER_IMAGE_PORT);
    }
    
    /**
     * Returns the command that will be executed in the Docker container during
     * startup.
     * 
     * @return
     *     The command to execute in the Docker container during startup.
     * 
     * @throws GuacamoleException 
     *     If guacamole.properties cannot be parsed.
     */
    public String getDockerImageCmd() throws GuacamoleException {
        return environment.getProperty(DOCKER_IMAGE_CMD);
    }
    
    /**
     * Return the URI used to communicate with the Docker host.  If not specified
     * this will default to the local Docker UNIX socket.
     * 
     * @return
     *     The URI used to communicate with the Docker host, or the local UNIX
     *     socket if not specified.
     * 
     * @throws GuacamoleException 
     *     If guacamole.properties cannot be parsed.
     */
    private String getDockerHost() throws GuacamoleException {
        return environment.getProperty(DOCKER_HOST,
                "unix:///var/run/docker.sock");
    }
    
    /**
     * Return a boolean value indicating whether or not the Docker client
     * should verify TLS certificates during the connection process.
     * 
     * @return
     *     A boolean true if certificates should be verified; otherwise false.
     *     The default is false.
     * 
     * @throws GuacamoleException
     *     If guacamole.properties cannot be parsed.
     */
    private Boolean getVerifyTls() throws GuacamoleException {
        return environment.getProperty(DOCKER_VERIFY_TLS, false);
    }
    
    /**
     * Return the path to a directory that stores certificates used to verify
     * TLS connections to the Docker host.
     * 
     * @return
     *     The path to a directory containing certificates to use for TLS
     *     verification.
     * 
     * @throws GuacamoleException
     *     If guacamole.properties cannot be parsed.
     */
    private File getDockerCertPath() throws GuacamoleException {
        return environment.getProperty(DOCKER_CERT_PATH);
    }
    
    /**
     * Return the path to the directory where configuration files can be found
     * for the Docker client used to communicate with the Docker host.
     * 
     * @return
     *     The path to the directory used to store Docker client configuration
     *     files.
     * 
     * @throws GuacamoleException
     *     If guacamole.properties cannot be parsed.
     */
    private File getDockerConfigPath() throws GuacamoleException {
        return environment.getProperty(DOCKER_CONFIG_PATH);
    }
    
    /**
     * Return the API version used to communicate with the Docker host.
     * 
     * @return
     *     The RemoteApiVersion used to communicate with the Docker host.
     * 
     * @throws GuacamoleException
     *     If guacamole.properties cannot be parsed.
     */
    private RemoteApiVersion getApiVersion() throws GuacamoleException {
        return RemoteApiVersion.parseConfigWithDefault(
                environment.getProperty(DOCKER_API_VERSION));
    }
    
    /**
     * Return the URI of the Docker registry to use when searching for Docker
     * images and deploying them to a Docker host.
     * 
     * @return
     *     A URI to use to communicate with the Docker registry.
     * 
     * @throws GuacamoleException
     *     If guacamole.properties cannot be parsed.
     */
    private URI getRegistryUrl() throws GuacamoleException {
        try {
            return new URI(environment.getProperty(DOCKER_REGISTRY_URL));
        }
        catch (URISyntaxException e) {
            throw new GuacamoleServerException(e);
        }
    }
    
    /**
     * Return the username for authenticating to the Docker registry.
     * 
     * @return
     *     The username for authenticating to the Docker registry.
     * 
     * @throws GuacamoleException
     *     If guacamole.properties cannot be parsed.
     */
    private String getRegistryUser() throws GuacamoleException {
        return environment.getProperty(DOCKER_REGISTRY_USER);
    }
    
    /**
     * Return the password for authenticating to the Docker registry.
     * 
     * @return
     *     The password for authenticating to the Docker registry.
     * 
     * @throws GuacamoleException
     *     If guacamole.properties cannot be parsed.
     */
    private String getRegistryPassword() throws GuacamoleException {
        return environment.getProperty(DOCKER_REGISTRY_PASSWORD);
    }
    
    /**
     * Return the e-mail address to use with the Docker registry.
     * 
     * @return
     *     The e-mail address for use with the Docker registry.
     * 
     * @throws GuacamoleException
     *     If guacamole.properties cannot be parsed.
     */
    private String getRegistryEmail() throws GuacamoleException {
        return environment.getProperty(DOCKER_REGISTRY_EMAIL);
    }
    
    /**
     * Returns a DockerClientConfig that contains the parameters specified in
     * the guacamole.properties file, a configuration that can be passed on to
     * the DockerClient objects used within this extension.
     * 
     * @return
     *     A DockerClientConfig to use with DockerClient objects within this
     *     extension.
     * 
     * @throws GuacamoleException
     *     If guacamole.properties cannot be parsed.
     */
    public DockerClientConfig getDockerClientConfig() throws GuacamoleException {    
        return DefaultDockerClientConfig.createDefaultConfigBuilder()
                .withDockerHost(getDockerHost())
                .withDockerTlsVerify(getVerifyTls())
                .withDockerCertPath(getDockerCertPath().toString())
                .withDockerConfig(getDockerConfigPath().toString())
                .withApiVersion(getApiVersion())
                .withRegistryUrl(getRegistryUrl().toString())
                .withRegistryUsername(getRegistryUser())
                .withRegistryPassword(getRegistryPassword())
                .withRegistryEmail(getRegistryEmail())
                .build();
    }
    
}
