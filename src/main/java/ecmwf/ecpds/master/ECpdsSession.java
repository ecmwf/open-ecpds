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
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.io.Serializable;

import ecmwf.common.database.Activity;
import ecmwf.common.database.WebUser;

/**
 * The Class ECpdsSession.
 */
public final class ECpdsSession implements Serializable {
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 3927534092420201995L;

    /** The _webuser. */
    private final WebUser _webuser;

    /** The _session. */
    private final String _root;

    /** The _activity. */
    private final Activity _activity;

    /**
     * Instantiates a new ecpds session.
     *
     * @param webuser
     *            the webuser
     * @param root
     *            the root
     * @param activity
     *            the activity
     */
    ECpdsSession(final WebUser webuser, final String root, final Activity activity) {
        _webuser = webuser;
        _root = root;
        _activity = activity;
    }

    /**
     * Gets the web user.
     *
     * @return the web user
     */
    public WebUser getWebUser() {
        return _webuser;
    }

    /**
     * Gets the root.
     *
     * @return the root
     */
    String getRoot() {
        return _root;
    }

    /**
     * Gets the activity.
     *
     * @return the activity
     */
    Activity getActivity() {
        return _activity;
    }
}
