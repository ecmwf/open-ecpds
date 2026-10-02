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

package ecmwf.common.text;

/**
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2026-09-30
 */

import java.util.List;

import org.commonmark.Extension;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

/**
 * The Class MarkdownUtil.
 *
 * Converts Markdown source to a restricted, safe HTML fragment - used for the {@code markdown} Destination metadata
 * field type, both for its live edit-time preview and for its rendering into the Opsview notes export
 * ({@code ExportDestinationMetaNotesAction}), so the two are always byte-for-byte identical. CommonMark is parsed and
 * rendered to HTML first, then the result is run through a jsoup allow-list which strips anything not on it (including
 * all attributes other than the ones explicitly added), rather than trying to sanitise Markdown syntax itself - this
 * way the output stays predictable regardless of what the source contains.
 */
public final class MarkdownUtil {

    /** The extensions enabled on top of core CommonMark (currently just GFM tables). */
    private static final List<Extension> EXTENSIONS = List.of(TablesExtension.create());

    /** The parser. */
    private static final Parser PARSER = Parser.builder().extensions(EXTENSIONS).build();

    /** The renderer. */
    private static final HtmlRenderer RENDERER = HtmlRenderer.builder().extensions(EXTENSIONS).build();

    /**
     * The tags/attributes allowed through to the final output. Kept deliberately small and explicit rather than using
     * one of jsoup's built-in lists (e.g. {@code Safelist.basic()}, which excludes tables) - every tag here is one the
     * plan for this feature specifically asked for.
     */
    private static final Safelist SAFELIST = new Safelist()
            .addTags("p", "strong", "em", "h1", "h2", "h3", "ul", "ol", "li", "br", "code", "pre", "a", "table",
                    "thead", "tbody", "tr", "th", "td")
            .addAttributes("a", "href").addProtocols("a", "href", "http", "https", "mailto");

    /**
     * Instantiates a new markdown util.
     */
    private MarkdownUtil() {
        // Utility class.
    }

    /**
     * To safe html.
     *
     * @param markdown
     *            the markdown source, possibly {@code null}
     *
     * @return the rendered, allow-listed HTML (empty string if {@code markdown} is {@code null}/blank)
     */
    public static String toSafeHtml(final String markdown) {
        if (markdown == null || markdown.isBlank()) {
            return "";
        }
        final var document = PARSER.parse(markdown);
        final var html = RENDERER.render(document);
        final var cleaned = Jsoup.clean(html, "", SAFELIST);
        // Force every link to open in a new tab/page rather than navigating away from the current one - done as a
        // post-processing pass (rather than relying on whatever the source produced) so it applies uniformly
        // regardless of whether the link came from Markdown link syntax or raw inline HTML in the source, and
        // "noopener noreferrer" is the standard safeguard for a target="_blank" link opened from content we rendered.
        final var fragment = Jsoup.parseBodyFragment(cleaned);
        fragment.body().select("a[href]").attr("target", "_blank").attr("rel", "noopener noreferrer");
        return fragment.body().html();
    }
}
