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

/*
 * Created on 12-Oct-2006
 *
 */
package ecmwf.ecpds.master.plugin.http.home.navig;

import ecmwf.ecpds.master.plugin.http.dao.ModelSearchBase;

public class AllMenuItemsSearch extends ModelSearchBase {

    public AllMenuItemsSearch() {
        this.setCacheable(false);
    }

}
