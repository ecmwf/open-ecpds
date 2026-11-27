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

package ecmwf.ecpds.master.plugin.http.home.users.org;

/**
 * <p>This pretends to be a complete implementation of the "OrgPerson"
 * in the "ModelBean" style.
 *
 * <p>Created: Mon Sep 23 12:01:17 2002</p>
 *
 * @author <a href="mailto:dvarela@ecmwf.int">Daniel Varela Santoalla</a>
 * @version 0.1.0
 *
 */

import java.util.ArrayList;
import java.util.Collection;

import ecmwf.ecpds.master.plugin.http.home.ModelHomeBase;
import ecmwf.ecpds.master.plugin.http.model.ModelSearch;
import ecmwf.ecpds.master.plugin.http.model.users.org.OrgPerson;
import ecmwf.ecpds.master.plugin.http.model.users.org.OrgPersonException;
import ecmwf.ecpds.master.plugin.http.services.persistence.DAOException;
import ecmwf.ecpds.master.plugin.http.services.persistence.DAOService;

public class OrgPersonHome extends ModelHomeBase {

    private static final String INTERFACE = OrgPerson.class.getName();

    public static OrgPerson create() throws OrgPersonException {
        try {
            return (OrgPerson) DAOService.create(INTERFACE);
        } catch (DAOException e) {
            throw new OrgPersonException("Error retrieving object set: ", e);
        }
    }

    public static OrgPerson findByPrimaryKey(String key) throws OrgPersonException {
        try {
            // Get the person Bean USING the cache.
            return (OrgPerson) DAOService.findByPrimaryKey(INTERFACE, key, true);
        } catch (DAOException e) {
            throw new OrgPersonException("Error retrieving object set: ", e);
        }
    }

    public static OrgPerson findByUid(String uid) throws OrgPersonException {
        Collection c = find(getDefaultSearch("uid=" + uid));
        if (c.size() > 1 || c.size() == 0)
            throw new OrgPersonException("Expected one result for uid=" + uid + ". Got " + c.size());
        else
            return (OrgPerson) c.iterator().next();
    }

    public static Collection find(ModelSearch search) throws OrgPersonException {
        try {
            return DAOService.find(INTERFACE, search);
        } catch (DAOException e) {
            throw new OrgPersonException("Error retrieving object set: ", e);
        }
    }

    public static Collection findAll() throws OrgPersonException {
        ModelSearch search = getDefaultSearch("objectClass=pilotPerson");
        search.setSizeLimit(Integer.MAX_VALUE);
        search.setCacheable(false);
        return find(search);
    }

    public static ModelSearch getDefaultSearch() {
        ModelSearch search = ModelHomeBase.getDefaultSearch();
        search.setStoreSearchResults(true);
        return search;
    }

    /**
     * @return A ModelSearch that will define different base DNs and whose search results will be aggregated.
     */

    public static ModelSearch getMultiHomedSearch(Collection baseDns) {
        ModelSearch search = new MultiHomedSearch(baseDns);
        search.setStoreSearchResults(true);
        return search;
    }

    public static ModelSearch getDefaultSearch(String query) {
        ModelSearch search = ModelHomeBase.getDefaultSearch(query);
        search.setStoreSearchResults(true);
        return search;
    }

} // OrgPersonHome
