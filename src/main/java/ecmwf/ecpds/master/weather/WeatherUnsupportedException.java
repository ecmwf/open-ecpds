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

package ecmwf.ecpds.master.weather;

/** Explicit mixed-version incompatibility, distinct from authentication or connectivity failures. */
public final class WeatherUnsupportedException extends Exception {
    private static final long serialVersionUID = 1L;

    public WeatherUnsupportedException(String message) {
        super(message);
    }

    public static boolean isUnsupportedRmiMethod(Throwable failure) {
        for (int depth = 0; failure != null && depth < 8; depth++, failure = failure.getCause()) {
            if (failure instanceof java.rmi.UnmarshalException && failure.getMessage() != null && failure.getMessage()
                    .startsWith("unrecognized method hash: method not supported by remote object")) {
                return true;
            }
        }
        return false;
    }

    public static boolean isUnsupported(Throwable failure) {
        if (isUnsupportedRmiMethod(failure)) {
            return true;
        }
        for (int depth = 0; failure != null && depth < 8; depth++, failure = failure.getCause()) {
            if (failure instanceof WeatherUnsupportedException
                    || failure instanceof WeatherSourceIdentity.UnsupportedIdentityException) {
                return true;
            }
        }
        return false;
    }
}
