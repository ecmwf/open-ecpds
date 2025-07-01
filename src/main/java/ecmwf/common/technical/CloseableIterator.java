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

package ecmwf.common.technical;

/**
 * ECMWF Product Data Store (OpenECPDS) Project.
 *
 * @author Laurent Gougeon <syi@ecmwf.int>, ECMWF.
 * @version 6.8.6
 * @since 2025-06-06
 */

import java.util.Iterator;

/**
 * An iterator that must be explicitly closed to release resources, such as open database cursors or file handles.
 *
 * @param <T>
 *            the type of elements returned by this iterator
 */
public interface CloseableIterator<T> extends Iterator<T>, AutoCloseable {

    /**
     * Returns an empty CloseableIterator.
     *
     * @param <T>
     *            the element type
     *
     * @return a CloseableIterator with no elements
     */
    static <T> CloseableIterator<T> empty() {
        return new CloseableIterator<>() {
            @Override
            public boolean hasNext() {
                return false;
            }

            @Override
            public T next() {
                throw new java.util.NoSuchElementException("No elements");
            }
        };
    }

    @Override
    default void close() {
        // Default no-op
    }
}
