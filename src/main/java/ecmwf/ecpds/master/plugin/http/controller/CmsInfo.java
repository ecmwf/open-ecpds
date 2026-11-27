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
 * Created on 10 Oct 2008
 *
 */
package ecmwf.ecpds.master.plugin.http.controller;

public class CmsInfo {

    public static final int MAJOR_VERSION = 1;
    public static final int MINOR_VERSION = 2;
    public static final int PATCHLEVEL = 2;
    public static final String DATE = "2010-01-29";

    public static String getCmsInfo() {
        return "WebCMS v" + MAJOR_VERSION + "." + MINOR_VERSION + "." + PATCHLEVEL + " (" + DATE + ")";
    }

    public static void main(String[] args) {
        System.out.println(getCmsInfo());
    }
}
