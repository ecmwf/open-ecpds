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

/**
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import ecmwf.common.database.DataBaseObject;

/**
 * The Class ECpdsAction.
 */
final class ECpdsAction {
    /** The _action. */
    private final String _action;

    /** The _session. */
    private final ECpdsSession _session;

    /** The _argument. */
    private final DataBaseObject _argument;

    /**
     * Instantiates a new ecpds action.
     *
     * @param session
     *            the session
     * @param action
     *            the action
     * @param argument
     *            the argument
     */
    ECpdsAction(final ECpdsSession session, final String action, final DataBaseObject argument) {
        _action = action;
        _session = session;
        _argument = argument;
    }

    /**
     * Gets the ecpds session.
     *
     * @return the ecpds session
     */
    ECpdsSession getECpdsSession() {
        return _session;
    }

    /**
     * Gets the action.
     *
     * @return the action
     */
    String getAction() {
        return _action;
    }

    /**
     * Gets the argument.
     *
     * @return the argument
     */
    DataBaseObject getArgument() {
        return _argument;
    }

    /**
     * Gets the web user id.
     *
     * @return the web user id
     */
    String getWebUserId() {
        return _session.getWebUser().getId();
    }
}
