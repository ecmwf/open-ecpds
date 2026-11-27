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
 * Created on 17-Dec-2004
 *
 */
package ecmwf.ecpds.master.plugin.http.services.content;

import java.util.Collection;
import java.util.Map;

/**
 * @author sy8
 *
 *         TODO: Comment ME !!!
 */
public interface Command {

    public String getName();

    public String getDescription();

    public Collection getParameters();
    // public String getOption(String name) throws ContentProcessorException;

    /**
     * Set system wide option. Will only be set at startup time
     *
     * @param name
     *            Name of the option
     * @param value
     *            Value for it.
     */

    // public void setOption(String name, String value);
    // public void setOptions(ContentProcessorOptions options) throws ContentProcessorException;

    /**
     * Execute command and get content as an string
     *
     * @param container
     *            The parent element where this command was inserted
     * @param params
     *            Runtime parameters, set for every specificc invocation of the command
     * @param body
     *            If the command has a body, it will be supplied here
     * @param options
     *            Processing options that will be passed from the parent context
     *
     * @return Content as a String
     *
     * @throws ContentProcessorException
     *             If anything goes really wrong with the translation. Normally syntax problems, etc, will just display
     *             the error as result content.
     *
     */

    public String getOutput(Content container, Map params, String body, ContentProcessingOptions options)
            throws ContentProcessorException;

}
