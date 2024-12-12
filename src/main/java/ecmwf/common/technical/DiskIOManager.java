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

/**
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.9
 * @since 2024-10-28
 */

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.LockSupport;
import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.stream.IntStream;

public class DiskIOManager {
    private static final Logger logger = Logger.getLogger(DiskIOManager.class.getName());
    private static final AtomicLong totalWriteSize = new AtomicLong();
    private static final AtomicLong totalReadSize = new AtomicLong();
    private static final AtomicLong totalWriteTime = new AtomicLong();
    private static final AtomicLong totalReadTime = new AtomicLong();
    private static final AtomicLong writeCount = new AtomicLong();
    private static final AtomicLong readCount = new AtomicLong();
    private static final List<Integer> fileSizes = new ArrayList<>();
    private static final AtomicBoolean startFlag = new AtomicBoolean(false);
    private static final int BYTE_BUFFER_SIZE = 1024 * 1024;
    private static AtomicLong[] bytesWritten;
    private static List<File>[] fileNames;
    private static long maxDiskUsage;
    private static int minFileSize;
    private static int maxFileSize;

    static {
        // Remove default handlers from the root logger
        LogManager.getLogManager().reset();
        // Configure the logger
        var handler = new ConsoleHandler();
        handler.setFormatter(new SimpleMessageFormatter());
        logger.addHandler(handler);
        logger.setLevel(Level.ALL);
        handler.setLevel(Level.ALL);
    }

    // Custom formatter that only displays the message
    static class SimpleMessageFormatter extends Formatter {
        @Override
        public String format(final LogRecord logRecord) {
            return formatMillisToHHMMSS(logRecord.getMillis()) + " - " + logRecord.getLevel().toString() + ": "
                    + logRecord.getMessage() + System.lineSeparator();
        }

        public static String formatMillisToHHMMSS(long millis) {
            long hours = TimeUnit.MILLISECONDS.toHours(millis) % 24;
            long minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60;
            long seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60;
            return String.format("%02d:%02d:%02d", hours, minutes, seconds);
        }
    }

    @SuppressWarnings("unchecked")
    public static void main(String[] args) throws Exception {
        if (args.length < 9) {
            info("""
                    Usage: java DiskIOManager
                      <testDurationInSec>
                      <numDisks>
                      <numThreadsPerDisk>
                      <minFileSizeKB>
                      <maxFileSizeMB>
                      <baseDir>
                      <maxDiskUsageInGB>
                      <useByteBuffers>
                      <useVirtualThreads>
                    e.g. java DiskIOManager 3600 2 10 500 800 /path/to/disk{} 80 yes no
                    """);
            return;
        }
        var startTime = System.currentTimeMillis();
        var testDuration = Long.parseLong(args[0]) * 1000; // Convert seconds to milliseconds
        var numDisks = Integer.parseInt(args[1]);
        var numThreadsPerDisk = Integer.parseInt(args[2]);
        minFileSize = Integer.parseInt(args[3]) * 1024; // Convert KB to bytes
        maxFileSize = Integer.parseInt(args[4]) * 1024 * 1024; // Convert MB to bytes
        var baseDir = args[5];
        maxDiskUsage = Long.parseLong(args[6]) * 1024 * 1024 * 1024; // Convert GB to bytes
        var useByteBuffers = "yes".equalsIgnoreCase(args[7]);
        var useVirtualThreads = "yes".equalsIgnoreCase(args[8]);
        var totalThreadNumber = numDisks * numThreadsPerDisk;
        config("Test Duration: " + formatElapsedTime(testDuration));
        config("Number of Disks: " + numDisks);
        config("Number of Threads per Disk: " + numThreadsPerDisk);
        config("Minimum File Size: " + getReadableSize(minFileSize));
        config("Maximum File Size: " + getReadableSize(maxFileSize));
        config("Maximum Disk Usage: " + getReadableSize(maxDiskUsage));
        config("Base Directory: " + baseDir);
        config("Use Byte Buffers: " + (useByteBuffers ? "yes" : "no"));
        config("Use Virtual Threads: " + (useVirtualThreads ? "yes" : "no"));
        fileNames = new List[numDisks];
        bytesWritten = new AtomicLong[numDisks];
        for (var i = 0; i < numDisks; i++)
            bytesWritten[i] = new AtomicLong();
        generateFileSizes("file_sizes.cnf"); // Generate or read file sizes
        // Progress reporting thread
        try (var executor = getExecutorService(useVirtualThreads, totalThreadNumber);
                var scheduler = Executors.newScheduledThreadPool(1)) {
            Runtime.getRuntime().addShutdownHook(new Thread(() -> cleanup(executor, scheduler)));
            final Runnable schedulerTask = () -> {
                try {
                    if (!startFlag.get()) {
                        startFlag.set(true); // Start!
                    } else {
                        var elapsedTime = System.currentTimeMillis() - startTime;
                        var avgWriteTime = writeCount.get() > 0 ? totalWriteTime.get() / writeCount.get() : 0;
                        var avgReadTime = readCount.get() > 0 ? totalReadTime.get() / readCount.get() : 0;
                        info("Progress: " + formatElapsedTime(elapsedTime) + " elapsed, Threads: " + totalThreadNumber
                                + ", WriteCount: " + writeCount.get() + ", ReadCount: " + readCount.get());
                        info("Intermediate Metrics: Total Write Size: " + getReadableSize(totalWriteSize.get())
                                + ", Average Write Time: " + avgWriteTime + " ms, Total Read Size: "
                                + getReadableSize(totalReadSize.get()) + ", Average Read Time: " + avgReadTime + " ms");
                        for (var i = 0; i < numDisks; i++) {
                            var totalFileZize = getTotalFileSize(i);
                            var usage = ((double) totalFileZize / maxDiskUsage) * 100;
                            var fileCount = fileNames[i].size();
                            var writtenBytes = bytesWritten[i].get();
                            info("Disk " + i + ": Usage: " + String.format("%.2f", usage) + "%, Files: " + fileCount
                                    + ", Data Written: " + getReadableSize(writtenBytes));
                        }
                    }
                } catch (Exception e) {
                    severe("Progress Thread failed", e);
                }
            };
            scheduler.scheduleAtFixedRate(schedulerTask, 1, 1, TimeUnit.MINUTES);
            // Half the threads for writes, half for reads
            var writeThreads = numThreadsPerDisk / 2;
            var readThreads = numThreadsPerDisk - writeThreads; // Handle odd numbers
            final List<Future<?>> futureDiskTasks = new ArrayList<>();
            final var counter = new AtomicInteger(0);
            for (var i = 0; i < numDisks; i++) {
                final var diskNumber = counter.getAndIncrement();
                var diskPath = baseDir.contains("{}") ? baseDir.replace("{}", String.valueOf(diskNumber))
                        : baseDir + diskNumber;
                fileNames[diskNumber] = Collections.synchronizedList(new ArrayList<>());
                // Submit write tasks
                IntStream.range(0, writeThreads).mapToObj(j -> {
                    try {
                        var future = executor
                                .submit(new DiskTask(diskPath, true, testDuration, diskNumber, useByteBuffers));
                        info("Submitted write task for disk " + diskNumber + ", thread " + j);
                        return future;
                    } catch (Exception e) {
                        severe("Failed to submit write task for disk " + diskNumber + ", thread " + j, e);
                        return null;
                    }
                }).filter(Objects::nonNull) // Filter out any null futures
                        .forEach(futureDiskTasks::add);
                // Submit read tasks
                IntStream.range(0, readThreads).mapToObj(j -> {
                    try {
                        var future = executor
                                .submit(new DiskTask(diskPath, false, testDuration, diskNumber, useByteBuffers));
                        info("Submitted read task for disk " + diskNumber + ", thread " + j);
                        return future;
                    } catch (Exception e) {
                        severe("Failed to submit read task for disk " + diskNumber + ", thread " + j, e);
                        return null;
                    }
                }).filter(Objects::nonNull) // Filter out any null futures
                        .forEach(futureDiskTasks::add);
            }
            futureDiskTasks.stream().forEach(future -> {
                try {
                    future.get();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    severe("DiskTask was interrupted", e);
                } catch (CancellationException e) {
                    severe("DiskTask execution cancelled", e);
                } catch (ExecutionException e) {
                    severe("DiskTask execution failed", e);
                }
            });
            cleanup(executor, scheduler);
            info("Data Written: " + getReadableSize(totalWriteSize.get()));
            info("Data Read: " + getReadableSize(totalReadSize.get()));
            info("Average Read Time: " + (totalReadTime.get() / readCount.get()) + " ms");
            info("Average Write Time: " + (totalWriteTime.get() / writeCount.get()) + " ms");
            info("Average Read Time: " + (totalReadTime.get() / readCount.get()) + " ms");
        } catch (Exception e) {
            severe("DiskIOManager failed", e);
        }
    }

    private static ExecutorService getExecutorService(final boolean platformThreads, final int totalThreadNumber) {
        return platformThreads ? Executors.newFixedThreadPool(totalThreadNumber)
                : Executors.newThreadPerTaskExecutor(Thread.ofVirtual().name("DiskThread-", 0).factory());
    }

    private static void cleanup(final ExecutorService executor, final ScheduledExecutorService scheduler) {
        fine("Starting cleanup operations");
        try {
            // Perform cleanup operations here
            if (executor != null && !executor.isShutdown()) {
                executor.shutdown();
                if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            }
            if (scheduler != null && !scheduler.isShutdown()) {
                scheduler.shutdown();
                if (!scheduler.awaitTermination(60, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            }
            fine("Cleanup completed");
        } catch (InterruptedException e) {
            warning("Cleanup operations interrupted");
            Thread.currentThread().interrupt();
        }
    }

    private static void generateFileSizes(final String filename) throws IOException {
        var path = Paths.get(filename);
        if (Files.exists(path)) {
            // Read file sizes from the file
            var lines = Files.readAllLines(path);
            for (String line : lines)
                fileSizes.add(Integer.parseInt(line));
        }
        if (fileSizes.isEmpty()) {
            // Generate file sizes based on predefined distribution
            var random = ThreadLocalRandom.current();
            var totalFiles = 1000; // Total number of files to generate
            var smallFiles = totalFiles / 3; // 1/3 small files
            var mediumFiles = totalFiles / 3; // 1/3 medium files
            var largeFiles = totalFiles / 3; // 1/3 large files
            for (var i = 0; i < smallFiles; i++) {
                fileSizes.add(minFileSize + random.nextInt((minFileSize * 2) - minFileSize + 1)); // Small files
            }
            for (var i = 0; i < mediumFiles; i++) {
                fileSizes.add((minFileSize * 2) + random.nextInt((maxFileSize / 2) - (minFileSize * 2) + 1)); // Medium
                                                                                                              // files
            }
            for (var i = 0; i < largeFiles; i++) {
                fileSizes.add((maxFileSize / 2) + random.nextInt(maxFileSize - (maxFileSize / 2) + 1)); // Large files
            }
            Collections.shuffle(fileSizes); // Shuffle to randomize the order
            // Save the generated file sizes to the file
            List<String> lines = new ArrayList<>();
            for (int size : fileSizes) {
                lines.add(String.valueOf(size));
            }
            Files.write(path, lines);
            info("Generated and saved file sizes to " + filename + " (" + fileSizes.size() + " entries)");
        } else {
            info("File sizes loaded from " + filename + " (" + fileSizes.size() + " entries)");
        }
    }

    static class DiskTask implements Runnable {
        private final AtomicInteger fileSizeIndex = new AtomicInteger(0);
        private final String diskPath;
        private final boolean isWriter;
        private final long testDuration;
        private final int diskIndex;
        private final boolean useByteBuffers;

        DiskTask(final String diskPath, final boolean isWriter, final long testDuration, final int diskIndex,
                final boolean useByteBuffers) {
            this.diskPath = diskPath;
            this.isWriter = isWriter;
            this.testDuration = testDuration;
            this.diskIndex = diskIndex;
            this.useByteBuffers = useByteBuffers;
        }

        @Override
        public void run() {
            info("Starting " + (isWriter ? " writer " : " reader ") + " for " + diskPath);
            while (!startFlag.get()) {
                info("DiskTask Thread waiting for 5s");
                LockSupport.parkNanos(5_000_000_000L); // Sleep for 5s
            }
            var endTime = System.currentTimeMillis() + testDuration;
            try {
                info("DiskTask Thread operational");
                while (System.currentTimeMillis() < endTime) {
                    if (isWriter) {
                        writeFile(useByteBuffers);
                    } else {
                        readFile(useByteBuffers);
                    }
                    deleteSomeFilesIfRequired();
                }
            } catch (IOException e) {
                severe("DiskTask Thread failed", e);
            }
        }

        private void writeFile(final boolean useByteBuffers) throws IOException {
            var startTime = System.currentTimeMillis();
            var file = new File(diskPath, UUID.randomUUID().toString() + ".txt");
            var index = fileSizeIndex.getAndUpdate(i -> (i + 1) % fileSizes.size());
            var fileSize = fileSizes.get(index); // Get the next file size from the list
            try (var fos = new FileOutputStream(file)) {
                if (useByteBuffers) {
                    try (final var fileChannel = fos.getChannel()) {
                        final var buffer = ByteBuffer.allocateDirect(BYTE_BUFFER_SIZE); // Allocate a direct byte buffer
                        var offset = 0;
                        while (offset < fileSize) {
                            var length = Math.min(BYTE_BUFFER_SIZE, fileSize - offset);
                            buffer.clear(); // Prepare buffer for the next write
                            for (var i = 0; i < length; i++) {
                                buffer.put((byte) ThreadLocalRandom.current().nextInt(256)); // Generate random byte
                            }
                            buffer.flip(); // Prepare buffer for writing
                            while (buffer.hasRemaining()) {
                                fileChannel.write(buffer); // Write buffer to the file channel
                            }
                            offset += length;
                        }
                        fos.getFD().sync();
                    }
                } else {
                    try (var bos = new BufferedOutputStream(fos)) {
                        var buffer = new byte[BYTE_BUFFER_SIZE];
                        var offset = 0;
                        while (offset < fileSize) {
                            var length = Math.min(BYTE_BUFFER_SIZE, fileSize - offset);
                            ThreadLocalRandom.current().nextBytes(buffer); // Generate random bytes
                            bos.write(buffer, 0, length);
                            offset += length;
                        }
                        fos.getFD().sync();
                    }
                }
            }
            var endTime = System.currentTimeMillis();
            totalWriteSize.addAndGet(fileSize);
            totalWriteTime.addAndGet(endTime - startTime);
            writeCount.incrementAndGet();
            bytesWritten[diskIndex].addAndGet(fileSize);
            fileNames[diskIndex].add(file);
        }

        private File getRandomFile() {
            final var files = fileNames[diskIndex];
            synchronized (files) {
                if (!files.isEmpty()) {
                    return files.get(ThreadLocalRandom.current().nextInt(files.size()));
                } else
                    return null;
            }
        }

        private void readFile(final boolean useByteBuffers) throws IOException {
            var randomFile = getRandomFile();
            if (randomFile != null) {
                var startTime = System.currentTimeMillis();
                if (useByteBuffers) {
                    try (final var in = new FileInputStream(randomFile); final var fileChannel = in.getChannel()) {
                        final var buffer = ByteBuffer.allocate(BYTE_BUFFER_SIZE); // 1 MB buffer
                        while (fileChannel.read(buffer) != -1) {
                            buffer.flip(); // Prepare the buffer for writing
                            while (buffer.hasRemaining()) {
                                buffer.get(); // Read the byte from the buffer (simulate)
                            }
                            buffer.clear(); // Prepare the buffer for the next read
                        }
                    }
                } else {
                    try (var bis = new BufferedInputStream(new FileInputStream(randomFile))) {
                        while (bis.read() != -1) {
                            // Do something with the byte!
                        }
                    }
                }
                var endTime = System.currentTimeMillis();
                totalReadSize.addAndGet(randomFile.length());
                totalReadTime.addAndGet(endTime - startTime);
                readCount.incrementAndGet();
            } else {
                warning("No file found for reading");
            }
        }

        private void deleteSomeFilesIfRequired() {
            while (getTotalFileSize(diskIndex) > maxDiskUsage) {
                var file = fileNames[diskIndex].remove(0);
                var filePath = file.toPath();
                try {
                    Files.delete(filePath);
                    fine(">> Deleted file: " + filePath.getFileName());
                } catch (IOException e) {
                    warning(">> Failed to delete file: " + filePath.getFileName() + " due to " + e.getMessage());
                }
            }
        }
    }

    private static long getTotalFileSize(final int diskId) {
        final var files = fileNames[diskId];
        var totalSize = 0L;
        if (files != null)
            synchronized (files) {
                for (var file : files) {
                    totalSize += file.length(); // Add the file size to the total
                }
            }
        return totalSize;
    }

    public static String getReadableSize(final long writtenBytes) {
        if (writtenBytes >= 1024 * 1024 * 1024) {
            return (writtenBytes / (1024 * 1024 * 1024)) + " GB";
        } else if (writtenBytes >= 1024 * 1024) {
            return (writtenBytes / (1024 * 1024)) + " MB";
        } else if (writtenBytes >= 1024) {
            return (writtenBytes / 1024) + " KB";
        } else {
            return writtenBytes + " B";
        }
    }

    public static String formatElapsedTime(final long elapsedTimeMillis) {
        var hours = elapsedTimeMillis / (1000 * 60 * 60);
        var remainingMillis = elapsedTimeMillis % (1000 * 60 * 60);
        var minutes = remainingMillis / (1000 * 60);
        remainingMillis %= (1000 * 60);
        var seconds = remainingMillis / 1000;
        var milliseconds = remainingMillis % 1000;
        var result = new StringBuilder();
        if (hours > 0)
            result.append(hours).append(hours == 1 ? " hour, " : " hours, ");
        if (minutes > 0 || hours > 0)
            result.append(minutes).append(minutes == 1 ? " minute, " : " minutes, ");
        result.append(seconds).append(seconds == 1 ? " second, " : " seconds, ");
        result.append(milliseconds).append(milliseconds == 1 ? " millisecond" : " milliseconds");
        return result.toString();
    }

    private static String getMessage(final String message) {
        return "[" + Thread.currentThread().getName() + "] " + message;
    }

    private static void info(final String message) {
        if (logger.isLoggable(Level.INFO))
            logger.info(getMessage(message));
    }

    private static void fine(final String message) {
        if (logger.isLoggable(Level.FINE))
            logger.fine(getMessage(message));
    }

    private static void warning(final String message) {
        if (logger.isLoggable(Level.WARNING))
            logger.warning(getMessage(message));
    }

    private static void config(final String message) {
        if (logger.isLoggable(Level.CONFIG))
            logger.config(getMessage(message));
    }

    private static void severe(final String message, final Exception exception) {
        if (logger.isLoggable(Level.SEVERE))
            logger.log(Level.SEVERE, message, exception);
    }
}