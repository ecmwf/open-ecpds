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

package ecmwf.common.ectrans;

/**
 * ECMWF Product Data Store (OpenECPDS) Project.
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 *
 * @version 6.7.7
 *
 * @since 2024-07-01
 */
public final class ECtransSize extends ECtransAction {
    /** The _name. */
    private final String _name;

    /** The _size. */
    private long _size = -1;

    /**
     * Instantiates a new ectrans size.
     *
     * @param name
     *            the name
     */
    public ECtransSize(final String name) {
        _name = name;
    }

    /**
     * {@inheritDoc}
     *
     * Gets the name.
     */
    @Override
    protected String getName() {
        return "size";
    }

    /**
     * {@inheritDoc}
     *
     * Exec.
     */
    @Override
    protected void exec(final TransferModule module, final boolean interruptible) throws Exception {
        _size = module.size(_name);
    }

    /**
     * Gets the size.
     *
     * @return the size
     */
    public long getSize() {
        return _size;
    }
}
