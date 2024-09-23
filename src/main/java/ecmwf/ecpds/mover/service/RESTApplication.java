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

package ecmwf.ecpds.mover.service;

/**
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Application;

import ecmwf.common.ecaccess.StarterServer;
import ecmwf.ecpds.mover.MoverServer;

/**
 * The Class RESTApplication.
 */
public final class RESTApplication extends Application {
    /** The Constant _mover. */
    private static final MoverServer _mover = StarterServer.getInstance(MoverServer.class);

    static {
        // Register the RESTProvider!
        _mover.setRESTProvider(new RESTManager());
    }

    /**
     * {@inheritDoc}
     *
     * Gets the classes.
     */
    @Override
    public Set<Class<?>> getClasses() {
        final Set<Class<?>> s = new HashSet<>();
        s.add(RESTServer.class);
        return s;
    }

    /**
     * {@inheritDoc}
     *
     * Gets the singletons.
     */
    @Override
    public Set<Object> getSingletons() {
        final Set<Object> s = new HashSet<>();
        s.add(RESTProvider.getJacksonProvider());
        return s;
    }
}
