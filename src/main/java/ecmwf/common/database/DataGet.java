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

package ecmwf.common.database;

/**
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * The Class DataGet.
 */
public abstract class DataGet {
    /**
     * Gets the.
     *
     * @param <T>
     *            the generic type
     * @param object
     *            the object
     *
     * @return the t
     *
     * @throws DataBaseException
     *             the data base exception
     */
    abstract <T extends DataBaseObject> T get(T object) throws DataBaseException;

    /**
     * Gets the all.
     *
     * @param <T>
     *            the generic type
     * @param theClass
     *            the the class
     *
     * @return the all
     */
    abstract <T extends DataBaseObject> DBIterator<T> getAll(Class<T> theClass);

    /**
     * Log sql request.
     *
     * @param name
     *            the name
     * @param count
     *            the count
     */
    abstract void logSqlRequest(String name, long count);

    /**
     * Gets the incoming user.
     *
     * @param id
     *            the id
     *
     * @return the incoming user
     *
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     */
    public IncomingUser getIncomingUser(final String id) throws DataBaseException {
        return get(new IncomingUser(id));
    }

    /**
     * Gets the incoming user object.
     *
     * @param id
     *            the id
     *
     * @return the incoming user object
     */
    public IncomingUser getIncomingUserObject(final String id) {
        try {
            return getIncomingUser(id);
        } catch (final DataBaseException e) {
            return null;
        }
    }

    /**
     * Gets the activity.
     *
     * @param id
     *            the id
     *
     * @return the activity
     *
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     */
    public Activity getActivity(final long id) throws DataBaseException {
        return get(new Activity(id));
    }

    /**
     * Gets the activity object.
     *
     * @param id
     *            the id
     *
     * @return the activity object
     */
    public Activity getActivityObject(final long id) {
        try {
            return getActivity(id);
        } catch (final DataBaseException e) {
            return null;
        }
    }

    /**
     * Gets the activity.
     *
     * @param id
     *            the id
     *
     * @return the activity
     *
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     */
    public Activity getActivity(final String id) throws DataBaseException {
        return getActivity(Long.parseLong(id));
    }

    /**
     * Gets the activity iterator.
     *
     * @return the activity iterator
     */
    DBIterator<Activity> getActivityIterator() {
        return getAll(Activity.class);
    }

    /**
     * Gets the alias.
     *
     * @param desName
     *            the des name
     * @param destinationName
     *            the destination name
     *
     * @return the alias
     *
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     */
    public Alias getAlias(final String desName, final String destinationName) throws DataBaseException {
        if (desName == null || destinationName == null) {
            throw new DataBaseException("Alias not found: {" + desName + "," + destinationName + "}");
        }
        return get(new Alias(desName, destinationName));
    }

    /**
     * Gets the alias object.
     *
     * @param desName
     *            the des name
     * @param destinationName
     *            the destination name
     *
     * @return the alias object
     */
    public Alias getAliasObject(final String desName, final String destinationName) {
        try {
            return getAlias(desName, destinationName);
        } catch (final DataBaseException e) {
            return null;
        }
    }

    /**
     * Gets the alias iterator.
     *
     * @return the alias iterator
     */
    DBIterator<Alias> getAliasIterator() {
        return getAll(Alias.class);
    }

    /**
     * Gets the alias array.
     *
     * @return the alias array
     */
    public Alias[] getAliasArray() {
        final var iterator = getAliasIterator();
        final List<Alias> list = new ArrayList<>();
        while (iterator.hasNext()) {
            list.add(iterator.next());
        }
        Collections.sort(list,
                (alias1, alias2) -> alias1.getDestinationName().compareToIgnoreCase(alias2.getDestinationName()));
        logSqlRequest("getAliasArray", list.size());
        iterator.remove();
        return list.toArray(new Alias[list.size()]);
    }

    /**
     * Gets the association.
     *
     * @param destinationName
     *            the destination name
     * @param hostName
     *            the host name
     *
     * @return the association
     *
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     */
    public Association getAssociation(final String destinationName, final String hostName) throws DataBaseException {
        if (destinationName == null || hostName == null) {
            throw new DataBaseException("Association not found: {" + destinationName + "," + hostName + "}");
        }
        return get(new Association(destinationName, hostName));
    }

    /**
     * Gets the policy association.
     *
     * @param destinationName
     *            the destination name
     * @param policyId
     *            the policy id
     *
     * @return the policy association
     *
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     */
    public PolicyAssociation getPolicyAssociation(final String destinationName, final String policyId)
            throws DataBaseException {
        if (destinationName == null || policyId == null) {
            throw new DataBaseException("PolicyAssociation not found: {" + destinationName + "," + policyId + "}");
        }
        return get(new PolicyAssociation(destinationName, policyId));
    }

    /**
     * Gets the association object.
     *
     * @param destinationName
     *            the destination name
     * @param hostName
     *            the host name
     *
     * @return the association object
     */
    public Association getAssociationObject(final String destinationName, final String hostName) {
        try {
            return getAssociation(destinationName, hostName);
        } catch (final DataBaseException e) {
            return null;
        }
    }

    /**
     * Gets the association iterator.
     *
     * @return the association iterator
     */
    DBIterator<Association> getAssociationIterator() {
        return getAll(Association.class);
    }

    /**
     * Gets the association array.
     *
     * @return the association array
     */
    public Association[] getAssociationArray() {
        final var iterator = getAssociationIterator();
        final List<Association> list = new ArrayList<>();
        while (iterator.hasNext()) {
            list.add(iterator.next());
        }
        logSqlRequest("getAssociationArray", list.size());
        iterator.remove();
        return list.toArray(new Association[list.size()]);
    }

    /**
     * Gets the category.
     *
     * @param id
     *            the id
     *
     * @return the category
     *
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     */
    public Category getCategory(final long id) throws DataBaseException {
        return get(new Category(id));
    }

    /**
     * Gets the category object.
     *
     * @param id
     *            the id
     *
     * @return the category object
     */
    public Category getCategoryObject(final long id) {
        try {
            return getCategory(id);
        } catch (final DataBaseException e) {
            return null;
        }
    }

    /**
     * Gets the category iterator.
     *
     * @return the category iterator
     */
    DBIterator<Category> getCategoryIterator() {
        return getAll(Category.class);
    }

    /**
     * Gets the category array.
     *
     * @return the category array
     */
    public Category[] getCategoryArray() {
        final var iterator = getCategoryIterator();
        final List<Category> list = new ArrayList<>();
        while (iterator.hasNext()) {
            list.add(iterator.next());
        }
        Collections.sort(list, (category1, category2) -> category1.getName().compareToIgnoreCase(category2.getName()));
        logSqlRequest("getCategoryArray", list.size());
        iterator.remove();
        return list.toArray(new Category[list.size()]);
    }

    /**
     * Gets the cat url.
     *
     * @param categoryId
     *            the category id
     * @param urlName
     *            the url name
     *
     * @return the cat url
     *
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     */
    public CatUrl getCatUrl(final long categoryId, final String urlName) throws DataBaseException {
        if (urlName == null) {
            throw new DataBaseException("CatUrl not found: {" + categoryId + ",(null)}");
        }
        return get(new CatUrl(categoryId, urlName));
    }

    /**
     * Gets the cat url object.
     *
     * @param categoryId
     *            the category id
     * @param urlName
     *            the url name
     *
     * @return the cat url object
     */
    public CatUrl getCatUrlObject(final long categoryId, final String urlName) {
        try {
            return getCatUrl(categoryId, urlName);
        } catch (final DataBaseException e) {
            return null;
        }
    }

    /**
     * Gets the cat url iterator.
     *
     * @return the cat url iterator
     */
    DBIterator<CatUrl> getCatUrlIterator() {
        return getAll(CatUrl.class);
    }

    /**
     * Gets the cat url array.
     *
     * @return the cat url array
     */
    public CatUrl[] getCatUrlArray() {
        final var iterator = getCatUrlIterator();
        final List<CatUrl> list = new ArrayList<>();
        while (iterator.hasNext()) {
            list.add(iterator.next());
        }
        logSqlRequest("getCatUrlArray", list.size());
        iterator.remove();
        return list.toArray(new CatUrl[list.size()]);
    }

    /**
     * Gets the country.
     *
     * @param iso
     *            the iso
     *
     * @return the country
     *
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     */
    public Country getCountry(final String iso) throws DataBaseException {
        if (iso == null) {
            throw new DataBaseException("Country not found: {(null)}");
        }
        return get(new Country(iso));
    }

    /**
     * Gets the country object.
     *
     * @param iso
     *            the iso
     *
     * @return the country object
     */
    public Country getCountryObject(final String iso) {
        try {
            return getCountry(iso);
        } catch (final DataBaseException e) {
            return null;
        }
    }

    /**
     * Gets the country iterator.
     *
     * @return the country iterator
     */
    DBIterator<Country> getCountryIterator() {
        return getAll(Country.class);
    }

    /**
     * Gets the country array.
     *
     * @return the country array
     */
    public Country[] getCountryArray() {
        final var iterator = getCountryIterator();
        final List<Country> list = new ArrayList<>();
        while (iterator.hasNext()) {
            list.add(iterator.next());
        }
        Collections.sort(list, (country1, country2) -> country1.getName().compareToIgnoreCase(country2.getName()));
        logSqlRequest("getCountryArray", list.size());
        iterator.remove();
        return list.toArray(new Country[list.size()]);
    }

    /**
     * Gets the data file.
     *
     * @param id
     *            the id
     *
     * @return the data file
     *
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     */
    public DataFile getDataFile(final long id) throws DataBaseException {
        return get(new DataFile(id));
    }

    /**
     * Gets the data file object.
     *
     * @param id
     *            the id
     *
     * @return the data file object
     */
    public DataFile getDataFileObject(final long id) {
        try {
            return getDataFile(id);
        } catch (final DataBaseException e) {
            return null;
        }
    }

    /**
     * Gets the data file.
     *
     * @param id
     *            the id
     *
     * @return the data file
     *
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     */
    public DataFile getDataFile(final String id) throws DataBaseException {
        return getDataFile(Long.parseLong(id));
    }

    /**
     * Gets the data file iterator.
     *
     * @return the data file iterator
     */
    DBIterator<DataFile> getDataFileIterator() {
        return getAll(DataFile.class);
    }

    /**
     * Gets the data transfer.
     *
     * @param id
     *            the id
     *
     * @return the data transfer
     *
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     */
    public DataTransfer getDataTransfer(final long id) throws DataBaseException {
        return get(new DataTransfer(id));
    }

    /**
     * Gets the data transfer object.
     *
     * @param id
     *            the id
     *
     * @return the data transfer object
     */
    public DataTransfer getDataTransferObject(final long id) {
        try {
            return getDataTransfer(id);
        } catch (final DataBaseException e) {
            return null;
        }
    }

    /**
     * Gets the data transfer iterator.
     *
     * @return the data transfer iterator
     */
    DBIterator<DataTransfer> getDataTransferIterator() {
        return getAll(DataTransfer.class);
    }

    /**
     * Gets the destination.
     *
     * @param name
     *            the name
     *
     * @return the destination
     *
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     */
    public Destination getDestination(final String name) throws DataBaseException {
        if (name == null) {
            throw new DataBaseException("Destination not found: {(null)}");
        }
        return get(new Destination(name));
    }

    /**
     * Gets the destination object.
     *
     * @param name
     *            the name
     *
     * @return the destination object
     */
    public Destination getDestinationObject(final String name) {
        try {
            return getDestination(name);
        } catch (final DataBaseException e) {
            return null;
        }
    }

    /**
     * Gets the destination iterator.
     *
     * @return the destination iterator
     */
    DBIterator<Destination> getDestinationIterator() {
        return getAll(Destination.class);
    }

    /**
     * Gets the destination array.
     *
     * @return the destination array
     */
    public Destination[] getDestinationArray() {
        final var iterator = getDestinationIterator();
        final List<Destination> list = new ArrayList<>();
        while (iterator.hasNext()) {
            list.add(iterator.next());
        }
        Collections.sort(list,
                (destination1, destination2) -> destination1.getName().compareToIgnoreCase(destination2.getName()));
        logSqlRequest("getDestinationArray", list.size());
        iterator.remove();
        return list.toArray(new Destination[list.size()]);
    }

    /**
     * Gets the incoming user iterator.
     *
     * @return the incoming user iterator
     */
    DBIterator<IncomingUser> getIncomingUserIterator() {
        return getAll(IncomingUser.class);
    }

    /**
     * Gets the incoming user array.
     *
     * @return the incoming user array
     */
    public IncomingUser[] getIncomingUserArray() {
        final var iterator = getIncomingUserIterator();
        final List<IncomingUser> list = new ArrayList<>();
        while (iterator.hasNext()) {
            list.add(iterator.next());
        }
        logSqlRequest("getIncomingUserArray", list.size());
        iterator.remove();
        return list.toArray(new IncomingUser[list.size()]);
    }

    /**
     * Gets the incoming association iterator.
     *
     * @return the incoming association iterator
     */
    DBIterator<IncomingAssociation> getIncomingAssociationIterator() {
        return getAll(IncomingAssociation.class);
    }

    /**
     * Gets the incoming association array.
     *
     * @return the incoming association array
     */
    public IncomingAssociation[] getIncomingAssociationArray() {
        final var iterator = getIncomingAssociationIterator();
        final List<IncomingAssociation> list = new ArrayList<>();
        while (iterator.hasNext()) {
            list.add(iterator.next());
        }
        logSqlRequest("getIncomingAssociationArray", list.size());
        iterator.remove();
        return list.toArray(new IncomingAssociation[list.size()]);
    }

    /**
     * Gets the incoming permission iterator.
     *
     * @return the incoming permission iterator
     */
    DBIterator<IncomingPermission> getIncomingPermissionIterator() {
        return getAll(IncomingPermission.class);
    }

    /**
     * Gets the destination ecuser.
     *
     * @param destinationName
     *            the destination name
     * @param ecuserName
     *            the ecuser name
     *
     * @return the destination ecuser
     *
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     */
    public DestinationECUser getDestinationECUser(final String destinationName, final String ecuserName)
            throws DataBaseException {
        if (destinationName == null || ecuserName == null) {
            throw new DataBaseException("DestinationECUser not found: {" + destinationName + "," + ecuserName + "}");
        }
        return get(new DestinationECUser(destinationName, ecuserName));
    }

    /**
     * Gets the destination ecuser object.
     *
     * @param destinationName
     *            the destination name
     * @param ecuserName
     *            the ecuser name
     *
     * @return the destination ecuser object
     */
    public DestinationECUser getDestinationECUserObject(final String destinationName, final String ecuserName) {
        try {
            return getDestinationECUser(destinationName, ecuserName);
        } catch (final DataBaseException e) {
            return null;
        }
    }

    /**
     * Gets the destination ecuser iterator.
     *
     * @return the destination ecuser iterator
     */
    DBIterator<DestinationECUser> getDestinationECUserIterator() {
        return getAll(DestinationECUser.class);
    }

    /**
     * Gets the destination ecuser array.
     *
     * @return the destination ecuser array
     */
    public DestinationECUser[] getDestinationECUserArray() {
        final var iterator = getDestinationECUserIterator();
        final List<DestinationECUser> list = new ArrayList<>();
        while (iterator.hasNext()) {
            list.add(iterator.next());
        }
        logSqlRequest("getDestinationECUserArray", list.size());
        iterator.remove();
        return list.toArray(new DestinationECUser[list.size()]);
    }

    /**
     * Gets the EC session.
     *
     * @param id
     *            the id
     *
     * @return the EC session
     *
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     */
    public ECSession getECSession(final int id) throws DataBaseException {
        return get(new ECSession(id));
    }

    /**
     * Gets the EC session object.
     *
     * @param id
     *            the id
     *
     * @return the EC session object
     */
    public ECSession getECSessionObject(final int id) {
        try {
            return getECSession(id);
        } catch (final DataBaseException e) {
            return null;
        }
    }

    /**
     * Gets the EC session.
     *
     * @param id
     *            the id
     *
     * @return the EC session
     *
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     */
    public ECSession getECSession(final String id) throws DataBaseException {
        return getECSession(Integer.parseInt(id));
    }

    /**
     * Gets the ec session iterator.
     *
     * @return the EC session iterator
     */
    DBIterator<ECSession> getECSessionIterator() {
        return getAll(ECSession.class);
    }

    /**
     * Gets the ectrans destination.
     *
     * @param name
     *            the name
     *
     * @return the ectrans destination
     *
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     */
    public ECtransDestination getECtransDestination(final String name) throws DataBaseException {
        if (name == null) {
            throw new DataBaseException("ECtransDestination not found: {" + name + "}");
        }
        return get(new ECtransDestination(name));
    }

    /**
     * Gets the ectrans destination object.
     *
     * @param name
     *            the name
     *
     * @return the ectrans destination object
     */
    public ECtransDestination getECtransDestinationObject(final String name) {
        try {
            return getECtransDestination(name);
        } catch (final DataBaseException e) {
            return null;
        }
    }

    /**
     * Gets the ectrans destination iterator.
     *
     * @return the ectrans destination iterator
     */
    DBIterator<ECtransDestination> getECtransDestinationIterator() {
        return getAll(ECtransDestination.class);
    }

    /**
     * Gets the ectrans destination array.
     *
     * @return the ectrans destination array
     */
    public ECtransDestination[] getECtransDestinationArray() {
        final var iterator = getECtransDestinationIterator();
        final List<ECtransDestination> list = new ArrayList<>();
        while (iterator.hasNext()) {
            list.add(iterator.next());
        }
        Collections.sort(list,
                (destination1, destination2) -> destination1.getName().compareToIgnoreCase(destination2.getName()));
        logSqlRequest("getECtransDestinationArray", list.size());
        iterator.remove();
        return list.toArray(new ECtransDestination[list.size()]);
    }

    /**
     * Gets the ectrans accounting.
     *
     * @param id
     *            the id
     *
     * @return the ectrans accounting
     *
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     */
    public ECtransAccounting getECtransAccounting(final int id) throws DataBaseException {
        return get(new ECtransAccounting(id));
    }

    /**
     * Gets the ectrans accounting object.
     *
     * @param id
     *            the id
     *
     * @return the ectrans accounting object
     */
    public ECtransAccounting getECtransAccountingObject(final int id) {
        try {
            return getECtransAccounting(id);
        } catch (final DataBaseException e) {
            return null;
        }
    }

    /**
     * Gets the ectrans accounting.
     *
     * @param id
     *            the id
     *
     * @return the ectrans accounting
     *
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     */
    public ECtransAccounting getECtransAccounting(final String id) throws DataBaseException {
        return getECtransAccounting(Integer.parseInt(id));
    }

    /**
     * Gets the ectrans accounting iterator.
     *
     * @return the ectrans accounting iterator
     */
    DBIterator<ECtransAccounting> getECtransAccountingIterator() {
        return getAll(ECtransAccounting.class);
    }

    /**
     * Gets the ectrans module.
     *
     * @param name
     *            the name
     *
     * @return the ectrans module
     *
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     */
    public ECtransModule getECtransModule(final String name) throws DataBaseException {
        if (name == null) {
            throw new DataBaseException("ECtransModule not found: {(null)}");
        }
        return get(new ECtransModule(name));
    }

    /**
     * Gets the ectrans module object.
     *
     * @param name
     *            the name
     *
     * @return the ectrans module object
     */
    public ECtransModule getECtransModuleObject(final String name) {
        try {
            return getECtransModule(name);
        } catch (final DataBaseException e) {
            return null;
        }
    }

    /**
     * Gets the ectrans module iterator.
     *
     * @return the ectrans module iterator
     */
    DBIterator<ECtransModule> getECtransModuleIterator() {
        return getAll(ECtransModule.class);
    }

    /**
     * Gets the ectrans module array.
     *
     * @return the ectrans module array
     */
    public ECtransModule[] getECtransModuleArray() {
        final var iterator = getECtransModuleIterator();
        final List<ECtransModule> list = new ArrayList<>();
        while (iterator.hasNext()) {
            list.add(iterator.next());
        }
        Collections.sort(list, (module1, module2) -> module1.getName().compareToIgnoreCase(module2.getName()));
        logSqlRequest("getECtransModuleArray", list.size());
        iterator.remove();
        return list.toArray(new ECtransModule[list.size()]);
    }

    /**
     * Gets the EC user.
     *
     * @param name
     *            the name
     *
     * @return the EC user
     *
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     */
    public ECUser getECUser(final String name) throws DataBaseException {
        if (name == null) {
            throw new DataBaseException("ECUser not found: {(null)}");
        }
        return get(new ECUser(name));
    }

    /**
     * Gets the EC user object.
     *
     * @param name
     *            the name
     *
     * @return the EC user object
     */
    public ECUser getECUserObject(final String name) {
        try {
            return getECUser(name);
        } catch (final DataBaseException e) {
            return null;
        }
    }

    /**
     * Gets the ecuser iterator.
     *
     * @return the EC user iterator
     */
    DBIterator<ECUser> getECUserIterator() {
        return getAll(ECUser.class);
    }

    /**
     * Gets the EC user array.
     *
     * @return the EC user array
     */
    public ECUser[] getECUserArray() {
        final var iterator = getECUserIterator();
        final List<ECUser> list = new ArrayList<>();
        while (iterator.hasNext()) {
            list.add(iterator.next());
        }
        Collections.sort(list, (ecuser1, ecuser2) -> ecuser1.getName().compareToIgnoreCase(ecuser2.getName()));
        logSqlRequest("getECUserArray", list.size());
        iterator.remove();
        return list.toArray(new ECUser[list.size()]);
    }

    /**
     * Gets the incoming policy.
     *
     * @param id
     *            the id
     *
     * @return the incoming policy
     *
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     */
    public IncomingPolicy getIncomingPolicy(final String id) throws DataBaseException {
        if (id == null) {
            throw new DataBaseException("IncomingPolicy not found: {(null)}");
        }
        return get(new IncomingPolicy(id));
    }

    /**
     * Gets the EC user object.
     *
     * @param id
     *            the id
     *
     * @return the EC user object
     */
    public IncomingPolicy getIncomingPolicyObject(final String id) {
        try {
            return getIncomingPolicy(id);
        } catch (final DataBaseException e) {
            return null;
        }
    }

    /**
     * Gets the incoming policy iterator.
     *
     * @return the incoming policy iterator
     */
    DBIterator<IncomingPolicy> getIncomingPolicyIterator() {
        return getAll(IncomingPolicy.class);
    }

    /**
     * Gets the incoming policy array.
     *
     * @return the incoming policy array
     */
    public IncomingPolicy[] getIncomingPolicyArray() {
        final var iterator = getIncomingPolicyIterator();
        final List<IncomingPolicy> list = new ArrayList<>();
        while (iterator.hasNext()) {
            list.add(iterator.next());
        }
        logSqlRequest("getIncomingPolicyArray", list.size());
        iterator.remove();
        return list.toArray(new IncomingPolicy[list.size()]);
    }

    /**
     * Gets the event.
     *
     * @param id
     *            the id
     *
     * @return the event
     *
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     */
    public Event getEvent(final long id) throws DataBaseException {
        return get(new Event(id));
    }

    /**
     * Gets the event object.
     *
     * @param id
     *            the id
     *
     * @return the event object
     */
    public Event getEventObject(final int id) {
        try {
            return getEvent(id);
        } catch (final DataBaseException e) {
            return null;
        }
    }

    /**
     * Gets the event.
     *
     * @param id
     *            the id
     *
     * @return the event
     *
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     */
    public Event getEvent(final String id) throws DataBaseException {
        return getEvent(Integer.parseInt(id));
    }

    /**
     * Gets the event iterator.
     *
     * @return the event iterator
     */
    DBIterator<Event> getEventIterator() {
        return getAll(Event.class);
    }

    /**
     * Gets the publication.
     *
     * @param id
     *            the id
     *
     * @return the publication
     *
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     */
    public Publication getPublication(final int id) throws DataBaseException {
        return get(new Publication(id));
    }

    /**
     * Gets the publication object.
     *
     * @param id
     *            the id
     *
     * @return the publication object
     */
    public Publication getPublicationObject(final int id) {
        try {
            return getPublication(id);
        } catch (final DataBaseException e) {
            return null;
        }
    }

    /**
     * Gets the publication.
     *
     * @param id
     *            the id
     *
     * @return the publication
     *
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     */
    public Publication getPublication(final String id) throws DataBaseException {
        return getPublication(Integer.parseInt(id));
    }

    /**
     * Gets the publication iterator.
     *
     * @return the publication iterator
     */
    public DBIterator<Publication> getPublicationIterator() {
        return getAll(Publication.class);
    }

    /**
     * Gets the host.
     *
     * @param name
     *            the name
     *
     * @return the host
     *
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     */
    public Host getHost(final String name) throws DataBaseException {
        if (name == null) {
            throw new DataBaseException("Host not found: {(null)}");
        }
        return get(new Host(name));
    }

    /**
     * Gets the host object.
     *
     * @param name
     *            the name
     *
     * @return the host object
     */
    public Host getHostObject(final String name) {
        try {
            return getHost(name);
        } catch (final DataBaseException e) {
            return null;
        }
    }

    /**
     * Gets the host and its output.
     *
     * @param name
     *            the name
     *
     * @return the host and its output.
     *
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     */
    public Host getHostWithOutput(final String name) throws DataBaseException {
        final var host = getHost(name);
        host.setHostOutput(getHostOutput(host.getHostOutputId()));
        return host;
    }

    /**
     * Gets the host iterator.
     *
     * @return the host iterator
     */
    DBIterator<Host> getHostIterator() {
        return getAll(Host.class);
    }

    /**
     * Gets the host array.
     *
     * @return the host array
     */
    public Host[] getHostArray() {
        final var iterator = getHostIterator();
        final List<Host> list = new ArrayList<>();
        while (iterator.hasNext()) {
            list.add(iterator.next());
        }
        logSqlRequest("getHostArray", list.size());
        iterator.remove();
        return list.toArray(new Host[list.size()]);
    }

    /**
     * Gets the host array.
     *
     * @param comparator
     *            the comparator
     *
     * @return the host array
     */
    public Host[] getHostArray(final Comparator<Host> comparator) {
        final var iterator = getHostIterator();
        final List<Host> list = new ArrayList<>();
        while (iterator.hasNext()) {
            list.add(iterator.next());
        }
        logSqlRequest("getHostArray", list.size());
        iterator.remove();
        list.sort(comparator);
        return list.toArray(new Host[list.size()]);
    }

    /**
     * Gets the host stats.
     *
     * @param id
     *            the id
     *
     * @return the host
     *
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     */
    public HostStats getHostStats(final int id) throws DataBaseException {
        return get(new HostStats(id));
    }

    /**
     * Gets the host stats object.
     *
     * @param id
     *            the id
     *
     * @return the host object
     */
    public HostStats getHostStatsObject(final int id) {
        try {
            return getHostStats(id);
        } catch (final DataBaseException e) {
            return null;
        }
    }

    /**
     * Gets the host location.
     *
     * @param id
     *            the id
     *
     * @return the host
     *
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     */
    public HostLocation getHostLocation(final int id) throws DataBaseException {
        return get(new HostLocation(id));
    }

    /**
     * Gets the host location object.
     *
     * @param id
     *            the id
     *
     * @return the host object
     */
    public HostLocation getHostLocationObject(final int id) {
        try {
            return getHostLocation(id);
        } catch (final DataBaseException e) {
            return null;
        }
    }

    /**
     * Gets the host output.
     *
     * @param id
     *            the id
     *
     * @return the host
     *
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     */
    public HostOutput getHostOutput(final int id) throws DataBaseException {
        return get(new HostOutput(id));
    }

    /**
     * Gets the host output object.
     *
     * @param id
     *            the id
     *
     * @return the host object
     */
    public HostOutput getHostOutputObject(final int id) {
        try {
            return getHostOutput(id);
        } catch (final DataBaseException e) {
            return null;
        }
    }

    /**
     * Gets the host ecuser.
     *
     * @param ecuserName
     *            the ecuser name
     * @param hostName
     *            the host name
     *
     * @return the host ecuser
     *
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     */
    public HostECUser getHostECUser(final String ecuserName, final String hostName) throws DataBaseException {
        if (ecuserName == null || hostName == null) {
            throw new DataBaseException("HostECUser not found: {" + ecuserName + "," + hostName + "}");
        }
        return get(new HostECUser(ecuserName, hostName));
    }

    /**
     * Gets the host ecuser object.
     *
     * @param ecuserName
     *            the ecuser name
     * @param hostName
     *            the host name
     *
     * @return the host ecuser object
     */
    public HostECUser getHostECUserObject(final String ecuserName, final String hostName) {
        try {
            return getHostECUser(ecuserName, hostName);
        } catch (final DataBaseException e) {
            return null;
        }
    }

    /**
     * Gets the host ecuser iterator.
     *
     * @return the host ecuser iterator
     */
    DBIterator<HostECUser> getHostECUserIterator() {
        return getAll(HostECUser.class);
    }

    /**
     * Gets the metadata attribute.
     *
     * @param name
     *            the name
     *
     * @return the metadata attribute
     *
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     */
    public MetadataAttribute getMetadataAttribute(final String name) throws DataBaseException {
        if (name == null) {
            throw new DataBaseException("MetadataAttribute not found: {" + name + "}");
        }
        return get(new MetadataAttribute(name));
    }

    /**
     * Gets the metadata attribute object.
     *
     * @param name
     *            the name
     *
     * @return the metadata attribute object
     */
    public MetadataAttribute getMetadataAttributeObject(final String name) {
        try {
            return getMetadataAttribute(name);
        } catch (final DataBaseException e) {
            return null;
        }
    }

    /**
     * Gets the metadata attribute iterator.
     *
     * @return the metadata attribute iterator
     */
    DBIterator<MetadataAttribute> getMetadataAttributeIterator() {
        return getAll(MetadataAttribute.class);
    }

    /**
     * Gets the metadata attribute array.
     *
     * @return the metadata attribute array
     */
    public MetadataAttribute[] getMetadataAttributeArray() {
        final var iterator = getMetadataAttributeIterator();
        final List<MetadataAttribute> list = new ArrayList<>();
        while (iterator.hasNext()) {
            list.add(iterator.next());
        }
        Collections.sort(list,
                (attribute1, attribute2) -> attribute1.getName().compareToIgnoreCase(attribute2.getName()));
        logSqlRequest("getMetadataAttributeArray", list.size());
        iterator.remove();
        return list.toArray(new MetadataAttribute[list.size()]);
    }

    /**
     * Gets the monitoring value.
     *
     * @param id
     *            the id
     *
     * @return the monitoring value
     *
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     */
    public MonitoringValue getMonitoringValue(final long id) throws DataBaseException {
        return get(new MonitoringValue(id));
    }

    /**
     * Gets the monitoring value object.
     *
     * @param id
     *            the id
     *
     * @return the monitoring value object
     */
    public MonitoringValue getMonitoringValueObject(final long id) {
        try {
            return getMonitoringValue(id);
        } catch (final DataBaseException e) {
            return null;
        }
    }

    /**
     * Gets the monitoring value.
     *
     * @param id
     *            the id
     *
     * @return the monitoring value
     *
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     */
    public MonitoringValue getMonitoringValue(final String id) throws DataBaseException {
        return getMonitoringValue(Long.parseLong(id));
    }

    /**
     * Gets the monitoring value iterator.
     *
     * @return the monitoring value iterator
     */
    DBIterator<MonitoringValue> getMonitoringValueIterator() {
        return getAll(MonitoringValue.class);
    }

    /**
     * Gets the monitoring history iterator.
     *
     * @return the monitoring history iterator
     */
    DBIterator<MonitoringHistory> getMonitoringHistoryIterator() {
        return getAll(MonitoringHistory.class);
    }

    /**
     * Gets the authorised ecuser.
     *
     * @param ecuserName
     *            the ecuser name
     * @param msuserName
     *            the msuser name
     *
     * @return the authorised ecuser
     *
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     */
    public AuthorizedECUser getAuthorizedECUser(final String ecuserName, final String msuserName)
            throws DataBaseException {
        if (ecuserName == null || msuserName == null) {
            throw new DataBaseException("AuthorizedECUser not found: {" + ecuserName + "," + msuserName + "}");
        }
        return get(new AuthorizedECUser(ecuserName, msuserName));
    }

    /**
     * Gets the authorized ecuser object.
     *
     * @param ecuserName
     *            the ecuser name
     * @param msuserName
     *            the msuser name
     *
     * @return the authorized ecuser object
     */
    public AuthorizedECUser getAuthorizedECUserObject(final String ecuserName, final String msuserName) {
        try {
            return getAuthorizedECUser(ecuserName, msuserName);
        } catch (final DataBaseException e) {
            return null;
        }
    }

    /**
     * Gets the authorized ecuser iterator.
     *
     * @return the authorized ecuser iterator
     */
    DBIterator<AuthorizedECUser> getAuthorizedECUserIterator() {
        return getAll(AuthorizedECUser.class);
    }

    /**
     * Gets the notification.
     *
     * @param id
     *            the id
     *
     * @return the notification
     *
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     */
    public Notification getNotification(final int id) throws DataBaseException {
        return get(new Notification(id));
    }

    /**
     * Gets the notification object.
     *
     * @param id
     *            the id
     *
     * @return the notification object
     */
    public Notification getNotificationObject(final int id) {
        try {
            return getNotification(id);
        } catch (final DataBaseException e) {
            return null;
        }
    }

    /**
     * Gets the notification.
     *
     * @param id
     *            the id
     *
     * @return the notification
     *
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     */
    public Notification getNotification(final String id) throws DataBaseException {
        return getNotification(Integer.parseInt(id));
    }

    /**
     * Gets the notification iterator.
     *
     * @return the notification iterator
     */
    DBIterator<Notification> getNotificationIterator() {
        return getAll(Notification.class);
    }

    /**
     * Gets the operation.
     *
     * @param name
     *            the name
     *
     * @return the operation
     *
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     */
    public Operation getOperation(final String name) throws DataBaseException {
        if (name == null) {
            throw new DataBaseException("Operation not found: {(null)}");
        }
        return get(new Operation(name));
    }

    /**
     * Gets the operation object.
     *
     * @param name
     *            the name
     *
     * @return the operation object
     */
    public Operation getOperationObject(final String name) {
        try {
            return getOperation(name);
        } catch (final DataBaseException e) {
            return null;
        }
    }

    /**
     * Gets the operation iterator.
     *
     * @return the operation iterator
     */
    DBIterator<Operation> getOperationIterator() {
        return getAll(Operation.class);
    }

    /**
     * Gets the operation array.
     *
     * @return the operation array
     */
    public Operation[] getOperationArray() {
        final var iterator = getOperationIterator();
        final List<Operation> list = new ArrayList<>();
        while (iterator.hasNext()) {
            list.add(iterator.next());
        }
        Collections.sort(list,
                (operation1, operation2) -> operation1.getName().compareToIgnoreCase(operation2.getName()));
        logSqlRequest("getOperationArray", list.size());
        iterator.remove();
        return list.toArray(new Operation[list.size()]);
    }

    /**
     * Gets the permission.
     *
     * @param ecuserName
     *            the ecuser name
     * @param operationName
     *            the operation name
     *
     * @return the permission
     *
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     */
    public Permission getPermission(final String ecuserName, final String operationName) throws DataBaseException {
        if (ecuserName == null || operationName == null) {
            throw new DataBaseException("Permission not found: {" + ecuserName + "," + operationName + "}");
        }
        return get(new Permission(ecuserName, operationName));
    }

    /**
     * Gets the permission object.
     *
     * @param ecuserName
     *            the ecuser name
     * @param operationName
     *            the operation name
     *
     * @return the permission object
     */
    public Permission getPermissionObject(final String ecuserName, final String operationName) {
        try {
            return getPermission(ecuserName, operationName);
        } catch (final DataBaseException e) {
            return null;
        }
    }

    /**
     * Gets the permission iterator.
     *
     * @return the permission iterator
     */
    DBIterator<Permission> getPermissionIterator() {
        return getAll(Permission.class);
    }

    /**
     * Gets the privileged.
     *
     * @param ecuserName
     *            the ecuser name
     * @param notificationId
     *            the notification id
     *
     * @return the privileged
     *
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     */
    public Privileged getPrivileged(final String ecuserName, final int notificationId) throws DataBaseException {
        if (ecuserName == null || false) {
            throw new DataBaseException("Privileged not found: {(null)," + notificationId + "}");
        }
        return get(new Privileged(ecuserName, notificationId));
    }

    /**
     * Gets the privileged object.
     *
     * @param ecuserName
     *            the ecuser name
     * @param notificationId
     *            the notification id
     *
     * @return the privileged object
     */
    public Privileged getPrivilegedObject(final String ecuserName, final int notificationId) {
        try {
            return getPrivileged(ecuserName, notificationId);
        } catch (final DataBaseException e) {
            return null;
        }
    }

    /**
     * Gets the privileged iterator.
     *
     * @return the privileged iterator
     */
    DBIterator<Privileged> getPrivilegedIterator() {
        return getAll(Privileged.class);
    }

    /**
     * Gets the product status.
     *
     * @param id
     *            the id
     *
     * @return the product status
     *
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     */
    public ProductStatus getProductStatus(final long id) throws DataBaseException {
        return get(new ProductStatus(id));
    }

    /**
     * Gets the product status object.
     *
     * @param id
     *            the id
     *
     * @return the product status object
     */
    public ProductStatus getProductStatusObject(final long id) {
        try {
            return getProductStatus(id);
        } catch (final DataBaseException e) {
            return null;
        }
    }

    /**
     * Gets the product status.
     *
     * @param id
     *            the id
     *
     * @return the product status
     *
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     */
    public ProductStatus getProductStatus(final String id) throws DataBaseException {
        return getProductStatus(Long.parseLong(id));
    }

    /**
     * Gets the product status iterator.
     *
     * @return the product status iterator
     */
    DBIterator<ProductStatus> getProductStatusIterator() {
        return getAll(ProductStatus.class);
    }

    /**
     * Gets the product status array.
     *
     * @return the product status array
     */
    public ProductStatus[] getProductStatusArray() {
        final var iterator = getProductStatusIterator();
        final List<ProductStatus> list = new ArrayList<>();
        while (iterator.hasNext()) {
            list.add(iterator.next());
        }
        logSqlRequest("getProductStatusArray", list.size());
        iterator.remove();
        return list.toArray(new ProductStatus[list.size()]);
    }

    /**
     * Gets the reception.
     *
     * @param id
     *            the id
     *
     * @return the reception
     *
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     */
    public Reception getReception(final int id) throws DataBaseException {
        return get(new Reception(id));
    }

    /**
     * Gets the reception object.
     *
     * @param id
     *            the id
     *
     * @return the reception object
     */
    public Reception getReceptionObject(final int id) {
        try {
            return getReception(id);
        } catch (final DataBaseException e) {
            return null;
        }
    }

    /**
     * Gets the reception.
     *
     * @param id
     *            the id
     *
     * @return the reception
     *
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     */
    public Reception getReception(final String id) throws DataBaseException {
        return getReception(Integer.parseInt(id));
    }

    /**
     * Gets the reception iterator.
     *
     * @return the reception iterator
     */
    DBIterator<Reception> getReceptionIterator() {
        return getAll(Reception.class);
    }

    /**
     * Gets the scheduler value.
     *
     * @param id
     *            the id
     *
     * @return the scheduler value
     *
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     */
    public SchedulerValue getSchedulerValue(final int id) throws DataBaseException {
        return get(new SchedulerValue(id));
    }

    /**
     * Gets the scheduler value object.
     *
     * @param id
     *            the id
     *
     * @return the scheduler value object
     */
    public SchedulerValue getSchedulerValueObject(final int id) {
        try {
            return getSchedulerValue(id);
        } catch (final DataBaseException e) {
            return null;
        }
    }

    /**
     * Gets the scheduler value.
     *
     * @param id
     *            the id
     *
     * @return the scheduler value
     *
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     */
    public SchedulerValue getSchedulerValue(final String id) throws DataBaseException {
        return getSchedulerValue(Integer.parseInt(id));
    }

    /**
     * Gets the scheduler value iterator.
     *
     * @return the scheduler value iterator
     */
    DBIterator<SchedulerValue> getSchedulerValueIterator() {
        return getAll(SchedulerValue.class);
    }

    /**
     * Gets the spool.
     *
     * @param id
     *            the id
     *
     * @return the spool
     *
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     */
    public Spool getSpool(final int id) throws DataBaseException {
        return get(new Spool(id));
    }

    /**
     * Gets the spool object.
     *
     * @param id
     *            the id
     *
     * @return the spool object
     */
    public Spool getSpoolObject(final int id) {
        try {
            return getSpool(id);
        } catch (final DataBaseException e) {
            return null;
        }
    }

    /**
     * Gets the spool.
     *
     * @param id
     *            the id
     *
     * @return the spool
     *
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     */
    public Spool getSpool(final String id) throws DataBaseException {
        return getSpool(Integer.parseInt(id));
    }

    /**
     * Gets the spool iterator.
     *
     * @return the spool iterator
     */
    DBIterator<Spool> getSpoolIterator() {
        return getAll(Spool.class);
    }

    /**
     * Gets the transfer group.
     *
     * @param name
     *            the name
     *
     * @return the transfer group
     *
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     */
    public TransferGroup getTransferGroup(final String name) throws DataBaseException {
        if (name == null) {
            throw new DataBaseException("TransferGroup not found: {(null)}");
        }
        return get(new TransferGroup(name));
    }

    /**
     * Gets the transfer group object.
     *
     * @param name
     *            the name
     *
     * @return the transfer group object
     */
    public TransferGroup getTransferGroupObject(final String name) {
        try {
            return getTransferGroup(name);
        } catch (final DataBaseException e) {
            return null;
        }
    }

    /**
     * Gets the transfer group iterator.
     *
     * @return the transfer group iterator
     */
    DBIterator<TransferGroup> getTransferGroupIterator() {
        return getAll(TransferGroup.class);
    }

    /**
     * Gets the transfer group array.
     *
     * @return the transfer group array
     */
    public TransferGroup[] getTransferGroupArray() {
        final var iterator = getTransferGroupIterator();
        final List<TransferGroup> list = new ArrayList<>();
        while (iterator.hasNext()) {
            list.add(iterator.next());
        }
        Collections.sort(list, (group1, group2) -> group1.getName().compareToIgnoreCase(group2.getName()));
        logSqlRequest("getTransferGroupArray", list.size());
        iterator.remove();
        return list.toArray(new TransferGroup[list.size()]);
    }

    /**
     * Gets the transfer history.
     *
     * @param id
     *            the id
     *
     * @return the transfer history
     *
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     */
    public TransferHistory getTransferHistory(final long id) throws DataBaseException {
        return get(new TransferHistory(id));
    }

    /**
     * Gets the transfer history object.
     *
     * @param id
     *            the id
     *
     * @return the transfer history object
     */
    public TransferHistory getTransferHistoryObject(final long id) {
        try {
            return getTransferHistory(id);
        } catch (final DataBaseException e) {
            return null;
        }
    }

    /**
     * Gets the transfer history.
     *
     * @param id
     *            the id
     *
     * @return the transfer history
     *
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     */
    public TransferHistory getTransferHistory(final String id) throws DataBaseException {
        return getTransferHistory(Long.parseLong(id));
    }

    /**
     * Gets the transfer method.
     *
     * @param name
     *            the name
     *
     * @return the transfer method
     *
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     */
    public TransferMethod getTransferMethod(final String name) throws DataBaseException {
        if (name == null) {
            throw new DataBaseException("TransferMethod not found: {(null)}");
        }
        return get(new TransferMethod(name));
    }

    /**
     * Gets the transfer method object.
     *
     * @param name
     *            the name
     *
     * @return the transfer method object
     */
    public TransferMethod getTransferMethodObject(final String name) {
        try {
            return getTransferMethod(name);
        } catch (final DataBaseException e) {
            return null;
        }
    }

    /**
     * Gets the transfer method iterator.
     *
     * @return the transfer method iterator
     */
    DBIterator<TransferMethod> getTransferMethodIterator() {
        return getAll(TransferMethod.class);
    }

    /**
     * Gets the transfer method array.
     *
     * @return the transfer method array
     */
    public TransferMethod[] getTransferMethodArray() {
        final var iterator = getTransferMethodIterator();
        final List<TransferMethod> list = new ArrayList<>();
        while (iterator.hasNext()) {
            list.add(iterator.next());
        }
        Collections.sort(list, (method1, method2) -> method1.getName().compareToIgnoreCase(method2.getName()));
        logSqlRequest("getTransferMethodArray", list.size());
        iterator.remove();
        return list.toArray(new TransferMethod[list.size()]);
    }

    /**
     * Gets the transfer module.
     *
     * @param name
     *            the name
     *
     * @return the transfer module
     *
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     */
    public TransferModule getTransferModule(final String name) throws DataBaseException {
        if (name == null) {
            throw new DataBaseException("TransferModule not found: {(null)}");
        }
        return get(new TransferModule(name));
    }

    /**
     * Gets the transfer module object.
     *
     * @param name
     *            the name
     *
     * @return the transfer module object
     */
    public TransferModule getTransferModuleObject(final String name) {
        try {
            return getTransferModule(name);
        } catch (final DataBaseException e) {
            return null;
        }
    }

    /**
     * Gets the transfer module iterator.
     *
     * @return the transfer module iterator
     */
    DBIterator<TransferModule> getTransferModuleIterator() {
        return getAll(TransferModule.class);
    }

    /**
     * Gets the transfer module array.
     *
     * @return the transfer module array
     */
    public TransferModule[] getTransferModuleArray() {
        final var iterator = getTransferModuleIterator();
        final List<TransferModule> list = new ArrayList<>();
        while (iterator.hasNext()) {
            list.add(iterator.next());
        }
        logSqlRequest("getTransferModuleArray", list.size());
        iterator.remove();
        return list.toArray(new TransferModule[list.size()]);
    }

    /**
     * Gets the transfer server.
     *
     * @param name
     *            the name
     *
     * @return the transfer server
     *
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     */
    public TransferServer getTransferServer(final String name) throws DataBaseException {
        if (name == null) {
            throw new DataBaseException("TransferServer not found: {(null)}");
        }
        return get(new TransferServer(name));
    }

    /**
     * Gets the transfer server object.
     *
     * @param name
     *            the name
     *
     * @return the transfer server object
     */
    public TransferServer getTransferServerObject(final String name) {
        try {
            return getTransferServer(name);
        } catch (final DataBaseException e) {
            return null;
        }
    }

    /**
     * Gets the transfer server iterator.
     *
     * @return the transfer server iterator
     */
    DBIterator<TransferServer> getTransferServerIterator() {
        return getAll(TransferServer.class);
    }

    /**
     * Gets the transfer server array.
     *
     * @return the transfer server array
     */
    public TransferServer[] getTransferServerArray() {
        final var iterator = getTransferServerIterator();
        final List<TransferServer> list = new ArrayList<>();
        while (iterator.hasNext()) {
            list.add(iterator.next());
        }
        Collections.sort(list, (server1, server2) -> server1.getName().compareToIgnoreCase(server2.getName()));
        logSqlRequest("getTransferServerArray", list.size());
        iterator.remove();
        return list.toArray(new TransferServer[list.size()]);
    }

    /**
     * Gets the transfer ecuser.
     *
     * @param ecuserName
     *            the ecuser name
     * @param transfermethodName
     *            the transfermethod name
     *
     * @return the transfer ecuser
     *
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     */
    public TransferECUser getTransferECUser(final String ecuserName, final String transfermethodName)
            throws DataBaseException {
        if (ecuserName == null || transfermethodName == null) {
            throw new DataBaseException("TransferECUser not found: {" + ecuserName + "," + transfermethodName + "}");
        }
        return get(new TransferECUser(ecuserName, transfermethodName));
    }

    /**
     * Gets the transfer ecuser object.
     *
     * @param ecuserName
     *            the ecuser name
     * @param transfermethodName
     *            the transfermethod name
     *
     * @return the transfer ecuser object
     */
    public TransferECUser getTransferECUserObject(final String ecuserName, final String transfermethodName) {
        try {
            return getTransferECUser(ecuserName, transfermethodName);
        } catch (final DataBaseException e) {
            return null;
        }
    }

    /**
     * Gets the transfer ecuser iterator.
     *
     * @return the transfer ecuser iterator
     */
    DBIterator<TransferECUser> getTransferECUserIterator() {
        return getAll(TransferECUser.class);
    }

    /**
     * Gets the url.
     *
     * @param name
     *            the name
     *
     * @return the url
     *
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     */
    public Url getUrl(final String name) throws DataBaseException {
        if (name == null) {
            throw new DataBaseException("Url not found: {(null)}");
        }
        return get(new Url(name));
    }

    /**
     * Gets the url object.
     *
     * @param name
     *            the name
     *
     * @return the url object
     */
    public Url getUrlObject(final String name) {
        try {
            return getUrl(name);
        } catch (final DataBaseException e) {
            return null;
        }
    }

    /**
     * Gets the url iterator.
     *
     * @return the url iterator
     */
    DBIterator<Url> getUrlIterator() {
        return getAll(Url.class);
    }

    /**
     * Gets the url array.
     *
     * @return the url array
     */
    public Url[] getUrlArray() {
        final var iterator = getUrlIterator();
        final List<Url> list = new ArrayList<>();
        while (iterator.hasNext()) {
            list.add(iterator.next());
        }
        Collections.sort(list, (url1, url2) -> url1.getName().compareToIgnoreCase(url2.getName()));
        logSqlRequest("getUrlArray", list.size());
        iterator.remove();
        return list.toArray(new Url[list.size()]);
    }

    /**
     * Gets the web user.
     *
     * @param id
     *            the id
     *
     * @return the web user
     *
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     */
    public WebUser getWebUser(final String id) throws DataBaseException {
        if (id == null) {
            throw new DataBaseException("WebUser not found: {(null)}");
        }
        return get(new WebUser(id));
    }

    /**
     * Gets the web user object.
     *
     * @param id
     *            the id
     *
     * @return the web user object
     */
    public WebUser getWebUserObject(final String id) {
        try {
            return getWebUser(id);
        } catch (final DataBaseException e) {
            return null;
        }
    }

    /**
     * Gets the web user iterator.
     *
     * @return the web user iterator
     */
    DBIterator<WebUser> getWebUserIterator() {
        return getAll(WebUser.class);
    }

    /**
     * Gets the web user array.
     *
     * @return the web user array
     */
    public WebUser[] getWebUserArray() {
        final var iterator = getWebUserIterator();
        final List<WebUser> list = new ArrayList<>();
        while (iterator.hasNext()) {
            list.add(iterator.next());
        }
        Collections.sort(list, (user1, user2) -> user1.getName().compareToIgnoreCase(user2.getName()));
        logSqlRequest("getWebUserArray", list.size());
        iterator.remove();
        return list.toArray(new WebUser[list.size()]);
    }

    /**
     * Gets the weu cat.
     *
     * @param categoryId
     *            the category id
     * @param webuserId
     *            the webuser id
     *
     * @return the weu cat
     *
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     */
    public WeuCat getWeuCat(final long categoryId, final String webuserId) throws DataBaseException {
        if (webuserId == null) {
            throw new DataBaseException("WeuCat not found: {" + categoryId + ",(null)}");
        }
        return get(new WeuCat(categoryId, webuserId));
    }

    /**
     * Gets the weu cat object.
     *
     * @param categoryId
     *            the category id
     * @param webuserId
     *            the webuser id
     *
     * @return the weu cat object
     */
    public WeuCat getWeuCatObject(final long categoryId, final String webuserId) {
        try {
            return getWeuCat(categoryId, webuserId);
        } catch (final DataBaseException e) {
            return null;
        }
    }

    /**
     * Gets the weu cat iterator.
     *
     * @return the weu cat iterator
     */
    DBIterator<WeuCat> getWeuCatIterator() {
        return getAll(WeuCat.class);
    }

    /**
     * Gets the weu cat array.
     *
     * @return the weu cat array
     */
    public WeuCat[] getWeuCatArray() {
        final var iterator = getWeuCatIterator();
        final List<WeuCat> list = new ArrayList<>();
        while (iterator.hasNext()) {
            list.add(iterator.next());
        }
        logSqlRequest("getWeuCatArray", list.size());
        iterator.remove();
        return list.toArray(new WeuCat[list.size()]);
    }
}
