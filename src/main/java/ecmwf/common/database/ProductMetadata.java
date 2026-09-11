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
 * Per-product (optionally per-type) metadata: a free-text description (used as the {@code {{DESCRIPTION}}}
 * placeholder in Product Status Messages) and a "Tips" text (shown on the product monitoring page). Stored in the
 * {@code PRODUCT_METADATA} table, keyed by (product, type). An empty {@code type} ({@code ""}) denotes a generic
 * entry that applies to all types of that product, used as a fallback when no type-specific entry exists.
 */

import java.io.Serializable;

/**
 * The Class ProductMetadata.
 */
public final class ProductMetadata implements Serializable {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The product name (e.g. "GOPER"). */
    private final String product;

    /** The product type (e.g. "AN"), or an empty string for a generic, all-types entry. */
    private final String type;

    /** The description text. */
    private final String description;

    /** The tips text. */
    private final String tips;

    /**
     * Instantiates a new product metadata.
     *
     * @param product
     *            the product
     * @param type
     *            the type
     * @param description
     *            the description
     * @param tips
     *            the tips
     */
    public ProductMetadata(final String product, final String type, final String description, final String tips) {
        this.product = product;
        this.type = type != null ? type : "";
        this.description = description;
        this.tips = tips;
    }

    /**
     * Gets the product.
     *
     * @return the product
     */
    public String getProduct() {
        return product;
    }

    /**
     * Gets the type.
     *
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * Gets the description.
     *
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Gets the tips.
     *
     * @return the tips
     */
    public String getTips() {
        return tips;
    }

    /**
     * Checks if this is a generic (all-types) entry, i.e. the type is empty.
     *
     * @return true, if is generic
     */
    public boolean isGeneric() {
        return type.isEmpty();
    }
}
