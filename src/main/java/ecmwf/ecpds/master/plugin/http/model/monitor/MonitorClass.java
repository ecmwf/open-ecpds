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

/*
 * Created on 18-Jul-2005
 *
 */
package ecmwf.ecpds.master.plugin.http.model.monitor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

/**
 * @author sy8
 *
 *         TODO: Comment ME !!!
 */
public interface MonitorClass {

    public final static int ERROR = 2;
    public final static int WARNING = 1;
    public final static int OK = 0;

    public static final int FAILING_MINUTES_SCOPE_FOR_WARNING = 15;
    public static final int FAILING_MINUTES_FOR_ERROR = 5;

    public String getName();

    public void setName(String s);

    public String getMonitoredSystemId();

    public void setMonitoredSystemId(String id);

    /**
     * From a general list of parameters, the Monitor class will take those that it needs for the tests it performs. If
     * a mandatory param doesn't exist, it will raise an exception.
     */
    public void configure(Map m) throws MonitorException;

    public Collection getConfigParamNames();

    public Map getConfigParameters();

    /**
     * @return Polling period in seconds
     */
    public int getPollingPeriod();

    /**
     * If the monitor is a Daemon, it should be treated slightly differently by the MonitorTask.... No polling, for a
     * starter
     */
    public boolean isDaemon();

    public Collection getMonitoringParameters(Date from, Date to) throws MonitorException;

    /**
     * Strategies to deduct the current Monitor status from the LAST_TESTS_SIZE last tests.
     *
     * @param tests
     *            The list will all last samples
     * @param currentTest
     *            Last test, that will be updated with some calculated values
     *
     * @return An Status code
     */

    public int calculateStatus(ArrayList tests, TestResults currentTest);

    /**
     * Perform all necessary tests, polls and probes and return results
     */
    public TestResults test();

}
