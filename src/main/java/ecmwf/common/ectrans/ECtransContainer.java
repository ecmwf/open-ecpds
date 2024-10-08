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

package ecmwf.common.ectrans;

/**
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import static ecmwf.common.ectrans.ECtransOptions.HOST_ECTRANS_CLOSE_ASYNCHRONOUS;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ECTRANS_CLOSE_TIME_OUT;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ECTRANS_CONNECT_TIME_OUT;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ECTRANS_DEBUG;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ECTRANS_DEL_TIME_OUT;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ECTRANS_GET_TIME_OUT;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ECTRANS_LIST_TIME_OUT;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ECTRANS_MKDIR_TIME_OUT;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ECTRANS_MOVE_TIME_OUT;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ECTRANS_PUT_TIME_OUT;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ECTRANS_RETRY_COUNT;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ECTRANS_RETRY_FREQUENCY;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ECTRANS_RMDIR_TIME_OUT;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ECTRANS_SIZE_TIME_OUT;
import static ecmwf.common.text.Util.isNotEmpty;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Date;
import java.sql.Time;
import java.util.Optional;
import java.util.concurrent.TimeoutException;

import javax.management.AttributeNotFoundException;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanConstructorInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.timer.Timer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ecmwf.common.database.ECUser;
import ecmwf.common.database.ECtransDestination;
import ecmwf.common.database.ECtransHistory;
import ecmwf.common.database.MSUser;
import ecmwf.common.ecaccess.MBeanRepository;
import ecmwf.common.mbean.MBeanManager;
import ecmwf.common.mbean.MBeanService;
import ecmwf.common.starter.Starter;
import ecmwf.common.technical.Cnf;
import ecmwf.common.technical.ThreadService.ConfigurableRunnable;
import ecmwf.common.technical.WaitingThread;
import ecmwf.common.text.Format;
import ecmwf.common.text.Options;

/**
 * The Class ECtransContainer.
 */
public final class ECtransContainer implements MBeanService {
    /** The Constant _log. */
    private static final Logger _log = LogManager.getLogger(ECtransContainer.class);

    /** The repository. */
    private final CookieRepository cookieRepository;

    /** The defaultProvider. */
    private final ECtransProvider defaultProvider;

    /** The activated. */
    private boolean activated = true;

    /** The inProgress. */
    private long inProgress = 0;

    /**
     * Instantiates a new ectrans container.
     *
     * @param defaultProvider
     *            the default provider
     */
    public ECtransContainer(final ECtransProvider defaultProvider) {
        this(defaultProvider, true);
    }

    /**
     * Instantiates a new ectrans container.
     *
     * @param defaultProvider
     *            the default provider
     * @param cookieEnabled
     *            the cookie enabled
     */
    public ECtransContainer(final ECtransProvider defaultProvider, final boolean cookieEnabled) {
        cookieRepository = cookieEnabled ? new CookieRepository("CookieRepository") : null;
        this.defaultProvider = defaultProvider;
    }

    /**
     * {@inheritDoc}
     *
     * Gets the attribute.
     */
    @Override
    public Object getAttribute(final String attributeName) throws AttributeNotFoundException, MBeanException {
        try {
            if ("Activated".equals(attributeName)) {
                return activated;
            }
            if ("InProgress".equals(attributeName)) {
                return inProgress;
            }
        } catch (final Exception e) {
            _log.warn("Getting an MBean attribute", e);
            throw new MBeanException(e);
        }
        throw new AttributeNotFoundException(
                "Cannot find " + attributeName + " attribute in " + this.getClass().getName());
    }

    /**
     * {@inheritDoc}
     *
     * Gets the MBean info.
     */
    @Override
    public MBeanInfo getMBeanInfo() {
        return new MBeanInfo(this.getClass().getName(), """
                The "ectrans" command is provided for Member State users, \
                who are using their shell account at ECMWF, to transfer \
                files, located at ECMWF, through the secure file transfer \
                feature of the ECaccess Server. This MBean provides \
                operations to check the ECtrans database configuration \
                and to test the ECtrans modules.""",
                new MBeanAttributeInfo[] {
                        new MBeanAttributeInfo("Activated", "java.lang.Boolean",
                                "Activated: specify if ECTrans can be used.", true, true, false),
                        new MBeanAttributeInfo("InProgress", "long", "InProgress: number of transfer(s) in progress.",
                                true, false, false) },
                new MBeanConstructorInfo[0],
                new MBeanOperationInfo[] {
                        new MBeanOperationInfo("size",
                                "size(ecuser,remote,target): get the size of a file using ECTrans",
                                new MBeanParameterInfo[] {
                                        new MBeanParameterInfo("ecuser", "java.lang.String", "ECMWF user name"),
                                        new MBeanParameterInfo("remote", "java.lang.String",
                                                "remote parameter of ectrans: msuser[:password]@destination"),
                                        new MBeanParameterInfo("target", "java.lang.String",
                                                "target parameter of ectrans: [location/]target-filename") },
                                "java.lang.Long", MBeanOperationInfo.ACTION),
                        new MBeanOperationInfo("size",
                                "size(cookie,ecuser,remote,target): get the size of a file using ECTrans",
                                new MBeanParameterInfo[] {
                                        new MBeanParameterInfo("cookie", "java.lang.String", "ECtrans cookie"),
                                        new MBeanParameterInfo("ecuser", "java.lang.String", "ECMWF user name"),
                                        new MBeanParameterInfo("remote", "java.lang.String",
                                                "remote parameter of ectrans: msuser[:password]@destination"),
                                        new MBeanParameterInfo("target", "java.lang.String",
                                                "target parameter of ectrans: [location/]target-filename") },
                                "java.lang.Long", MBeanOperationInfo.ACTION),
                        new MBeanOperationInfo("del", "del(ecuser,remote,target): del a file using ECTrans",
                                new MBeanParameterInfo[] {
                                        new MBeanParameterInfo("ecuser", "java.lang.String", "ECMWF user name"),
                                        new MBeanParameterInfo("remote", "java.lang.String",
                                                "remote parameter of ectrans: msuser[:password]@destination"),
                                        new MBeanParameterInfo("target", "java.lang.String",
                                                "target parameter of ectrans: [location/]target-filename") },
                                "void", MBeanOperationInfo.ACTION),
                        new MBeanOperationInfo("del", "del(ecuser,remote,target): del a file using ECTrans",
                                new MBeanParameterInfo[] {
                                        new MBeanParameterInfo("cookie", "java.lang.String", "ECtrans cookie"),
                                        new MBeanParameterInfo("ecuser", "java.lang.String", "ECMWF user name"),
                                        new MBeanParameterInfo("remote", "java.lang.String",
                                                "remote parameter of ectrans: msuser[:password]@destination"),
                                        new MBeanParameterInfo("target", "java.lang.String",
                                                "target parameter of ectrans: [location/]target-filename") },
                                "void", MBeanOperationInfo.ACTION),
                        new MBeanOperationInfo("getURL",
                                "getURL(ecuser,remote,target): get the target URL from the configuration",
                                new MBeanParameterInfo[] {
                                        new MBeanParameterInfo("ecuser", "java.lang.String", "ECMWF user name"),
                                        new MBeanParameterInfo("remote", "java.lang.String",
                                                "remote parameter of ectrans: msuser[:password]@destination"),
                                        new MBeanParameterInfo("target", "java.lang.String",
                                                "target parameter of ectrans: [location/]target-filename") },
                                "java.lang.String", MBeanOperationInfo.ACTION),
                        new MBeanOperationInfo("remove",
                                "remove(cookie,ecuser,remote): remove the ectrans referenced by this cookie",
                                new MBeanParameterInfo[] {
                                        new MBeanParameterInfo("cookie", "java.lang.String", "ECtrans cookie"),
                                        new MBeanParameterInfo("ecuser", "java.lang.String", "ECMWF user name"),
                                        new MBeanParameterInfo("remote", "java.lang.String",
                                                "remote parameter of ectrans: msuser[:password]@destination"),
                                        new MBeanParameterInfo("location", "java.lang.String", "ECtrans location") },
                                "java.lang.String", MBeanOperationInfo.ACTION) },
                new MBeanNotificationInfo[0]);
    }

    /**
     * {@inheritDoc}
     *
     * Invoke.
     */
    @Override
    public Object invoke(final String operationName, final Object[] params, final String[] signature)
            throws NoSuchMethodException, MBeanException {
        try {
            if ("size".equals(operationName) && signature.length == 3 && "java.lang.String".equals(signature[0])
                    && "java.lang.String".equals(signature[1]) && "java.lang.String".equals(signature[2])) {
                final var file = new File((String) params[2]);
                final var size = new ECtransSize(file.getName());
                syncExec(size, null, (String) params[0], (String) params[1], file.getParent(), false);
                return size.getSize();
            }
            if ("size".equals(operationName) && signature.length == 4 && "java.lang.String".equals(signature[0])
                    && "java.lang.String".equals(signature[1]) && "java.lang.String".equals(signature[2])
                    && "java.lang.String".equals(signature[3])) {
                final var file = new File((String) params[3]);
                final var size = new ECtransSize(file.getName());
                syncExec(size, (String) params[0], (String) params[1], (String) params[2], file.getParent(), false);
                return size.getSize();
            }
            if ("del".equals(operationName) && signature.length == 3 && "java.lang.String".equals(signature[0])
                    && "java.lang.String".equals(signature[1]) && "java.lang.String".equals(signature[2])) {
                final var file = new File((String) params[2]);
                syncExec(new ECtransDel(file.getName()), null, (String) params[0], (String) params[1], file.getParent(),
                        false);
                return Boolean.TRUE;
            }
            if ("del".equals(operationName) && signature.length == 4 && "java.lang.String".equals(signature[0])
                    && "java.lang.String".equals(signature[1]) && "java.lang.String".equals(signature[2])
                    && "java.lang.String".equals(signature[3])) {
                final var file = new File((String) params[3]);
                syncExec(new ECtransDel(file.getName()), (String) params[0], (String) params[1], (String) params[2],
                        file.getParent(), false);
                return Boolean.TRUE;
            }
            if ("getURL".equals(operationName) && signature.length == 3 && "java.lang.String".equals(signature[0])
                    && "java.lang.String".equals(signature[1]) && "java.lang.String".equals(signature[2])) {
                return getTransferURL(defaultProvider, new ECtransHistory(), (String) params[0], (String) params[1],
                        (String) params[2], true).getURL();
            }
            if ("remove".equals(operationName) && signature.length == 4 && "java.lang.String".equals(signature[0])
                    && "java.lang.String".equals(signature[1]) && "java.lang.String".equals(signature[2])
                    && "java.lang.String".equals(signature[3])) {
                return close(defaultProvider, (String) params[0], (String) params[1], (String) params[2],
                        (String) params[3]);
            }
        } catch (final Exception e) {
            _log.warn("Invoking the {} MBean method", operationName, e);
            throw new MBeanException(e);
        }
        throw new NoSuchMethodException(operationName);
    }

    /**
     * {@inheritDoc}
     *
     * Sets the attribute.
     */
    @Override
    public boolean setAttribute(final String name, final Object value)
            throws InvalidAttributeValueException, MBeanException {
        if ("Activated".equals(name) && value instanceof final Boolean bool) {
            activated = bool;
            return true;
        }
        return false;
    }

    /**
     * Gets the log message.
     *
     * @param request
     *            the request
     * @param args
     *            the args
     *
     * @return the string
     */
    private static final String getLogMessage(final String request, final Object... args) {
        final var result = new StringBuilder();
        for (final Object arg : args) {
            result.append(result.length() == 0 ? "" : ",").append(arg != null ? "\"" + arg + "\"" : "null");
        }
        return "ECtrans " + request + "(" + result.toString() + ")";
    }

    /**
     * _log.
     *
     * @param module
     *            the module
     * @param action
     *            the action
     * @param start
     *            the start
     */
    private static void log(final TransferModule module, final String action, final boolean start) {
        final var name = module.getECtransModule().getName() + "." + action + "()";
        final var cookie = module.getCookie();
        _log.debug((start ? "Start " + name : name + " completed") + (cookie != null ? " for cookie " + cookie : ""));
    }

    /**
     * Gets the init sequence.
     *
     * @param provider
     *            the provider
     * @param msuser
     *            the msuser
     * @param ecuser
     *            the ecuser
     * @param destination
     *            the destination
     * @param location
     *            the location
     * @param hidePassword
     *            the hide password
     *
     * @return the string
     */
    private static String getInitSequence(final RemoteProvider provider, final MSUser msuser, final ECUser ecuser,
            final ECtransDestination destination, final String location, final boolean hidePassword) {
        final var passwd = provider.decrypt(msuser.getPasswd());
        final var passwdToDisplay = hidePassword ? Format.hide(passwd) : passwd;
        final var ecdir = ecuser.getDir();
        final var sb = new StringBuilder(Cnf.getValue(destination.getValue()));
        Format.replaceAll(sb, "$msuser[name]", msuser.getName());
        Format.replaceAll(sb, "$msuser[comment]", msuser.getComment());
        Format.replaceAll(sb, "$msuser[dir]", msuser.getDir());
        Format.replaceAll(sb, "$msuser[host]", msuser.getHost());
        Format.replaceAll(sb, "$msuser[login]", msuser.getLogin());
        Format.replaceAll(sb, "$msuser[passwd]", passwdToDisplay);
        Format.replaceAll(sb, "$msuser[password]", passwdToDisplay);
        Format.replaceAll(sb, "$ecuser[name]", ecuser.getName());
        Format.replaceAll(sb, "$ecuser[uid]", String.valueOf(ecuser.getUid()));
        Format.replaceAll(sb, "$ecuser[gid]", String.valueOf(ecuser.getGid()));
        Format.replaceAll(sb, "$ecuser[dir]", isNotEmpty(ecdir) ? ecdir + File.separator : ecdir);
        Format.replaceAll(sb, "$ecuser[shell]", ecuser.getShell());
        Format.replaceAll(sb, "$ecuser[comment]", ecuser.getComment());
        Format.replaceAll(sb, "$ecuser", ecuser.getName());
        Format.replaceAll(sb, "$target", location);
        Format.replaceAll(sb, "$location", location);
        Format.replaceAll(sb, "$password", passwdToDisplay);
        Format.replaceAll(sb, "$passwd", passwdToDisplay);
        return sb.toString();
    }

    /**
     * Sync exec.
     *
     * @param action
     *            the action
     * @param cookie
     *            the cookie
     * @param ecuser
     *            the ecuser
     * @param remote
     *            the remote
     * @param location
     *            the location
     * @param interruptible
     *            the interruptible
     *
     * @throws ecmwf.common.ectrans.ECtransException
     *             the ectrans exception
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    public void syncExec(final ECtransAction action, final String cookie, final String ecuser, final String remote,
            final String location, final boolean interruptible) throws ECtransException, IOException {
        syncExec(action, cookie, ecuser, remote, location, null, interruptible);
    }

    /**
     * Sync exec.
     *
     * @param action
     *            the action
     * @param cookie
     *            the cookie
     * @param ecuser
     *            the ecuser
     * @param remote
     *            the remote
     * @param location
     *            the location
     * @param callback
     *            the callback
     * @param interruptible
     *            the interruptible
     *
     * @throws ecmwf.common.ectrans.ECtransException
     *             the ectrans exception
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    public void syncExec(final ECtransAction action, final String cookie, final String ecuser, final String remote,
            final String location, final ECtransCallback callback, final boolean interruptible)
            throws ECtransException, IOException {
        new ECtransThread(new LocalFinder(ecuser, remote), action, cookie, location, null, callback, interruptible)
                .exec();
    }

    /**
     * Sync exec.
     *
     * @param provider
     *            the provider
     * @param ecuser
     *            the ecuser
     * @param msuser
     *            the msuser
     * @param destination
     *            the destination
     * @param action
     *            the action
     * @param cookie
     *            the cookie
     * @param location
     *            the location
     * @param interruptible
     *            the interruptible
     *
     * @throws ecmwf.common.ectrans.ECtransException
     *             the ectrans exception
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    public void syncExec(final RemoteProvider provider, final ECUser ecuser, final MSUser msuser,
            final ECtransDestination destination, final ECtransAction action, final String cookie,
            final String location, final boolean interruptible) throws ECtransException, IOException {
        syncExec(provider, ecuser, msuser, destination, action, cookie, location, new DefaultCallback(provider, msuser),
                interruptible);
    }

    /**
     * Sync exec.
     *
     * @param provider
     *            the provider
     * @param ecuser
     *            the ecuser
     * @param msuser
     *            the msuser
     * @param destination
     *            the destination
     * @param action
     *            the action
     * @param cookie
     *            the cookie
     * @param location
     *            the location
     * @param callback
     *            the callback
     * @param interruptible
     *            the interruptible
     *
     * @throws ecmwf.common.ectrans.ECtransException
     *             the ectrans exception
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    public void syncExec(final RemoteProvider provider, final ECUser ecuser, final MSUser msuser,
            final ECtransDestination destination, final ECtransAction action, final String cookie,
            final String location, final ECtransCallback callback, final boolean interruptible)
            throws ECtransException, IOException {
        new ECtransThread(new RemoteFinder(provider, ecuser, msuser, destination), action, cookie, location, null,
                callback, interruptible).exec();
    }

    /**
     * Async exec.
     *
     * @param action
     *            the action
     * @param cookie
     *            the cookie
     * @param ecuser
     *            the ecuser
     * @param remote
     *            the remote
     * @param location
     *            the location
     *
     * @throws ecmwf.common.ectrans.ECtransException
     *             the ectrans exception
     */
    public void asyncExec(final ECtransAction action, final String cookie, final String ecuser, final String remote,
            final String location) throws ECtransException {
        asyncExec(action, cookie, ecuser, remote, location, null, null);
    }

    /**
     * Async exec.
     *
     * @param action
     *            the action
     * @param cookie
     *            the cookie
     * @param ecuser
     *            the ecuser
     * @param remote
     *            the remote
     * @param location
     *            the location
     * @param connectOptions
     *            the connect options
     * @param callback
     *            the callback
     *
     * @throws ecmwf.common.ectrans.ECtransException
     *             the ectrans exception
     */
    public void asyncExec(final ECtransAction action, final String cookie, final String ecuser, final String remote,
            final String location, final Options connectOptions, final ECtransCallback callback)
            throws ECtransException {
        final ConfigurableRunnable r = new ECtransThread(new LocalFinder(ecuser, remote), action, cookie, location,
                connectOptions, callback, false);
        r.execute(true);
    }

    /**
     * Async exec.
     *
     * @param action
     *            the action
     * @param cookie
     *            the cookie
     * @param ecuser
     *            the ecuser
     * @param remote
     *            the remote
     * @param location
     *            the location
     * @param callback
     *            the callback
     *
     * @throws ecmwf.common.ectrans.ECtransException
     *             the ectrans exception
     */
    public void asyncExec(final ECtransAction action, final String cookie, final String ecuser, final String remote,
            final String location, final ECtransCallback callback) throws ECtransException {
        final ConfigurableRunnable r = new ECtransThread(new LocalFinder(ecuser, remote), action, cookie, location,
                null, callback, false);
        r.execute(true);
    }

    /**
     * Async exec.
     *
     * @param provider
     *            the provider
     * @param ecuser
     *            the ecuser
     * @param msuser
     *            the msuser
     * @param destination
     *            the destination
     * @param action
     *            the action
     * @param cookie
     *            the cookie
     * @param location
     *            the location
     *
     * @throws ecmwf.common.ectrans.ECtransException
     *             the ectrans exception
     */
    public void asyncExec(final RemoteProvider provider, final ECUser ecuser, final MSUser msuser,
            final ECtransDestination destination, final ECtransAction action, final String cookie,
            final String location) throws ECtransException {
        asyncExec(provider, ecuser, msuser, destination, action, cookie, location,
                new DefaultCallback(provider, msuser));
    }

    /**
     * Async exec.
     *
     * @param provider
     *            the provider
     * @param ecuser
     *            the ecuser
     * @param msuser
     *            the msuser
     * @param destination
     *            the destination
     * @param action
     *            the action
     * @param cookie
     *            the cookie
     * @param location
     *            the location
     * @param callback
     *            the callback
     *
     * @throws ecmwf.common.ectrans.ECtransException
     *             the ectrans exception
     */
    public void asyncExec(final RemoteProvider provider, final ECUser ecuser, final MSUser msuser,
            final ECtransDestination destination, final ECtransAction action, final String cookie,
            final String location, final ECtransCallback callback) throws ECtransException {
        final ConfigurableRunnable r = new ECtransThread(new RemoteFinder(provider, ecuser, msuser, destination),
                action, cookie, location, null, callback, false);
        r.execute(true);
    }

    /**
     * Gets the transfer url.
     *
     * @param history
     *            the history
     * @param ecuser
     *            the ecuser
     * @param remote
     *            the remote
     * @param location
     *            the location
     * @param hidePassword
     *            the hide password
     *
     * @return the transfer url
     *
     * @throws ecmwf.common.ectrans.ECtransException
     *             the ectrans exception
     */
    public TransferURL getTransferURL(final ECtransHistory history, final String ecuser, final String remote,
            final String location, final boolean hidePassword) throws ECtransException {
        return getTransferURL(defaultProvider, history, ecuser, remote, location, hidePassword);
    }

    /**
     * Gets the transfer url.
     *
     * @param provider
     *            the provider
     * @param history
     *            the history
     * @param ecuser
     *            the ecuser
     * @param remote
     *            the remote
     * @param location
     *            the location
     * @param hidePassword
     *            the hide password
     *
     * @return the transfer url
     *
     * @throws ecmwf.common.ectrans.ECtransException
     *             the ectrans exception
     */
    public TransferURL getTransferURL(final RemoteProvider provider, final ECtransHistory history, final String ecuser,
            final String remote, final String location, final boolean hidePassword) throws ECtransException {
        String user;
        String target;
        int ind;
        if ((ind = remote.indexOf("@")) != -1) {
            user = remote.substring(0, ind);
            target = remote.substring(ind + 1);
        } else {
            user = remote;
            target = null;
        }
        if ((ind = user.indexOf(":")) != -1) {
            _log.warn("Password set but not used");
            user = user.substring(0, ind);
        }
        final var msuser = defaultProvider.getMSUser(ecuser, user);
        if (msuser == null) {
            throw new ECtransException("Association " + user + " not found");
        }
        if (target == null && (target = msuser.getECtransDestinationName()) == null) {
            throw new ECtransException("no default destination");
        }
        if (history != null) {
            history.setMSUserName(user);
            history.setMSUser(msuser);
            history.setLocation(location);
            history.setRemote(remote);
        }
        final var ecUser = defaultProvider.getECUser(ecuser);
        if (ecUser == null) {
            throw new ECtransException("ECMWF user " + ecuser + " not found");
        }
        final var destination = defaultProvider.getECtransDestination(target);
        if (destination == null) {
            throw new ECtransException("destination " + target + " not found");
        }
        if (history != null) {
            history.setECtransDestinationName(destination.getName());
            history.setECtransDestination(destination);
        }
        if (!msuser.getActive()) {
            throw new ECtransException("Association " + user + " not active");
        }
        if (!msuser.getECUserName().equals(ecuser) && !defaultProvider.isGranted(ecUser, msuser)) {
            throw new ECtransException("user not granted by " + msuser.getECUserName() + " for " + msuser.getName());
        }
        if (!destination.getActive()) {
            throw new ECtransException("destination " + target + " not active");
        }
        if (!destination.getResolve() && location != null && location.indexOf("..") != -1) {
            throw new ECtransException("destination " + target + " refuse '..' as a path element");
        }
        if (!defaultProvider.isGranted(msuser, destination)) {
            throw new ECtransException("Association " + user + " not granted for destination " + target);
        }
        if (history != null) {
            history.setUrl(getInitSequence(provider, msuser, ecUser, destination, location, true));
        }
        return new TransferURL(getInitSequence(provider, msuser, ecUser, destination, location, hidePassword),
                destination, msuser, ecUser);
    }

    /**
     * Close.
     *
     * @param cookie
     *            the cookie
     * @param ecuser
     *            the ecuser
     * @param remote
     *            the remote
     * @param location
     *            the location
     *
     * @return true, if successful
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    public boolean close(final String cookie, final String ecuser, final String remote, final String location)
            throws IOException {
        return close(defaultProvider, cookie, ecuser, remote, location);
    }

    /**
     * Close.
     *
     * @param provider
     *            the provider
     * @param cookie
     *            the cookie
     * @param ecuser
     *            the ecuser
     * @param remote
     *            the remote
     * @param location
     *            the location
     *
     * @return true, if successful
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    public boolean close(final RemoteProvider provider, final String cookie, final String ecuser, final String remote,
            final String location) throws IOException {
        if (cookieRepository == null) {
            _log.warn("No repository, DataTransfer not closed.");
            return false;
        }
        var address = remote;
        try {
            address = remote.indexOf("@") == -1 ? remote + "@"
                    + getTransferURL(provider, null, ecuser, remote, location, false).getECtransDestination().getName()
                    : remote;
        } catch (final Exception e) {
            // Ignore
        }
        final var module = cookieRepository.remove(provider, ecuser, address, location, cookie);
        final var removed = module != null;
        if (removed) {
            _log.info("Removing TransferModule for cookie {}", cookie);
            @SuppressWarnings("null")
            final var setup = new DefaultCallback(provider, module.getMSUser()).getECtransSetup();
            close(provider, module, null, setup.getDuration(HOST_ECTRANS_CLOSE_TIME_OUT).toMillis(),
                    setup.getBoolean(HOST_ECTRANS_CLOSE_ASYNCHRONOUS));
        }
        return removed;
    }

    /**
     * Close.
     *
     * @param provider
     *            the provider
     * @param ecuser
     *            the ecuser
     * @param msuser
     *            the msuser
     * @param destination
     *            the destination
     * @param cookie
     *            the cookie
     * @param location
     *            the location
     *
     * @return true, if successful
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    public boolean close(final RemoteProvider provider, final ECUser ecuser, final MSUser msuser,
            final ECtransDestination destination, final String cookie, final String location) throws IOException {
        if (cookieRepository == null) {
            return true;
        }
        final var module = cookieRepository.remove(provider, ecuser.getName(),
                msuser.getName() + "@" + destination.getName(), location, cookie);
        final var removed = module != null;
        if (removed) {
            _log.info("Removing TransferModule for cookie {}", cookie);
            final var setup = new DefaultCallback(provider, msuser).getECtransSetup();
            close(provider, module, null, setup.getDuration(HOST_ECTRANS_CLOSE_TIME_OUT).toMillis(),
                    setup.getBoolean(HOST_ECTRANS_CLOSE_ASYNCHRONOUS));
        }
        return removed;
    }

    /**
     * Close.
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    public void close() throws IOException {
        close(defaultProvider);
    }

    /**
     * Close.
     *
     * @param provider
     *            the provider
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    public void close(final RemoteProvider provider) throws IOException {
        if (cookieRepository == null) {
            return;
        }
        for (final CookieElement element : cookieRepository.getList()) {
            final var module = element.getTransferModule();
            _log.info("Removing TransferModule for cookie {}", module.getCookie());
            close(provider, module, null, 30 * Timer.ONE_SECOND, true);
        }
    }

    /**
     * Close.
     *
     * @param provider
     *            the provider
     * @param module
     *            the module
     * @param history
     *            the history
     * @param timeout
     *            the timeout
     * @param asynchronous
     *            the asynchronous
     *
     * @return true, if successful
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    boolean close(final RemoteProvider provider, final TransferModule module, final ECtransHistory history,
            final long timeout, final boolean asynchronous) throws IOException {
        if (history != null && history.getMSUser() != null && history.getECtransDestination() != null) {
            final var currentDate = System.currentTimeMillis();
            history.setDate(new Date(currentDate));
            history.setTime(new Time(currentDate));
            _log.debug("Saving history");
            try {
                provider.onClose(history);
            } catch (final Throwable t) {
                _log.warn("Closing provider", t);
            }
        }
        if (module != null) {
            final var close = new CloseTransferModuleThread(provider, module);
            close.exec(timeout, asynchronous);
            if (!asynchronous) {
                try {
                    close.completed();
                    return true;
                } catch (final IOException e) {
                    throw e;
                } catch (final Throwable t) {
                    final var ioe = new IOException(t.getMessage());
                    ioe.initCause(t);
                    throw ioe;
                }
            }
        }
        return false;
    }

    /**
     * The Class StarterUpdate.
     */
    private static final class StarterUpdate extends FileModule {
        /** The provider. */
        private final ECtransProvider provider;

        /** The done. */
        private boolean done = false;

        /**
         * Instantiates a new starter update.
         *
         * @param provider
         *            the provider
         */
        private StarterUpdate(final ECtransProvider provider) {
            this.provider = provider;
            connect(Starter.get("dist", System.getProperty("ecmwf.dir", "../..") + "/gateway/dist"), null);
        }

        /**
         * Put.
         *
         * @param name
         *            the name
         * @param posn
         *            the posn
         * @param size
         *            the size
         *
         * @return the output stream
         *
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        @Override
        public OutputStream put(final String name, final long posn, final long size) throws IOException {
            done = true;
            return super.put(name, posn, size);
        }

        /**
         * Close.
         */
        @Override
        public void close() {
            if (done) {
                provider.deploy();
            }
        }
    }

    /**
     * The Class ModuleFinder.
     */
    private interface ModuleFinder {

        /**
         * Gets the transfer module.
         *
         * @param history
         *            the history
         * @param cookie
         *            the cookie
         * @param location
         *            the location
         * @param connectOptions
         *            the connect options
         * @param closeTimeout
         *            the close timeout
         * @param asynchronous
         *            the asynchronous
         *
         * @return the transfer module
         *
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         * @throws ECtransException
         *             the ectrans exception
         */
        TransferModule getTransferModule(ECtransHistory history, String cookie, String location,
                final Options connectOptions, long closeTimeout, boolean asynchronous)
                throws IOException, ECtransException;

        /**
         * Gets the EC user name.
         *
         * @return the EC user name
         */
        String getECUserName();

        /**
         * Gets the remote provider.
         *
         * @return the remote provider
         */
        RemoteProvider getRemoteProvider();

        /**
         * Gets the ectrans setup.
         *
         * @return the ectrans setup
         */
        ECtransSetup getECtransSetup();
    }

    /**
     * The Class RemoteFinder.
     */
    private final class RemoteFinder implements ModuleFinder {
        /** The provider. */
        private final RemoteProvider provider;

        /** The ecuser. */
        private final ECUser ecuser;

        /** The msuser. */
        private final MSUser msuser;

        /** The destination. */
        private final ECtransDestination destination;

        /** The setup. */
        private final ECtransSetup setup;

        /**
         * Instantiates a new remote finder.
         *
         * @param provider
         *            the provider
         * @param ecuser
         *            the ecuser
         * @param msuser
         *            the msuser
         * @param destination
         *            the destination
         *
         * @throws ECtransException
         *             the ectrans exception
         */
        private RemoteFinder(final RemoteProvider provider, final ECUser ecuser, final MSUser msuser,
                final ECtransDestination destination) throws ECtransException {
            this.destination = destination.getActive() ? destination
                    : provider.getECtransDestination(destination.getName());
            if (this.destination == null) {
                throw new ECtransException("destination " + destination.getName() + " not found");
            }
            this.provider = provider;
            this.ecuser = ecuser;
            this.msuser = msuser;
            setup = new ECtransSetup(this.destination.getECtransModuleName(), provider.decrypt(msuser.getData()));
        }

        /**
         * Gets the transfer module.
         *
         * @param history
         *            the history
         * @param cookie
         *            the cookie
         * @param location
         *            the location
         * @param connectOptions
         *            the connect options
         * @param closeTimeout
         *            the close timeout
         * @param asynchronous
         *            the asynchronous
         *
         * @return the transfer module
         *
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         * @throws ECtransException
         *             the ectrans exception
         */
        @Override
        public TransferModule getTransferModule(final ECtransHistory history, final String cookie,
                final String location, final Options connectOptions, final long closeTimeout,
                final boolean asynchronous) throws IOException, ECtransException {
            final var ectrans = destination.getECtransModule();
            if (history != null) {
                history.setMSUser(msuser);
                history.setMSUserName(msuser.getName());
                history.setECtransDestination(destination);
                history.setECtransDestinationName(destination.getName());
                history.setLocation(location);
                history.setRemote(msuser.getName() + "@" + destination.getName());
                history.setUrl(getInitSequence(provider, msuser, ecuser, destination, location, true));
            }
            TransferModule module = null;
            if (cookie != null && cookieRepository != null && (module = cookieRepository.get(provider, ecuser.getName(),
                    msuser.getName() + "@" + destination.getName(), location, cookie)) != null) {
                _log.debug("TransferModule restored from cookie {}", cookie);
                return module;
            }
            _log.debug("TransferModule to be created (cookie={})", cookie);
            var success = false;
            try {
                final var initial = msuser.toString();
                module = provider.loadTransferModule(ectrans);
                module.setRemoteProvider(provider);
                module.setMSUser(msuser);
                module.setECUser(ecuser);
                module.setECtransModule(ectrans);
                module.setECtransDestination(destination);
                module.setCookie(cookie);
                module.setDebug(setup.getBoolean(HOST_ECTRANS_DEBUG));
                module.setClearPassword(provider.decrypt(msuser.getPasswd()));
                if (isNotEmpty(connectOptions)) {
                    module.setAttribute("connectOptions", connectOptions);
                }
                final var url = getInitSequence(provider, msuser, ecuser, destination, location, false);
                log(module, "connect", true);
                synchronized (module) {
                    module.connect(url, setup);
                }
                log(module, "connect", false);
                msuser.setData(provider.encrypt(setup.getData()));
                if (msuser.getECtransDestinationName() == null) {
                    msuser.setECtransDestinationName(destination.getName());
                    msuser.setECtransDestination(destination);
                    _log.debug("Set {} as default destination for {}", destination.getName(), msuser.getName());
                }
                if (!initial.equals(msuser.toString())) {
                    _log.debug("Update destination {} for {}", destination.getName(), msuser.getName());
                    provider.updateMSUser(msuser);
                }
                if (cookie != null && cookieRepository != null) {
                    cookieRepository.add(provider, ecuser.getName(), msuser.getName() + "@" + destination.getName(),
                            location, cookie, module);
                }
                success = true;
                return module;
            } catch (final ClassNotFoundException e) {
                final var msg = "module " + ectrans.getName() + " not installed";
                if (history != null) {
                    history.setComment(msg);
                }
                throw new ECtransException(msg, e);
            } catch (final Throwable t) {
                if (history != null) {
                    history.setComment(t.getMessage());
                }
                throw new ECtransException("module " + ectrans.getName() + " error", t);
            } finally {
                if (!success) {
                    close(provider, module, history, closeTimeout, asynchronous);
                }
            }
        }

        /**
         * Gets the EC user name.
         *
         * @return the EC user name
         */
        @Override
        public String getECUserName() {
            return ecuser.getName();
        }

        /**
         * Gets the remote provider.
         *
         * @return the remote provider
         */
        @Override
        public RemoteProvider getRemoteProvider() {
            return provider;
        }

        /**
         * Gets the ectrans setup.
         *
         * @return the ectrans setup
         */
        @Override
        public ECtransSetup getECtransSetup() {
            return setup;
        }
    }

    /**
     * The Class LocalFinder.
     */
    private final class LocalFinder implements ModuleFinder {
        /** The ecuser. */
        private final String ecuser;

        /** The remote. */
        private final String remote;

        /** The setup. */
        private final ECtransSetup setup;

        /**
         * Instantiates a new local finder.
         *
         * @param ecuser
         *            the ecuser
         * @param remote
         *            the remote
         *
         * @throws ECtransException
         *             the ectrans exception
         */
        private LocalFinder(final String ecuser, final String remote) throws ECtransException {
            this.ecuser = ecuser;
            this.remote = remote;
            final var url = getTransferURL(defaultProvider, null, ecuser, remote, null, false);
            setup = new ECtransSetup(url.getECtransDestination().getECtransModuleName(),
                    defaultProvider.decrypt(url.getMSUser().getData()));
        }

        /**
         * Gets the transfer module.
         *
         * @param history
         *            the history
         * @param cookie
         *            the cookie
         * @param location
         *            the location
         * @param connectOptions
         *            the connect options
         * @param closeTimeout
         *            the close timeout
         * @param asynchronous
         *            the asynchronous
         *
         * @return the transfer module
         *
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         * @throws ECtransException
         *             the ectrans exception
         */
        @Override
        public TransferModule getTransferModule(final ECtransHistory history, final String cookie,
                final String location, final Options connectOptions, final long closeTimeout,
                final boolean asynchronous) throws IOException, ECtransException {
            if ("deploy".equals(ecuser)) {
                return new StarterUpdate(defaultProvider);
            }
            final var url = getTransferURL(defaultProvider, history, ecuser, remote, location, false);
            final var protocol = url.getECtransDestination().getECtransModuleName();
            final var ectrans = defaultProvider.getECtransModule(protocol);
            if (ectrans == null) {
                throw new ECtransException("module " + protocol + " not found");
            }
            if (!ectrans.getActive()) {
                throw new ECtransException("module " + protocol + " not active");
            }
            return new RemoteFinder(defaultProvider, url.getECUser(), url.getMSUser(), url.getECtransDestination())
                    .getTransferModule(history, cookie, location, connectOptions, closeTimeout, asynchronous);
        }

        /**
         * Gets the EC user name.
         *
         * @return the EC user name
         */
        @Override
        public String getECUserName() {
            return ecuser;
        }

        /**
         * Gets the remote provider.
         *
         * @return the remote provider
         */
        @Override
        public RemoteProvider getRemoteProvider() {
            return defaultProvider;
        }

        /**
         * Gets the ectrans setup.
         *
         * @return the ectrans setup
         */
        @Override
        public ECtransSetup getECtransSetup() {
            return setup;
        }
    }

    /**
     * The Class TransferURL.
     */
    public static final class TransferURL {
        /** The url. */
        private final String url;

        /** The msuser. */
        private final MSUser msuser;

        /** The ecuser. */
        private final ECUser ecuser;

        /** The destination. */
        private final ECtransDestination destination;

        /**
         * Instantiates a new transfer url.
         *
         * @param url
         *            the url
         * @param destination
         *            the destination
         * @param msuser
         *            the msuser
         * @param ecuser
         *            the ecuser
         */
        private TransferURL(final String url, final ECtransDestination destination, final MSUser msuser,
                final ECUser ecuser) {
            this.url = url;
            this.destination = destination;
            this.msuser = msuser;
            this.ecuser = ecuser;
        }

        /**
         * Gets the url.
         *
         * @return the url
         */
        public String getURL() {
            return destination.getName() + ":" + url;
        }

        /**
         * Gets the MS user.
         *
         * @return the MS user
         */
        public MSUser getMSUser() {
            return msuser;
        }

        /**
         * Gets the EC user.
         *
         * @return the EC user
         */
        public ECUser getECUser() {
            return ecuser;
        }

        /**
         * Gets the ectrans destination.
         *
         * @return the ectrans destination
         */
        public ECtransDestination getECtransDestination() {
            return destination;
        }
    }

    /**
     * The Class ECtransThread.
     */
    private final class ECtransThread extends ConfigurableRunnable implements MBeanService {
        /** The start date. */
        private final Date startDate;

        /** The action. */
        private final ECtransAction action;

        /** The cookie. */
        private final String cookie;

        /** The location. */
        private final String location;

        /** The connectOptions. */
        private final Options connectOptions;

        /** The callback. */
        private final ECtransCallback callback;

        /** The history. */
        private final ECtransHistory history;

        /** The finder. */
        private final ModuleFinder finder;

        /** The connect timeout. */
        private long connectTimeout = 0;

        /** The exec timeout. */
        private long execTimeout = 0;

        /** The close timeout. */
        private long closeTimeout = 0;

        /** The retry count. */
        private int retryCount = 0;

        /** The retry frequency. */
        private long retryFrequency = 0;

        /** The asynchronous. */
        private boolean asynchronous = true;

        /** The interruptible. */
        private boolean interruptible = false;

        /**
         * Instantiates a new ectrans thread.
         *
         * @param finder
         *            the finder
         * @param action
         *            the action
         * @param cookie
         *            the cookie
         * @param location
         *            the location
         * @param connectOptions
         *            the connect options
         * @param callback
         *            the callback
         * @param interruptible
         *            the interruptible
         *
         * @throws ECtransException
         *             the ectrans exception
         */
        private ECtransThread(final ModuleFinder finder, final ECtransAction action, final String cookie,
                final String location, final Options connectOptions, final ECtransCallback callback,
                final boolean interruptible) throws ECtransException {
            if (!activated) {
                throw new ECtransException("ECtrans not activated on " + finder.getRemoteProvider().getRoot());
            }
            setThreadNameAndCookie(action.getName(), null, cookie, null);
            this.interruptible = interruptible;
            this.cookie = cookie;
            startDate = new Date(System.currentTimeMillis());
            this.finder = finder;
            this.action = action;
            this.location = location;
            this.connectOptions = connectOptions;
            if (callback == null) {
                _log.debug("Using default callback");
                this.callback = new DefaultCallback(finder.getECtransSetup());
            } else {
                _log.debug("Using callback provided");
                this.callback = callback;
            }
            final var setup = this.callback.getECtransSetup();
            history = action.getECtransHistory();
            connectTimeout = setup.getDuration(HOST_ECTRANS_CONNECT_TIME_OUT).toMillis();
            Optional.ofNullable(getECtransOptions(action.getName())).ifPresent(ectransOption -> {
                final var timeout = setup.getDuration(ectransOption);
                if (timeout != null) {
                    execTimeout = timeout.toMillis();
                }
            });
            closeTimeout = setup.getDuration(HOST_ECTRANS_CLOSE_TIME_OUT).toMillis();
            retryCount = setup.getInteger(HOST_ECTRANS_RETRY_COUNT) + 1;
            retryFrequency = setup.getDuration(HOST_ECTRANS_RETRY_FREQUENCY).toMillis();
            asynchronous = setup.getBoolean(HOST_ECTRANS_CLOSE_ASYNCHRONOUS);
            _log.debug(getLogMessage(action.getName(), cookie, finder.getECUserName(), location));
            if (setup.getBoolean(HOST_ECTRANS_DEBUG)) {
                _log.debug("Association data: {}", setup.getData());
            }
        }

        /**
         * Gets the ectrans options.
         *
         * @param actionName
         *            the action name
         *
         * @return the ectrans options
         */
        private static ECtransOptions getECtransOptions(final String actionName) {
            return switch (actionName) {
            case "del" -> HOST_ECTRANS_DEL_TIME_OUT;
            case "get" -> HOST_ECTRANS_GET_TIME_OUT;
            case "list" -> HOST_ECTRANS_LIST_TIME_OUT;
            case "mkdir" -> HOST_ECTRANS_MKDIR_TIME_OUT;
            case "move" -> HOST_ECTRANS_MOVE_TIME_OUT;
            case "put" -> HOST_ECTRANS_PUT_TIME_OUT;
            case "rmdir" -> HOST_ECTRANS_RMDIR_TIME_OUT;
            case "size" -> HOST_ECTRANS_SIZE_TIME_OUT;
            default -> null;
            };
        }

        /**
         * Sets the attribute.
         *
         * @param name
         *            the name
         * @param value
         *            the value
         *
         * @return true, if successful
         *
         * @throws InvalidAttributeValueException
         *             the invalid attribute value exception
         * @throws MBeanException
         *             the MBean exception
         */
        @Override
        public boolean setAttribute(final String name, final Object value)
                throws InvalidAttributeValueException, MBeanException {
            return false;
        }

        /**
         * Gets the MBean info.
         *
         * @return the MBean info
         */
        @Override
        public MBeanInfo getMBeanInfo() {
            return new MBeanInfo(this.getClass().getName(),
                    "The TransferThread is dedicated to a specific data transfer " + "to a remote server.",
                    new MBeanAttributeInfo[] {
                            new MBeanAttributeInfo("StartDate", "java.util.Date",
                                    "StartDate: when the TransferThread has been started.", true, false, false),
                            new MBeanAttributeInfo("Action", "java.lang.String", "Action: the action to start.", true,
                                    false, false),
                            new MBeanAttributeInfo("Cookie", "java.lang.String",
                                    "Cookie: the cookie used to maintain persistence.", true, false, false),
                            new MBeanAttributeInfo("RetryCount", "java.lang.Integer",
                                    "RetryCount: the number of retry allowed.", true, false, false),
                            new MBeanAttributeInfo("RetryFrequency", "java.lang.Integer",
                                    "RetryFrequency: the delay before to retry.", true, false, false),
                            new MBeanAttributeInfo("ConnectTimeout", "java.lang.Integer",
                                    "ConnectTimeout: the timeout for the connection.", true, false, false),
                            new MBeanAttributeInfo("ExecTimeout", "java.lang.Integer",
                                    "ExecTimeout: the timeout for the action.", true, false, false),
                            new MBeanAttributeInfo("CloseTimeout", "java.lang.Integer",
                                    "CloseTimeout: the timeout for the close.", true, false, false),
                            new MBeanAttributeInfo("CloseAsynchronous", "java.lang.Boolean",
                                    "CloseAsynchronous: the close is performed asynchronously.", true, false, false),
                            new MBeanAttributeInfo("ECuser", "java.lang.String",
                                    "ECuser: the user associated to the transfer.", true, false, false),
                            new MBeanAttributeInfo("Location", "java.lang.String",
                                    "Location: the location associated to the transfer.", true, false, false) },
                    new MBeanConstructorInfo[0], new MBeanOperationInfo[0], new MBeanNotificationInfo[0]);
        }

        /**
         * Invoke.
         *
         * @param operationName
         *            the operation name
         * @param params
         *            the params
         * @param signature
         *            the signature
         *
         * @return the object
         *
         * @throws NoSuchMethodException
         *             the no such method exception
         * @throws MBeanException
         *             the MBean exception
         */
        @Override
        public Object invoke(final String operationName, final Object[] params, final String[] signature)
                throws NoSuchMethodException, MBeanException {
            throw new NoSuchMethodException(operationName);
        }

        /**
         * Gets the attribute.
         *
         * @param attributeName
         *            the attribute name
         *
         * @return the attribute
         *
         * @throws AttributeNotFoundException
         *             the attribute not found exception
         * @throws MBeanException
         *             the MBean exception
         */
        @Override
        public Object getAttribute(final String attributeName) throws AttributeNotFoundException, MBeanException {
            try {
                if ("StartDate".equals(attributeName)) {
                    return startDate;
                }
                if ("Action".equals(attributeName)) {
                    return action.getName();
                }
                if ("Cookie".equals(attributeName)) {
                    return cookie;
                }
                if ("RetryCount".equals(attributeName)) {
                    return retryCount;
                }
                if ("RetryFrequency".equals(attributeName)) {
                    return retryFrequency;
                }
                if ("ConnectTimeout".equals(attributeName)) {
                    return connectTimeout;
                }
                if ("CloseAsynchronous".equals(attributeName)) {
                    return asynchronous;
                }
                if ("ExecTimeout".equals(attributeName)) {
                    return execTimeout;
                }
                if ("CloseTimeout".equals(attributeName)) {
                    return closeTimeout;
                }
                if ("ECuser".equals(attributeName)) {
                    return finder.getECUserName();
                }
                if ("Location".equals(attributeName)) {
                    return location;
                }
            } catch (final Exception e) {
                _log.warn("Getting an MBean attribute", e);
                throw new MBeanException(e);
            }
            throw new AttributeNotFoundException(
                    "Cannot find " + attributeName + " attribute in " + this.getClass().getName());
        }

        /**
         * Gets the transfer module.
         *
         * @return the transfer module
         *
         * @throws Throwable
         *             the throwable
         */
        private TransferModule getTransferModule() throws Throwable {
            GetTransferModuleThread get = null;
            TransferModule module = null;
            var duration = 0L;
            for (var i = 1; i <= retryCount; i++) {
                try {
                    get = new GetTransferModuleThread(finder, history, cookie, location, connectOptions, closeTimeout,
                            asynchronous);
                    duration = get.exec(connectTimeout, false);
                    get.completed();
                    if ((module = get.getTransferModule()) == null) {
                        _log.debug("Connection timeout (TransferModule not connected)");
                        throw new ECtransException("Connection timeout");
                    }
                    _log.debug("TransferModule connected");
                    break;
                } catch (final Throwable t) {
                    connectTimeout -= duration;
                    if (connectTimeout <= 0 || i >= retryCount) {
                        _log.error("TransferModule not connected ({})",
                                connectTimeout <= 0 ? "connection timeout" : "max retry count", t);
                        throw get != null && get.timeOutExpired() ? new ECtransException("Connection timeout") : t;
                    }
                    callback.retry(t.getMessage());
                    duration = 0;
                }
                if (retryFrequency > 0) {
                    try {
                        Thread.sleep(retryFrequency);
                    } catch (final InterruptedException e) {
                        // Ignore
                    }
                }
            }
            return module;
        }

        /**
         * Sets the error.
         *
         * @param comment
         *            the comment
         *
         * @return the string
         */
        private String setAndGetError(final String comment) {
            if (history != null && !history.getError()) {
                history.setComment(comment);
                history.setError(true);
            }
            return comment;
        }

        /**
         * _exec.
         *
         * @throws ECtransException
         *             the ectrans exception
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        private void exec() throws ECtransException, IOException {
            TransferModule module = null;
            var completed = false;
            String comment = null;
            try {
                module = getTransferModule();
                action.init(finder.getRemoteProvider(), ECtransContainer.this, callback, cookie);
                callback.setMSUser(module.getMSUser());
                log(module, action.getName(), true);
                final var exec = new ExecTransferModuleThread(module, action, interruptible);
                exec.exec(execTimeout, false);
                exec.completed();
                log(module, action.getName(), false);
                completed = true;
            } catch (final TimeoutException | InterruptedException e) {
                comment = setAndGetError("timeout occured on " + action.getName() + " request");
                _log.warn("Processing {} request: {}", action.getName(), comment);
                throw new ECtransException("ECtrans " + action.getName() + " timeout", e);
            } catch (final ECtransException | IOException e) {
                comment = setAndGetError(Format.getMessage(e));
                _log.warn("Processing {} request: {}", action.getName(), comment, e);
                throw e;
            } catch (final Throwable t) {
                comment = setAndGetError(Format.getMessage(t));
                _log.warn("Processing {} request: {}", action.getName(), comment, t);
                throw new ECtransException("ECtrans " + action.getName() + " error", t);
            } finally {
                if (module != null) {
                    module.setClosedOnError(!completed);
                }
                action.close(module, closeTimeout, asynchronous);
                if (completed && (history == null || !history.getError())) {
                    callback.completed(module);
                } else {
                    callback.failed(module, history != null && history.getError() && isNotEmpty(history.getComment())
                            ? history.getComment() : comment);
                }
            }
        }

        /**
         * Configurable run.
         */
        @Override
        public void configurableRun() {
            MBeanManager manager = null;
            try {
                manager = new MBeanManager("Transfer:transfer=" + callback.getUniqueName(), this);
            } catch (final Throwable t) {
                _log.warn("Can not register MBeanManager", t);
            }
            try {
                exec();
            } catch (final ECtransException | IOException e) {
                // Ignore
            } catch (final Throwable t) {
                _log.warn(t);
            } finally {
                if (manager != null) {
                    manager.unregister();
                }
            }
        }
    }

    /**
     * The Class GetTransferModuleThread.
     */
    private static final class GetTransferModuleThread extends WaitingThread {
        /** The finder. */
        private final ModuleFinder finder;

        /** The history. */
        private final ECtransHistory history;

        /** The location. */
        private final String location;

        /** The location. */
        private final Options connectOptions;

        /** The cookie. */
        private final String cookie;

        /** The close timeout. */
        private final long closeTimeout;

        /** The asynchronous. */
        private final boolean asynchronous;

        /** The module. */
        private TransferModule module = null;

        /**
         * Instantiates a new gets the transfer module thread.
         *
         * @param finder
         *            the finder
         * @param history
         *            the history
         * @param cookie
         *            the cookie
         * @param location
         *            the location
         * @param connectOptions
         *            the connect options
         * @param closeTimeout
         *            the close timeout
         * @param asynchronous
         *            the asynchronous
         */
        private GetTransferModuleThread(final ModuleFinder finder, final ECtransHistory history, final String cookie,
                final String location, final Options connectOptions, final long closeTimeout,
                final boolean asynchronous) {
            setThreadNameAndCookie(null, null, cookie, null);
            this.finder = finder;
            this.history = history;
            this.cookie = cookie;
            this.location = location;
            this.connectOptions = connectOptions;
            this.closeTimeout = closeTimeout;
            this.asynchronous = asynchronous;
        }

        /**
         * Action.
         *
         * @throws Exception
         *             the exception
         */
        @Override
        public void action() throws Exception {
            module = finder.getTransferModule(history, cookie, location, connectOptions, closeTimeout, asynchronous);
        }

        /**
         * Gets the transfer module.
         *
         * @return the transfer module
         */
        public TransferModule getTransferModule() {
            return module;
        }
    }

    /**
     * The Class CloseTransferModuleThread.
     */
    private static final class CloseTransferModuleThread extends WaitingThread {
        /** The provider. */
        private final RemoteProvider provider;

        /** The module. */
        private final TransferModule module;

        /**
         * Instantiates a new close transfer module thread.
         *
         * @param provider
         *            the provider
         * @param module
         *            the module
         */
        private CloseTransferModuleThread(final RemoteProvider provider, final TransferModule module) {
            setThreadNameAndCookie(null, null, module.getCookie(), null);
            this.provider = provider;
            this.module = module;
        }

        /**
         * Action.
         *
         * @throws Exception
         *             the exception
         */
        @Override
        public void action() throws Exception {
            log(module, "close", true);
            try {
                module.close();
            } finally {
                provider.unloadTransferModule(module);
                log(module, "close", false);
            }
        }
    }

    /**
     * The Class ExecTransferModuleThread.
     */
    private final class ExecTransferModuleThread extends WaitingThread {
        /** The module. */
        private final TransferModule module;

        /** The action. */
        private final ECtransAction action;

        /** The action. */
        private final boolean interruptible;

        /**
         * Instantiates a new exec transfer module thread.
         *
         * @param module
         *            the module
         * @param action
         *            the action
         * @param interruptible
         *            the interruptible
         */
        private ExecTransferModuleThread(final TransferModule module, final ECtransAction action,
                final boolean interruptible) {
            setThreadNameAndCookie(null, null, module.getCookie(), null);
            this.module = module;
            this.action = action;
            this.interruptible = interruptible;
        }

        /**
         * Action.
         *
         * @throws Exception
         *             the exception
         */
        @Override
        public void action() throws Exception {
            progressUpdate(1);
            try {
                action.exec(module, interruptible);
            } finally {
                progressUpdate(-1);
            }
        }

        /**
         * _progress update.
         *
         * @param count
         *            the count
         */
        private synchronized void progressUpdate(final int count) {
            inProgress += count;
        }
    }

    /**
     * The Class CookieRepository.
     */
    private static final class CookieRepository extends MBeanRepository<CookieElement> {
        /**
         * Instantiates a new cookie repository.
         *
         * @param name
         *            the name
         */
        private CookieRepository(final String name) {
            super(name);
        }

        /**
         * Gets the.
         *
         * @param provider
         *            the provider
         * @param ecuser
         *            the ecuser
         * @param remote
         *            the remote
         * @param location
         *            the location
         * @param cookie
         *            the cookie
         *
         * @return the transfer module
         */
        private TransferModule get(final RemoteProvider provider, final String ecuser, final String remote,
                final String location, final String cookie) {
            final var element = getValue(getKey(new CookieElement(provider, ecuser, remote, location, cookie)));
            return element != null ? element.getTransferModule() : null;
        }

        /**
         * Adds the.
         *
         * @param provider
         *            the provider
         * @param ecuser
         *            the ecuser
         * @param remote
         *            the remote
         * @param location
         *            the location
         * @param cookie
         *            the cookie
         * @param module
         *            the module
         */
        private void add(final RemoteProvider provider, final String ecuser, final String remote, final String location,
                final String cookie, final TransferModule module) {
            put(new CookieElement(provider, ecuser, remote, location, cookie, module));
        }

        /**
         * Removes the.
         *
         * @param provider
         *            the provider
         * @param ecuser
         *            the ecuser
         * @param remote
         *            the remote
         * @param location
         *            the location
         * @param cookie
         *            the cookie
         *
         * @return the transfer module
         */
        private TransferModule remove(final RemoteProvider provider, final String ecuser, final String remote,
                final String location, final String cookie) {
            final var element = removeKey(getKey(new CookieElement(provider, ecuser, remote, location, cookie)));
            return element != null ? element.getTransferModule() : null;
        }

        /**
         * Gets the key.
         *
         * @param element
         *            the element
         *
         * @return the key
         */
        @Override
        public String getKey(final CookieElement element) {
            final var location = element.getLocation();
            return "[" + element.getECtransProvider().getRoot() + "][" + element.getECUser() + "]["
                    + element.getRemote() + "][" + (location == null ? "" : location) + "][" + element.getCookie()
                    + "]";
        }

        /**
         * Gets the status.
         *
         * @param element
         *            the element
         *
         * @return the status
         */
        @Override
        public String getStatus(final CookieElement element) {
            return element.getTransferModule().getStatus();
        }

        /**
         * Next step.
         *
         * @return the int
         */
        @Override
        public int nextStep() {
            return NEXT_STEP_DELAY;
        }
    }

    /**
     * The Class CookieElement.
     */
    private static final class CookieElement {
        /** The provider. */
        private final RemoteProvider provider;

        /** The ecuser. */
        private final String ecuser;

        /** The remote. */
        private final String remote;

        /** The location. */
        private final String location;

        /** The cookie. */
        private final String cookie;

        /** The module. */
        private final TransferModule module;

        /**
         * Instantiates a new cookie element.
         *
         * @param provider
         *            the provider
         * @param ecuser
         *            the ecuser
         * @param remote
         *            the remote
         * @param location
         *            the location
         * @param cookie
         *            the cookie
         * @param module
         *            the module
         */
        private CookieElement(final RemoteProvider provider, final String ecuser, final String remote,
                final String location, final String cookie, final TransferModule module) {
            this.provider = provider;
            this.ecuser = ecuser;
            this.remote = remote;
            this.location = location;
            this.cookie = cookie;
            this.module = module;
        }

        /**
         * Instantiates a new cookie element.
         *
         * @param provider
         *            the provider
         * @param ecuser
         *            the ecuser
         * @param remote
         *            the remote
         * @param location
         *            the location
         * @param cookie
         *            the cookie
         */
        private CookieElement(final RemoteProvider provider, final String ecuser, final String remote,
                final String location, final String cookie) {
            this(provider, ecuser, remote, location, cookie, null);
        }

        /**
         * Gets the ectrans provider.
         *
         * @return the ectrans provider
         */
        private RemoteProvider getECtransProvider() {
            return provider;
        }

        /**
         * Gets the transfer module.
         *
         * @return the transfer module
         */
        private TransferModule getTransferModule() {
            return module;
        }

        /**
         * Gets the EC user.
         *
         * @return the EC user
         */
        private String getECUser() {
            return ecuser;
        }

        /**
         * Gets the remote.
         *
         * @return the remote
         */
        private String getRemote() {
            return remote;
        }

        /**
         * Gets the location.
         *
         * @return the location
         */
        private String getLocation() {
            return location;
        }

        /**
         * Gets the cookie.
         *
         * @return the cookie
         */
        private String getCookie() {
            return cookie;
        }
    }
}
