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
 * ECMWF Product Data Store (OpenPDS) Project.
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 *
 * @version 6.7.7
 *
 * @since 2024-07-01
 */

public final class ECtransGetAttribute extends ECtransAction {
    /** The _key. */
    private final Object _key;

    /** The _value. */
    private Object _value = null;

    /**
     * Instantiates a new ectrans get attribute.
     *
     * @param key
     *            the key
     */
    public ECtransGetAttribute(final Object key) {
        _key = key;
    }

    /**
     * Gets the name.
     *
     * @return the name
     */
    @Override
    protected String getName() {
        return "getAttribute";
    }

    /**
     * Gets the value.
     *
     * @return the value
     */
    public Object getValue() {
        return _value;
    }

    /**
     * Exec.
     *
     * @param module
     *            the module
     * @param interruptible
     *            the interruptible
     *
     * @throws Exception
     *             the exception
     */
    @Override
    protected void exec(final TransferModule module, final boolean interruptible) throws Exception {
        _value = module.getAttribute(_key);
        getECtransHistory().setComment(_key + "=" + _value);
    }
}
