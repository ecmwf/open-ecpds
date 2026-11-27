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

package ecmwf.ecpds.master.plugin.http.services.content.util;

import org.apache.oro.text.regex.MalformedPatternException;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.PatternCompiler;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;
import org.apache.oro.text.regex.Perl5Substitution;
import org.apache.oro.text.regex.Util;

import ecmwf.ecpds.master.plugin.http.services.config.ConfigException;
import ecmwf.ecpds.master.plugin.http.services.config.ConfigService;
import ecmwf.ecpds.master.plugin.http.services.content.Content;
import ecmwf.ecpds.master.plugin.http.services.content.ContentProcessorException;

public class WikiWordSubstitution {
    public static String parseWikiWords(String original, Content content) throws ContentProcessorException {
        try {
            if (ConfigService.getOptionalParameter("content", "wikiWord.parsing", "false").equals("true")) {
                PatternCompiler compiler = new Perl5Compiler();
                Pattern patternWiki = compiler.compile(ConfigService.getParameter("content", "wikiWordMatch.regexp"),
                        Perl5Compiler.READ_ONLY_MASK);
                Pattern patternUnWiki = compiler.compile(
                        ConfigService.getParameter("content", "wikiWordUnMatch.regexp"), Perl5Compiler.READ_ONLY_MASK);
                String basePath = content.getBasePathKey() + content.getWikiWordPath();
                String tmp = Util.substitute(new Perl5Matcher(), patternWiki,
                        new Perl5Substitution("$1<a class=\"wiki\" href=\"" + basePath + "$2\">$2</a>"), original,
                        Util.SUBSTITUTE_ALL);
                return Util.substitute(new Perl5Matcher(), patternUnWiki, new Perl5Substitution("$1"), tmp,
                        Util.SUBSTITUTE_ALL);
            }
            return original;
        } catch (MalformedPatternException e) {
            throw new ContentProcessorException("Problem compiling WikiWord regexp pattern", e);
        } catch (ConfigException e) {
            throw new ContentProcessorException("Problem configuring WikiWord regexp pattern", e);
        }
    }
}
