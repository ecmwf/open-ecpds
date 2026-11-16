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

package ecmwf.ecpds.mover;

import ecmwf.common.technical.GenericFile;
import ecmwf.common.technical.GenericFileFilter;

/**
 * The Class DataFileFilter.
 */
final class DataFileFilter implements GenericFileFilter {
    /** The filePath. */
    private final String filePath;

    /**
     * Instantiates a new data file filter.
     *
     * @param file
     *            the file
     */
    DataFileFilter(final GenericFile file) {
        this.filePath = file.getAbsolutePath();
    }

    /**
     * Accept.
     *
     * @param dir
     *            the dir
     * @param name
     *            the name
     *
     * @return true, if successful
     */
    @Override
    public boolean accept(final GenericFile dir, final String name) {
        final var path = GenericFile.getGenericFile(dir, name).getAbsolutePath();
        return path.equals(filePath) || path.startsWith(filePath + ".");
    }
}
