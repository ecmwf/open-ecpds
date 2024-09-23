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

import static ecmwf.common.ectrans.ECtransGroups.Module.USER_PORTAL;
import static ecmwf.common.ectrans.ECtransOptions.USER_PORTAL_DELETE_PATH_PERM_REGEX;
import static ecmwf.common.ectrans.ECtransOptions.USER_PORTAL_DIR_PATH_PERM_REGEX;
import static ecmwf.common.ectrans.ECtransOptions.USER_PORTAL_GET_PATH_PERM_REGEX;
import static ecmwf.common.ectrans.ECtransOptions.USER_PORTAL_MKDIR_PATH_PERM_REGEX;
import static ecmwf.common.ectrans.ECtransOptions.USER_PORTAL_MTIME_PATH_PERM_REGEX;
import static ecmwf.common.ectrans.ECtransOptions.USER_PORTAL_PUT_PATH_PERM_REGEX;
import static ecmwf.common.ectrans.ECtransOptions.USER_PORTAL_RENAME_PATH_PERM_REGEX;
import static ecmwf.common.ectrans.ECtransOptions.USER_PORTAL_RMDIR_PATH_PERM_REGEX;
import static ecmwf.common.ectrans.ECtransOptions.USER_PORTAL_SIZE_PATH_PERM_REGEX;

import java.io.Serializable;
import java.util.List;

import ecmwf.common.database.Destination;
import ecmwf.common.database.IncomingPermission;
import ecmwf.common.database.IncomingUser;
import ecmwf.common.ecaccess.EccmdException;
import ecmwf.common.ectrans.ECtransOptions;
import ecmwf.common.ectrans.ECtransSetup;

/**
 * The Class IncomingProfile.
 */
public final class IncomingProfile implements Serializable {
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = -7364686675763821621L;

    /** The user. */
    private final IncomingUser user;

    /** The setup. */
    private final ECtransSetup setup;

    /** The permissions. */
    private final List<IncomingPermission> permissions;

    /** The destinations. */
    private final List<Destination> destinations;

    /**
     * Instantiates a new incoming profile.
     *
     * @param user
     *            the user
     * @param permissions
     *            the permissions
     * @param destinations
     *            the destinations
     */
    IncomingProfile(final IncomingUser user, final List<IncomingPermission> permissions,
            final List<Destination> destinations) {
        this.user = user;
        this.setup = USER_PORTAL.getECtransSetup(user.getData());
        this.permissions = permissions;
        this.destinations = destinations;
    }

    /**
     * Gets the incoming user.
     *
     * @return the incoming user
     */
    public IncomingUser getIncomingUser() {
        return user;
    }

    /**
     * Gets the incoming user setup.
     *
     * @return the incoming user
     */
    public ECtransSetup getECtransSetup() {
        return setup;
    }

    /**
     * Gets the permissions.
     *
     * @return the permissions
     */
    public List<IncomingPermission> getPermissions() {
        return permissions;
    }

    /**
     * Gets the destinations.
     *
     * @return the destinations
     */
    public List<Destination> getDestinations() {
        return destinations;
    }

    /**
     * Check permission.
     *
     * @param permission
     *            the filename
     * @param filename
     *            the filename
     *
     * @throws ecmwf.common.ecaccess.EccmdException
     *             the eccmd exception
     */
    public void checkPermission(final String permission, final String filename) throws EccmdException {
        for (final IncomingPermission p : permissions) {
            final var operationName = p.getOperationName();
            if (operationName != null && operationName.equals(permission)) {
                final var option = getECtransOptions(operationName);
                if (option == null || setup.matches(option, filename, ".*")) {
                    return;
                }
                break;
            }
        }
        throw new EccmdException("Permission denied for " + permission);
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
        case "dir" -> USER_PORTAL_DIR_PATH_PERM_REGEX;
        case "get" -> USER_PORTAL_GET_PATH_PERM_REGEX;
        case "mtime" -> USER_PORTAL_MTIME_PATH_PERM_REGEX;
        case "put" -> USER_PORTAL_PUT_PATH_PERM_REGEX;
        case "rename" -> USER_PORTAL_RENAME_PATH_PERM_REGEX;
        case "size" -> USER_PORTAL_SIZE_PATH_PERM_REGEX;
        case "delete" -> USER_PORTAL_DELETE_PATH_PERM_REGEX;
        case "mkdir" -> USER_PORTAL_MKDIR_PATH_PERM_REGEX;
        case "rmdir" -> USER_PORTAL_RMDIR_PATH_PERM_REGEX;
        default -> null;
        };
    }
}
