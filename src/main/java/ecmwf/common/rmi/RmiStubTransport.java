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
 */

package ecmwf.common.rmi;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.OutputStream;
import java.lang.reflect.Proxy;
import java.rmi.server.RemoteObject;
import java.rmi.server.RemoteObjectInvocationHandler;
import java.rmi.server.RemoteRef;

import javax.rmi.ssl.SslRMIClientSocketFactory;

/** Diagnostic for the socket factory carried by a standard JDK JRMP stub, not a peer-authentication check. */
public final class RmiStubTransport {
    public enum Kind {
        MUTUAL_TLS_FACTORY, TLS_FACTORY, NON_TLS_FACTORY, UNKNOWN
    }

    private RmiStubTransport() {
    }

    public static Kind inspect(final Object stub) {
        final RemoteRef ref;
        if (stub != null && Proxy.isProxyClass(stub.getClass())
                && Proxy.getInvocationHandler(stub) instanceof final RemoteObjectInvocationHandler handler
                && handler.getClass() == RemoteObjectInvocationHandler.class) {
            ref = handler.getRef();
        } else if (stub instanceof final java.rmi.server.RemoteStub legacy) {
            ref = legacy.getRef();
        } else {
            return Kind.UNKNOWN;
        }
        if (ref == null || ref.getClass().getModule() != RemoteObject.class.getModule()) {
            return Kind.UNKNOWN;
        }
        return switch (ref.getClass().getName()) {
        case "sun.rmi.server.UnicastRef" -> Kind.NON_TLS_FACTORY;
        case "sun.rmi.server.UnicastRef2" -> inspectFactory(ref);
        default -> Kind.UNKNOWN;
        };
    }

    private static Kind inspectFactory(final RemoteRef ref) {
        try (final var capture = new FactoryCapture()) {
            // JDK JRMP writes the endpoint's client factory as an object. Intercept it without serializing its fields.
            ref.writeExternal(capture);
            return capture.kind;
        } catch (final IOException | RuntimeException _) {
            return Kind.UNKNOWN;
        }
    }

    private static final class FactoryCapture extends DataOutputStream implements ObjectOutput {
        private Kind kind = Kind.NON_TLS_FACTORY;

        FactoryCapture() {
            super(OutputStream.nullOutputStream());
        }

        @Override
        public void writeObject(final Object object) {
            kind = Kind.UNKNOWN;
            if (object instanceof java.rmi.server.RMIClientSocketFactory) {
                final var type = object.getClass();
                if (type == StrictRmiTlsSocketFactory.class) {
                    kind = Kind.MUTUAL_TLS_FACTORY;
                } else if (type == RMISSLSocketFactory.class || type == SslRMIClientSocketFactory.class) {
                    kind = Kind.TLS_FACTORY;
                } else if (type == RMIClientSocketFactory.class) {
                    kind = Kind.NON_TLS_FACTORY;
                }
            }
        }
    }
}
