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
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.io.Serializable;

/**
 * The Class DataBaseCursor.
 */
public class DataBaseCursor implements Serializable {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = -3162345288699949039L;

    /** The sort. */
    private final String sort;

    /** The order. */
    private final String order;

    /** The start. */
    private final int start;

    /** The end. */
    private final int end;

    /**
     * Instantiates a new data base cursor.
     *
     * @param sort
     *            the sort
     * @param order
     *            the order
     * @param start
     *            the start
     * @param end
     *            the end
     */
    public DataBaseCursor(final String sort, final String order, final int start, final int end) {
        this.sort = sort;
        this.order = order;
        this.start = start;
        this.end = end;
    }

    /**
     * Gets the start.
     *
     * @return the start
     */
    public int getStart() {
        return start;
    }

    /**
     * Gets the end.
     *
     * @return the end
     */
    public int getEnd() {
        return end;
    }

    /**
     * Gets the length.
     *
     * @return the length
     */
    public int getLength() {
        return end - start;
    }

    /**
     * Gets the sort.
     *
     * @return the sort
     */
    public String getSort() {
        return sort;
    }

    /**
     * Gets the order.
     *
     * @return the order
     */
    public String getOrder() {
        return order;
    }

    /**
     * {@inheritDoc}
     *
     * To string.
     */
    @Override
    public String toString() {
        return "sort=" + sort + ",order=" + order + ",start=" + start + ",end=" + end;
    }
}
