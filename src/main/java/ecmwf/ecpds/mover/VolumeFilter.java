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
 * The Class VolumeFilter.
 */
final class VolumeFilter implements GenericFileFilter {
    /** The volumeIndexMax. */
    private final int volumeIndexMax;

    /**
     * Instantiates a new data file filter.
     *
     * @param file
     *            the file
     */
    VolumeFilter(final int volumeIndexMax) {
        this.volumeIndexMax = volumeIndexMax;
    }

    // Check if the format is as expected for a volume
    private boolean endsWithVolumeIndex(final String filename) {
        if (filename == null)
            return false;
        final var prefix = "volume";
        if (!filename.startsWith(prefix))
            return false;
        final var numberPart = filename.substring(prefix.length());
        if (!numberPart.matches("\\d+"))
            return false;
        final var value = Integer.parseInt(numberPart);
        return value >= 0 && value <= volumeIndexMax;
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
        return endsWithVolumeIndex(name);
    }
}
