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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class DiskIOManager {
    private static int numDisks;
    private static double maxDiskUsage;
    private static String baseDir;
    private static int minFileSize;
    private static int maxFileSize;

    private static final AtomicLong totalWriteTime = new AtomicLong();
    private static final AtomicLong totalReadTime = new AtomicLong();
    private static final AtomicLong writeCount = new AtomicLong();
    private static final AtomicLong readCount = new AtomicLong();
    private static AtomicLong[] bytesWritten;

    public static void main(String[] args) throws InterruptedException, ExecutionException {
        if (args.length < 7) {
            System.out.println(
                    """
                            Usage: java DiskIOManager <testDurationInSec> <numDisks> <numThreads> <minFileSizeKB> <maxFileSizeMB> <baseDir> <maxDiskUsage>
                            e.g. java DiskIOManager 3600 2 10 500 800 /path/to/disk{} 0.8
                            """);
            return;
        }

        var testDuration = Long.parseLong(args[0]) * 1000; // Convert seconds to milliseconds
        numDisks = Integer.parseInt(args[1]);
        var numThreads = Integer.parseInt(args[2]);
        minFileSize = Integer.parseInt(args[3]) * 1024; // Convert KB to bytes
        maxFileSize = Integer.parseInt(args[4]) * 1024 * 1024; // Convert MB to bytes
        baseDir = args[5];
        maxDiskUsage = Double.parseDouble(args[6]);

        bytesWritten = new AtomicLong[numDisks];
        for (var i = 0; i < numDisks; i++) {
            bytesWritten[i] = new AtomicLong();
        }

        var executor = Executors.newFixedThreadPool(numThreads);
        List<Future<?>> futures = new ArrayList<>();

        for (var i = 0; i < numDisks; i++) {
            var diskPath = baseDir.contains("{}") ? baseDir.replace("{}", String.valueOf(i)) : baseDir + i;
            futures.add(executor.submit(new DiskTask(diskPath, true, testDuration, i))); // Writing threads
            futures.add(executor.submit(new DiskTask(diskPath, false, testDuration, i))); // Reading threads
        }

        // Progress reporting thread
        var progressThread = new Thread(() -> {
            long startTime = System.currentTimeMillis();
            while (System.currentTimeMillis() - startTime < testDuration) {
                try {
                    Thread.sleep(60000); // Sleep for 1 minute
                    long elapsedTime = (System.currentTimeMillis() - startTime) / 1000;
                    long avgWriteTime = writeCount.get() > 0 ? totalWriteTime.get() / writeCount.get() : 0;
                    long avgReadTime = readCount.get() > 0 ? totalReadTime.get() / readCount.get() : 0;
                    System.out.println("Progress: " + elapsedTime + " seconds elapsed.");
                    System.out.println("Intermediate Metrics: Average Write Time: " + avgWriteTime
                            + " ms, Average Read Time: " + avgReadTime + " ms");

                    for (int i = 0; i < numDisks; i++) {
                        String diskPath = baseDir.contains("{}") ? baseDir.replace("{}", String.valueOf(i))
                                : baseDir + i;
                        File disk = new File(diskPath);
                        long totalSpace = disk.getTotalSpace();
                        long usableSpace = disk.getUsableSpace();
                        double usage = (double) (totalSpace - usableSpace) / totalSpace * 100;
                        File[] files = disk.listFiles();
                        int fileCount = files != null ? files.length : 0;
                        long writtenBytes = bytesWritten[i].get();
                        String writtenSize = writtenBytes > 1024 * 1024 * 1024
                                ? (writtenBytes / (1024 * 1024 * 1024)) + " GB"
                                : (writtenBytes / (1024 * 1024)) + " MB";

                        System.out.println("Disk " + i + ": Usage: " + String.format("%.2f", usage) + "%, Files: "
                                + fileCount + ", Data Written: " + writtenSize);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
        progressThread.start();

        for (Future<?> future : futures) {
            future.get();
        }

        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.HOURS);
        progressThread.interrupt();

        System.out.println("Average Write Time: " + (totalWriteTime.get() / writeCount.get()) + " ms");
        System.out.println("Average Read Time: " + (totalReadTime.get() / readCount.get()) + " ms");
    }

    static class DiskTask implements Runnable {
        private final String diskPath;
        private final boolean isWriter;
        private final long testDuration;
        private final int diskIndex;

        DiskTask(String diskPath, boolean isWriter, long testDuration, int diskIndex) {
            this.diskPath = diskPath;
            this.isWriter = isWriter;
            this.testDuration = testDuration;
            this.diskIndex = diskIndex;
        }

        @Override
        public void run() {
            var endTime = System.currentTimeMillis() + testDuration;
            try {
                while (System.currentTimeMillis() < endTime) {
                    if (isWriter) {
                        writeFile();
                    } else {
                        readFile();
                    }

                    if (getDiskUsage(diskPath) > maxDiskUsage) {
                        deleteSomeFiles(diskPath);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void writeFile() throws IOException {
            var startTime = System.currentTimeMillis();
            var file = new File(diskPath, UUID.randomUUID().toString() + ".txt");
            var fileSize = minFileSize + new Random().nextInt(maxFileSize - minFileSize + 1);
            var data = new byte[fileSize];
            new Random().nextBytes(data);

            try (var bos = new BufferedOutputStream(new FileOutputStream(file))) {
                bos.write(data);
            }

            var endTime = System.currentTimeMillis();
            totalWriteTime.addAndGet(endTime - startTime);
            writeCount.incrementAndGet();
            bytesWritten[diskIndex].addAndGet(fileSize);
        }

        private void readFile() throws IOException {
            var startTime = System.currentTimeMillis();
            var files = new File(diskPath).listFiles();
            if (files != null && files.length > 0) {
                var file = files[new Random().nextInt(files.length)];
                try (var bis = new BufferedInputStream(new FileInputStream(file))) {
                    while (bis.read() != -1) {
                        // Reading file
                    }
                }
            }
            var endTime = System.currentTimeMillis();
            totalReadTime.addAndGet(endTime - startTime);
            readCount.incrementAndGet();
        }

        private double getDiskUsage(String path) {
            var file = new File(path);
            var totalSpace = file.getTotalSpace();
            var usableSpace = file.getUsableSpace();
            return (double) (totalSpace - usableSpace) / totalSpace;
        }

        private void deleteSomeFiles(String path) {
            var files = new File(path).listFiles();
            if (files != null && files.length > 0) {
                Arrays.sort(files, Comparator.comparingLong(File::lastModified));
                var spaceToFree = (long) ((getDiskUsage(path) - maxDiskUsage) * files[0].getTotalSpace());
                var freedSpace = 0L;

                for (File file : files) {
                    if (file.delete()) {
                        freedSpace += file.length();
                        System.out.println("Deleted file: " + file.getName());
                    }
                    if (freedSpace >= spaceToFree) {
                        break;
                    }
                }
            }
        }
    }
}
