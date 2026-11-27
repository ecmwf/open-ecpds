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

package ecmwf.ecpds.master.plugin.http.services.content;

/**
 * ContentProcessor.java
 *
 *
 * Created: Mon Jan 13 12:08:37 2003
 *
 * @author <a href="mailto: "Daniel Varela Santoalla</a>
 *
 * @version
 */

public interface ContentProcessor {

    /**
     * Return content based in a specific attribute of other content. This is necessary when the attribute IS NOT
     * Content, but String, int, etc.
     *
     * @param content
     *            The content to convert.
     * @param attribute
     *            The name of the attribute to convert.
     * @param options
     *            Options applying to this conversion
     *
     * @return The conversion, once applied the options.
     *
     */

    public Content process(Content original, String attribute, ContentProcessingOptions options)
            throws ContentProcessorException;

    /**
     * It allows the conversion of anything to anything, so that it could be used also to convert from and to binary
     * formats
     *
     * @param original
     *            The original content to convert.
     * @param options
     *            Options applying to this conversion
     *
     * @return Converted content.
     *
     * @throws ContentProcessorException
     *             If the content format is invalid or if the options are invalid.
     */

    public Content process(Content original, ContentProcessingOptions options) throws ContentProcessorException;

    /**
     * Get a outputContentType dependent representation of the Content structure TODO: See a way of returning the actual
     * structure, instead of a "cooked" one.
     *
     * @param content
     *            The original content for which we want to get the structure.
     * @param attribute
     *            From which attribute we get the body?
     * @param startLevel
     *            Ignore any levels (header levels, h1, h2, h3, etc) lower than this.
     * @param options
     *            Options applying to the way we are getting the information
     *
     * @return Content that will represent the structure of some other content
     *
     * @throws ContentProcessorException
     *             If the content format is invalid
     */
    public Content getStructure(Content content, String attribute, int startLevel, ContentProcessingOptions options)
            throws ContentProcessorException;

    /**
     * Ensure that some content is valid for this processor.
     *
     * @param content
     *            The content
     *
     * @throws ContentProcessorException
     *             If the content isn't valid.
     */

    public void validate(Content content, ContentProcessingOptions options) throws ContentProcessorException;

    public void validate(Content content, String attribute, ContentProcessingOptions options)
            throws ContentProcessorException;

    /**
     * Configure this instance of ContentProcessor with some options. There can be different instances of the same
     * processor, processing different content types. This methods allow to specifically configure different processors
     * with different parameters.
     *
     * @param options
     *            Options to apply to this processor.
     *
     * @throws ContentProcessorException
     *             If any option has an invalid value.
     */

    public void configure(ContentProcessorOptions options) throws ContentProcessorException;

    /**
     * Set only one option of the content processor instance.
     *
     * @param name
     *            The name of the option
     * @param value
     *            The value of the option
     *
     * @throws ContentProcessorException
     *             If the name or the value are invalid.
     *
     * @see configure
     */

    public void setProperty(String name, String value) throws ContentProcessorException;

} // ContentProcessor
