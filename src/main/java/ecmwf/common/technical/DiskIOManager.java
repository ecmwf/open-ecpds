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
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class DiskIOManager {
	private static final AtomicLong totalWriteTime = new AtomicLong();
	private static final AtomicLong totalReadTime = new AtomicLong();
	private static final AtomicLong writeCount = new AtomicLong();
	private static final AtomicLong readCount = new AtomicLong();
	private static final List<Integer> fileSizes = new ArrayList<>();
	private static final AtomicBoolean startFlag = new AtomicBoolean(false);
	private static final int byteBufferSize = 1024 * 1024;
	private static AtomicLong[] bytesWritten;
	private static List<File>[] fileNames;
	private static long maxDiskUsage;
	private static int minFileSize;
	private static int maxFileSize;

	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws InterruptedException, IOException, ExecutionException {
		if (args.length < 8) {
			log("""
					Usage: java DiskIOManager <testDurationInSec> <numDisks> <numThreadsPerDisk> <minFileSizeKB> <maxFileSizeMB> <baseDir> <maxDiskUsageInGB> <useByteBuffers>
					e.g. java DiskIOManager 3600 2 10 500 800 /path/to/disk{} 80 yes
					""");
			return;
		}
		var testDuration = Long.parseLong(args[0]) * 1000; // Convert seconds to milliseconds
		var numDisks = Integer.parseInt(args[1]);
		fileNames = new List[numDisks];
		bytesWritten = new AtomicLong[numDisks];
		var numThreadsPerDisk = Integer.parseInt(args[2]);
		minFileSize = Integer.parseInt(args[3]) * 1024; // Convert KB to bytes
		maxFileSize = Integer.parseInt(args[4]) * 1024 * 1024; // Convert MB to bytes
		var baseDir = args[5];
		maxDiskUsage = Long.parseLong(args[6]) * 1024 * 1024 * 1024; // Convert GB to bytes
		var useByteBuffers = "yes".equalsIgnoreCase(args[7]);
		for (var i = 0; i < numDisks; i++)
			bytesWritten[i] = new AtomicLong();
		// Generate or read file sizes
		generateFileSizes("file_sizes.cnf");
		var totalThreadNumber = numDisks * numThreadsPerDisk;
		// Progress reporting thread
		var progressThread = new Thread(() -> {
			var startTime = System.currentTimeMillis();
			while (System.currentTimeMillis() - startTime < testDuration) {
				try {
					Thread.sleep(60000); // Sleep for 1 minute
					if (!startFlag.get()) {
						startFlag.set(true); // Start!
					} else {
						var elapsedTime = System.currentTimeMillis() - startTime;
						var avgWriteTime = writeCount.get() > 0 ? totalWriteTime.get() / writeCount.get() : 0;
						var avgReadTime = readCount.get() > 0 ? totalReadTime.get() / readCount.get() : 0;
						log("Progress: " + formatElapsedTime(elapsedTime) + " elapsed, Threads: " + totalThreadNumber
								+ ", WriteCount: " + writeCount.get() + ", ReadCount: " + readCount.get());
						log("Intermediate Metrics: Average Write Time: " + avgWriteTime + " ms, Average Read Time: "
								+ avgReadTime + " ms");
						for (var i = 0; i < numDisks; i++) {
							var totalFileZize = getTotalFileSize(i);
							var usage = ((double) totalFileZize / maxDiskUsage) * 100;
							var fileCount = fileNames[i].size();
							var writtenBytes = bytesWritten[i].get();
							var writtenSize = writtenBytes > 1024 * 1024 * 1024
									? (writtenBytes / (1024 * 1024 * 1024)) + " GB"
									: (writtenBytes / (1024 * 1024)) + " MB";
							log("Disk " + i + ": Usage: " + String.format("%.2f", usage) + "%, Files: " + fileCount
									+ ", Data Written: " + writtenSize);
						}
					}
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}
		});
		progressThread.start();
		var executor = Executors.newFixedThreadPool(totalThreadNumber);
		List<Future<?>> futures = new ArrayList<>();
		for (var i = 0; i < numDisks; i++) {
			var diskPath = baseDir.contains("{}") ? baseDir.replace("{}", String.valueOf(i)) : baseDir + i;
			fileNames[i] = Collections.synchronizedList(new ArrayList<>());
			// Half the threads for writes, half for reads
			var writeThreads = numThreadsPerDisk / 2;
			var readThreads = numThreadsPerDisk - writeThreads; // Handle odd numbers
			// Submit write tasks
			for (var j = 0; j < writeThreads; j++) {
				futures.add(executor.submit(new DiskTask(diskPath, true, testDuration, i, useByteBuffers)));
			}
			// Submit read tasks
			for (var j = 0; j < readThreads; j++) {
				futures.add(executor.submit(new DiskTask(diskPath, false, testDuration, i, useByteBuffers)));
			}
		}
		for (Future<?> future : futures) {
			future.get();
		}
		executor.shutdown();
		executor.awaitTermination(1, TimeUnit.HOURS);
		progressThread.interrupt();
		log("Average Write Time: " + (totalWriteTime.get() / writeCount.get()) + " ms");
		log("Average Read Time: " + (totalReadTime.get() / readCount.get()) + " ms");
	}

	private static void generateFileSizes(String filename) throws IOException {
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
			log("Generated and saved file sizes to " + filename + " (" + fileSizes.size() + " entries)");
		} else {
			log("File sizes loaded from " + filename + " (" + fileSizes.size() + " entries)");
		}
	}

	static class DiskTask implements Runnable {
		private final String diskPath;
		private final boolean isWriter;
		private final long testDuration;
		private final int diskIndex;
		private static int fileSizeIndex = 0;
		private final boolean useByteBuffers;

		DiskTask(String diskPath, boolean isWriter, long testDuration, int diskIndex, boolean useByteBuffers) {
			this.diskPath = diskPath;
			this.isWriter = isWriter;
			this.testDuration = testDuration;
			this.diskIndex = diskIndex;
			this.useByteBuffers = useByteBuffers;
		}

		@Override
		public void run() {
			while (!startFlag.get()) {
				Thread.yield(); // Give control back to other threads while waiting
			}
			log("Starting " + (isWriter ? " writer " : " reader ") + " thread (" + Thread.currentThread().getName()
					+ ")");
			var endTime = System.currentTimeMillis() + testDuration;
			try {
				while (System.currentTimeMillis() < endTime) {
					if (isWriter) {
						writeFile(useByteBuffers);
					} else {
						readFile(useByteBuffers);
					}
					deleteSomeFilesIfRequired();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		private void writeFile(boolean useByteBuffers) throws IOException {
			var startTime = System.currentTimeMillis();
			var file = new File(diskPath, UUID.randomUUID().toString() + ".txt");
			int fileSize = fileSizes.get(fileSizeIndex); // Get the next file size from the list
			fileSizeIndex = (fileSizeIndex + 1) % fileSizes.size(); // Move to the next index, wrap around if needed
			var data = new byte[fileSize];
			ThreadLocalRandom.current().nextBytes(data);
			try (var fos = new FileOutputStream(file)) {
				if (useByteBuffers) {
					try (FileChannel fileChannel = fos.getChannel()) {
						final var buffer = ByteBuffer.allocateDirect(byteBufferSize); // Allocate a direct byte buffer
						int offset = 0;
						while (offset < fileSize) {
							int length = Math.min(byteBufferSize, fileSize - offset);
							buffer.put(data, offset, length); // Write data to the buffer
							buffer.flip(); // Prepare buffer for writing
							while (buffer.hasRemaining()) {
								fileChannel.write(buffer); // Write buffer to the file channel
							}
							buffer.clear(); // Prepare buffer for the next write
							offset += length;
						}
						fos.getFD().sync();
					}
				} else {
					try (var bos = new BufferedOutputStream(fos)) {
						bos.write(data);
						fos.getFD().sync();
					}
				}
			}
			var endTime = System.currentTimeMillis();
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

		private void readFile(boolean useByteBuffers) throws IOException {
			var randomFile = getRandomFile();
			if (randomFile != null) {
				var startTime = System.currentTimeMillis();
				if (useByteBuffers) {
					try (final var in = new FileInputStream(randomFile); final var fileChannel = in.getChannel()) {
						final ByteBuffer buffer = ByteBuffer.allocate(byteBufferSize); // 1 MB buffer
						while (fileChannel.read(buffer) != -1) {
							buffer.flip(); // Prepare the buffer for writing
							while (buffer.hasRemaining()) {
								byte bytes[] = new byte[65536];
								buffer.get(bytes); // Read the byte from the buffer (simulate)
							}
							buffer.clear(); // Prepare the buffer for the next read
						}
					}
				} else {
					try (var bis = new BufferedInputStream(new FileInputStream(randomFile))) {
						while (bis.read() != -1) {
							// e.g. socketChannel.write(buffer);
						}
					}
				}
				var endTime = System.currentTimeMillis();
				totalReadTime.addAndGet(endTime - startTime);
				readCount.incrementAndGet();
			}
		}

		private void deleteSomeFilesIfRequired() {
			while (getTotalFileSize(diskIndex) > maxDiskUsage) {
				var file = fileNames[diskIndex].remove(0);
				if (file.delete())
					log(">> Deleted file: " + file.getName());
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

	public static String formatElapsedTime(long elapsedTimeMillis) {
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

	private static void log(final String message) {
		System.out.println(message);
		System.out.flush();
	}
}