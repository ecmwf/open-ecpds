package ecmwf.ecpds.extension;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.rmi.Remote;
import java.util.Map;

import ecmwf.ecpds.mover.MasterProxy;

public final class RemoteServicesTest {
    private static int closes;

    public static void main(final String[] args) throws Exception {
        try (final var extensions = RemoteServices.load("master", Map.of(), section -> Map.of())) {
            fail(() -> extensions.open("unregistered", "target"));
        }
        try (final var extensions = load(Provider.class)) {
            if (extensions.open("service", "missing") != null) {
                throw new AssertionError("An unavailable service must remain absent");
            }
            fail(() -> extensions.open("unregistered", "missing"));
            fail(() -> extensions.open("service", ""));
        }
        if (closes != 1) {
            throw new AssertionError("Provider must close once");
        }
        fail(() -> load(FailingProvider.class));
        fail(() -> load(IncompatibleProvider.class));
        if (closes != 3) {
            throw new AssertionError("Failed startup must release providers");
        }
        fail(() -> load(String.class));
        fail(() -> RemoteServices.load("master", Map.of("service", "MissingConfiguration"), section -> Map.of()));
        final var closed = load(Provider.class);
        closed.close();
        fail(() -> closed.open("service", "missing"));
        final var before = closes;
        fail(() -> RemoteServices.load("master", Map.of("first", "Ready", "second", "Failing"), section -> Map
                .of("class", ("Ready".equals(section) ? Provider.class : FailingProvider.class).getName())));
        if (closes != before + 2) {
            throw new AssertionError("Partial startup must close both the failed and previously started providers");
        }
        final var rest = (MasterProxy) Proxy.newProxyInstance(MasterProxy.class.getClassLoader(),
                new Class<?>[] { MasterProxy.class },
                (proxy, method, values) -> InvocationHandler.invokeDefault(proxy, method, values));
        fail(() -> rest.getExtensionService("service", "target"));
        System.out.println("PASS: explicit remote service loading, startup cleanup, close and REST rejection");
    }

    private static RemoteServices load(final Class<?> type) throws IOException {
        return RemoteServices.load("master", Map.of("service", "ServiceOptions"),
                section -> Map.of("class", type.getName()));
    }

    private static void fail(final Operation operation) throws Exception {
        try {
            operation.run();
            throw new AssertionError("Expected IOException");
        } catch (IOException expected) {
        }
    }

    private interface Operation {
        void run() throws Exception;
    }

    public static class Provider implements RemoteServiceProvider {
        @Override
        public int apiVersion() {
            return API_VERSION;
        }

        @Override
        public Remote open(final String target) throws IOException {
            if (!"missing".equals(target)) {
                throw new IOException("Unexpected target");
            }
            return null;
        }

        @Override
        public void close() {
            closes++;
        }
    }

    public static final class FailingProvider extends Provider {
        @Override
        public void start(final ExtensionContext context) throws IOException {
            throw new IOException("Expected startup failure");
        }
    }

    public static final class IncompatibleProvider extends Provider {
        @Override
        public int apiVersion() {
            return API_VERSION + 1;
        }
    }
}
