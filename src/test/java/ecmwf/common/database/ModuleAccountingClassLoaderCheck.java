package ecmwf.common.database;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

/** Exercises put accounting with modules visible only through a child plugin loader. */
public final class ModuleAccountingClassLoaderCheck {
    public static void main(final String[] args) throws Exception {
        final var classes = Path.of(args.length > 0 ? args[0] : "ecpds-core/target/classes").toUri().toURL();
        final var urls = new ArrayList<URL>();
        urls.add(classes);
        try (var jars = Files.list(Path.of(args.length > 1 ? args[1] : "lib"))) {
            for (final var jar : jars.filter(p -> p.toString().endsWith(".jar")).toList()) {
                urls.add(jar.toUri().toURL());
            }
        }
        try (var core = new URLClassLoader(urls.toArray(URL[]::new), ClassLoader.getPlatformClassLoader()) {
            @Override
            protected Class<?> loadClass(final String name, final boolean resolve) throws ClassNotFoundException {
                if (name.startsWith("ecmwf.common.ectrans.module.")) {
                    throw new ClassNotFoundException(name);
                }
                return super.loadClass(name, resolve);
            }
        }; var plugins = new URLClassLoader(new URL[] { classes }, core)) {
            final var moduleType = core.loadClass("ecmwf.common.ectrans.TransferModule");
            final var actionType = core.loadClass("ecmwf.common.ectrans.ECtransPut");
            final var callbackType = core.loadClass("ecmwf.common.ectrans.ECtransCallback");
            final var callback = Proxy.newProxyInstance(core, new Class<?>[] { callbackType }, (proxy, method, values) -> {
                if ("isByteAccountingEnabled".equals(method.getName())) return true;
                if ("getECtransSetup".equals(method.getName())) throw new IllegalStateException("accounting check passed");
                throw new AssertionError("Unexpected callback: " + method.getName());
            });
            final var exec = actionType.getDeclaredMethod("exec", moduleType, boolean.class);
            exec.setAccessible(true);
            for (final String name : new String[] { "PortalModule", "TestModule", "FtpModule" }) {
                final var pluginType = plugins.loadClass("ecmwf.common.ectrans.module." + name);
                final var module = pluginType.getDeclaredConstructor().newInstance();
                final var expected = "FtpModule".equals(name);
                if (!Boolean.valueOf(expected).equals(moduleType.getMethod("supportsPutByteAccounting").invoke(module))) {
                    throw new AssertionError("Wrong accounting capability: " + name);
                }
                final var action = actionType.getConstructor(String.class, Object.class, long.class, long.class)
                        .newInstance("target", null, 0L, 0L);
                actionType.getMethod("init", core.loadClass("ecmwf.common.ectrans.RemoteProvider"),
                        core.loadClass("ecmwf.common.ectrans.ECtransContainer"), callbackType, String.class)
                        .invoke(action, null, null, callback, null);
                try {
                    exec.invoke(action, module, false);
                    throw new AssertionError("Expected setup sentinel");
                } catch (final InvocationTargetException e) {
                    if (!(e.getCause() instanceof IllegalStateException)
                            || !"accounting check passed".equals(e.getCause().getMessage())) {
                        throw new AssertionError("Put failed before setup for child-loaded " + name, e.getCause());
                    }
                }
            }
        }
        System.out.println("Isolated plugin classloader accounting checks passed");
    }
}
