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

package ecmwf.ecpds.master.plugin.http.services.content.images;

import ecmwf.ecpds.master.plugin.http.services.config.ConfigService;
import ecmwf.ecpds.master.plugin.http.services.content.Content;
import ecmwf.ecpds.master.plugin.http.services.content.ContentProcessingOptions;
import ecmwf.ecpds.master.plugin.http.services.content.ContentProcessor;
import ecmwf.ecpds.master.plugin.http.services.content.ContentProcessorBase;
import ecmwf.ecpds.master.plugin.http.services.content.ContentProcessorException;

public class ImageContentProcessor extends ContentProcessorBase implements ContentProcessor {

    private static final int DEFAULT_WIDTH = 36;
    private static final int DEFAULT_VSPACE = 5;
    private static final int DEFAULT_HSPACE = 10;

    public Content process(Content content, String attribute, ContentProcessingOptions options)
            throws ContentProcessorException {
        return this.process(content, options);
    }

    public Content process(Content content, ContentProcessingOptions options) throws ContentProcessorException {

        int width = DEFAULT_WIDTH, vspace = DEFAULT_VSPACE, hspace = DEFAULT_HSPACE;

        if (options.getImageWidth() > 0)
            width = options.getImageWidth();
        if (options.getImageHSpace() > 0)
            width = options.getImageHSpace();
        if (options.getImageVSpace() > 0)
            width = options.getImageVSpace();

        if ("text/html".equals(options.getOutputContentType())) {
            String size = "";
            if (options.getSize() == ContentProcessingOptions.SIZE_THUMBNAIL)
                size = "width=\"" + width + "\"";

            String align = ConfigService.getOptionalParameter("content", "cms.image.align", "false");
            if (!"false".equals(align))
                align = " align=\"" + align + "\"";
            else
                align = "";

            // TODO: Instead of content.getBasePathKey() we should have its value as a key from the ECMWFResources file
            String output = "<img vspace=\"" + vspace + "\" hspace=\"" + hspace + "\" src=\"" + content.getBasePathKey()
                    + content.getAccessPath() + "\" border=\"0\" alt=\"Image " + content.getName() + "\" title=\""
                    + content.getTitle() + "\" " + size + align + ">";
            return createContent(output);
        } else {
            return getIconContent(content, options);
        }

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
