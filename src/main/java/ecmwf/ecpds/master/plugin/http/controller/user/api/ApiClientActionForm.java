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

package ecmwf.ecpds.master.plugin.http.controller.user.api;

import java.util.Collection;

import ecmwf.ecpds.master.plugin.http.home.transfer.CountryHome;
import ecmwf.ecpds.master.plugin.http.model.transfer.Country;
import ecmwf.web.controller.ECMWFActionForm;

public class ApiClientActionForm extends ECMWFActionForm {
    private static final long serialVersionUID = 1L;

    private String id = "";
    private String comment = "";
    private String active = "true";
    private String countryIso = "";

    public String getId() {
        return id;
    }

    public void setId(final String v) {
        id = v;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(final String v) {
        comment = v;
    }

    public String getActive() {
        return active;
    }

    public void setActive(final String v) {
        active = v;
    }

    public String getCountryIso() {
        return countryIso;
    }

    public void setCountryIso(final String v) {
        countryIso = v != null ? v : "";
    }

    public Collection<Country> getCountryOptions() {
        try {
            return CountryHome.findAll();
        } catch (final Exception e) {
            return java.util.Collections.emptyList();
        }
    }
}
