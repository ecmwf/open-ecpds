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

#include <sys/socketvar.h>
#include <stdio.h>
#include <ctype.h>
#include <stdlib.h>
#include <unistd.h>
#include <string.h>
#include <pwd.h>
#include <errno.h>
#include <netdb.h>
#include <fcntl.h>
#include <sys/stat.h>
#include <sys/socket.h>
#include <sys/param.h>
#include <netinet/in.h>
#include <signal.h>
#include <time.h>
#include <sys/types.h>
#include <netinet/tcp.h>
#include <openssl/sha.h>

/**
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * ecpds command-line
 *
 * Description:
 * This program is designed to submit data files to OpenECPDS.
 * It supports a wide range of functionalities including:
 * - Connecting to data movers and proxies
 * - Sending and receiving commands and data
 * - Handling various command-line options for configuration
 * - Managing file transfers with options for retries, timeouts, and buffering
 * - Providing verbose and debug information for monitoring and troubleshooting
 * - Supporting operations such as scheduling, starting, stopping, and checking tasks
 * - Ensuring secure and efficient data transmission with error handling and cleanup mechanisms
 *
 * Usage:
 * The program accepts a variety of command-line options to customize its behavior. For detailed usage instructions, run the program with the -help option.
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.9
 * @since 2024-07-01
 */

/* Version of the software */
#define VERSION "6.7.9-22102024"

/* Default list of Master Server */
#define DEFAULT_ECHOSTS_LIST "localhost,host.docker.internal"

/* Default port number */
#define ECPORT "2640";

/* Maximum number of hostnames in the list */
#define MAX_HOSTNAMES 10

/* Secret concatenated with the challenge to compute the response hash */
#define SECRET getenv("ECPDS_SHARED_SECRET")

/* Number of bytes in the challenge sent by the server */
#define CHALLENGE_SIZE 32

/* Length of the SHA-256 hash, which is the output size of the SHA-256 algorithm */
#define RESPONSE_SIZE SHA256_DIGEST_LENGTH

/* Connect timeout used when connecting to Masters/Movers */
int CONNECT_TIMEOUT_IN_SECONDS = 10;

/* Retry count */
int TRY_COUNT = 6;

/* Sleep between connections attempt in seconds */
int TRY_DELAY_IN_SECONDS = 10;

/* The buff size for the IO operations */
int BUFFSIZE = 65536;

/* The source filename */
char *source = NULL;

/* Used for error logs */
int err, debug = 0;

/* Local host name */
char localHostName[256];

/* Used in debug/warn messages */
char date[20];

/**
 * Computes a SHA-256 hash response based on a given challenge and a shared secret.
 * This function concatenates the provided challenge with a predefined secret,
 * then computes the SHA-256 hash of the concatenated string and stores the result in the response buffer.
 *
 * @param challenge The challenge string provided by the server.
 * @param response The buffer where the computed SHA-256 hash will be stored.
 */
void compute_response(const char *challenge, unsigned char *response) {
    char buffer[CHALLENGE_SIZE + strlen(SECRET) + 1];   
    snprintf(buffer, sizeof(buffer), "%s%s", challenge, SECRET);
    SHA256((unsigned char *)buffer, strlen(buffer), response);
}

/**
 * Shuffles an array of strings using the Fisher-Yates shuffle algorithm.
 * This function randomly permutes the elements of the array.
 *
 * @param arr The array of strings to be shuffled.
 * @param size The size of the array.
 */
void shuffleArray(char *arr[], int size)
{
	// Initialize random number generator
	srand(time(NULL));

	// Perform Fisher-Yates shuffle
	for (int i = size - 1; i > 0; i--)
	{
		// Generate a random index between 0 and i
		int j = rand() % (i + 1);

		// Swap arr[i] and arr[j]
		char *temp = arr[i];
		arr[i] = arr[j];
		arr[j] = temp;
	}
}

/**
 * Displays a message with or without errno information.
 * If errno is set, the function includes the corresponding error message.
 *
 * @param prompt The prompt to display (e.g., "error" or "warning").
 * @param str The message to display.
 */
void message(char *prompt, char *str)
{
	if (errno != 0)
	{
		fprintf(stderr, "%s: %s - %s\n", prompt, str, strerror(errno));
		errno = 0; // Reset errno after displaying the message
	}
	else
	{
		fprintf(stderr, "%s: %s\n", prompt, str);
	}
}

/**
 * Displays an error message.
 * This function uses the message function to display the error.
 *
 * @param str The error message to display.
 */
void error(char *str)
{
	message("error", str);
}

/**
 * Displays a warning message.
 * This function uses the message function to display the warning.
 *
 * @param str The warning message to display.
 */
void warning(char *str)
{
	message("warning", str);
}

/**
 * Retrieves the local hostname.
 * This function gets the local hostname and returns it. If an error occurs, it returns "[unknown]".
 *
 * @return The local hostname or "[unknown]" if an error occurs.
 */
char *getHostName()
{
	if (gethostname(localHostName, sizeof(localHostName)) == 0)
	{
		return localHostName;
	}
	else
	{
		snprintf(localHostName, sizeof(localHostName), "[unknown]");
		return localHostName;
	}
}

/**
 * Retrieves the current time formatted as a string.
 * This function gets the current local time and formats it as "MM/DD/YY HH:MM:SS".
 *
 * @return The formatted current time as a string.
 */
char *getTime()
{
	time_t curtime = time(NULL);
	struct tm *loctime = localtime(&curtime);
	strftime(date, sizeof(date), "%D %T", loctime);
	return date;
}

/**
 * Attempts to establish a non-blocking connection to a specified address with a timeout.
 * The function sets the socket to non-blocking mode, attempts to connect, and waits for the
 * connection to be established within the given timeout period. If the connection is successful,
 * the socket is reset to its original state. The function also includes optional debugging output.
 *
 * @param sockno The socket file descriptor.
 * @param addr The address to connect to.
 * @param addrlen The length of the address structure.
 * @param timeout The timeout duration for the connection attempt.
 * @return 0 on success, -1 on error, or 1 if the connection timed out.
 */
int connect_wait(int sockno, struct sockaddr *addr, size_t addrlen, struct timeval *timeout)
{
	clock_t start_time, end_time;
	int res, opt;

	// Get socket flags
	if ((opt = fcntl(sockno, F_GETFL, NULL)) < 0)
	{
		perror("fcntl F_GETFL");
		return -1;
	}

	// Set socket non-blocking
	if (fcntl(sockno, F_SETFL, opt | O_NONBLOCK) < 0)
	{
		perror("fcntl F_SETFL O_NONBLOCK");
		return -1;
	}

	// Start measuring the duration
	start_time = clock();

	// Try to connect
	if ((res = connect(sockno, addr, addrlen)) < 0)
	{
		if (errno == EINPROGRESS)
		{
			fd_set wait_set;

			// Make file descriptor set with socket
			FD_ZERO(&wait_set);
			FD_SET(sockno, &wait_set);

			// Wait for socket to be writable; return after given timeout
			res = select(sockno + 1, NULL, &wait_set, NULL, timeout);
		}
	}
	else
	{
		// Connection was successful immediately
		res = 1;
	}

	// Stop measuring the duration
	end_time = clock();

	// Reset socket flags
	if (fcntl(sockno, F_SETFL, opt) < 0)
	{
		perror("fcntl F_SETFL reset");
		return -1;
	}

	// An error occurred in connect or select
	if (res < 0)
	{
		perror("connect/select error");
		return -1;
	}
	else if (res == 0)
	{
		// Select timed out
		errno = ETIMEDOUT;
		return 1;
	}
	else
	{
		socklen_t len = sizeof(opt);

		// Check for errors in socket layer
		if (getsockopt(sockno, SOL_SOCKET, SO_ERROR, &opt, &len) < 0)
		{
			perror("getsockopt");
			return -1;
		}

		// There was an error
		if (opt)
		{
			errno = opt;
			return -1;
		}
	}

	// Calculate the duration in seconds
	if (debug)
	{
		double duration = (double)(end_time - start_time) / CLOCKS_PER_SEC;
		fprintf(stderr, "[%.19s] DEBUG: connected(%d) duration=%.2f second(s)\n",
				getTime(), sockno, duration);
	}

	return 0;
}

/**
 * Reads a line of text from a socket descriptor, stopping at a newline character.
 * The function reads one character at a time from the socket until a newline character
 * is encountered or the buffer is full. The newline character is replaced with a null
 * terminator to form a proper C string. The function also includes optional debugging output.
 *
 * @param sd The socket file descriptor.
 * @param buf The buffer to store the read line.
 * @param size The size of the buffer.
 * @return the number of bytes read on success, or -1 on error.
 */
int readLine(int sd, char *buf, int size)
{
	int i = 0;
	int err;

	for (i = 0; i < (size - 1); i++)
	{
		if ((err = read(sd, buf + i, 1)) != 1)
		{
			error("reading message from server (read)");
			return -1;
		}
		if (buf[i] == '\n')
		{
			buf[i] = '\0';
			break;
		}
	}

	// Ensure null termination within bounds
	if (i < size - 1)
	{
		buf[i + 1] = '\0';
	}
	else
	{
		buf[size - 1] = '\0';
	}

	if (debug)
	{
		fprintf(stderr, "[%.19s] DEBUG: readLine(%d) %d byte(s) [%s]\n",
				getTime(), sd, i, buf);
	}

	return i; // Return the number of bytes read
}

/**
 * Opens a connection to a specified hostname and port with a timeout and optional retry mechanism.
 * The function resolves the hostname, creates a socket, and attempts to connect to the specified
 * port within the given timeout period. It also sets socket options such as SO_KEEPALIVE and
 * includes optional debugging output. If the connection fails, the function retries the connection
 * a specified number of times with a delay between attempts.
 *
 * @param hostname The hostname to connect to.
 * @param port The port number to connect to, as a string.
 * @param timeout_seconds The timeout duration for each connection attempt, in seconds.
 * @return The socket descriptor on success, or -1 on failure.
 */
int openConnection(char *hostname, char *port, int timeout_seconds)
{
	struct sockaddr_in sa;
	struct sockaddr_in si;
	struct hostent *host;
	int on = 1;
	unsigned int len;
	short p;
	int s;
	int r1;
	int r2 = -1;
	int err;

	// Convert port to integer
	if ((p = atoi(port)) == 0)
	{
		error("setting port (atoi)");
		return -1;
	}

	// Create socket
	if ((s = socket(AF_INET, SOCK_STREAM, 0)) == -1)
	{
		error("creating end-point for communication (socket)");
		return -1;
	}

	// Resolve hostname
	if ((host = gethostbyname(hostname)) == NULL)
	{
		error("querying domain name information (gethostbyname)");
		return -1;
	}

	// Initialize local address structure
	memset(&sa, 0, sizeof(sa));
	sa.sin_port = htons(0);
	sa.sin_family = AF_INET;

	// Bind to a reserved port if possible
	if (bindresvport(s, &sa) >= 0)
	{
		if (debug)
		{
			fprintf(stderr, "[%.19s] DEBUG: local port set to %d\n", getTime(), ntohs(sa.sin_port));
		}
	}
	else
	{
		if (debug)
		{
			fprintf(stderr, "[%.19s] DEBUG: socket binded to an unprivileged port (bindresvport failure)\n", getTime());
		}
	}

	// Initialize server address structure
	memset(&si, 0, sizeof(si));
	si.sin_port = htons(p);
	si.sin_family = AF_INET;
	memcpy(&si.sin_addr, host->h_addr, host->h_length);

	// Define a timeout for the connection
	struct timeval timeout;
	timeout.tv_sec = timeout_seconds;
	timeout.tv_usec = 0;

	// Attempt to connect with timeout
	if ((err = connect_wait(s, (struct sockaddr *)&si, sizeof(si), &timeout)) == -1)
	{
		if (debug)
		{
			fprintf(stderr, "[%.19s] DEBUG: connection failed to %s:%s\n", getTime(), hostname, port);
		}
		return -1;
	}

	// Get local socket address
	len = sizeof(si);
	if (getsockname(s, (struct sockaddr *)&si, &len) < 0)
	{
		error("getting socket bind address (getsockname)");
		return -1;
	}

	// Set socket options
	if ((r1 = setsockopt(s, SOL_SOCKET, SO_KEEPALIVE, &on, sizeof(on))) < 0)
	{
		warning("setting SO_KEEPALIVE options (setsockopt)");
	}

	if (debug)
	{
		fprintf(stderr, "[%.19s] DEBUG: connected(%d) on %s:%s(local=%d) (SO_KEEPALIVE=%d,TCP_NODELAY=%d)\n",
				getTime(), s, hostname, port, ntohs(si.sin_port), r1, r2);
	}

	return s;
}

/**
 * Attempts to establish a connection to one of the provided hostnames on the specified port.
 * The function tries to connect to each hostname in a randomized order, with a retry mechanism
 * that includes a delay between attempts. If a connection is successfully established, the socket
 * descriptor is returned. If all attempts fail, the function returns -1.
 *
 * @param hostnames A comma-separated string of hostnames to attempt connections to.
 * @param port The port number to connect to, as a string.
 * @param timeout_seconds The timeout duration for each connection attempt, in seconds.
 * @return The socket descriptor on success, or -1 on failure.
 */
int tryConnection(char *hostnames, char *port, int timeout_seconds)
{
	char *token;
	char *hostnamesList[MAX_HOSTNAMES];
	int numHostnames = 0;
	int s = -1;
	char *buff = strdup(hostnames); // Use strdup for simplicity
	if (!buff)
	{
		error("memory allocation failed (strdup)");
		return -1;
	}

	// Parse the string and store hostnames in the array
	token = strtok(buff, ",");
	while (token != NULL && numHostnames < MAX_HOSTNAMES)
	{
		hostnamesList[numHostnames] = token;
		numHostnames++;
		token = strtok(NULL, ",");
	}

	// Shuffle the array in a random order
	shuffleArray(hostnamesList, numHostnames);

	// Retry mechanism
	for (int i = 0; s == -1 && i < TRY_COUNT; i++)
	{
		// Try to connect using the shuffled hostname list
		for (int j = 0; j < numHostnames; j++)
		{
			s = openConnection(hostnamesList[j], port, timeout_seconds);
			if (s != -1)
			{
				free(buff); // Free the buffer before returning
				return s;
			}
		}
		if (i + 1 < TRY_COUNT)
		{
			if (debug)
			{
				fprintf(stderr, "[%.19s] DEBUG: connect failed (%d/%d) - waiting for %d seconds\n",
						getTime(), i + 1, TRY_COUNT, TRY_DELAY_IN_SECONDS);
			}
			sleep(TRY_DELAY_IN_SECONDS);
		}
	}

	// An error occurred
	char buf[strlen(hostnames) + 256];
	sprintf(buf, "connection failed to [%s]:%s", hostnames, port);
	error(buf);

	free(buff); // Free the buffer before returning
	return -1;
}

/**
 * Sends a key-value pair message to a server via a socket descriptor.
 * The function formats the key and value into a single message, checks for potential buffer overflow,
 * and writes the message to the socket. It includes error handling and optional debugging output.
 *
 * @param sd The socket file descriptor.
 * @param key The key part of the message.
 * @param value The value part of the message.
 * @return 0 on success, or -1 on error.
 */
int sendCommand(int sd, char *key, char *value)
{
	if (key == NULL || value == NULL)
		return 0;

	char buf[MAXPATHLEN + 256];
	int valueLength = strlen(value);
	if ((strlen(key) + valueLength + 2) >= (MAXPATHLEN + 256))
	{
		sprintf(buf, "sending command to server (buffer overflow)");
		error(buf);
		return -1;
	}

	if (valueLength > 0)
	{
		sprintf(buf, "%s %s\n", key, value);
	}
	else
	{
		sprintf(buf, "%s\n", key);
	}

	if (debug)
		fprintf(stderr, "[%.19s] DEBUG: write(%d) %s", getTime(), sd, buf);

	int length = strlen(buf);
	int size = 0;
	while (size != length)
	{
		int current;
		if ((current = write(sd, buf, length - size)) <= 0)
		{
			sprintf(buf, "sending %s to server (write)", key);
			error(buf);
			return -1;
		}
		size += current;
	}

	if (debug)
		fprintf(stderr, "[%.19s] DEBUG: %d byte(s) sent\n", getTime(), size);

	return 0;
}

/**
 * Sends an action command to a server via a socket descriptor.
 * This function is a wrapper around sendCommand, used for sending commands that do not require parameters.
 *
 * @param sd The socket file descriptor.
 * @param key The command to be sent.
 * @return 0 on success, or -1 on error.
 */
int sendAction(int sd, char *key)
{
	return sendCommand(sd, key, "");
}

/**
 * Sends a boolean value to a server via a socket descriptor.
 * This function sends the string "true" if the condition is true, otherwise it does nothing.
 *
 * @param sd The socket file descriptor.
 * @param key The key associated with the boolean value.
 * @param cond The condition to be evaluated.
 * @return 0 if the condition is false, or the result of sendCommand if true.
 */
int sendBoolean(int sd, char *key, int cond)
{
	if (cond)
	{
		return sendCommand(sd, key, "true");
	}
	else
	{
		return 0;
	}
}

/**
 * Sends an integer value to a server via a socket descriptor.
 * This function converts the integer value to a string and sends it using sendCommand.
 *
 * @param sd The socket file descriptor.
 * @param key The key associated with the integer value.
 * @param value The integer value to be sent.
 * @return The result of sendCommand.
 */
int sendInteger(int sd, char *key, int value)
{
	char buf[256];
	snprintf(buf, sizeof(buf), "%d", value);
	return sendCommand(sd, key, buf);
}

/**
 * Sends a 64-bit long integer value to a server via a socket descriptor.
 * This function converts the 64-bit integer value to a string and sends it using sendCommand.
 *
 * @param sd The socket file descriptor.
 * @param key The key associated with the 64-bit integer value.
 * @param value The 64-bit integer value to be sent.
 * @return The result of sendCommand.
 */
int sendLong64(int sd, char *key, off64_t value)
{
	char buf[256];
	snprintf(buf, sizeof(buf), "%lld", value);
	return sendCommand(sd, key, buf);
}

/**
 * Receives a command from a server via a socket descriptor.
 * This function reads a line from the socket, processes the command, and extracts the value if a key is provided.
 * It includes error handling and optional debugging output.
 *
 * @param sd The socket file descriptor.
 * @param key The key to match in the received command (can be NULL).
 * @param value The buffer to store the extracted value (if key is matched).
 * @param length The length of the value buffer.
 * @return 0 on success, or -1 on error.
 */
int receiveCommand(int sd, char *key, char *value, int length)
{
	if (debug)
	{
		fprintf(stderr, "[%.19s] DEBUG: read(%d) %s\n", getTime(), sd, key == NULL ? "line" : key);
	}

	char buf[MAXPATHLEN + 256];
	int i = 0;
	int err;

	// Read from the socket until a newline character is encountered
	while (i == 0 || buf[i - 1] != '\n')
	{
		if (i >= (MAXPATHLEN + 256))
		{
			error("receiving message from server (buffer overflow)");
			return -1;
		}
		else
		{
			if ((err = read(sd, buf + i, 1)) != 1)
			{
				error("receiving message from server (read)");
				return -1;
			}
			i++;
		}
	}

	buf[i - 1] = '\0'; // Null-terminate the string

	if (debug)
	{
		fprintf(stderr, "[%.19s] DEBUG: read(%d) %d byte(s) [%s]\n", getTime(), sd, i, buf);
	}

	char *c = NULL;
	if (buf[0] == '-')
	{
		error(buf + 1); // Handle error message from server
		return -1;
	}
	else
	{
		c = buf[0] == '+' ? buf + 1 : buf; // Skip the '+' character if present
		if (value != NULL)
		{
			int clength = strlen(c);
			if (key != NULL)
			{
				int klength = strlen(key);
				if (clength > klength && strncmp(c, key, klength) == 0)
				{
					if (length <= (clength - klength - 1))
					{
						error("allocating message from server (buffer overflow)");
						return -1;
					}
					else
					{
						strcpy(value, c + klength + 1); // Copy the value part
					}
				}
			}
			else
			{
				if (length <= clength)
				{
					error("allocating message from server (buffer overflow)");
					return -1;
				}
				else
				{
					strcpy(value, c); // Copy the entire message
				}
			}
		}
	}

	if (debug)
	{
		fprintf(stderr, "[%.19s] DEBUG: received(%d) %s\n", getTime(), sd, c);
	}

	return 0;
}

/**
 * Splits a string at the last occurrence of a separator and returns the next parameter.
 * This function modifies the input string by inserting a null terminator at the position
 * of the last occurrence of the separator, effectively splitting the string into two parts.
 *
 * @param msg The input string to be split.
 * @param separator The character used as the separator.
 * @return A pointer to the next parameter after the separator, or NULL if the separator is not found.
 */
char *nextParam(char *msg, char separator)
{
	char *next;

	// Find the last occurrence of the separator
	if ((next = strrchr(msg, separator)) == NULL)
	{
		return NULL;
	}

	// Insert a null terminator to split the string
	*next = '\0';

	// Return the next parameter after the separator
	return next + 1;
}

/**
 * POSIX signal handler.
 * This function is called when a signal is received.
 *
 * @param sig The signal number.
 */
void handler(int sig)
{
	// Handle the signal (currently empty)
}

/**
 * Signal handler for SIGALRM to handle timeouts while reading from stdin.
 * This function reports a timeout error, removes a temporary file if defined, and exits the program.
 *
 * @param sig The signal number (expected to be SIGALRM).
 */
void sig_alarm(int sig)
{
	error("timeout occurred while reading from stdin");
	/* If the source file is defined then let's remove it (temporary file) */
	if (source != NULL)
	{
		remove(source);
	}
	exit(-1);
}

/* Macro to enable the timeout alarm (5 minutes) */
#define timeout_on() (signal(SIGALRM, sig_alarm), alarm(5 * 60))

/* Macro to disable the timeout alarm */
#define timeout_off() alarm(0)

/**
 * Displays the help message (usage) for the program.
 * This function prints the usage instructions and available command-line options to stdout.
 *
 * @return Always returns 1.
 */
int usage(void)
{
	fprintf(stdout, "ECpds-v%s\n\n", VERSION);
	fprintf(stdout, "usage: ecpds -destination name -source filename (*)\n");
	fprintf(stdout, "       ecpds [-expected|-started|-completed|-reset] [-at arg] -metadata metadata (**)\n");
	fprintf(stdout, "       ecpds -scheduler [-start|-stop|-check] [-destination name] [-streams arg] [-timeout arg]] (***)\n");
	fprintf(stdout, "       ecpds -waitfor groupby (****)\n");
	fprintf(stdout, "\n");
	fprintf(stdout, "  DataFiles unicity is based on the target, destination, version and standby\n");
	fprintf(stdout, "  flag association.\n");
	fprintf(stdout, "\n");
	fprintf(stdout, " -destination {arg} - destination name\n");
	fprintf(stdout, " -source      {arg} - source file name (default: stdin)\n");
	fprintf(stdout, " -priority    {arg} - transmission priority 0-99 (default: 99)\n");
	fprintf(stdout, " -metadata    {arg} - metadata(s) (param=value,...)\n");
	fprintf(stdout, " -target      {arg} - target file name (default: source file name)\n");
	fprintf(stdout, " -identity    {arg} - identity of the product (default: target file name)\n");
	fprintf(stdout, " -lifetime    {arg} - lifetime of the data file (default: 2d) (*****)\n");
	fprintf(stdout, " -delay       {arg} - transmission delay (default: immediate transfer) (*****)\n");
	fprintf(stdout, " -at          {arg} - transmission date (default: immediate transfer)\n");
	fprintf(stdout, " -format      {arg} - define the date format (default: yyyyMMddHHmmss)\n");
	fprintf(stdout, " -group       {arg} - define the transfer group (default: random)\n");
	fprintf(stdout, " -version     {arg} - optional version associated with the DataFile\n");
	fprintf(stdout, " -reqid       {arg} - optional DataFileId for the requeue/purge option\n");
	fprintf(stdout, " -groupby     {arg} - organise transfers by groups\n");
	fprintf(stdout, " -echost      {arg} - dns name of the Master\n");
	fprintf(stdout, " -streams     {arg} - maximum number of retrieval streams (scheduler/check)\n");
	fprintf(stdout, " -timeout     {arg} - timeout for each retrieval stream (scheduler/check)\n");
	fprintf(stdout, " -index             - in groupby mode source file is index of source files\n");
	fprintf(stdout, " -noretrieval       - file not retrieved in groupby mode (taken from source)\n");
	fprintf(stdout, " -expected          - the task is identified with the metadata(s)\n");
	fprintf(stdout, " -started           - the task is identified with the metadata(s)\n");
	fprintf(stdout, " -completed         - the task is identified with the metadata(s)\n");
	fprintf(stdout, " -reset             - the task is identified with the metadata(s)\n");
	fprintf(stdout, " -buffer            - the task is identified with the metadata(s) (*****)\n");
	fprintf(stdout, " -asap              - send file as soon as possible (******)\n");
	fprintf(stdout, " -event             - notification triggered once data is available (e.g. mqtt)\n");
	fprintf(stdout, " -standby           - spool the data file only\n");
	fprintf(stdout, " -remove            - remove source when transfer successful\n");
	fprintf(stdout, " -requeue           - requeue a dataFile and reset the related transfer(s)\n");
	fprintf(stdout, " -purge             - purge the dataFile and the related transfer(s)\n");
	fprintf(stdout, " -force             - force a requeue when a duplicate dataFile is found\n");
	fprintf(stdout, " -buffsize          - buffer size for read and write (default: %d bytes)\n", BUFFSIZE);
	fprintf(stdout, " -connectTimeoutSec - when connecting to Masters/Movers (default: %d seconds)\n", CONNECT_TIMEOUT_IN_SECONDS);
	fprintf(stdout, " -tryCount          - when connecting to Masters/Movers (default: %d)\n", TRY_COUNT);
	fprintf(stdout, " -tryDelaySec       - when connecting to Masters/Movers (default: %d seconds)\n", TRY_DELAY_IN_SECONDS);
	fprintf(stdout, " -verbose           - verbose mode on\n");
	fprintf(stdout, " -start             - (re)start of specified destination\n");
	fprintf(stdout, " -stop              - graceful stop of specified destination\n");
	fprintf(stdout, " -opts              - send debug options\n");
	fprintf(stdout, " -help              - this message\n");
	fprintf(stdout, " -v                 - version number\n");
	fprintf(stdout, "\n");
	fprintf(stdout, "     (*) If successful, a DataFileID is returned, which can be used to keep track\n");
	fprintf(stdout, "         of the transfer requests through the web interface.\n");
	fprintf(stdout, "    (**) Notify the monitoring module that a task is expected/started/completed.\n");
	fprintf(stdout, "   (***) Allow starting or stopping the download of the preset files on ecpds.\n");
	fprintf(stdout, "  (****) Wait for a group of preset files to be retrieved on ecpds.\n");
	fprintf(stdout, " (*****) Duration in weeks, days, hours, minutes or seconds (e.g. 1w|2d).\n");
	fprintf(stdout, "(******) File sent once all the files from the same group are retrieved.\n");

	return 1;
}

/**
 * Main function for the command.
 * This function initializes necessary variables, sets up signal handlers, and parses command-line options.
 * It handles various options such as setting connection timeouts, specifying source and target files,
 * and configuring other parameters. The function also includes error handling and usage instructions.
 *
 * @param argc The number of command-line arguments.
 * @param argv The array of command-line arguments.
 * @return 0 on successful execution, or a non-zero error code on failure.
 */
int main(int argc, char *argv[])
{
	char buf[MAXPATHLEN + 256];
	char ecproxyHost[128];
	char ecproxyPort[16];
	char hosts[512];
	char message[512];
	char stats[512];
	struct stat64 stcert;
	struct passwd *pw = NULL;
	off64_t filelen = 0;
	off64_t fileres = 0;
	int sd = -1, pd = -1, fd = -1, verb = 0,
		requeue = 0, force = 0, purge = 0, remv = 0, timefile = 0, del = 0,
		useTmpFile = 0, fsize = 0, res = -1, standby = 0, index = 0,
		expected = 0, started = 0, completed = 0, reset = 0, statsrc, tst = -1, local = 1, scheduler = 0, start = 0, stop = 0, asap = 0,
		event = 0, check = 0, noretrieval = 0, buffsize = BUFFSIZE;
	char *caller = NULL, *echost = NULL, *originalechost = NULL, *ecport = NULL, *ecuser = NULL,
		 *resolved = NULL, *format = NULL, *group = NULL, *reqid = NULL,
		 *destination = NULL, *delay = NULL, *tmp = NULL, *priority = NULL,
		 *lifetime = NULL, *at = NULL, *metadata = NULL, *original = NULL,
		 *target = NULL, *version = NULL, *identity = NULL, *opts = NULL,
		 *ecproxy = NULL, *groupby = NULL, *waitfor = NULL, *streams = NULL,
		 *timeout = NULL, *bufferMon = NULL;

	struct sigaction act, oact;

	/* POSIX signal handler */
	act.sa_handler = handler;
	sigemptyset(&act.sa_mask);
	act.sa_flags = 0;
	sigaction(SIGPIPE, &act, &oact);

	argc--;
	argv++;

	/* Let's parse the options */
	while (argc >= 1)
	{
		if (strcmp(*argv, "-echost") == 0)
		{
			if (--argc < 1)
				return usage();
			echost = *(++argv);
		}
		else if (strcmp(*argv, "-caller") == 0)
		{
			if (--argc < 1)
				return usage();
			caller = *(++argv);
		}
		else if (strcmp(*argv, "-ecport") == 0)
		{
			if (--argc < 1)
				return usage();
			ecport = *(++argv);
		}
		else if (strcmp(*argv, "-connectTimeoutSec") == 0)
		{
			if (--argc < 1)
				return usage();
			if ((CONNECT_TIMEOUT_IN_SECONDS = strtol(*(++argv), NULL, 10)) <= 0)
			{
				error("-connectTimeout must be positive");
				return usage();
			}
		}
		else if (strcmp(*argv, "-tryCount") == 0)
		{
			if (--argc < 1)
				return usage();
			if ((TRY_COUNT = strtol(*(++argv), NULL, 10)) <= 0)
			{
				error("-tryCount must be positive");
				return usage();
			}
		}
		else if (strcmp(*argv, "-tryDelaySec") == 0)
		{
			if (--argc < 1)
				return usage();
			if ((TRY_DELAY_IN_SECONDS = strtol(*(++argv), NULL, 10)) <= 0)
			{
				error("-tryDelay must be positive");
				return usage();
			}
		}
		else if (strcmp(*argv, "-original") == 0)
		{
			if (--argc < 1)
				return usage();
			tmp = *(++argv);
		}
		else if (strcmp(*argv, "-identity") == 0)
		{
			if (--argc < 1)
				return usage();
			identity = *(++argv);
		}
		else if (strcmp(*argv, "-format") == 0)
		{
			if (--argc < 1)
				return usage();
			format = *(++argv);
		}
		else if (strcmp(*argv, "-reqid") == 0)
		{
			if (--argc < 1)
				return usage();
			reqid = *(++argv);
		}
		else if (strcmp(*argv, "-group") == 0)
		{
			if (--argc < 1)
				return usage();
			group = *(++argv);
		}
		else if (strcmp(*argv, "-scheduler") == 0)
		{
			scheduler = 1;
		}
		else if (strcmp(*argv, "-start") == 0)
		{
			start = 1;
		}
		else if (strcmp(*argv, "-stop") == 0)
		{
			stop = 1;
		}
		else if (strcmp(*argv, "-check") == 0)
		{
			check = 1;
		}
		else if (strcmp(*argv, "-force") == 0)
		{
			force = 1;
		}
		else if (strcmp(*argv, "-requeue") == 0)
		{
			requeue = 1;
		}
		else if (strcmp(*argv, "-expected") == 0)
		{
			expected = 1;
		}
		else if (strcmp(*argv, "-noretrieval") == 0)
		{
			noretrieval = 1;
		}
		else if (strcmp(*argv, "-started") == 0)
		{
			started = 1;
		}
		else if (strcmp(*argv, "-completed") == 0)
		{
			completed = 1;
		}
		else if (strcmp(*argv, "-reset") == 0)
		{
			reset = 1;
		}
		else if (strcmp(*argv, "-destination") == 0)
		{
			if (--argc < 1)
				return usage();
			destination = *(++argv);
		}
		else if (strcmp(*argv, "-streams") == 0)
		{
			if (--argc < 1)
				return usage();
			streams = *(++argv);
		}
		else if (strcmp(*argv, "-timeout") == 0)
		{
			if (--argc < 1)
				return usage();
			timeout = *(++argv);
		}
		else if (strcmp(*argv, "-priority") == 0)
		{
			long int p = 0;
			if (--argc < 1)
				return usage();
			priority = *(++argv);
			if ((p = strtol(priority, NULL, 10)) < 0 || p > 99)
			{
				error("-priority must be in 0..99");
				return usage();
			}
		}
		else if (strcmp(*argv, "-source") == 0)
		{
			if (--argc < 1)
				return usage();
			source = malloc(MAXPATHLEN);
			if (source == NULL)
			{
				error("allocating memory (malloc)");
				goto clean;
			}
			snprintf(source, MAXPATHLEN, "%s", *(++argv));
		}
		else if (strcmp(*argv, "-target") == 0)
		{
			if (--argc < 1)
				return usage();
			target = malloc(MAXPATHLEN);
			if (target == NULL)
			{
				error("allocating memory (malloc)");
				goto clean;
			}
			snprintf(target, MAXPATHLEN, "%s", *(++argv));
		}
		else if (strcmp(*argv, "-lifetime") == 0)
		{
			if (--argc < 1)
				return usage();
			lifetime = *(++argv);
			if (strtol(lifetime, NULL, 10) <= 0)
			{
				error("-lifetime must be a positive integer");
				return usage();
			}
		}
		else if (strcmp(*argv, "-buffer") == 0)
		{
			if (--argc < 1)
				return usage();
			bufferMon = *(++argv);
			if (strtol(bufferMon, NULL, 10) < 0)
			{
				error("-buffer must be a positive or null integer");
				return usage();
			}
		}
		else if (strcmp(*argv, "-delay") == 0)
		{
			if (--argc < 1)
				return usage();
			delay = *(++argv);
			if (strtol(delay, NULL, 10) <= 0)
			{
				error("-delay must be a positive integer");
				return usage();
			}
		}
		else if (strcmp(*argv, "-buffsize") == 0)
		{
			if (--argc < 1)
				return usage();
			if ((buffsize = strtol(*(++argv), NULL, 10)) <= 0)
			{
				error("-buffsize must be a positive integer");
				return usage();
			}
		}
		else if (strcmp(*argv, "-at") == 0)
		{
			if (--argc < 1)
				return usage();
			at = *(++argv);
		}
		else if (strcmp(*argv, "-metadata") == 0)
		{
			if (--argc < 1)
				return usage();
			metadata = *(++argv);
		}
		else if (strcmp(*argv, "-opts") == 0)
		{
			if (--argc < 1)
				return usage();
			opts = *(++argv);
		}
		else if (strcmp(*argv, "-groupby") == 0)
		{
			if (--argc < 1)
				return usage();
			groupby = *(++argv);
		}
		else if (strcmp(*argv, "-waitfor") == 0)
		{
			if (--argc < 1)
				return usage();
			waitfor = *(++argv);
		}
		else if (strcmp(*argv, "-standby") == 0)
		{
			standby = 1;
		}
		else if (strcmp(*argv, "-index") == 0)
		{
			index = 1;
		}
		else if (strcmp(*argv, "-dontsend") == 0)
		{
			standby = 1;
		}
		else if (strcmp(*argv, "-remove") == 0)
		{
			remv = 1;
		}
		else if (strcmp(*argv, "-purge") == 0)
		{
			purge = 1;
		}
		else if (strcmp(*argv, "-asap") == 0)
		{
			asap = 1;
		}
		else if (strcmp(*argv, "-event") == 0)
		{
			event = 1;
		}
		else if (strcmp(*argv, "-verbose") == 0)
		{
			verb = 1;
		}
		else if (strcmp(*argv, "-version") == 0)
		{
			if (--argc < 1)
				return usage();
			version = *(++argv);
		}
		else if (strcmp(*argv, "-v") == 0)
		{
			fprintf(stdout, "ecpds version %s\n", VERSION);
			return (1);
		}
		else if (strcmp(*argv, "-debug") == 0)
		{
			debug = 1;
			verb = 1;
		}
		else if (strcmp(*argv, "-s") == 0 || (tst = strcmp(*argv, "-M")) == 0)
		{
			char *p;
			char q;
			if (--argc < 1)
				return usage();
			if (fsize != 0)
			{
				error("-s and -M are incompatible");
				return usage();
			}
			if ((fsize = strtol(*(++argv), &p, 10)) <= 0)
			{
				error("-s|-M must be a positive integer");
				return usage();
			}
			fsize <<= (q = tolower(*p)) == 'k' ? 10 : q == 'm' ? 20
															   : 0;
			/* maximum file size indicated by negative value */
			if (tst == 0)
				fsize = -fsize;
		}
		else
		{
			if (strcmp(*argv, "-help") != 0)
				fprintf(stderr, "error: illegal option %s\n", *argv);
			return usage();
		}
		argc--;
		argv++;
	}

	/**
	 * The following validate command-line options for compatibility and correctness.
	 * Checks for mutually exclusive options and ensures that required options are used together.
	 * It provides error messages and usage instructions if any validation fails.
	 */

	if (noretrieval && !groupby)
	{
		error("-noretrieval can only be used with -groupby");
		return usage();
	}

	if (!scheduler && (start || stop || check))
	{
		error("-start, -stop and -check are only valid with -scheduler");
		return usage();
	}

	if (!(scheduler && check) && (timeout != NULL || streams != NULL))
	{
		error("-timeout and -streams are only valid with '-scheduler -check'");
		return usage();
	}

	if (scheduler && !start && !stop && !check)
	{
		error("-scheduler requires -start, -stop, or -check");
		return usage();
	}

	if ((start && stop) || (start && check) || (stop && check))
	{
		error("-start, -stop, and -check are incompatible");
		return usage();
	}

	if (force && requeue)
	{
		error("-force and -requeue are incompatible");
		return usage();
	}

	if (groupby && remv)
	{
		error("-groupby and -remove are incompatible");
		return usage();
	}

	if (index && !groupby)
	{
		error("-index is only available with -groupby");
		return usage();
	}

	if (purge && (force || requeue))
	{
		error("-force and -requeue are incompatible with -purge");
		return usage();
	}

	/**
	 * If reading from stdin, create a temporary file and store the data.
	 * This block handles the case where input is expected from stdin and stores it in a temporary file.
	 * It ensures that the -target option is provided when reading from stdin.
	 */
	if (source == NULL && waitfor == NULL && !scheduler && !purge && !completed && !started && !expected && !reset)
	{
		// Ensure the -target option is provided when reading from stdin
		if (target == NULL)
		{
			error("-target option is mandatory when expecting input from stdin");
			return usage();
		}
		else
		{
			del = 1;
			useTmpFile = 1;

			// Create a temporary file to store stdin data
			char template[] = "/tmp/tmpfileXXXXXX";
			if ((sd = mkstemp(template)) != -1)
			{
				source = strdup(template); // Store the name of the temporary file
				filelen = 0;

				// Read from stdin and write to the temporary file
				while ((timeout_on(), err = read(STDIN_FILENO, buf, sizeof(buf))) > 0)
				{
					filelen += err;
					if (write(sd, buf, err) <= 0)
					{
						error("writing temporary file (write)");
						goto clean;
					}
				}

				// Debugging output for the number of bytes received from stdin
				if (debug)
				{
					fprintf(stderr, "[%.19s] DEBUG: %lld bytes received from stdin\n", getTime(), filelen);
				}
			}
			else
			{
				error("creating temporary file (mkstemp)");
				goto clean;
			}

			// Disable the timeout and set the remove flag
			timeout_off();
			remv = 1;
			close(sd);
			sd = -1;
		}
	}

	/**
	 * If there is no target specified, create one from the source file name.
	 * This block handles the case where the target is not specified and derives it from the source file name.
	 */
	if (target == NULL && waitfor == NULL && !scheduler && !purge && !completed && !expected && !started && !reset)
	{
		// Allocate memory for the target
		target = malloc(MAXPATHLEN);
		if (target == NULL)
		{
			error("allocating memory (malloc)");
			goto clean;
		}

		// Extract the file name from the source path
		char *slash;
		if ((slash = strrchr(source, '/')) == NULL)
		{
			snprintf(target, MAXPATHLEN, "%s", source);
		}
		else
		{
			snprintf(target, MAXPATHLEN, "%s", slash + 1);
		}
	}

	/**
	 * Save the original echost used to send back to the Master.
	 * This block saves the original echost value for later use.
	 */
	originalechost = malloc(MAXPATHLEN);
	if (echost != NULL)
	{
		strcpy(originalechost, echost);
	}
	else
	{
		strcpy(originalechost, "[default]");
	}

	/**
	 * Use default values for missing parameters.
	 * This block sets default values for parameters that are not provided.
	 */
	if (caller == NULL)
	{
		caller = getenv("EC_job_stdout");
	}
	if (echost == NULL)
	{
		echost = getenv("ECHOST");
	}
	if (ecport == NULL)
	{
		ecport = getenv("ECPORT");
	}
	if (echost == NULL)
	{
		echost = DEFAULT_ECHOSTS_LIST;
	}
	if (ecport == NULL)
	{
		ecport = ECPORT;
	}

	/**
	 * Determine who is using this command.
	 * This block retrieves the username of the current user.
	 */
	if ((pw = getpwuid(getuid())) == NULL)
	{
		error("getting password file entry (getpwuid)");
		goto clean;
	}
	else
	{
		ecuser = pw->pw_name;
	}

	/**
	 * Display verbose information if the verbose flag is set.
	 * This block prints various pieces of information to stderr for debugging and informational purposes.
	 */
	if (verb)
	{
		fprintf(stderr, "[%.19s] INFO: requested=%s\n", getTime(), originalechost);
		fprintf(stderr, "[%.19s] INFO: echost=%s\n", getTime(), echost);
		fprintf(stderr, "[%.19s] INFO: ecport=%s\n", getTime(), ecport);
		fprintf(stderr, "[%.19s] INFO: ecuser=%s\n", getTime(), ecuser);
		if (caller != NULL)
		{
			fprintf(stderr, "[%.19s] INFO: caller=%s\n", getTime(), caller);
		}
	}

	/**
	 * Display additional verbose information if certain conditions are met.
	 * This block prints more detailed information about the operation if the verbose flag is set and specific conditions are not met.
	 */
	if (waitfor == NULL && !scheduler && !completed && !expected && !started && !reset && verb)
	{
		fprintf(stderr, "[%.19s] INFO: destination=%s\n", getTime(), destination);
		fprintf(stderr, "[%.19s] INFO: priority=%s\n", getTime(), priority);
		fprintf(stderr, "[%.19s] INFO: source=%s\n", getTime(), source);
		fprintf(stderr, "[%.19s] INFO: target=%s\n", getTime(), target);
		fprintf(stderr, "[%.19s] INFO: lifetime=%s\n", getTime(), lifetime);
		fprintf(stderr, "[%.19s] INFO: delay=%s\n", getTime(), delay);
		fprintf(stderr, "[%.19s] INFO: at=%s\n", getTime(), at);
		fprintf(stderr, "[%.19s] INFO: metadata=%s\n", getTime(), metadata);
		fprintf(stderr, "[%.19s] INFO: groupby=%s\n", getTime(), groupby != NULL ? groupby : "[none]");
		fprintf(stderr, "[%.19s] INFO: noretrieval=%s\n", getTime(), noretrieval ? "true" : "false");
		fprintf(stderr, "[%.19s] INFO: force=%s\n", getTime(), force ? "true" : "false");
		fprintf(stderr, "[%.19s] INFO: requeue=%s\n", getTime(), requeue ? "true" : "false");
		fprintf(stderr, "[%.19s] INFO: standby=%s\n", getTime(), standby ? "true" : "false");
		fprintf(stderr, "[%.19s] INFO: asap=%s\n", getTime(), asap ? "true" : "false");
		fprintf(stderr, "[%.19s] INFO: event=%s\n", getTime(), event ? "true" : "false");
		fprintf(stderr, "[%.19s] INFO: index=%s\n", getTime(), index ? "true" : "false");
		fprintf(stderr, "[%.19s] INFO: remove=%s\n", getTime(), remv ? "true" : "false");
		fprintf(stderr, "[%.19s] INFO: purge=%s\n", getTime(), purge ? "true" : "false");
		fprintf(stderr, "[%.19s] INFO: format=%s\n", getTime(), format != NULL ? format : "yyyyMMddHHmmss");
		fprintf(stderr, "[%.19s] INFO: version=%s\n", getTime(), version);
		fprintf(stderr, "[%.19s] INFO: reqid=%s\n", getTime(), reqid);
		fprintf(stderr, "[%.19s] INFO: group=%s\n", getTime(), group != NULL ? group : "[random]");
		fprintf(stderr, "[%.19s] INFO: identity=%s\n", getTime(), identity != NULL ? identity : "[target-name]");
	}

	original = source;

	/* Let's check the source file */
	if (waitfor == NULL && !scheduler && !purge && !completed && !expected && !started && !reset)
	{
		uid_t uid = getuid();
		uid_t euid = geteuid();

		/* Temporary to allow opening the file */
		if (seteuid(uid) < 0)
		{
			warning("setting effective user-id (seteuid)");
		}

		statsrc = stat64(source, &stcert);
		if (statsrc < 0)
		{
			error("getting source file status (stat64)");
			goto clean;
		}

		resolved = malloc(MAXPATHLEN);
		if (resolved == NULL)
		{
			error("allocating memory (malloc)");
			goto clean;
		}

		if (realpath(source, resolved) != NULL)
		{
			strcpy(source, resolved);
		}
		else
		{
			warning(
				"getting canonicalized absolute source pathname (realpath)");
		}

		if (S_ISFIFO(stcert.st_mode))
		{
			/* This is a named pipe! */
			if (groupby == NULL)
			{
				error("named pipe supported in groupby mode only");
				goto clean;
			}
			if (index)
			{
				error("index not supported with named pipe");
				goto clean;
			}
			/* We don't know the size of the file yet! */
			filelen = -1;
			index = -1;
		}
		else
		{
			if ((fd = open64(source, O_RDONLY, 0640)) == -1)
			{
				error("opening source file (open64)");
				goto clean;
			}

			/* Let's put it back to what it was (root) */
			if (seteuid(euid) < 0)
			{
				warning("setting effective user-id (seteuid)");
			}

			if (index)
			{
				/* The source file is an index file so we have to add the size of all the files
				 *  specified!
				 */
				index = 0;
				filelen = 0;
				{
					char *fileName = NULL;
					FILE *file = fopen(source, "r");
					if (file == NULL)
					{
						error("opening index file (fopen)");
						goto clean;
					}
					char line[1024];
					while (fgets(line, sizeof(line), file))
					{
						struct stat64 st;
						fileName = strtok(line, "\n");
						if (fileName != NULL && strlen(fileName) > 0 && strncmp(fileName, "#", 1) != 0)
						{
							if (stat64(fileName, &st) < 0)
							{
								/* Cannot get the size of the selected file! */
								char buf[2048];
								sprintf(buf, "getting %s status (stat64)",
										line);
								error(buf);
								fclose(file);
								goto clean;
							}
							filelen += st.st_size;
							index++;
						}
					}
					if (!feof(file))
					{
						error("end-of-file indicator not set (feof)");
						fclose(file);
						goto clean;
					}
					fclose(file);
					if (!index)
					{
						error("no file(s) found in index");
						goto clean;
					}
					else
					{
						if (index == 1)
						{
							/* Let's optimise and treat it as a normal ecpds without the index option */
							if (verb)
								fprintf(stderr, "[%.19s] INFO: force source=%s\n", getTime(), fileName);
							original = fileName;
							sprintf(source, "%.*s", MAXPATHLEN, fileName);
							index = -1;
						}
					}
				}
			}
			else
			{
				/* We use the size of the source file! */
				filelen = stcert.st_size;
				index = -1;
			}
		}

		timefile = (unsigned int)stcert.st_mtime;

		/* Do we have a fixed size provided */
		if (fsize != 0)
		{
			if (fsize < 0 && filelen > abs(fsize))
			{
				errno = 0;
				error("size of file exceeds maximum specified");
				goto clean;
			}
			else
			{
				if (fsize > 0 && fsize != filelen)
				{
					errno = 0;
					error("size of file differs from specified value");
					goto clean;
				}
			}
		}

		if (verb)
			fprintf(stderr, "[%.19s] INFO: %lld bytes to transfer\n",
					getTime(), filelen);
	}

	/* Let's open the connection to the master and authenticate */
	char versionAndPid[512];
	sprintf(versionAndPid, "%s (cmd=ecpds,node=%s,user=%s,pid=%d,req=%s)", VERSION, getHostName(), ecuser, getpid(), originalechost);
	if ((sd = tryConnection(echost, ecport, CONNECT_TIMEOUT_IN_SECONDS)) == -1 || sendCommand(sd, "VERSION", versionAndPid) == -1 || sendCommand(sd, "USER", ecuser) == -1 || sendCommand(sd, "OPTS", opts) == -1 || sendCommand(sd, "CALLER", caller) == -1 || receiveCommand(sd, "MESSAGE", message, 512) == -1)
	{
		goto clean;
	}

	/* Is it a notification? */
	if (completed || expected || started || reset)
	{
		if ((bufferMon != NULL && sendCommand(sd, "BUFFER", bufferMon) == -1) || sendCommand(sd, "METADATA", metadata) == -1 || (at != NULL && sendCommand(sd, "AT", at) == -1) || sendAction(sd, completed ? "COMPLETED" : expected ? "EXPECTED"
																																																						: reset		 ? "RESET"
																																																									 : "STARTED") == -1 ||
			receiveCommand(sd, "MESSAGE", message, 512) == -1)
			goto clean;
		fprintf(stdout, "%s\n", message);
		res = 0;
		goto clean;
	}

	/* Is it a scheduler request? */
	if (scheduler)
	{
		if (start || stop)
		{
			if (destination == NULL)
			{
				if (sendAction(sd, start ? "SCHEDULERSTART" : "SCHEDULERSTOP") == -1 || receiveCommand(sd, "MESSAGE", message, 512) == -1)
					goto clean;
			}
			else
			{ // This is a start/stop destination request
				if (sendCommand(sd, "DESTINATION", destination) == -1 || sendAction(sd, start ? "DESTINATIONSTART" : "DESTINATIONSTOP") == -1 || receiveCommand(sd, "MESSAGE", message, 512) == -1)
					goto clean;
			}
		}
		else
		{
			if (sendCommand(sd, "STREAMS", streams) == -1 || sendCommand(sd, "TIMEOUT", timeout) == -1 || sendAction(sd, "SCHEDULERCHECK") == -1 || receiveCommand(sd, "MESSAGE", message, 512) == -1)
				goto clean;
		}
		fprintf(stdout, "%s\n", message);
		res = 0;
		goto clean;
	}

	/* Is it a waitfor command? */
	if (waitfor != NULL)
	{
		if (sendCommand(sd, "WAITFORGROUP", waitfor) == -1)
			goto clean;
		if (debug)
			fprintf(stderr, "[%.19s] DEBUG: receiving update\n", getTime());
		while ((err = readLine(sd, buf, sizeof(buf))) >= 0)
		{
			if (buf[0] == '-')
			{
				error(buf + 1);
				goto clean;
			}
			else
			{
				if (strncmp("+QUIT", buf, 5) == 0)
				{
					res = 0;
					goto clean;
				}
				else
				{
					fprintf(stdout, "%s\n", buf + 1);
				}
			}
		}
		if (debug)
			fprintf(stderr, "[%.19s] DEBUG: exiting without acknowledgement\n",
					getTime());
		goto clean;
	}

	/* This is a request related to a data-file */
	{
		int mspds = 0;
		if (sendBoolean(sd, "TIMECRITICAL", mspds) == -1 || sendCommand(sd, "DESTINATION", destination) == -1 || sendInteger(sd, "TIMEFILE", timefile) == -1 || sendCommand(sd, "FORMAT", format) == -1 || (index > 0 && sendInteger(sd, "INDEX", index) == -1) || sendCommand(sd, "GROUP", group) == -1 || sendCommand(sd, "REQID", reqid) == -1 || sendCommand(sd, "PRIORITY", priority) == -1 || sendCommand(sd, "UNIQUENAME", version) == -1 || sendCommand(sd, "IDENTITY", identity) == -1 || sendCommand(sd, "ORIGINAL", tmp == NULL ? original : tmp) == -1 || sendCommand(sd, "SOURCE", source) == -1 || sendCommand(sd, "TARGET", target) == -1 || sendCommand(sd, "LIFETIME", lifetime) == -1 || sendCommand(sd, "DELAY", delay) == -1 || sendCommand(sd, "AT", at) == -1 || sendCommand(sd, "METADATA", metadata) == -1 || (filelen != -1 && sendLong64(sd, "SIZE", filelen) == -1) || sendCommand(sd, "GROUPBY", groupby) == -1 || sendBoolean(sd, "NORETRIEVAL", noretrieval) == -1 || sendBoolean(sd, "FORCE", force) == -1 || sendBoolean(sd, "REQUEUE", requeue) == -1 || sendBoolean(sd, "STANDBY", standby) == -1 || sendBoolean(sd, "ASAP", asap) == -1 || sendBoolean(sd, "EVENT", event) == -1 || sendBoolean(sd, "REMOVE", remv) == -1 || sendBoolean(sd, "PURGE", purge) == -1 || sendAction(sd, "PUT") == -1 || (!purge && groupby == NULL && receiveCommand(sd, "TARGET", target, MAXPATHLEN) == -1) || (!purge && groupby == NULL && receiveCommand(sd, "ECPROXY", hosts, 512) == -1) || receiveCommand(sd, "MESSAGE", message, 512) == -1)
		{
			goto clean;
		}
	}

	/* Is it a purge command? */
	if (purge || groupby != NULL)
	{
		fprintf(stdout, "%s\n", message);
		res = 0;
		goto clean;
	}

	/* Some verbose or debug information */
	if (verb || debug)
	{
		fprintf(stderr, "[%.19s] INFO: new target=%s\n", getTime(), target);
	}

	/**
	 * Process the list of ecproxies and handle file transfer.
	 * This block iterates through the list of ecproxies, establishes connections, and transfers the file content.
	 */
	int hasMore = 1;
	int success = 0;
	while (hasMore)
	{
		// Get the next ecproxy from the list
		char *current = nextParam(hosts, '|');
		if (current == NULL)
		{
			ecproxy = hosts;
			hasMore = 0;
		}
		else
		{
			ecproxy = current;
		}

		// Extract the host and port from the ecproxy
		char *h, *p;
		if ((p = nextParam(h = ecproxy, ':')) == NULL)
		{
			continue;
		}
		strcpy(ecproxyHost, h);
		strcpy(ecproxyPort, p);

		// Verbose or debug information
		if (verb || debug)
		{
			fprintf(stderr, "[%.19s] INFO: ecproxyHost=%s\n", getTime(), ecproxyHost);
			fprintf(stderr, "[%.19s] INFO: ecproxyPort=%s\n", getTime(), ecproxyPort);
		}

		// Close the previous data-mover connection if this is not the first attempt
		if (pd != -1)
		{
			close(pd);
			pd = -1;
		}

		// Open the connection to the data-mover
		sprintf(buf, "ECPDS %s", VERSION);
		if ((pd = tryConnection(ecproxyHost, ecproxyPort, CONNECT_TIMEOUT_IN_SECONDS)) == -1 ||
			sendCommand(pd, buf, "") == -1 ||
			sendCommand(pd, "OPTS", opts) == -1)
		{
			continue;
		}
		if (sendCommand(pd, "TARGET", target) == -1 ||
			receiveCommand(pd, "CONNECT", buf, MAXPATHLEN + 256) == -1)
		{
			continue;
		}
		if (sendLong64(pd, "SIZE", filelen) == -1)
		{
			continue;
		}

		// Send the file content
		{
			if (buffsize > filelen)
			{
				buffsize = (int)filelen;
				if (debug)
				{
					fprintf(stderr, "[%.19s] DEBUG: use small file buffer (%d)\n", getTime(), buffsize);
				}
			}
			char transferBuf[buffsize];
			fileres = 0;
			if (filelen > 0)
			{
				if (debug)
				{
					fprintf(stderr, "[%.19s] DEBUG: sending file content\n", getTime());
				}
				while ((err = read(fd, transferBuf, buffsize)) > 0)
				{
					if (write(pd, transferBuf, err) <= 0)
					{
						if (debug)
						{
							fprintf(stderr, "[%.19s] DEBUG: transmission aborted (write)\n", getTime());
						}
						break;
					}
					else
					{
						fileres += err;
					}
				}
				if (filelen != fileres)
				{
					error("transmission failed");
					if (lseek64(fd, 0, SEEK_SET) == (off_t)-1)
					{
						error("repositioning source file offset (lseek64)");
						goto clean;
					}
					continue;
				}
			}
			else
			{
				if (verb)
				{
					fprintf(stderr, "[%.19s] INFO: empty file\n", getTime());
				}
			}
		}

		// Get stats and send bye command
		if (receiveCommand(pd, "STAT", stats, 512) == -1 ||
			receiveCommand(pd, "BYE", buf, MAXPATHLEN + 256) == -1 ||
			sendAction(pd, "BYE") == -1)
		{
			continue;
		}

		// No problems, mark success
		success = 1;
		break;
	}

	/**
	 * Check if the transmission succeeded.
	 * If not, update the stats with a failure message.
	 */
	if (!success)
	{
		sprintf(stats, "-Transmission failed to each Data Mover");
	}

	/**
	 * Send the statistics to the server.
	 * This block sends the stats, sends a BYE command, and receives a message from the server.
	 */
	if (sendCommand(sd, "HOST", stats) == -1 ||
		sendAction(sd, "BYE") == -1 ||
		receiveCommand(sd, "MESSAGE", message, 512) == -1)
	{
		goto clean;
	}

	/**
	 * Check if the source file should be removed.
	 * If the remove flag is set, mark the file for deletion.
	 */
	if (remv)
	{
		del = 1;
	}

/**
 * Clean up resources and close file descriptors.
 * This block ensures that all allocated resources are freed and file descriptors are closed.
 */
clean:
	if (sd != -1)
	{
		close(sd);
	}
	if (pd != -1)
	{
		close(pd);
	}
	if (fd != -1)
	{
		close(fd);
	}
	if (del && source != NULL)
	{
		remove(source);
	}
	if (!useTmpFile && source != NULL)
	{
		free(source);
	}
	if (originalechost != NULL)
	{
		free(originalechost);
	}
	if (target != NULL)
	{
		free(target);
	}
	if (resolved != NULL)
	{
		free(resolved);
	}

	/**
	 * Return the correct exit code.
	 * This block returns the result of the operation.
	 */
	return res;
}
