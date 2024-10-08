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
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import ecmwf.common.technical.Reflection;

/**
 * The Class ECtransInvoke.
 */
public final class ECtransInvoke extends ECtransAction {
    /** The _obj. */
    private Object _obj = null;

    /** The _method. */
    private final String _method;

    /** The _arg types. */
    private final Class<?>[] _argTypes;

    /** The _args. */
    private final Object[] _args;

    /**
     * Instantiates a new ectrans invoke.
     *
     * @param method
     *            the method
     * @param argTypes
     *            the arg types
     * @param args
     *            the args
     */
    public ECtransInvoke(final String method, final Class<?>[] argTypes, final Object[] args) {
        _method = method;
        _argTypes = argTypes;
        _args = args;
    }

    /**
     * {@inheritDoc}
     *
     * Gets the name.
     */
    @Override
    protected String getName() {
        return "invoke";
    }

    /**
     * {@inheritDoc}
     *
     * Exec.
     */
    @Override
    protected void exec(final TransferModule module, final boolean interruptible) throws Exception {
        _obj = Reflection.execute(module, _method, _argTypes, _args);
    }

    /**
     * Gets the object.
     *
     * @return the object
     */
    public Object getObject() {
        return _obj;
    }
}
