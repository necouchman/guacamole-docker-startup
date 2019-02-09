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

package org.apache.guacamole.auth.docker.user;

import com.google.inject.Inject;
import java.util.Collection;
import java.util.Collections;
import org.apache.guacamole.GuacamoleException;
import org.apache.guacamole.auth.docker.conf.ConfigurationService;
import org.apache.guacamole.form.Form;
import org.apache.guacamole.net.auth.AuthenticationProvider;
import org.apache.guacamole.net.auth.DecoratingDirectory;
import org.apache.guacamole.net.auth.DelegatingUserContext;
import org.apache.guacamole.net.auth.Directory;
import org.apache.guacamole.net.auth.Permissions;
import org.apache.guacamole.net.auth.User;
import org.apache.guacamole.net.auth.UserContext;
import org.apache.guacamole.net.auth.UserGroup;
import org.apache.guacamole.net.auth.permission.ObjectPermission;
import org.apache.guacamole.net.auth.permission.ObjectPermissionSet;
import org.apache.guacamole.net.auth.permission.SystemPermission;
import org.apache.guacamole.net.auth.permission.SystemPermissionSet;

/**
 * A UserContext that delegates authentication and storage of user attributes
 * to another module, and provides for handling startup of and connection to
 * a Docker container.
 */
public class DockerStartupUserContext extends DelegatingUserContext {
    
    /**
     * The configuration service for this module.
     */
    @Inject
    private ConfigurationService confService;
    
    /**
     * The authentication provider associated with this user context.
     */
    private final AuthenticationProvider authProvider;
    
    /**
     * The username used to log in to Guacamole.
     */
    private final String username;
    
    public DockerStartupUserContext(UserContext userContext) {
        super(userContext);
        this.authProvider = userContext.getAuthenticationProvider();
        this.username = userContext.self().getIdentifier();
    }
    
    @Override
    public AuthenticationProvider getAuthenticationProvider() {
        return authProvider;
    }
    
    @Override
    public Directory<User> getUserDirectory() throws GuacamoleException {
        return new DecoratingDirectory<User>(super.getUserDirectory()) {
            
            @Override
            protected User decorate(User object) throws GuacamoleException {
                Permissions effective = self().getEffectivePermissions();
                SystemPermissionSet sys = effective.getSystemPermissions();
                ObjectPermissionSet obj = effective.getUserPermissions();
                Boolean canUpdate = false;
                if (sys.hasPermission(SystemPermission.Type.ADMINISTER)
                        || obj.hasPermission(ObjectPermission.Type.UPDATE, object.getIdentifier()))
                    canUpdate = true;
                return new DockerStartupUser(object, canUpdate);
            }
            
            @Override
            protected User undecorate(User object) throws GuacamoleException {
                assert(object instanceof DockerStartupUser);
                return ((DockerStartupUser) object).getUndecorated();
            }
            
            
        };
    }
    
    @Override
    public Directory<UserGroup> getUserGroupDirectory() throws GuacamoleException {
        return new DecoratingDirectory<UserGroup>(super.getUserGroupDirectory()) {
            
            @Override
            protected UserGroup decorate(UserGroup object) throws GuacamoleException {
                Permissions effective = self().getEffectivePermissions();
                SystemPermissionSet sys = effective.getSystemPermissions();
                ObjectPermissionSet obj = effective.getUserGroupPermissions();
                Boolean canUpdate = false;
                if (sys.hasPermission(SystemPermission.Type.ADMINISTER)
                        || obj.hasPermission(ObjectPermission.Type.UPDATE, object.getIdentifier()))
                    canUpdate = true;
                return new DockerStartupUserGroup(object, canUpdate);
            }
            
            @Override
            protected UserGroup undecorate(UserGroup object) {
                assert(object instanceof DockerStartupUserGroup);
                return ((DockerStartupUserGroup) object).getUndecorated();
            }
            
        };
    }
    
    @Override
    public Collection<Form> getUserAttributes() {
        Collection<Form> allAttributes = super.getUserAttributes();
        allAttributes.addAll(DockerStartupUser.ATTRIBUTES);
        return Collections.unmodifiableCollection(allAttributes);
    }
    
    @Override
    public Collection<Form> getUserGroupAttributes() {
        Collection<Form> allAttributes = super.getUserGroupAttributes();
        allAttributes.addAll(DockerStartupUserGroup.ATTRIBUTES);
        return Collections.unmodifiableCollection(allAttributes);
    }
    
}
