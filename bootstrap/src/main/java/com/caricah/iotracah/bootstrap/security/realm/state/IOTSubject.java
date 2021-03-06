/*
 *
 * Copyright (c) 2016 Caricah <info@caricah.com>.
 *
 * Caricah licenses this file to you under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License. You may obtain a copy
 *  of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under
 *  the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS
 *  OF ANY  KIND, either express or implied.  See the License for the specific language
 *  governing permissions and limitations under the License.
 *
 *
 *
 *
 */

package com.caricah.iotracah.bootstrap.security.realm.state;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.HostAuthenticationToken;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.Permission;
import org.apache.shiro.authz.UnauthenticatedException;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.SessionException;
import org.apache.shiro.session.mgt.SessionContext;
import org.apache.shiro.subject.ExecutionException;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.subject.support.DisabledSessionException;
import org.apache.shiro.subject.support.SubjectCallable;
import org.apache.shiro.subject.support.SubjectRunnable;
import org.apache.shiro.util.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author <a href="mailto:bwire@caricah.com"> Peter Bwire </a>
 * @version 1.0 2/3/16
 */
public class IOTSubject implements Subject {

    SessionContext sessionContext;

    public void setSessionContext(SessionContext sessionContext) {
        this.sessionContext = sessionContext;
    }

    public IOTSubject(PrincipalCollection principals, boolean authenticated, String host, Session session, boolean sessionCreationEnabled, org.apache.shiro.mgt.SecurityManager securityManager) {

        if (securityManager == null) {
            throw new IllegalArgumentException("SecurityManager argument cannot be null.");
        }
        this.securityManager = securityManager;
        this.principals = principals;
        this.authenticated = authenticated;
        this.host = host;
        this.session = session;
        this.sessionCreationEnabled = sessionCreationEnabled;

    }


    private static final Logger log = LoggerFactory.getLogger(IOTSubject.class);

    protected PrincipalCollection principals;
    protected boolean authenticated;
    protected String host;
    protected Session session;
    /**
     * @since 1.2
     */
    protected boolean sessionCreationEnabled;

    protected transient org.apache.shiro.mgt.SecurityManager securityManager;



    public SecurityManager getSecurityManager() {
        return securityManager;
    }

    protected boolean hasPrincipals() {
        return !CollectionUtils.isEmpty(getPrincipals());
    }

    /**
     * Returns the host name or IP associated with the client who created/is interacting with this Subject.
     *
     * @return the host name or IP associated with the client who created/is interacting with this Subject.
     */
    public String getHost() {
        return this.host;
    }

    private Object getPrimaryPrincipal(PrincipalCollection principals) {
        if (!CollectionUtils.isEmpty(principals)) {
            return principals.getPrimaryPrincipal();
        }
        return null;
    }

    /**
     * @see Subject#getPrincipal()
     */
    public Object getPrincipal() {
        return getPrimaryPrincipal(getPrincipals());
    }

    public PrincipalCollection getPrincipals() {
        return this.principals;
    }

    public boolean isPermitted(String permission) {
        return hasPrincipals() && securityManager.isPermitted(getPrincipals(), permission);
    }

    public boolean isPermitted(Permission permission) {
        return hasPrincipals() && securityManager.isPermitted(getPrincipals(), permission);
    }

    public boolean[] isPermitted(String... permissions) {
        if (hasPrincipals()) {
            return securityManager.isPermitted(getPrincipals(), permissions);
        } else {
            return new boolean[permissions.length];
        }
    }

    public boolean[] isPermitted(List<Permission> permissions) {
        if (hasPrincipals()) {
            return securityManager.isPermitted(getPrincipals(), permissions);
        } else {
            return new boolean[permissions.size()];
        }
    }

    public boolean isPermittedAll(String... permissions) {
        return hasPrincipals() && securityManager.isPermittedAll(getPrincipals(), permissions);
    }

    public boolean isPermittedAll(Collection<Permission> permissions) {
        return hasPrincipals() && securityManager.isPermittedAll(getPrincipals(), permissions);
    }

    protected void assertAuthzCheckPossible() throws AuthorizationException {
        if (!hasPrincipals()) {
            String msg = "This subject is anonymous - it does not have any identifying principals and " +
                    "authorization operations require an identity to check against.  A Subject instance will " +
                    "acquire these identifying principals automatically after a successful login is performed " +
                    "be executing " + Subject.class.getName() + ".login(AuthenticationToken) or when 'Remember Me' " +
                    "functionality is enabled by the SecurityManager.  This exception can also occur when a " +
                    "previously logged-in Subject has logged out which " +
                    "makes it anonymous again.  Because an identity is currently not known due to any of these " +
                    "conditions, authorization is denied.";
            throw new UnauthenticatedException(msg);
        }
    }

    public void checkPermission(String permission) throws AuthorizationException {
        assertAuthzCheckPossible();
        securityManager.checkPermission(getPrincipals(), permission);
    }

    public void checkPermission(Permission permission) throws AuthorizationException {
        assertAuthzCheckPossible();
        securityManager.checkPermission(getPrincipals(), permission);
    }

    public void checkPermissions(String... permissions) throws AuthorizationException {
        assertAuthzCheckPossible();
        securityManager.checkPermissions(getPrincipals(), permissions);
    }

    public void checkPermissions(Collection<Permission> permissions) throws AuthorizationException {
        assertAuthzCheckPossible();
        securityManager.checkPermissions(getPrincipals(), permissions);
    }

    public boolean hasRole(String roleIdentifier) {
        return hasPrincipals() && securityManager.hasRole(getPrincipals(), roleIdentifier);
    }

    public boolean[] hasRoles(List<String> roleIdentifiers) {
        if (hasPrincipals()) {
            return securityManager.hasRoles(getPrincipals(), roleIdentifiers);
        } else {
            return new boolean[roleIdentifiers.size()];
        }
    }

    public boolean hasAllRoles(Collection<String> roleIdentifiers) {
        return hasPrincipals() && securityManager.hasAllRoles(getPrincipals(), roleIdentifiers);
    }

    public void checkRole(String role) throws AuthorizationException {
        assertAuthzCheckPossible();
        securityManager.checkRole(getPrincipals(), role);
    }

    public void checkRoles(String... roleIdentifiers) throws AuthorizationException {
        assertAuthzCheckPossible();
        securityManager.checkRoles(getPrincipals(), roleIdentifiers);
    }

    public void checkRoles(Collection<String> roles) throws AuthorizationException {
        assertAuthzCheckPossible();
        securityManager.checkRoles(getPrincipals(), roles);
    }

    public void login(AuthenticationToken token) throws AuthenticationException {
        Subject subject = securityManager.login(this, token);

        PrincipalCollection principals;


            IOTSubject iotSubject = (IOTSubject) subject;
            //we have to do this in case there are assumed identities - we don't want to lose the 'real' principals:
            principals = iotSubject.principals;
        String    host = iotSubject.host;

        if (principals == null || principals.isEmpty()) {
            String msg = "Principals returned from securityManager.login( token ) returned a null or " +
                    "empty value.  This value must be non null and populated with one or more elements.";
            throw new IllegalStateException(msg);
        }
        this.principals = principals;
        this.authenticated = true;
        if (token instanceof HostAuthenticationToken) {
            host = ((HostAuthenticationToken) token).getHost();
        }
        if (host != null) {
            this.host = host;
        }
        this.session = subject.getSession(false);

    }

    public boolean isAuthenticated() {
        return authenticated;
    }

    public boolean isRemembered() {
        PrincipalCollection principals = getPrincipals();
        return principals != null && !principals.isEmpty() && !isAuthenticated();
    }

    /**
     * Returns {@code true} if this Subject is allowed to create sessions, {@code false} otherwise.
     *
     * @return {@code true} if this Subject is allowed to create sessions, {@code false} otherwise.
     * @since 1.2
     */
    protected boolean isSessionCreationEnabled() {
        return this.sessionCreationEnabled;
    }

    public Session getSession() {
        return getSession(true);
    }

    public Session getSession(boolean create) {
        if (log.isTraceEnabled()) {
            log.trace("attempting to get session; create = " + create +
                    "; session is null = " + (this.session == null) +
                    "; session has id = " + (this.session != null && session.getId() != null));
        }

        if (this.session == null && create) {

            //added in 1.2:
            if (!isSessionCreationEnabled()) {
                String msg = "Session creation has been disabled for the current subject.  This exception indicates " +
                        "that there is either a programming error (using a session when it should never be " +
                        "used) or that Shiro's configuration needs to be adjusted to allow Sessions to be created " +
                        "for the current Subject.  See the " + DisabledSessionException.class.getName() + " JavaDoc " +
                        "for more.";
                throw new DisabledSessionException(msg);
            }

            log.trace("Starting session for host {}", getHost());
            this.session = this.securityManager.start(sessionContext);
        }
        return this.session;
    }




    public void logout() {
        try {
            this.securityManager.logout(this);
        } finally {
            this.session = null;
            this.principals = null;
            this.authenticated = false;
            //Don't set securityManager to null here - the Subject can still be
            //used, it is just considered anonymous at this point.  The SecurityManager instance is
            //necessary if the subject would log in again or acquire a new session.  This is in response to
            //https://issues.apache.org/jira/browse/JSEC-22
            //this.securityManager = null;
        }
    }

    @Override
    public <V> V execute(Callable<V> callable) throws ExecutionException {
        return null;
    }

    @Override
    public void execute(Runnable runnable) {

    }

    @Override
    public <V> Callable<V> associateWith(Callable<V> callable) {
        return callable;
    }

    @Override
    public Runnable associateWith(Runnable runnable) {
        return runnable;
    }

    @Override
    public void runAs(PrincipalCollection principals) throws NullPointerException, IllegalStateException {

    }

    @Override
    public boolean isRunAs() {
        return false;
    }

    @Override
    public PrincipalCollection getPreviousPrincipals() {
        return null;
    }

    @Override
    public PrincipalCollection releaseRunAs() {
        return null;
    }


}
