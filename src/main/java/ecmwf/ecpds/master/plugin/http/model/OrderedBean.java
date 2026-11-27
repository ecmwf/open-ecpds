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
 * Created on Feb 10, 2004
 *
 */
package ecmwf.ecpds.master.plugin.http.model;

import java.util.Collection;

/**
 * This interface allows to get "prior" and "next" beans to a given one.
 *
 * @author sy8
 */
public interface OrderedBean {

    /**
     * This is also in ModelBean, but it will save some castings...
     */
    public String getId();

    /**
     * @return The bean after this in some default list.
     *
     * @throws ModelException
     */
    public OrderedBean getNext() throws ModelException;

    /**
     * @return The bean after this in some default list.
     *
     * @throws ModelException
     */
    public OrderedBean getPrior() throws ModelException;

    /**
     * @param search
     *
     * @return The bean after this in the list result of this search.
     *
     * @throws ModelException
     */
    public OrderedBean getNextWithin(ModelSearch search) throws ModelException;

    /**
     * @param search
     *
     * @return The bean before this in the list result of this search.
     *
     * @throws ModelException
     */
    public OrderedBean getPriorWithin(ModelSearch search) throws ModelException;

    /**
     * @param c
     *            A list of NewsItems
     *
     * @return The bean after this in the supplied list
     *
     * @throws ModelException
     */
    public OrderedBean getNextWithin(Collection c) throws ModelException;

    /**
     * @param c
     *            A list of NewsItems
     *
     * @return The bean before this in the supplied list
     *
     * @throws ModelException
     */
    public OrderedBean getPriorWithin(Collection c) throws ModelException;

    /**
     * @param c
     *            A list of ModelSortItems
     *
     * @return The bean after this in the default list, sorted as specified
     *
     * @throws ModelException
     */
    public OrderedBean getNextWithinSort(Collection c) throws ModelException;

    /**
     * @param c
     *            A list of ModelSortItems
     *
     * @return The bean before this in the default list, sorted as specified
     *
     * @throws ModelException
     */
    public OrderedBean getPriorWithinSort(Collection c) throws ModelException;

}
