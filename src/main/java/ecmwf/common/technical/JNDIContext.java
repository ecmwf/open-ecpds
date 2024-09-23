/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * In applying the License, ECMWF does not waive the privileges and immunities
 * granted to it by virtue of its status as an inter-governmental organization
 * nor does it submit to any jurisdiction.
 */

package ecmwf.common.technical;

/**
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ecmwf.common.security.Tools;

/**
 * The Class JNDIContext.
 */
public final class JNDIContext implements Closeable {
    /** The Constant _log. */
    private static final Logger _log = LogManager.getLogger(JNDIContext.class);

    /** The _login context. */
    private LoginContext _loginContext = null;

    /** The _homes. */
    private final Map<String, Object> _homes = Cnf.at("JNDI", "useCache", true) ? new ConcurrentHashMap<>() : null;

    /** The _initial context. */
    private InitialContext _initialContext = null;

    /** The _attempt. */
    private int _attempt = 0;

    /** The _current. */
    private int _current = 0;

    /** The _providers. */
    private final List<String> _providers = Cnf.listAt("JNDI", "urlProvider");

    /** The _jaas module. */
    private final String _jaasModule = Cnf.at("Security", "jaasModule");

    /** The _root. */
    private final String _root = Cnf.at("Security", "ejbUser");

    /** The _certificate. */
    private final char[] _certificate = Cnf.notEmptyStringAt("Security", "ejbPassword", Tools.newPassword(_root))
            .toCharArray();

    /**
     * Gets the root.
     *
     * @return the root
     */
    public String getRoot() {
        return _root;
    }

    /**
     * Gets the certificate.
     *
     * @return the certificate
     */
    public String getCertificate() {
        return new String(_certificate);
    }

    /**
     * Invoke bean.
     *
     * @param bean
     *            the bean
     * @param method
     *            the method
     * @param types
     *            the types
     * @param values
     *            the values
     *
     * @return the object
     *
     * @throws java.lang.NoSuchMethodException
     *             the no such method exception
     * @throws java.lang.IllegalAccessException
     *             the illegal access exception
     * @throws java.lang.reflect.InvocationTargetException
     *             the invocation target exception
     */
    public static Object invokeBean(final Object bean, final String method, final Class<?>[] types,
            final Object[] values) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        final var thread = Thread.currentThread();
        final var loader = thread.getContextClassLoader();
        thread.setContextClassLoader(JNDIContext.class.getClassLoader());
        try {
            return bean.getClass().getMethod(method, types).invoke(bean, values);
        } finally {
            thread.setContextClassLoader(loader);
        }
    }

    /**
     * Gets the initial context.
     *
     * @return the initial context
     *
     * @throws NamingException
     *             the naming exception
     */
    private synchronized InitialContext _getInitialContext() throws NamingException {
        if (_initialContext == null) {
            final var env = new Hashtable<String, String>();
            env.put(Context.INITIAL_CONTEXT_FACTORY, Cnf.at("JNDI", "initialFactory"));
            if (_attempt == 0) {
                _current = _current == _providers.size() ? 1 : _current + 1;
                _attempt = Cnf.at("JNDI", "retry", 5);
            } else {
                _attempt--;
            }
            env.put(Context.PROVIDER_URL, _providers.get(_current - 1));
            try {
                _initialContext = new InitialContext(env);
            } catch (final NamingException ne) {
                _initialContext = null;
                throw ne;
            }
        }
        return _initialContext;
    }

    /**
     * Gets the bean.
     *
     * @param bean
     *            the bean
     *
     * @return the object
     *
     * @throws NamingException
     *             the naming exception
     */
    private Object _getBean(final String bean) throws NamingException {
        if (_homes != null && _homes.containsKey(bean)) {
            return _homes.get(bean);
        }
        final var home = _getInitialContext().lookup(bean);
        if (_homes != null) {
            _homes.put(bean, home);
        }
        return home;
    }

    /**
     * Creates the bean.
     *
     * @param bean
     *            the bean
     *
     * @return the object
     *
     * @throws java.lang.NoSuchMethodException
     *             the no such method exception
     * @throws java.lang.IllegalAccessException
     *             the illegal access exception
     * @throws java.lang.reflect.InvocationTargetException
     *             the invocation target exception
     * @throws javax.naming.NamingException
     *             the naming exception
     */
    public Object createBean(final String bean)
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, NamingException {
        return invokeBean(_getBean(bean), "create", null, null);
    }

    /**
     * Creates the bean.
     *
     * @param bean
     *            the bean
     * @param token
     *            the token
     *
     * @return the object
     *
     * @throws java.lang.NoSuchMethodException
     *             the no such method exception
     * @throws java.lang.IllegalAccessException
     *             the illegal access exception
     * @throws java.lang.reflect.InvocationTargetException
     *             the invocation target exception
     * @throws javax.naming.NamingException
     *             the naming exception
     */
    public Object createBean(final String bean, final String token)
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, NamingException {
        return invokeBean(_getBean(bean), "create", token == null ? null : new Class[] { String.class },
                token == null ? null : new Object[] { token });
    }

    /**
     * Login.
     *
     * @throws javax.security.auth.login.LoginException
     *             the login exception
     */
    public synchronized void login() throws LoginException {
        if (logged()) {
            try {
                logout();
            } catch (final LoginException e) {
                _log.debug(e);
            }
        }
        close();
        _loginContext = new LoginContext(_jaasModule, new EJBLoginHandler(_root, _certificate));
        var logged = false;
        try {
            _loginContext.login();
            // _log.debug("Logged");
            logged = true;
        } finally {
            if (!logged) {
                _log.debug("Not logged");
                _loginContext = null;
            }
        }
    }

    /**
     * Logout.
     *
     * @throws javax.security.auth.login.LoginException
     *             the login exception
     */
    public synchronized void logout() throws LoginException {
        try {
            if (logged()) {
                _loginContext.logout();
            }
        } finally {
            _loginContext = null;
        }
    }

    /**
     * Logged.
     *
     * @return true, if successful
     */
    public boolean logged() {
        return _loginContext != null;
    }

    /**
     * {@inheritDoc}
     *
     * Close.
     */
    @Override
    public synchronized void close() {
        try {
            if (_initialContext != null) {
                _initialContext.close();
            }
        } catch (final NamingException e) {
            _log.debug(e);
        }
        if (_homes != null) {
            _homes.clear();
        }
        _initialContext = null;
    }

    /**
     * The Class EJBLoginHandler.
     */
    static final class EJBLoginHandler implements CallbackHandler {
        /** The _username. */
        private final String _username;

        /** The _password. */
        private final char[] _password;

        /**
         * Instantiates a new EJB login handler.
         *
         * @param username
         *            the username
         * @param password
         *            the password
         */
        public EJBLoginHandler(final String username, final char[] password) {
            _username = username;
            _password = password;
        }

        /**
         * Handle.
         *
         * @param callbacks
         *            the callbacks
         *
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         * @throws UnsupportedCallbackException
         *             the unsupported callback exception
         */
        @Override
        public void handle(final Callback[] callbacks) throws IOException, UnsupportedCallbackException {
            for (final Callback callback : callbacks) {
                if (callback instanceof final NameCallback nc) {
                    nc.setName(_username);
                } else if (callback instanceof final PasswordCallback pc) {
                    pc.setPassword(_password);
                } else {
                    throw new UnsupportedCallbackException(callback, "Unrecognized callback");
                }
            }
        }
    }
}
