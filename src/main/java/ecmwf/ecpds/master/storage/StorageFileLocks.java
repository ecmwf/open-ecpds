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

package ecmwf.ecpds.master.storage;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/** Bounded locks shared by lifecycle evaluation, status changes and scheduler reservations. */
public final class StorageFileLocks {
    private static final ReentrantReadWriteLock[] LOCKS = new ReentrantReadWriteLock[4096];
    static {
        for (int i = 0; i < LOCKS.length; i++) {
            LOCKS[i] = new ReentrantReadWriteLock();
        }
    }

    private StorageFileLocks() {
    }

    private static ReentrantReadWriteLock lock(final long fileId) {
        return LOCKS[(Long.hashCode(fileId) & Integer.MAX_VALUE) % LOCKS.length];
    }

    public static Lease read(final long fileId) {
        final var read = lock(fileId).readLock();
        read.lock();
        return new Lease(read);
    }

    public static Lease tryWrite(final long fileId) {
        final var write = lock(fileId).writeLock();
        return write.tryLock() ? new Lease(write) : null;
    }

    public static final class Lease implements AutoCloseable {
        private final Lock lock;

        private Lease(final Lock lock) {
            this.lock = lock;
        }

        @Override
        public void close() {
            lock.unlock();
        }
    }
}
