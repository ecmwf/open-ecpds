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

package ecmwf.ecpds.master.plugin.http.util.bean;

import ecmwf.ecpds.master.plugin.http.home.news.NewsItemHome;
import ecmwf.ecpds.master.plugin.http.model.news.NewsChannel;
import ecmwf.ecpds.master.plugin.http.model.news.NewsException;
import ecmwf.ecpds.master.plugin.http.model.news.NewsItem;
import ecmwf.ecpds.master.plugin.http.services.content.Content;

public class NewsItemUtil {
    public static NewsItem lookupItem(String itemName, Content container) throws NewsException {
        if (itemName.indexOf(NewsChannel.CHANNEL_QUALIFIER) > 0) {
            return NewsItemHome.findByQualifiedName(itemName);
        } else if (container instanceof NewsItem) {
            return NewsItemHome.findByNameAndChannelId(itemName, ((NewsItem) container).getChannelId());
        } else if (container instanceof NewsChannel) {
            NewsChannel channel = (NewsChannel) container;
            return NewsItemHome.findByNameAndChannelId(itemName, channel.getId());
        } else {
            throw new NewsException(
                    "Command needs either a qualified 'name' or to be called from a NewsItem or NewsChannel");
        }
    }
}
