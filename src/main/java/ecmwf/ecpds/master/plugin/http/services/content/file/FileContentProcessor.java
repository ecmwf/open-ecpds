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

package ecmwf.ecpds.master.plugin.http.services.content.file;

import ecmwf.ecpds.master.plugin.http.services.content.Content;
import ecmwf.ecpds.master.plugin.http.services.content.ContentProcessingOptions;
import ecmwf.ecpds.master.plugin.http.services.content.ContentProcessor;
import ecmwf.ecpds.master.plugin.http.services.content.ContentProcessorBase;
import ecmwf.ecpds.master.plugin.http.services.content.ContentProcessorException;

/**
 * @author sy8
 *
 *         To change this generated comment edit the template variable "typecomment": Window>Preferences>Java>Templates.
 *         To enable and disable the creation of type comments go to Window>Preferences>Java>Code Generation.
 */

public class FileContentProcessor extends ContentProcessorBase implements ContentProcessor {

    // private static Log log = LogFactory.getLog(FileContentProcessor.class);

    public Content process(Content content, String attribute, ContentProcessingOptions options)
            throws ContentProcessorException {

        return this.process(content, options);

    }

    public Content process(Content content, ContentProcessingOptions options) throws ContentProcessorException {

        return getIconContent(content, options);

    }

    public void validate(Content origin, ContentProcessingOptions options) throws ContentProcessorException {
        throw new ContentProcessorException("validate(Content, ContentProcessingOptions) hasn't been implemented by '"
                + this.getClass().getName() + "'");
    }

    public void validate(Content origin, String attribute, ContentProcessingOptions options)
            throws ContentProcessorException {
        throw new ContentProcessorException(
                "validate(Content, String, ContentProcessingOptions) hasn't been implemented by '"
                        + this.getClass().getName() + "'");
    }

}
