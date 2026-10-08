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

package ecmwf.common.technical;

import java.io.FilterInputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.function.LongConsumer;

/** Counts observed stream bytes, including partial attempts, without disk or network I/O. */
public final class AccountingStreams {
    private AccountingStreams() {
    }

    public static InputStream input(InputStream stream, LongConsumer counter) {
        return new FilterInputStream(stream) {
            @Override
            public int read() throws IOException {
                int value = in.read();
                if (value >= 0)
                    counter.accept(1);
                return value;
            }

            @Override
            public int read(byte[] data, int offset, int length) throws IOException {
                int count = in.read(data, offset, length);
                if (count > 0)
                    counter.accept(count);
                return count;
            }
        };
    }

    public static OutputStream output(OutputStream stream, LongConsumer counter) {
        return new FilterOutputStream(stream) {
            @Override
            public void write(int value) throws IOException {
                out.write(value);
                counter.accept(1);
            }

            @Override
            public void write(byte[] data, int offset, int length) throws IOException {
                out.write(data, offset, length);
                counter.accept(length);
            }
        };
    }
}
