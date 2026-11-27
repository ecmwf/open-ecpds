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

/**
 * Created on Oct 15, 2003
 *
 */
package ecmwf.ecpds.master.plugin.http;

import java.util.ArrayList;
import java.util.List;

/**
 * The root of all evil.
 *
 * @author sy8
 */

public class ECMWFException extends Exception {

    public ECMWFException(String message, Throwable t) {
        super(message, t);
    }

    public ECMWFException(String message) {
        super(message);
    }

    /**
     * @return Messages from this exception and its entire cause chain.
     */
    public String[] getMessages() {
        final List<String> messages = new ArrayList<>();
        Throwable t = this;
        while (t != null) {
            messages.add(t.getMessage());
            t = t.getCause();
        }
        return messages.toArray(new String[0]);
    }

    /**
     * @return All the exceptions' messages appended into a single string.
     */
    public String getFullMessage() {
        final String[] messages = getMessages();
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < messages.length; i++) {
            sb.append("(").append(i).append(") ").append(messages[i] != null ? messages[i] : "null").append("; ");
        }
        return sb.toString();
    }
}
