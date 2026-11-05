package ecmwf.ecpds.master;

import java.io.InterruptedIOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import ecmwf.common.ecaccess.AbstractTicket;
import ecmwf.common.technical.PipedInputStream;
import ecmwf.common.technical.PipedOutputStream;

public final class ECauthInterruptionTest {
    public static void main(final String[] args) throws Exception {
        try (var in = new PipedInputStream(); var out = new PipedOutputStream(in)) {
            final var waiting = new CountDownLatch(1);
            final var failure = new AtomicReference<Throwable>();
            final var reader = new Thread(() -> {
                try {
                    waiting.countDown();
                    in.read();
                    failure.set(new AssertionError("Read unexpectedly completed"));
                } catch (final InterruptedIOException e) {
                    if (!Thread.currentThread().isInterrupted() || e.getCause() == null
                            || !e.getMessage().contains("waiting for pipe data")) {
                        failure.set(new AssertionError("Interruption context lost", e));
                    }
                } catch (final Throwable t) {
                    failure.set(t);
                }
            });
            reader.start();
            assert waiting.await(2, TimeUnit.SECONDS);
            reader.interrupt();
            reader.join(2000);
            assert !reader.isAlive();
            assert failure.get() == null : failure.get();
        }
        final var ticket = new Ticket();
        ticket.setError("Download", new InterruptedIOException());
        assert ticket.getError().equals("Download (exception InterruptedIOException)") : ticket.getError();
        ticket.setError("later failure", new Exception("other"));
        assert ticket.getError().startsWith("Download ");
        final var timeout = new Ticket();
        timeout.setError("Download data channel did not connect within 120 seconds");
        timeout.setError("GetThread", new InterruptedIOException());
        assert timeout.getError().equals("Download data channel did not connect within 120 seconds");
        System.out.println(
                "PASS: interrupted pipe preserves cause/status, message-less exception handling and first failure retained.");
    }

    private static final class Ticket extends AbstractTicket {
        @Override
        public String getStatus() {
            return "test";
        }
    }
}
