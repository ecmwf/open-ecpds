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

package ecmwf.common.ectrans;

/**
 * A configuration-derived hint for locating a remote service geographically. The Master resolves provider regions to
 * approximate coordinates and may use the lookup host with its GeoIP database when the region is not recognised.
 *
 * @param provider
 *            the provider identifier, such as {@code aws}, {@code gcs}, or {@code azure}
 * @param region
 *            the configured provider region or bucket location
 * @param lookupHost
 *            an optional region-specific hostname for GeoIP fallback
 */
public record GeoLocationHint(String provider, String region, String lookupHost) {
}
