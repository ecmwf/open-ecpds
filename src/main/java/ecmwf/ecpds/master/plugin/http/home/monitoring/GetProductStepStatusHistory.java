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

package ecmwf.ecpds.master.plugin.http.home.monitoring;

/**
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * @author Daniel Varela Santoalla <sy8@ecmwf.int>, ECMWF.
 * @version 6.7.7
 * @since 2004-10-09
 */

import ecmwf.web.dao.ModelSearchBase;

/**
 * The Class GetProductStepStatusHistory.
 */
public class GetProductStepStatusHistory extends ModelSearchBase {

    /** The product. */
    private final String product;

    /** The time. */
    private final String time;

    /** The step. */
    private final long step;

    /** The type. */
    private final String type;

    /** The limit. */
    private int limit = 21;

    /**
     * Instantiates a new gets the product step status history.
     *
     * @param product
     *            the product
     * @param time
     *            the time
     * @param step
     *            the step
     * @param type
     *            the type
     */
    public GetProductStepStatusHistory(final String product, final String time, final long step, final String type) {
        this.product = product;
        this.time = time;
        this.step = step;
        this.type = type;
        this.setCacheable(false);
    }

    /**
     * Gets the product.
     *
     * @return the product
     */
    public String getProduct() {
        return this.product;
    }

    /**
     * Gets the time.
     *
     * @return the time
     */
    public String getTime() {
        return this.time;
    }

    /**
     * Gets the step.
     *
     * @return the step
     */
    public long getStep() {
        return this.step;
    }

    /**
     * Gets the type.
     *
     * @return the type
     */
    public String getType() {
        return this.type;
    }

    /**
     * Gets the limit.
     *
     * @return the limit
     */
    public int getLimit() {
        return this.limit;
    }

    /**
     * Sets the limit.
     *
     * @param i
     *            the new limit
     */
    public void setLimit(final int i) {
        this.limit = i;
    }
}
