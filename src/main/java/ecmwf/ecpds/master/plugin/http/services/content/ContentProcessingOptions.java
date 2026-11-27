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
 * Options passed to a <code>ContentProcessor</code> that only apply to one execution. This options lifespan is only one
 * conversion request.
 *
 * Created: Tue Jan 14 14:34:16 2003
 *
 * @author <a href="mailto:dvarela@ecmwf.int">Daniel Varela Santoalla</a>
 *
 * @version 0.1.0
 */

public class ContentProcessingOptions {

    public static final int SIZE_THUMBNAIL = 0;
    public static final int SIZE_FULL = 1;

    public static final int SHOW_EMBEDDED = 2;
    public static final int SHOW_ASLINK = 3;

    // public static final ContentProcessingOptions DEFAULT = new ContentProcessingOptions(SIZE_THUMBNAIL, SHOW_ASLINK);
    // public static final ContentProcessingOptions DEFAULT_EMBEDDED = new ContentProcessingOptions(SIZE_FULL,
    // SHOW_EMBEDDED);

    private int size;
    private int maxLines = Integer.MAX_VALUE;
    private int disposition;
    private int imageWidth = -1;
    private int imageHSpace = -1;
    private int imageVSpace = -1;

    private String outputContentType;
    private String processingContext = "cms";
    private Object externalContext = null; // Here we can store a JSP PageContext or SOAP context or whatever.
                                           // ContentProcessingOptions doesn't want to know. Really.
    private String contextLabel = null;

    private boolean cacheable = true;

    protected ContentProcessingOptions() {
        this.size = SIZE_THUMBNAIL;
        this.disposition = SHOW_ASLINK;
        this.outputContentType = "text/html";
    }

    protected ContentProcessingOptions(int size, int disposition) {
        this.size = size;
        this.disposition = disposition;
        this.outputContentType = "text/html";
    }

    public static ContentProcessingOptions getDefaultEmbedded() {
        return new ContentProcessingOptions(SIZE_FULL, SHOW_EMBEDDED);
    }

    public static ContentProcessingOptions getDefault() {
        return new ContentProcessingOptions(SIZE_THUMBNAIL, SHOW_ASLINK);
    }

    public int getSize() {
        return this.size;
    }

    public void setSize(int v) {
        this.size = v;
    }

    public int getDisposition() {
        return this.disposition;
    }

    public void setDisposition(int v) {
        this.disposition = v;
    }

    public void setMaxLines(int lines) {
        this.maxLines = lines;
    }

    public int getMaxLines() {
        return this.maxLines;
    }

    public String getOutputContentType() {
        return this.outputContentType;
    }

    public void setOutputContentType(String v) {
        this.outputContentType = v;
    }

    public String getKey() {
        return this.getOutputContentType() + ":" + this.getSize() + this.getDisposition() + ":" + maxLines + ":"
                + this.getProcessingContext() + ":image:" + this.imageWidth + ":" + this.imageHSpace + ":"
                + this.imageVSpace;
    }

    public boolean isCacheable() {
        return cacheable;
    }

    public void setCacheable(boolean b) {
        cacheable = b;
    }

    public String getProcessingContext() {
        return processingContext;
    }

    public void setProcessingContext(String string) {
        processingContext = string;
    }

    public Object getExternalContext() {
        return this.externalContext;
    }

    public void setExternalContext(Object o) {
        this.externalContext = o;
    }

    public String getContextLabel() {
        return contextLabel;
    }

    public void setContextLabel(String contextLabel) {
        this.contextLabel = contextLabel;
    }

    public int getImageHSpace() {
        return imageHSpace;
    }

    public void setImageHSpace(int imageHSpace) {
        this.imageHSpace = imageHSpace;
    }

    public int getImageVSpace() {
        return imageVSpace;
    }

    public void setImageVSpace(int imageVSpace) {
        this.imageVSpace = imageVSpace;
    }

    public int getImageWidth() {
        return imageWidth;
    }

    public void setImageWidth(int imageWidth) {
        this.imageWidth = imageWidth;
    }

} // ContentProcessingOptions
