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

package ecmwf.ecpds.master;

import static ecmwf.common.text.Util.isNotEmpty;
import java.sql.Timestamp;
import javax.management.AttributeNotFoundException;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.timer.Timer;
import ecmwf.common.database.DataBaseException;
import ecmwf.common.database.Host;
import ecmwf.common.database.TransferServer;
import ecmwf.common.ecaccess.MBeanScheduler;
import ecmwf.common.mbean.MBeanManager;
import ecmwf.common.technical.Cnf;
import ecmwf.common.text.Format;
import ecmwf.ecpds.master.transfer.TransferServerProvider;
import ecmwf.ecpds.master.transfer.TransferServerProvider.TransferServerException;

/**
 * The Class HostCheckScheduler.
 */
final class HostCheckScheduler extends MBeanScheduler {
    private final MasterServer _master;

    /** The _check file name. */
    private String _checkFileName = null;

    /** The _test phrase. */
    private byte[] _testPhrase = null;

    /** The _length. */
    private long _length = 0;

    /**
     * Instantiates a new host check scheduler.
     *
     * @param name
     *            the name
     */
    HostCheckScheduler(final MasterServer _master, final String name) {
        // Bind the Master before superclass construction and field initialization.
        this._master = _master;
        super(name);
        setDelay(Cnf.durationAt("Scheduler", "hostCheckScheduler", 5 * Timer.ONE_MINUTE));
        setJammedTimeout(Cnf.durationAt("Scheduler", "hostCheckSchedulerJammedTimeout", 30 * Timer.ONE_MINUTE));
        setTimeRanges(Cnf.listOfTimeRangesAt("Scheduler", "hostCheckSchedulerTimeRanges"));
        _checkFileName = Cnf.at("Server", "checkFileName", "ecpds.tst");
        _setTestPhrase(Cnf.at("Server", "testPhrase", "Test transfer file"));
    }

    /**
     * Sets the test phrase.
     *
     * @param testPhrase
     *            the test phrase
     */
    private void _setTestPhrase(final String testPhrase) {
        _testPhrase = testPhrase.getBytes();
        _length = testPhrase.length();
    }

    /**
     * Next step.
     *
     * @return the int
     */
    @Override
    public int nextStep() {
        final var base = _master.getECpdsBase();
        for (final Host host : base.getHostsToCheck()) {
            try {
                check(host, false, false);
            } catch (final MasterException | DataBaseException | TransferServerException e) {
                MasterServer._log.warn(e);
            }
            waitFor(5 * Timer.ONE_SECOND);
        }
        return NEXT_STEP_DELAY;
    }

    /**
     * Check.
     *
     * @param host
     *            the host
     * @param force
     *            the force
     * @param notify
     *            the notify
     *
     * @return true, if successful
     *
     * @throws DataBaseException
     *             the data base exception
     * @throws MasterException
     *             the master exception
     * @throws TransferServerException
     */
    public boolean check(final Host host, final boolean force, final boolean notify)
            throws DataBaseException, MasterException, TransferServerException {
        final var currentTime = System.currentTimeMillis();
        final var hostStats = host.getHostStats();
        TransferServer server = null;
        final var provider = new TransferServerProvider("check", host.getTransferGroupName());
        for (final TransferServer theServer : provider.getTransferServersByLeastActivity()) {
            if (theServer.getCheck()) {
                server = theServer;
                break;
            }
        }
        if (server == null) {
            throw new MasterException(
                    "No TransferServer(s) available for checking in TransferGroup " + provider.getTransferGroupName());
        }
        final var valid = hostStats.getValid();
        Timestamp checkTime = null;
        var sent = 0L;
        if (!force && (checkTime = hostStats.getCheckTime()) != null
                && currentTime - checkTime.getTime() < host.getCheckFrequency()) {
            return true;
        }
        MasterServer._log.debug("Check host " + host.getName());
        var checkFileName = host.getCheckFilename();
        checkFileName = isNotEmpty(checkFileName) ? checkFileName : _checkFileName;
        String message = null;
        try {
            sent = _master.transfer(_testPhrase, server, host, checkFileName, 0, _testPhrase.length);
            hostStats.setValid(true);
        } catch (final MasterException e) {
            message = e.getMessage();
            if (message != null && ("Transfer server " + server.getName() + " not subscribed").equals(message)) {
                throw e;
            }
            MasterServer._log.warn("Host " + host.getName() + " warning", e);
            hostStats.setValid(false);
        } catch (final Exception e) {
            message = e.getMessage();
            MasterServer._log.warn("Host " + host.getName() + " warning", e);
            hostStats.setValid(false);
        }
        MasterServer._log.debug("Host " + host.getName() + " " + (hostStats.getValid() ? "validated" : "not validated")
                + " (" + checkFileName + ")");
        _master._updateHostStats(host, 0, 0, 0, hostStats.getValid(), new Timestamp(currentTime));
        if (notify || !hostStats.getValid() && host.getMailOnError() && (valid || !host.getNotifyOnce())
                || hostStats.getValid() && host.getMailOnSuccess() && (!valid || !host.getNotifyOnce())) {
            final var mail = host.getUserMail();
            var data = host.getData();
            if (data != null && data.endsWith("\n")) {
                data = data.substring(0, data.length() - 1);
            }
            if (isNotEmpty(mail))
                _master.sendECpdsMessage(mail,
                        "Host check " + " for " + host.getHost() + " ("
                                + (hostStats.getValid() ? "SUCCESSFUL" : "NOT-SUCCESSFUL") + ")",
                        "Host check " + (hostStats.getValid() ? "successful" : "failure") + ": " + host.getName()
                                + "\nHost name: " + host.getHost() + "\nHost comment: " + host.getComment()
                                + "\nTransfer server: " + server.getName() + "\nTransfer group: "
                                + provider.getTransferGroupName() + "\nTransfer method: " + host.getTransferMethodName()
                                + "\nLogin name: " + host.getLogin()
                                + (isNotEmpty(host.getDir()) ? "\nDirectory: " + host.getDir() : "") + "\nFile name: "
                                + checkFileName + "\nByte(s) sent: " + sent + "/" + _length
                                + (!hostStats.getValid() && isNotEmpty(message) ? "\nError message: " + message : "")
                                + "\nCheck time: " + Format.formatTime(currentTime)
                                + (isNotEmpty(data) ? "\n\nData:\n===============================\n" + data
                                        + "\n===============================\n" : ""));
        }
        return hostStats.getValid();
    }

    /**
     * Gets the MBean info.
     *
     * @return the MBean info
     */
    @Override
    public MBeanInfo getMBeanInfo() {
        return MBeanManager.addMBeanInfo(super.getMBeanInfo(),
                "This MBean provides operations to monitor and manage " + "the HostCheckScheduler",
                new MBeanAttributeInfo[] { new MBeanAttributeInfo("TestPhrase", "java.lang.String",
                        "TestPhrase: text content uploaded to a Host during the check operation to verify connectivity and write access.",
                        true, true, false),
                        new MBeanAttributeInfo("DefaultCheckFileName", "java.lang.String",
                                "DefaultCheckFileName: filename used to upload TestPhrase during a Host check when the Host has no CheckFilename override.",
                                true, true, false) },
                new MBeanOperationInfo[] { new MBeanOperationInfo("check",
                        "check(hostname): check if a host is available",
                        new MBeanParameterInfo[] {
                                new MBeanParameterInfo("hostname", "java.lang.String", "Name of the host to check"),
                                new MBeanParameterInfo("force", "java.lang.Boolean",
                                        "Check even if within its validity time period."),
                                new MBeanParameterInfo("notify", "java.lang.Boolean", "Activate mail notification") },
                        "boolean", MBeanOperationInfo.ACTION) });
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
            if ("TestPhrase".equals(attributeName)) {
                return new String(_testPhrase);
            }
            if ("DefaultCheckFileName".equals(attributeName)) {
                return _checkFileName;
            }
        } catch (final Exception e) {
            MasterServer._log.warn("Getting an MBean attribute", e);
            throw new MBeanException(e);
        }
        return super.getAttribute(attributeName);
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
        if ("TestPhrase".equals(name)) {
            _setTestPhrase(String.valueOf(value));
            return true;
        }
        if ("DefaultCheckFileName".equals(name)) {
            _checkFileName = String.valueOf(value);
            return true;
        }
        return super.setAttribute(name, value);
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
        try {
            if ("check".equals(operationName) && signature.length == 3 && "java.lang.String".equals(signature[0])
                    && "java.lang.Boolean".equals(signature[1]) && "java.lang.Boolean".equals(signature[2])) {
                final var base = _master.getECpdsBase();
                return check(base.getHost((String) params[0]), ((Boolean) params[1]), ((Boolean) params[2]));
            }
        } catch (final Exception e) {
            MasterServer._log.warn("Invoking the {} MBean method", operationName, e);
            throw new MBeanException(e);
        }
        return super.invoke(operationName, params, signature);
    }
}
