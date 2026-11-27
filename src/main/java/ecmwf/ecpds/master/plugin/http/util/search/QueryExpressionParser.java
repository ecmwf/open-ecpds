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

package ecmwf.ecpds.master.plugin.http.util.search;

/**
 * Convert a search query, by parsing, into a <code>BooleanExpression</code>.
 *
 * Created: Mon Jul 22 14:56:51 2002
 *
 * @author <a href="mailto:dvarela@ecmwf.int">Daniel Varela Santoalla</a>
 * @version 1.0
 */

import java.io.ByteArrayInputStream;
import ecmwf.ecpds.master.plugin.http.util.search.parser.ExpressionParser;
import ecmwf.ecpds.master.plugin.http.util.search.parser.ParseException;
import ecmwf.ecpds.master.plugin.http.util.search.parser.TokenMgrError;
import ecmwf.ecpds.master.plugin.http.util.search.elements.BooleanExpression;

public class QueryExpressionParser {

    public static BooleanExpression createExpression(String query) throws BooleanExpressionException {

        try {
            // Not using a StringBufferInputStream because it is deprecated. The recommended StringReader has
            // no way to get a InputStream....????
            // An empty string would get a "null" expression.
            BooleanExpression expr = ExpressionParser.getBooleanExpression(new ByteArrayInputStream(query.getBytes()));
            if (expr == null)
                throw new BooleanExpressionException(
                        "Error parsing search string (Parser): Empty expression yields null BooleanExpression");
            else
                return expr;
        } catch (ParseException e) {
            throw new BooleanExpressionException("Error parsing search string (Parser)", e);
        } catch (TokenMgrError e) {
            throw new BooleanExpressionException("Error parsing search string (TokenManager)", e);
        }

    }

} // QueryExpressionParser
