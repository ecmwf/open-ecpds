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
package ecmwf.common.database;

import java.sql.Types;

import org.hibernate.Session;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.type.CustomType;
import org.hibernate.usertype.UserTypeSupport;
import org.hibernate.usertype.UserVersionType;

/** Revision zero preserves existing nonblocking writes; positive revisions are fenced. */
public final class TransferNotificationRevisionType extends UserTypeSupport<Long> implements UserVersionType<Long> {
    public TransferNotificationRevisionType() {
        super(Long.class, Types.BIGINT);
    }

    @Override
    public Long seed(final SharedSessionContractImplementor session) {
        return 0L;
    }

    @Override
    public Long next(final Long current, final SharedSessionContractImplementor session) {
        if (current < 0) {
            throw new IllegalStateException("Invalid notification transfer revision");
        }
        return current == 0 ? 0L : Math.addExact(current, 1);
    }

    @Override
    public int compare(final Long left, final Long right) {
        return Long.compare(left, right);
    }

    static boolean isMapped(final Session session) {
        final var mapping = session.getSessionFactory().unwrap(SessionFactoryImplementor.class).getMappingMetamodel()
                .getEntityDescriptor(DataTransfer.class);
        return mapping.isVersioned() && "DAT_NOTIFICATION_REVISION".equals(mapping.getVersionColumnName())
                && "notificationRevision".equals(mapping.getPropertyNames()[mapping.getVersionProperty()])
                && mapping.getVersionType() instanceof CustomType<?> type
                && type.getUserType() instanceof TransferNotificationRevisionType;
    }
}
