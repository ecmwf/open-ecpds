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

package ecmwf.common.ectrans.module;

/**
 * ECMWF Product Data Store (ECPDS) Project
 * 
 * @author Cristina-Iulia Bucur <cristina-iulia.bucur@ecmwf.int>, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import com.google.api.gax.paging.Page;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.storage.Acl.Entity;
import com.google.cloud.storage.Storage.BlobListOption;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.BucketInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.cloud.storage.StorageException;
import com.google.cloud.http.HttpTransportOptions;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.Channels;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.zip.Deflater;
import java.util.zip.GZIPOutputStream;

import ecmwf.common.ectrans.ECtransSetup;
import ecmwf.common.ectrans.TransferModule;
import ecmwf.common.rmi.ClientSocketStatistics;
import ecmwf.common.rmi.SSLClientSocketFactory;
import ecmwf.common.rmi.SocketConfig;

import org.apache.logging.log4j.Logger;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.logging.log4j.LogManager;

import ecmwf.common.technical.StreamPlugThread;
import ecmwf.common.text.Format;

import static ecmwf.common.text.Util.isNotEmpty;
import static ecmwf.common.ectrans.ECtransOptions.*;

/**
 * The Class GCSModule.
 */
public final class GcsModule extends TransferModule {

	/** The Constant _log. */
	private static final Logger _log = LogManager.getLogger(GcsModule.class);

	/** The status. */
	private String currentStatus = "INIT";

	/** The gcs input. */
	private InputStream gcsInput;

	/** The scheme. */
	private String scheme = "http";

	/** The prefix. */
	private String gcsprefix = "";

	/** The project id. */
	private String projectId = null;

	/** The gcs storage. */
	private Storage gcs = null;

	/** The setup. */
	private ECtransSetup currentSetup = null;

	/** The bucket name. */
	private String bucketName = null;

	/** The closed. */
	private final AtomicBoolean closed = new AtomicBoolean(false);

	/** The socket factory. */
	private SSLClientSocketFactory socketFactory;

	/**
	 * Gets the status.
	 *
	 * @return the status
	 */
	@Override
	public String getStatus() {
		return currentStatus;
	}

	/**
	 * Gets the port.
	 *
	 * @param setup the setup
	 * @return the port
	 */
	@Override
	public int getPort(final ECtransSetup setup) {
		return setup.getInteger(HOST_GCS_PORT);
	}

	/**
	 * Update socket statistics.
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	@Override
	public void updateSocketStatistics() throws IOException {
		if (socketFactory != null)
			socketFactory.updateStatistics();
	}

	/**
	 * Connect.
	 *
	 * @param location the location
	 * @param setup    the setup
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void connect(final String location, final ECtransSetup setup) throws IOException {

		// The location is: user:password@host/bucketName
		// user: client_id, 21-digit numeric identifier
		// password: private_key_id, 40-character alphanumeric string 
		// host: an IP address
		// bucketName: optional
		currentSetup = setup;
		setStatus("CONNECT");
		int pos;
		if ((pos = location.lastIndexOf("@")) == -1) {
			throw new IOException("Malformed URL ('@' not found)");
		}
		String host = location.substring(pos + 1);
		String user = location.substring(0, pos);
		if ((pos = user.indexOf(":")) == -1) {
			throw new IOException("Malformed URL (':' not found)");
		}
		final String password = user.substring(pos + 1);
		user = user.substring(0, pos);
		if ((pos = host.indexOf("/")) != -1) {
			bucketName = host.substring(pos + 1);
			host = host.substring(0, pos);
		}
		final int port = getPort(getSetup());

		scheme = setup.getString(HOST_GCS_SCHEME);
		bucketName = setup.getString(HOST_GCS_BUCKET_NAME);
		gcsprefix = setup.getString(HOST_GCS_PREFIX).trim();

		if (isNotEmpty(gcsprefix) && !gcsprefix.endsWith("/"))
			gcsprefix += "/";

		final String url = setup.get(HOST_GCS_URL, scheme + "://" + host + (port != 80 ? ":" + port : ""));
		_log.debug("GCS connection on {} ({})", url, user);

		setAttribute("remote.hostName", host);

		/* add socket statistics */
		final ClientSocketStatistics statistics;
		if (setup.getBoolean(HOST_ECTRANS_SOCKET_STATISTICS)) {
			statistics = new ClientSocketStatistics();
			setAttribute(statistics);
		} else {
			statistics = null;
		}
		final SocketConfig socketConfig = new SocketConfig(statistics, "GCSSocketConfig");
		socketConfig.setDebug(getDebug());
		setup.getOptionalBoolean(HOST_ECTRANS_TCP_NO_DELAY).ifPresent(socketConfig::setTcpNoDelay);
		setup.getOptionalBoolean(HOST_ECTRANS_TCP_KEEP_ALIVE).ifPresent(socketConfig::setKeepAlive);
		setup.getOptionalBoolean(HOST_ECTRANS_TCP_TIME_STAMP).ifPresent(socketConfig::setTCPTimeStamp);
		setup.getOptionalBoolean(HOST_ECTRANS_TCP_QUICK_ACK).ifPresent(socketConfig::setTCPQuickAck);
		setup.getOptionalString(HOST_ECTRANS_TCP_CONGESTION_CONTROL).ifPresent(socketConfig::setTCPCongestion);
		setup.getOptionalInteger(HOST_ECTRANS_TCP_MAX_SEGMENT).ifPresent(socketConfig::setTCPMaxSegment);
		setup.getOptionalInteger(HOST_ECTRANS_TCP_WINDOW_CLAMP).ifPresent(socketConfig::setTCPWindowClamp);
		setup.getOptionalInteger(HOST_ECTRANS_TCP_KEEP_ALIVE_TIME).ifPresent(socketConfig::setTCPKeepAliveTime);
		setup.getOptionalInteger(HOST_ECTRANS_TCP_KEEP_ALIVE_INTERVAL).ifPresent(socketConfig::setTCPKeepAliveInterval);
		setup.getOptionalInteger(HOST_ECTRANS_TCP_KEEP_ALIVE_PROBES).ifPresent(socketConfig::setTCPKeepAliveProbes);
		setup.getOptionalInteger(HOST_ECTRANS_TCP_USER_TIMEOUT).ifPresent(socketConfig::setTCPUserTimeout);
		setup.getOptionalBoolean(HOST_ECTRANS_TCP_LINGER_ENABLE).ifPresent(enable -> {
			setup.getOptionalInteger(HOST_ECTRANS_TCP_LINGER_TIME).ifPresent(time -> {
				socketConfig.setTCPLinger(enable, time);
			});
		});

		boolean connected = false;

		ServiceAccountCredentials credentials = null;
		
		projectId = setup.getString(HOST_GCS_PROJECT_ID);
		final String clientId = isNotEmpty(user) ? user : setup.getString(HOST_GCS_CLIENT_ID);
		final String privateKeyId = isNotEmpty(password) ? password : setup.getString(HOST_GCS_PRIVATE_KEY_ID);
		final String privateKey = setup.getString(HOST_GCS_PRIVATE_KEY); // RSA private key in PKCS8 format
		final String clientEmail = setup.getString(HOST_GCS_CLIENT_EMAIL);

		try {
			credentials = ServiceAccountCredentials.newBuilder().setClientId(clientId)
					.setClientEmail(clientEmail).setPrivateKeyString(privateKey) 
					.setPrivateKeyId(privateKeyId).setProjectId(projectId).build();

			socketFactory = socketConfig.getSSLSocketFactory(setup.getString(HOST_GCS_PROTOCOL),
					setup.getBoolean(HOST_GCS_SSL_VALIDATION));

			// Create the custom HTTP transport with the custom SSLSocketFactory
			HttpTransport httpTransport = new NetHttpTransport.Builder().setSslSocketFactory(socketFactory).build();

			gcs = StorageOptions.newBuilder().setProjectId(projectId).setCredentials(credentials)
					.setTransportOptions(
							HttpTransportOptions.newBuilder().setHttpTransportFactory(() -> httpTransport).build())
					.build().getService();

			if (isNotEmpty(bucketName) && setup.getBoolean(HOST_GCS_MK_BUCKET)) {
				// The user has configured a Bucket Name!
				boolean bucketFound = false;

				for (final Bucket bucket : gcs.list().iterateAll()) {
					if (bucketName.equals(bucket.getName())) {
						bucketFound = true;
						break;
					}
				}
				if (!bucketFound)
					gcs.create(BucketInfo.of(bucketName));
			}

			connected = true;
		} catch (Throwable t) {
			_log.error("Connection failed to {}", url, t);
			throw new IOException("Connection failed to " + url + ": " + Format.getMessage(t, "", 0));
		} finally {
			if (!connected && gcs != null) {
				try {
					gcs.close();
				} catch (Throwable t) {
					// Ignore!
				}
			}
		}
	}

	/**
	 * Gets the bucket name and filename (object with full "path" in bucket). Return
	 * {"bucketName", "ObjectName"}, similar to S3
	 *
	 * @param name the name
	 * @return the string[]
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private String[] getBucketNameAndObjectName(final String name) throws IOException {
		final StringTokenizer token = new StringTokenizer(name == null ? "" : name.replace('\\', '/'), "/");
		final int count = token.countTokens();
		final String[] result;
		if (isNotEmpty(bucketName)) {
			// The bucket name is defined in the setup, not the path!
			if (count == 0) {
				throw new IOException("No object name specified (filename)");
			} else {
				final String fileName = token.nextToken("\0");
				result = new String[] { bucketName, gcsprefix + fileName };
			}
		} else {
			if (getSetup().getBoolean(HOST_GCS_ALLOW_EMPTY_BUCKET_NAME)) {
				result = new String[] { "", gcsprefix + token.nextToken("\0") };
			} else {
				// The name should be in the format bucketName/filename.
				if (count == 0) {
					throw new IOException("No Bucket specified");
				} else if (count == 1) {
					throw new IOException("No object specified (filename)");
				} else {
					final String extractedBucketName = token.nextToken();
//					final String blobName = token.nextToken("\0").substring(1);
					final String fileName = token.nextToken("\0");
					result = new String[] { extractedBucketName, gcsprefix + fileName };
				}
			}
		}
		result[1] = processKey(result[1]);
		if (getDebug())
			_log.debug("BucketName: {}, BlobName: {}", result[0], result[1]);
		return result;
	}

	/**
	 * Process key. Make sure the filename does not have a starting '/'.
	 *
	 * @param key the key
	 * @return the string
	 */
	private static String processKey(final String key) {
		return key.startsWith("/") ? key.substring(1) : key;
	}

	/**
	 * Del. If file does not exist, command still successful
	 *
	 * @param name the name
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void del(final String name) throws IOException { // name can be a folder name, prefix or object name
		_log.debug("Del file {}", name);
		setStatus("DEL");
		final String[] bucketNameAndObject = getBucketNameAndObjectName(name);
		try {
			Blob blob = gcs.get(bucketNameAndObject[0], bucketNameAndObject[1]);

			// for avoiding race conditions, 412 error if precondition does not match
			Storage.BlobSourceOption precondition = Storage.BlobSourceOption.generationMatch(blob.getGeneration());

			gcs.delete(blob.getBucket(), blob.getName(), precondition);

		} catch (Exception e) {
			_log.debug("deleteObject", e);
			throw new IOException("Deleting object " + name + ": " + Format.getMessage(e, "", 0));
		}
	}

	/**
	 * Put.
	 *
	 * @param in   the in
	 * @param name the name
	 * @param posn the posn
	 * @param size the size
	 * @return true, if successful
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public boolean put(final InputStream in, final String name, final long posn, final long size) throws IOException {
		_log.debug("Put file {} ({})", name, posn);
		setStatus("PUT");
		final String[] bucketNameAndObject = getBucketNameAndObjectName(name);
		if (posn > 0)
			throw new IOException("Resume not supported by the " + getSetup().getModuleName() + " module");

		try {
			String bucketName = bucketNameAndObject[0];
			String objectName = bucketNameAndObject[1];

			BlobId objectId = BlobId.of(bucketName, objectName);
			BlobInfo objectInfo = BlobInfo.newBuilder(objectId).build();

			// set to avoid potential race
			// request returns a 412 error if preconditions are not met
			Storage.BlobWriteOption precondition;
			if (gcs.get(bucketName, objectName) == null) {
				// request fails if the object is created before the request runs
				precondition = Storage.BlobWriteOption.doesNotExist();
			} else {
				// If the destination already exists, the request fails if the existing object's
				// generation changes before the request runs
				precondition = Storage.BlobWriteOption.generationMatch(gcs.get(bucketName, objectName).getGeneration());
			}

//			int largeBufferSize = 150 * 1024 * 1024;
//			gcs.createFrom(objectInfo, in, largeBufferSize, precondition);

			gcs.createFrom(objectInfo, in, precondition);

		} catch (Exception e) {
			_log.debug("putObject", e);
			throw new IOException("Pushing object " + name + ": " + Format.getMessage(e, "", 0));

		}
		return true;
	}

	/**
	 * Put.
	 *
	 * @param name the name
	 * @param posn the posn
	 * @param size the size
	 * @return the output stream
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public OutputStream put(final String name, final long posn, final long size) throws IOException {
		_log.warn("Fake put of: {} (posn={})", name, posn);
		setStatus("PUT");

		final String[] bucketNameAndObject = getBucketNameAndObjectName(name);
		if (posn > 0)
			throw new IOException("Resume not supported by the " + getSetup().getModuleName() + " module");
		_log.debug("Using GCS put");

		try {

			BlobId objectId = BlobId.of(bucketNameAndObject[0], bucketNameAndObject[1]);
			BlobInfo objectInfo = BlobInfo.newBuilder(objectId).build();

			return Channels.newOutputStream(gcs.writer(objectInfo));

		} catch (Exception e) {
			_log.debug("putObject", e);
			throw new IOException("Pushing object " + name + ": " + Format.getMessage(e, "", 0));

		}
	}

	/**
	 * Gets the.
	 *
	 * @param name the name
	 * @param posn the posn
	 * @return the input stream
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public InputStream get(String name, long posn) throws IOException {
		_log.debug("Get file " + name);
		setStatus("GET");
		if (posn > 0) {
			throw new IOException("Resume not supported by the " + getSetup().getModuleName() + " module");
		}
		final String[] bucketNameAndObject = getBucketNameAndObjectName(name);

		try {
			gcsInput = Channels.newInputStream(gcs.reader(bucketNameAndObject[0], bucketNameAndObject[1]));

			return gcsInput;
		} catch (Exception e) {
			_log.debug("getObject", e);
			throw new IOException("Getting object " + name + ": " + Format.getMessage(e, "", 0));
		}
	}

	/**
	 * Size.
	 *
	 * @param name the name
	 * @return the long
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public long size(final String name) throws IOException {
		_log.debug("Size {}", name);
		setStatus("SIZE");
		final String[] bucketNameAndObject = getBucketNameAndObjectName(name);
		try {
			// content length of the data in bytes
			return gcs.get(bucketNameAndObject[0], bucketNameAndObject[1],
					Storage.BlobGetOption.fields(Storage.BlobField.values())).getSize();
		} catch (Exception e) {
			_log.debug("getObject", e);
			throw new IOException("Getting object " + name + ": " + Format.getMessage(e));
		}
	}

	/**
	 * Mkdir.
	 *
	 * @param directory the directory
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void mkdir(final String directory) throws IOException {
		_log.debug("Mkdir {}", directory);
		setStatus("MKDIR");
		if (isNotEmpty(bucketName))
			throw new IOException(
					"Mkdir not allowed when parameter " + getSetup().getModuleName() + ".bucketName is set");
		final StringTokenizer token = new StringTokenizer(directory == null ? "" : directory.replace('\\', '/'), "/");
		final int count = token.countTokens();
		if (count == 0) {
			throw new IOException("Invalid directory specified: empty");
		} else if (count == 1) {
			try {
				gcs.create(BucketInfo.newBuilder(token.nextToken()).build());
//				gcs.create(BucketInfo.of(token.nextToken())); // alternative

			} catch (Exception e) {
				_log.debug("createBucket", e);
				throw new IOException("Creating Bucket " + directory + ": " + Format.getMessage(e, "", 0));
			}
		} else {
			throw new IOException("Subdirectories not allowed in Bucket");
		}
	}

	/**
	 * Rmdir.
	 *
	 * @param directory the directory
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void rmdir(final String directory) throws IOException {
		_log.debug("Rmdir {}", directory);
		setStatus("RMDIR");
		if (isNotEmpty(bucketName))
			throw new IOException(
					"Rmdir not allowed when parameter " + getSetup().getModuleName() + ".bucketName is set");
		final StringTokenizer token = new StringTokenizer(directory == null ? "" : directory.replace('\\', '/'), "/");
		final int count = token.countTokens();
		if (count == 0) {
			throw new IOException("Invalid directory specified: empty");
		} else if (count == 1) {
			try {
				gcs.get(token.nextToken()).delete();
			} catch (Exception e) {
				_log.debug("deleteBucket", e);
				throw new IOException("Deleting Bucket " + directory + ": " + Format.getMessage(e, "", 0));
			}
		} else {
			throw new IOException("Subdirectories not allowed in Bucket");
		}
	}

	/**
	 * The Interface ListOutput.
	 */
	private interface ListOutput {

		/**
		 * Adds the.
		 *
		 * @param line the line
		 * @throws IOException Signals that an I/O exception has occurred.
		 */
		public void add(String line) throws IOException;
	}

	/**
	 * List.
	 *
	 * @param output    the output
	 * @param directory the directory
	 * @param pattern   the pattern
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private void list(final ListOutput output, final String directory, final String pattern) throws IOException {
		if (getDebug())
			_log.debug("list: {},{}", directory, pattern);

		final StringTokenizer token = new StringTokenizer(directory == null ? "" : directory.replace('\\', '/'), "/");
		final int count = token.countTokens();
		try {
			if (isNotEmpty(bucketName)) {
				// We have a Bucket defined in the setup!
				if (count == 0) {
					// We are going to list the specified Bucket!
					listObjects(output, bucketName, null, pattern);
				} else {
					// We have a file name specified!
					listObjects(output, bucketName, processKey(token.nextToken("\0")), pattern);
				}
			} else {
				if (count == 0) {
					// We have to display the list of Buckets!
					listBuckets(output, null, pattern);
				} else if (count == 1) {
					final String extractedBucketName = token.nextToken();
					if (extractedBucketName.contains("*") || extractedBucketName.contains("?")) {
						// the name contains wildcards so we will show the list!
						listBuckets(output, bucketName, pattern);
					} else {
						// We are going to list the specified Bucket!
						listObjects(output, extractedBucketName, null, pattern);
					}
				} else {
					// We have a file name specified!
					final String extractedBucketName = token.nextToken();
					listObjects(output, extractedBucketName, processKey(token.nextToken("\0")), pattern);
				}
			}
		} catch (StorageException e) {
			_log.debug("list", e);
			throw new IOException("Listing " + directory + ": " + e.getCode() + " <- " + Format.getMessage(e, "", 0));

		} catch (Exception e) {
			_log.debug("list", e);
			throw new IOException("Listing " + directory + ": " + Format.getMessage(e, "", 0));
		}
	}

	/**
	 * List as string array.
	 *
	 * @param directory the directory
	 * @param pattern   the pattern
	 * @return the string[]
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public String[] listAsStringArray(final String directory, final String pattern) throws IOException {
		_log.debug("listAsStringArray: {},{}", directory, pattern);
		setStatus("LIST");
		final List<String> list = new ArrayList<>();

		list(list::add, directory, pattern);
		return list.toArray(new String[list.size()]);
	}

	/**
	 * List as byte array.
	 *
	 * @param directory the directory
	 * @param pattern   the pattern
	 * @return the byte[]
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public byte[] listAsByteArray(final String directory, final String pattern) throws IOException {
		_log.debug("listAsByteArray: {},{}", directory, pattern);
		setStatus("LIST");
		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		final GZIPOutputStream gzip = new GZIPOutputStream(out, Deflater.BEST_COMPRESSION);

		list(line -> gzip.write(line.concat("\n").getBytes()), directory, pattern);
		gzip.close();
		return out.toByteArray();
	}

	/**
	 * List buckets.
	 *
	 * @param output     the output
	 * @param bucketName the bucket name
	 * @param pattern    the pattern
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private void listBuckets(final ListOutput output, final String bucketName, final String pattern)
			throws IOException {
		if (getDebug())
			_log.debug("listBuckets: {},{}", bucketName, pattern);

		Page<Bucket> buckets = gcs.list();
		for (final Bucket bucket : buckets.iterateAll()) {
			if (bucket != null) {
				final Entity owner = bucket.getOwner();
				final String ownerName = owner != null ? owner.toString() : "unknown";
				final String name = bucket.getName();

				if ((pattern == null || name.matches(pattern))
						&& (bucketName == null || new WildcardFileFilter(bucketName).accept(new File(name))))
					output.add(Format.getFtpList("drw-r--r--", getSetup().get(HOST_GCS_FTPUSER, ownerName),
							getSetup().get(HOST_GCS_FTPGROUP, ownerName), "2048",
							bucket.getCreateTimeOffsetDateTime() != null
									? (bucket.getCreateTimeOffsetDateTime().toEpochSecond() * 1000)
									: new Date().getTime(),
							name));

			}
		}
	}

	/**
	 * List the objects in the specified bucket!
	 *
	 * @param output     the output
	 * @param bucketName the bucket name
	 * @param fileName   the file name
	 * @param pattern    the pattern
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private void listObjects(final ListOutput output, final String bucketName, final String fileName,
			final String pattern) throws IOException {
		if (getDebug())
			_log.debug("listObjects: {},{},{}", bucketName, fileName, pattern);

		final String prefix = gcsprefix + (fileName != null ? fileName : "");

		if (isNotEmpty(prefix)) { // prefix cannot be null, but it can be blank
			if (getDebug()) {
				_log.debug("Using prefix: {}", prefix);
			}
		}

		// listing current level directories and files
		final Iterable<Blob> blobs = gcs
				.list(bucketName, BlobListOption.currentDirectory(),
						Storage.BlobListOption
								.prefix(prefix.isBlank() ? "" : (prefix.endsWith("/") ? prefix : prefix + "/")))
				.iterateAll();

		for (final Blob blob : blobs) {
			String name = blob.getName();
			if (getDebug())
				_log.debug("Processing prefix+filename: {}", name);
			if (isNotEmpty(prefix)) {
				// one of the entries will be an empty one; e.g. prefix = "folder_1/" => name=""
				name = name.substring(prefix.length());
				if (getDebug())
					_log.debug("Name: {} ({})", name, pattern);
			}

			if (pattern == null || name.matches(pattern)) {
				// blob.isDirectory() considers root folder as file when a prefix is given
				final boolean isDirectory = name.endsWith("/");
				final Entity owner = blob.getOwner();
				final String ownerName = owner != null ? owner.toString() : "unknown";

				if (!name.isBlank()) {

					final String entry = Format.getFtpList((isDirectory ? "d" : "-") + "rw-r--r--",
							getSetup().get(HOST_GCS_FTPUSER, ownerName), getSetup().get(HOST_GCS_FTPGROUP, ownerName),
							String.valueOf(blob.getSize()),
							blob.getUpdateTimeOffsetDateTime() != null
									? (blob.getUpdateTimeOffsetDateTime().toEpochSecond() * 1000)
									: new Date().getTime(),
							(isDirectory ? name.substring(0, name.length() - 1) : name));
					output.add(entry);
					if (getDebug())
						_log.debug("Adding entry: {}", entry);
				}
			}
		}
	}

	/**
	 * Gets the setup. Utility call to get the ECtransSetup and check if the module
	 * is not closed!
	 *
	 * @return the setup
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private ECtransSetup getSetup() throws IOException {
		if (closed.get())
			throw new IOException("Module closed");
		return currentSetup;
	}

	/**
	 * Close.
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void close() throws IOException {
		if (closed.compareAndSet(false, true)) {
			_log.debug("Close connection");
			currentStatus = "CLOSE";
			StreamPlugThread.closeQuietly(gcsInput);
			if (gcs != null)
				try {
					gcs.close();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			_log.debug("Close completed");
		} else {
			_log.debug("Already closed");
		}
	}

	/**
	 * Sets the status.
	 *
	 * @param status the status
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private void setStatus(final String status) throws IOException {
		_log.debug("Status set to: {}", status);
		if (closed.get())
			throw new IOException("Module closed");
		currentStatus = status;
	}

}
