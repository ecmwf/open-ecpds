# Properties Documentation

## Table of Contents

- [Acquisition Options](#acquisition-options)
- [Alias Options](#alias-options)
- [Azure Options](#azure-options)
- [Ecaccess Options](#ecaccess-options)
- [Ecpds Options](#ecpds-options)
- [Ectrans Options](#ectrans-options)
- [Exec Options](#exec-options)
- [Ftp Options](#ftp-options)
- [Ftps Options](#ftps-options)
- [Gcs Options](#gcs-options)
- [Http Options](#http-options)
- [Incoming Options](#incoming-options)
- [Master Options](#master-options)
- [Mqtt Options](#mqtt-options)
- [Portal Options](#portal-options)
- [Proxy Options](#proxy-options)
- [Retrieval Options](#retrieval-options)
- [S3 Options](#s3-options)
- [Scheduler Options](#scheduler-options)
- [Sftp Options](#sftp-options)
- [Test Options](#test-options)
- [Upload Options](#upload-options)

## Acquisition Options

These options help fine-tune the acquisition process and are accessible via the acquisition host editor.

### acquisition.action
This option allows choosing which action to take on the selected files. The "queue" action triggers the queueing of the selected files and the "delete" action triggers the deletion of the selected files on the remote site.

### acquisition.datedelta
When resolving the "$date" and "$dirdate" parameters in "acquisition.metadata" or "acquisition.target", this option allows applying a delta to the date. The delta can be positive, to go forward in time or negative to go backward in time.

### acquisition.dateformat
When resolving the "$date" and "$dirdate" parameters in "acquisition.metadata" or "acquisition.target", this option allows specifying which date format to use. The date format required should adhere to the format supported by Java SimpleDateFormat facility.

### acquisition.datepattern
When the "acquisition.datesource" is defined, this option allows specifying which date pattern to use for the parsing of the date.

### acquisition.datesource
When resolving the "$date" and "$dirdate" parameters in "acquisition.metadata" or "acquisition.target", this option allows specifying a source to parse the date from. If no source is specified then the current date and time is used.

### acquisition.debug
Allow requesting debug messages in the master logs related to the acquisition process for this host.

### acquisition.defaultDateFormat
When listing remote files, this parameter represents the default date format string expected by the FTP server. It defines the format in which dates are typically sent or received by the FTP server. It adheres to the format supported by the SimpleDateFormat class in Java.

### acquisition.deleteoriginal
When a new file is discovered and registered as a data transfer, allow specifying if the original file on the remote site should be deleted once successfully retrieved.

### acquisition.event
Enable the generation of an event once a new file is discovered, registered as a data transfer and successfully retrieved and disseminated (or available through the data portal), triggering a notification system push (e.g. MQTT).

### acquisition.fileage
Allow selecting files based on their age using expressions like "&gt;10d", "&lt;=5d", or "2d". When the "$age" parameter is detected, it is treated as a JavaScript expression (e.g. "$age &gt; (5*60*1000) && $age &lt; (20*24*60*60*1000)") and evaluated. The "$age" placeholder gets replaced by the file timestamp in milliseconds.

### acquisition.filesize
Allow file selection based on size using expressions such as "&gt;10kb", "&lt;=5mb", or "2096". When the "$size" parameter is detected, it is interpreted as a JavaScript expression (e.g. "$size&gt;1024 && $size&lt;1024*1024") and evaluated. The "$size" placeholder is substituted with the file size in bytes. Please note that the "b", "kb", "mb", and other extensions are not applicable within JavaScript expressions.

### acquisition.groupby
When a new file is discovered and registered as a data transfer, allow specifying a group-by name (default is "ACQ_{destination-name}_{host-identifier}").

### acquisition.interruptSlow
Terminate slow acquisition listing upon reaching the specified maximum duration in "acquisition.maximumDuration". If this option is not explicitly set, the systemwide value will be used instead.

### acquisition.lifetime
When a new file is discovered and registered as a data transfer, allow specifying a lifetime. When a data transfer has exceeded its designated lifetime, it is marked as expired and becomes unavailable for dissemination or download.

### acquisition.listMaxThreads
When "acquisition.listParallel" is enabled, specify the maximum concurrent connections allowed for listing directories.

### acquisition.listMaxWaiting
When "acquisition.listParallel" is enabled, set the upper limit for the capacity of the queue handling requests for listing directories. Attempt to add elements beyond the specified capacity will block until a listing has completed.

### acquisition.listParallel
Allow processing the listings of multiple directories specified in the directory editor simultaneously.

### acquisition.listSynchronous
When enabled, wait for the listing to complete before starting to process the output. Otherwise, the listing is processed on-the-fly as it is received from the remote site. For example, this is NOT recommended when receiving notifications from MQTT brokers, as the listing might take a long time. Disabling this option allows for accelerated processing of the listing.

### acquisition.maximumDuration
Maximum duration allowed for an acquisition listing.

### acquisition.metadata
When a new file is discovered, it is possible to associate some metadata with it. The associated value can contain the following placeholders: "$destination", "$name" (filename), "$target" (target name), "$original" (original name), "$link", "$dirdate" (primarily use date options set in the directory current line processed), "$date" (current date or date parsed through "acquisition.datesource") and "$timestamp" (in milliseconds). Multiple metadata entries can be set using a comma as a separator (e.g. "name1=value1,name2=value2").

### acquisition.noretrieval
When a new file is discovered and registered as a data transfer, allow specifying the no-retrieval flag. Enabling this flag will prevent the file from being downloaded onto the data movers. Instead, it will be accessed as needed through the source host used to discover the file.

### acquisition.onlyValidTime
This flag, when activated upon discovering a new file, determines the action in case the timestamp cannot be read or parsed successfully from the listing. When enabled, files lacking proper timestamps are discarded. If disabled, all files are selected regardless of their timestamp status. It is important to note that selecting a file with an improper timestamp will exempt it from undergoing any tests specified by the "acquisition.fileage" option.

### acquisition.priority
When a new file is discovered and registered as a data transfer, allow specifying a priority for the dissemination process.

### acquisition.recentDateFormat
When listing remote files, this parameter is related to the recent date format string expected by the FTP server. It defines the format in which recent dates (if different from "acquisition.defaultDateFormat") are sent or received by the FTP server. It also adheres to the format supported by SimpleDateFormat.

### acquisition.regexFormat
This pattern delineates how the FTP server listing data is segmented and transformed into distinct file attributes (e.g. file name, permissions, size, timestamp). For more details, please refer to the documentation related to the FTP file entry parser within the Apache Commons Net library.

### acquisition.regexPattern
This option allows setting a regex to filter the files returned in the transfer module listing output. The files are filtered at the data mover level rather than at the master level, saving considerable network traffic when handling extensive listings. The placeholders "$date" and "$namePattern" are replaced respectively by the current date (or date parsed through "acquisition.datesource") and the original pattern specified at the end of the current path in the directory editor. Using the option "acquisition.wildcardFilter" is preferable. However, its availability is not guaranteed, as it depends on the underlying transfer module implementation and capabilities.

### acquisition.removeParameters
When processing the listing output with URLs in place of filenames, this option allows for the removal of HTTP options, including the "?" symbol and anything following it, from the filenames.

### acquisition.requeueon
When encountering a previously registered file during discovery, enable the specification of actions to take. This involves a JavaScript expression expected to yield a boolean result. Several placeholders are accessible for use within this expression: "$size1" represents the original file size, "$size2" indicates the size from the most recent listing output, "$time1" denotes the original file timestamp, and "$time2" represents the timestamp from the latest listing (e.g. "$time2 &gt; $time1 && $size2 != $size1"). Additionally, the placeholders "$destination," "$target," and "$original" are available for use.

### acquisition.requeueonsamesize
Only used if "acquisition.requeueon" is not set and "acquisition.requeueonupdate" is enabled. This is the same as setting "acquisition.requeueon" to "$time2 &gt; $time1".

### acquisition.requeueonupdate
Only used if "acquisition.requeueon" is not set. This is the same as setting "acquisition.requeueon" to "$time2 &gt; $time1 && $size2 != $size1".

### acquisition.serverLanguageCode
When listing remote files, this parameter specifies the language code used by the FTP server. It indicates the language in which the server sends messages or information.

### acquisition.serverTimeZoneId
When listing remote files, this parameter specifies the time zone identifier used by the FTP server. It indicates the time zone in which the server operates or sends time-related information.

### acquisition.shortMonthNames
When listing remote files, this parameter represents a string containing the short names of the months. It provides an alternative way for the server to indicate month names in abbreviated form.

### acquisition.standby
When a new file is discovered and registered as a data transfer, allow specifying if the data transfer should be in standby mode for the dissemination process.

### acquisition.systemKey
When listing remote files, this parameter refers to the system key, which is used to identify the specific configuration. It allows specifying the configuration details for a particular FTP server system.

### acquisition.target
When a new file is discovered, it is possible to set a target filename which is different from the source filename. The target filename can contain the following placeholders: "$destination", "$name" (filename), "$target" (target name), "$original" (original name), "$link", "$dirdate" (primarily use date options set in the directory current line processed), "$date" (current date or date parsed through "acquisition.datesource") and "$timestamp" (in milliseconds).

### acquisition.transferGroup
When a new file is discovered and registered as a data transfer, allow specifying a transfer group for processing the request.

### acquisition.uniqueByNameAndTime
This parameter is relevant to the creation of the key used for identifying the data file found in the listing. It is crucial, as this key serves to determine whether any duplicates of the data file are already registered. If this option is enabled and a valid timestamp is detected, it is used when searching duplicates.

### acquisition.uniqueByTargetOnly
This parameter is relevant to the creation of the key used for identifying the data file found in the listing. It is crucial, as this key serves to determine whether any duplicates of the data file are already registered. This option enables the utilization of solely the target name when searching for duplicates.

### acquisition.useSymlink
Allow specifying whether symbolic links should be considered or excluded when parsing listing output.

### acquisition.useTargetAsUniqueName
This parameter is relevant to the creation of the key used for identifying the data file found in the listing. It is crucial, as this key serves to determine whether any duplicates of the data file are already registered. If the option "acquisition.uniqueByTargetOnly" is enabled, this option allow using the target name rather than the original name when searching for duplicates.

### acquisition.wildcardFilter
Allow specifying a wildcard filter when processing the listing. This is useful to limit the number of processed files. This differs from the "acquisition.regexPattern" option, which filters the files returned by the transfer module listing. The "acquisition.wildcardFilter" allows the limitation of the number of files directly returned by the transfer module listing itself at an earlier stage. This method is preferable to using the "acquisition.regexPattern" option. However, its availability is not guaranteed, as it depends on the underlying transfer module implementation and capabilities.

## Alias Options

This option help fine-tune the aliasing process and are accessible via the destination editor.

### alias.pattern
This option filters files designated to be aliased to the target destination. Multiple parameters, separated by a comma (e.g. "param1=value1,param2=value2"), can be specified. The "pattern" and "ignore" parameters allow specifying regular expressions (regex) to respectively include or reject files based on their target names (e.g. "pattern=M(T|C|E|G)(.*),ignore=(.*).tmp"). Once the files are selected, it becomes possible to enforce new parameters such as "lifeTime" (ISO-8601 duration), "priority" (0-99), "asap" (boolean), "event" (boolean), and "delay" (ISO-8601 duration) before creating the alias. For instance: "lifetime=P2D,priority=80,asap=yes,event=no,delay=PT15M". Various placeholders ("$name", "$path", "$parent", "$destination", and "$alias") are replaced by their respective values. Additionally, a "$date" placeholder can be specified. By default, this represents the current date in "yyyyMMdd" format, but can be adjusted using "dateformat", "datedelta", "datesource", and "datepattern" options. For example, to extract the date from the target file name (characters 2 to 12 in the "yyyyMMddHH" pattern), subtract 1 day, and display it in "MMdd" format: "dateformat=MMdd,datedelta=-1,datesource=$target[2..12],datepattern=yyyyMMddHH". In case different parameters need to be enforced based on the target, this option allows a value that spans multiple lines. Each line follows this format: "({operator} target) {parameters}". The {operator} can be "==", "!=", ".=", or "=.", and the parameters are a comma-separated list as described above. When the "==" operator is used, if the target is enclosed by "{}", it is considered a regex pattern. For example, the first line might be: "(== {(.*).dat}) lifetime=P2D,priority=80,asap=yes,event=no,delay=PT15M,target=/tmp/$target".

## Azure Options

These options help fine-tune the azure transfer module and are accessible via the host editor.

### azure.blockSize
Allow specifying the maximum size of data chunks to be transferred in parallel

### azure.containerName
Allow specifying the container name for the Azure account. These containers act as a way to group blobs together, somewhat similar to folders in a file system, but without the hierarchical structure.

### azure.ftpgroup
Allow forcing a group name in the listing output. Default group name is the login name. 

### azure.ftpuser
Allow forcing a user name in the listing output. Default user name is the login name.

### azure.ignoreCheck
By default, after a push, the size of the remote file is retrieved to verify its match with the size of the original file. It also compares the MD5, if available, at both ends (locally available if "azure.useMD5" is set). Enabling this flag bypasses these checks.

### azure.ignoreDelete
If a blob with the same name already exists before data push, it is deleted by default before initiating the transmission. Enabling this flag prevents this deletion from occurring.

### azure.mkContainer
Allow creating missing containers when pushing data. This requires specific permissions.

### azure.multipartSize
When the file size is predetermined and exceeds this specified value, a multipart upload is initiated. If the file size is not known in advance (e.g. due to on-the-fly compression), a multipart upload is always performed.

### azure.numBuffers
Allow specifying the maximum number of concurrent operations allowed during a parallel transfer operation.

### azure.overwrite
Enable the option to force overwrite of existing files when pushing data.

### azure.port
Allow specifying the port number of the remote Azure endpoint to connect to.

### azure.sasSubscriptionKey
This is the key to include in the HTTP requests to access the APIs.

### azure.sasUrl
This is a secure URL that grants limited access to resources in an Azure storage account without exposing the account keys. This access is controlled through the SAS token appended to the URL, which specifies permissions and duration for which access is granted.

### azure.scheme
Allow specifying the scheme when connecting to the endpoint.

### azure.url
Allow specifying an alternative URL to connect to the endpoint. By default, the URL is constructed using the "azure.scheme", "azure.port" options and hostname field (scheme://hostname:port).

### azure.useMD5
Enable the request for processing the MD5 checksum during data transmission. If the MD5 is available at both ends and the "azure.ignoreCheck" option is not set, the local MD5 is compared with the remote MD5 after transmission.

## Ecaccess Options

These options help fine-tune the ecaccess transfer module and are accessible via the host editor.

### ecaccess.destination
Allow specifying which protocol to use from the ECaccess Gateway when connecting to the target host.

### ecaccess.gateway
Allow selecting the ECaccess Gateway for transfer requests. If the "ecaccess.loadbalancing" option is enabled, multiple Gateways can be specified, separated by commas.

### ecaccess.loadbalancing
Enable round-robin load balancing for transfer requests among the specified list of Gateways defined in the "ecaccess.gateway" option.

## Ecpds Options

These options help fine-tune the allocation of data movers for various activities and are accessible via the host editor.

### ecpds.moverListForBackup
Allow selecting the list of data movers to use when backing up a destination. This option accepts a value spanning multiple lines. Each line follows this format: "({operator} transferGroupName) {moversList}". The {operator} can be "==", "!=", ".=", or "=.", and the moversList is a comma-separated list of data movers. For example, the first line might be: "(== internet) ecpds-dm1,ecpds-dm2".

### ecpds.moverListForProcessing
Allow selecting the list of data movers to use when disseminating, retrieving (acquisition) and generating network reports. This option accepts a value spanning multiple lines. Each line follows this format: "({operator} transferGroupName) {moversList}". The {operator} can be "==", "!=", ".=", or "=.", and the moversList is a comma-separated list of data movers. For example, the first line might be: "(== internet) ecpds-dm1,ecpds-dm2".

### ecpds.moverListForSource
Allow selecting the list of data movers to use when downloading source files. This option accepts a value spanning multiple lines. Each line follows this format: "({operator} transferGroupName) {moversList}". The {operator} can be "==", "!=", ".=", or "=.", and the moversList is a comma-separated list of data movers. For example, the first line might be: "(== internet) ecpds-dm1,ecpds-dm2".

## Ectrans Options

These options help fine-tune the processing of the selected transfer module and are accessible via the host editor.

### ectrans.buffInputSize
When ECtrans reads the input stream, this option allows for the setup of a buffer. The use of a buffer optimizes data transfer, enhances efficiency, and ensures smoother communication within a system, particularly when there are differences in processing speeds between streams.

### ectrans.buffOutputSize
When ECtrans write to the output stream, this option allows for the setup of a buffer. The use of a buffer optimizes data transfer, enhances efficiency, and ensures smoother communication within a system, particularly when there are differences in processing speeds between streams.

### ectrans.checkfiltersize
If the data file is already compressed, this option allows configuration for handling cases where the compressed file size exceeds that of the original file. Such instances may occur due to certain compression algorithms generating larger files based on the file's content. When enabled (default setting), if a compressed file is larger than the original, the original file is utilized. Consequently, this results in disseminating the original file without the compression extension.

### ectrans.closeAsynchronous
This option enables the asynchronous closure of the transfer module once the data transfer has completed, which accelerates dissemination, especially under heavy loads on target hosts where closing streams might take some time. However, it's important to note a potential issue: if there's a failure during the closure process, indicating a problem on the remote host, the transfer scheduler remains unaware of this failure, and the file isn't requeued. This behaviour depends on the underlying transfer module.

### ectrans.closeTimeOut
Allow specifying a timeout duration for processing the closure of the underlying transfer module.

### ectrans.connectTimeOut
Allow specifying a timeout duration for processing the connection to the remote site by the underlying transfer module.

### ectrans.createChecksum
If this option is enabled and no MD5 checksum has already been generated, then an MD5 checksum is computed on the fly during the file dissemination. If the MD5 checksum is computed then it is passed to the underlying transfer module which can be configured to make some use of it.

### ectrans.debug
Allow requesting debug messages in the data mover logs related to the transfer module activity for this host.

### ectrans.delTimeOut
Allow specifying a timeout duration for processing the removal of a file on the remote site by the underlying transfer module.

### ectrans.filterMinimumSize
When disseminating files with compression enabled, this option allows for the provision of a minimum size to filter which files should undergo compression.

### ectrans.filterpattern
When disseminating files with compression enabled, this option allows for the provision of a regex pattern to filter which files should undergo compression.

### ectrans.getHandler
When defined, allow delegating the data retrieval to an external module running in a spawn process in the underlying operating system. If this option is enabled then the "ectrans.getHandlerCmd" option is required.

### ectrans.getHandlerAck
When data retrieval is delegated to an external module, this option enables the specification of the expected acknowledgment from the last line of the output stream. If the received string does not match the expected acknowledgment, a failure is signaled to the transfer scheduler.

### ectrans.getHandlerCmd
When "ectrans.getHandler" is enabled, this option provides the command line for execution in the external process, with available placeholders such as "$source" and "$target" that can be used within the provided string. Once the external process initiates, it attempts to extract the transfer progress as a percentage from the output, typically ending in the format "XX.XX%" (e.g. "progress: 12.98%"), thereby updating the transfer scheduler. If the "ectrans.getHandlerAck" option is defined, it endeavors to acquire it from the last line of the process output.

### ectrans.getHandlerExitCode
When data retrieval is handed over to an external module, this option allows for specifying the anticipated exit code from the underlying process. If the received exit code does not match the expected one, it triggers a failure signal to the transfer scheduler.

### ectrans.getTimeOut
Allow specifying a timeout duration for processing the retrieval of a file from the remote site by the underlying transfer module.

### ectrans.hostSelector
Allow selecting an alternative host-name or IP to use when connecting to the remote site. This option accepts a value spanning multiple lines. Each line follows this format: "({key} {operator} {value}) {host-name or IP}". The {operator} can be "==", "!=", ".=", or "=.". For example, a line might be: "($host == 127.0.0.1) localhost". Placeholders such as "$mover", "$host", "$network" and "$group" can be used within the provided string. If no match is found a default value can be provided in the first line (e.g. if the first line is "$host" and no match is found then the default host-name is used).

### ectrans.listTimeOut
Allow specifying a timeout duration for processing the listing of files on the remote site by the underlying transfer module.

### ectrans.location
Allow specifying the location metadata when sending notifications (this parameter can be resolved in the event script or host JavaScript options with the placeholder "$location"). The placeholders "$filename", "$movername", "$datafileid", "$datafileuuid", "$datatransferid" and "$datatransferuuid" can be used within the provided location string.

### ectrans.mkdirTimeOut
Allow specifying a timeout duration for processing the creation of a directory on the remote site by the underlying transfer module.

### ectrans.moveTimeOut
Allow specifying a timeout duration for processing the moving of a file on the remote site by the underlying transfer module.

### ectrans.multipleInputStream
When processing data retrieval via index files and "ectrans.usemget" is disabled, this option allow configuring the multiple input streams. The parameters are "retryCount" (default 1), "retryFrequency" (default 1000), "useCache" (default false), "cacheSize" (655360 bytes) and "queueSize" (default 3). For example "retryCount=2,queueSize=4,useCache=yes".

### ectrans.plugBuffSize
When ECtrans is connecting the input and output streams, this option allows for the setup of a buffer. The use of buffers between input and output streams helps optimize data transfer, improves efficiency, and ensures smoother communication between different parts of a system, reducing the impact of differences in processing speeds between streams.

### ectrans.plugDoFlush
When ECtrans is connecting the input and output streams, this option allows for forcing a flush between each write. If the underlying host is a dissemination host and there is compression on-the-fly, then the flush is always performed.

### ectrans.plugReadFully
When ECtrans connects the input and output streams, this option enables the forcing of reading until the buffer is full or the input stream reaches its end.

### ectrans.putHandler
When defined, allow delegating the data dissemination to an external module running in a spawn process in the underlying operating system (if "ectrans.putHandlerCmd" is defined) or using the "copy" feature supported by some transfer modules.

### ectrans.putHandlerAck
When data dissemination is delegated to an external module ("ectrans.putHandlerCmd"), this option enables the specification of the expected acknowledgment from the last line of the output stream. If the received string does not match the expected acknowledgment, a failure is signaled to the transfer scheduler.

### ectrans.putHandlerCmd
When "ectrans.putHandler" is enabled, this option provides the command line for execution in the external process, with available placeholders such as "$source" and "$target" that can be used within the provided string. Once the external process initiates, it attempts to extract the transfer progress as a percentage from the output, typically ending in the format "XX.XX%" (e.g. "progress: 12.98%"), thereby updating the transfer scheduler. If the "ectrans.putHandlerAck" option is defined, it endeavors to acquire it from the last line of the process output.

### ectrans.putHandlerExitCode
When data file dissemination is handed over to an external module ("ectrans.putHandlerCmd"), this option allows for specifying the anticipated exit code from the underlying process. If the received exit code does not match the expected one, it triggers a failure signal to the transfer scheduler.

### ectrans.putMonitoredInputDelta
Allow activating a debug in the data mover logs displaying the number of bytes already disseminated through this host. A new entry is displayed every time the duration defined in this parameters has passed.

### ectrans.putTimeOut
Allow specifying a timeout duration for processing the pushing of a file to the remote site by the underlying transfer module.

### ectrans.retryCount
Allow defining an internal retry mechanism when connecting to the remote site. This mechanism is not triggering an exception to the transfer scheduler unless the number of retries is exhausted and the connection is still not successful.

### ectrans.retryFrequency
If the internal retry mechanism is activated (positive "ectrans.retryCount") then this option allow setting a delay between retries.

### ectrans.rmdirTimeOut
Allow specifying a timeout duration for processing the removal of a directory from the remote site by the underlying transfer module.

### ectrans.sizeTimeOut
Allow specifying a timeout duration for getting the size of a file from the remote site by the underlying transfer module.

### ectrans.soMaxPacingRate
Maximum transmit rate in bytes per second for the socket. TCP pacing is good for flows having idle times, as the congestion window permits TCP stack to queue a possibly large number of packets.

### ectrans.socketStatistics
When enabled, it allows gathering socket statistics for each individual data transfer pushed (dissemination), such as the number of TCP packets retransmitted.

### ectrans.streamTimeout
Allow specifying a timeout duration for streaming a file to the remote site by the underlying transfer module. This is different from the "ectrans.putTimeOut" which is taking into account the protocol overhead on top of the streaming.

### ectrans.supportFilter
When the remote server is a DissFTP server, enabling this option allows for on-the-fly decompression at the remote end during dissemination.

### ectrans.tcpCongestionControl
Enable the ability to specify an alternative congestion control algorithm when creating sockets. The selected algorithm must be available on the underlying operating system where the data mover is running (e.g. "bbr", "cubic" or "reno").

### ectrans.tcpKeepAlive
When enabled, it allows for periodically checking if the connection to the receiver is still alive. This helps detect and manage situations where one end of a TCP connection becomes unreachable or unresponsive due to network issues, hardware failures, or crashes.

### ectrans.tcpKeepAliveInterval
The interval between subsequential keepalive probes, regardless of what the connection has exchanged in the meantime.

### ectrans.tcpKeepAliveProbes
The number of unacknowledged probes to send before considering the connection dead and notifying the application layer.

### ectrans.tcpKeepAliveTime
The interval between the last data packet sent (simple ACKs are not considered data) and the first keepalive probe; after the connection is marked to need keepalive, this counter is not used any further.

### ectrans.tcpLingerEnable
Indicate whether linger is enable.

### ectrans.tcpLingerTime
The amount of time, in seconds, the socket should linger before closing.

### ectrans.tcpMaxSegment
Maximum amount of data that can be sent in a single TCP segment.

### ectrans.tcpNoDelay
When enabled, it disables the Nagle algorithm, which is responsible for delaying the transmission of small packets in order to optimize network utilization.

### ectrans.tcpQuickAck
When enabled, the TCP stack sends immediate acknowledgment for incoming data without waiting for the delayed acknowledgment timer.

### ectrans.tcpTimeStamp
Enables or disables the use of timestamps in TCP packets.

### ectrans.tcpUserTimeout
Maximum amount of time, in milliseconds, that transmitted data may remain unacknowledged before an error is returned.

### ectrans.tcpWindowClamp
Bound the size of the advertised window to this value

### ectrans.usednsname
Enabling this option allows bypassing DNS resolution and providing the hostname verbatim to the transfer module. By default, DNS resolution is processed, and the IP address is passed to the transfer module rather than the hostname.

### ectrans.usemget
When processing data retrieval via index files, enabling this option allows for managing the data retrieval directly through the transfer module (optimization supported by the ECauth module). By default, ECtrans retrieves the list of files in the index, and each file is retrieved at the ECtrans level.

## Exec Options

These options help fine-tune the exec transfer module and are accessible via the host editor.

### exec.returnCode
Allow setting the expected exit code of the script started with the "script" option specified in the ECaccess Gateway configuration file. If the exit code is different from the one specified in this parameter then the transmission is tagged as failed. This option is only used in the "synchronous" mode (see the table below), otherwise the script is started in the background and ECtrans does not wait for the exit code to return.

## Ftp Options

These options help fine-tune the ftps transfer module and are accessible via the host editor.

### ftp.commTimeOut
Enable/disable a timeout when opening the control channel. The value must be &gt; 0. A timeout of zero is interpreted as an infinite timeout.

### ftp.cwd
Allow changing directory just after the login (a "cd" command is issued with the specified directory).

### ftp.dataAlive
Only applicable with DissFTP remote servers. Requests a persistent data channel to be maintained across consecutive transfers.

### ftp.dataTimeOut
Enable/disable a timeout when reading from the data channel. The value must be &gt; 0. A timeout of zero is interpreted as an infinite timeout.

### ftp.deleteOnRename
If set, the FTP client tries to delete the target file before doing a rename (e.g. when using temporary file names with the "ftp.usetmp" option).

### ftp.extended
Its use is required when connecting to a remote IPv6 server (EPSV/EPRT commands are issued instead of PASV/PORT commands when opening data channels).

### ftp.ftpLike
If the remote FTP server exclusively supports NLIST, tries to gather the required information to build a similar output to what would be obtained from a LIST.

### ftp.ftpgroup
When "ftp.ftpLike" option is selected allow forcing a group name in the listing output. Default group name is the login name. 

### ftp.ftpuser
When "ftp.ftpLike" option is selected allow forcing a user name in the listing output. Default user name is the login name.

### ftp.ignoreCheck
If set, the remote size of the file is not checked after an upload has been completed. If not set, the remote size of the uploaded file is checked and compared against the size of the source file.

### ftp.ignoreDelete
If not set, the FTP client tries to delete the target file before the upload is processed.

### ftp.ignoreMkdirsCmdErrors
If set, ignore any error occurring during the execution of the command specified in "ftp.preMkdirsCmd" or "ftp.postMkdirsCmd".

### ftp.keepAlive
Allow keeping FTP control connections alive in a pool. If the connection is unused for longer than the duration specified by this option then the connection is closed and removed from the pool. A duration of zero is interpreted as no pooling.

### ftp.keepControlConnectionAlive
If set, the FTP client will keep sending NOOPS commands through the control channel while waiting for a data transmission to complete. This proves beneficial during prolonged transfers, preventing control channel blockages caused by some firewalls.

### ftp.listenAddress
Allow specifying the listen address used by the FTP client (e.g. when waiting for an incoming data connection). By default, the system will pick up a valid local address. A value of "0.0.0.0" will cause the binding of all the valid network interfaces.

### ftp.login
Allow setting the login while logging into the remote FTP server. This is overwriting the login set through the interface.

### ftp.lowPort
Allow forcing the FTP client to bind a privileged port (500 &lt;= N &lt;= 1023) instead of an unprivileged port (N &gt; 1023) when using the Active FTP mode.

### ftp.md5Ext
When requesting a checksum with "ectrans.createChecksum", allow configuring the MD5 extension.

### ftp.mkdirs
Allow creating the directory named by the target pathname, including any necessary but non-existent parent directories.

### ftp.mkdirsCmdIndex
When using the "ftp.preMkdirsCmd" and "ftp.postMkdirsCmd" options, this index allow selecting the directories for which a command should be triggered. For example, if the directory is "/home/uid/test/data/out/bin", an index of 3 will make sure the FTP client start the "preMkdirsCmd" and/or "postMkdirsCmd" commands for the "home", "uid" and "test" directories. If the index is negative then the selection start from the end of the path instead of the beginning. In the previous example it would make sure the FTP client start the "preMkdirsCmd" and/or "postMkdirsCmd" commands for the "data", "out" and "bin" directories. Please note that the full path is taken into account as defined in the directory field. To define a home directory that should not be taken into account in this process, it should be defined through the "ftp.cwd" option.

### ftp.mksuffix
If set, the FTP client will generate a random suffix for the "ftp.usetmp" option with 3 characters (e.g. ".3te"). If this option is used then the "ftp.prefix" and "ftp.suffix" options are ignored. The "ftp.usesuffix" option can be used to customize the created suffix.

### ftp.nopassword
Some FTP servers may not allow the provision of a password during login. This option allow bypassing the PASS command in such cases.

### ftp.parallelStreams
Only applicable with DissFTP remote servers. Allow setting the number of parallel data channels when processing an upload or a retrieval.

### ftp.passive
Allow selecting the Passive or Active FTP mode. In Passive mode, the FTP client initiates both connections to the server, solving the problem of firewalls filtering the incoming data port connection to the client from the server. In Active mode, the FTP client connects from a random unprivileged port (N &gt; 1023) to the FTP server command PORT, usually port 21. Then, the client starts listening to port N+1 and sends the FTP command PORT N+1 to the FTP server. The server will then connect back to the client specified data port from its local data port, which is usually port 20.

### ftp.password
Allow setting the password while logging into the remote FTP server. This is overwriting the password set through the interface.

### ftp.port
Allow specifying the port number of the remote FTP server to connect to (used to open the control channel).

### ftp.portTimeOut
Enable/disable a timeout when opening the data channel specified by a PORT command. The value must be &gt; 0. A timeout of zero is interpreted as an infinite timeout.

### ftp.postConnectCmd
Allow issuing an FTP command on the remote FTP server right after the control connection is established (multiple commands can be specified using the semi columns separator).

### ftp.postGetCmd
Allow issuing an FTP command on the remote FTP server right after getting a file (multiple commands can be specified using the semi columns separator). The parameter "$filename" can be introduced in the commands and it gives the name of the file which was just retrieved.

### ftp.postMkdirsCmd
Allow issuing an FTP command on the remote FTP server right after creating a directory (multiple commands can be specified using the semi columns separator). The parameter "$dirname" can be introduced in the commands and it gives the name of the directory just created (including all the parent directories). The parameter "$uid" can also be introduced, and it gives the login name. Example: ftp.postMkdirsCmd="SITE CHMOD 0755 $dirname".

### ftp.postPutCmd
Allow issuing an FTP command on the remote FTP server right after putting a file (multiple commands can be specified using the semi columns separator). The parameter "$filename" can be introduced in the commands and it gives the name of the file which was just uploaded. If the "ftp.usetmp" option is used then it will give the temporary name. Example: ftp.postPutCmd="SITE CHMOD 0644 $filename".

### ftp.preCloseCmd
Allow issuing an FTP command on the remote FTP server right before closing the control connection (multiple commands can be specified using the semi columns separator).

### ftp.preGetCmd
Allow issuing an FTP command on the remote FTP server right before getting a file (multiple commands can be specified using the semi columns separator). The parameter "$filename" can be introduced in the commands and it gives the name of the file which is going to be downloaded.

### ftp.preMkdirsCmd
Allow issuing an FTP command on the remote FTP server right before creating a directory (multiple commands can be specified using the semi columns separator). The parameter "$dirname" can be introduced in the commands and it gives the name of the directory to create (including all the parent directories). The parameter "$uid" can also be introduced, and it gives the login name.

### ftp.prePutCmd
Allow issuing an FTP command on the remote FTP server right before putting a file (multiple commands can be specified using the semi columns separator). The parameter "$filename" can be introduced in the commands and it gives the name of the file which is going to be uploaded.

### ftp.prefix
When using the "ftp.usetmp" option, this option allow setting the prefix to use for the temporary file name (e.g. ".").

### ftp.receiveBuffSize
Allow specifying a buffer size when receiving data from the data channel.

### ftp.retryAfterTimeoutOnCheck
If set, the FTP client will try to reconnect if the control connection has timed-out during the data transmission. This proves beneficial during prolonged transfers, preventing control channel blockages caused by specific firewalls.

### ftp.sendBuffSize
Allow specifying a buffer size when sending data on the data channel.

### ftp.setNoop
Allow specifying the "No-Operation" command. This command does not affect anything at all. It performs no action other than having the server send an OK reply. This command is used to keep connections with the FTP servers "alive" (connected) while nothing is being done.

### ftp.suffix
When using the "ftp.usetmp" option, this option allow setting the suffix to use for the temporary file name (e.g. ".tmp").

### ftp.useAppend
Force the use of the APPE FTP command (for appending) instead of the STOR FTP command when pushing a file. This is useful, for example, when one wants to push files to "/dev/null" in order to test network performance and bypass disk storage.

### ftp.useNoop
When the "ftp.keepAlive" option is configured, then this option allow sending NOOPS commands to the remote server in order to keep the connection alive. The value of this option specify the interval between 2 sending of the NOOPS command. A value of 0 deactivate the sending of the NOOPS command. A custom NOOPS command can be specified with the "ftp.setNoop" option.

### ftp.usecleanpath
When using the "ftp.mkdirs" command this option allow forcing the FTP client to fix directory pathnames (e.g. remove multiple instances of /).

### ftp.usenlist
Force using the NLIST command on the remote FTP server when doing a directory listing instead of the standard LIST. This options is required for some FTP servers which do not allow (or do not implement) the LIST command. If a LIST is required and only NLIST is available, the "ftp.ftpLike" option can be used.

### ftp.usesuffix
If set and the "ftp.mksuffix" is set as well then the suffix specified in the "ftp.suffix" option will be concatenated to the generated suffix (e.g. ".3te.tmp").

### ftp.usetmp
Force using a temporary name when transmitting a file to the remote FTP server. Once the transmission has completed successfully, the file is renamed with its final target name. The temporary file name is by default the file name with the ".tmp" suffix concatenated to it, however this behavior can be customized with the "ftp.mksuffix", "ftp.usesuffix", "ftp.prefix" and "ftp.suffix" options.

### ftp.wmoLikeFormat
When initiating a file upload, try converting the target filename to its WMO format (for example, "TEY12080000122300001" becomes "ZTEY01ECMF080000RRA.TEY12080000122300001.LT"). If the transformation process fails, the original filename will be used instead.

## Ftps Options

### ftps.closeTimeOut
Defines the time the connection should wait before closing, after receiving the close signal, ensuring that the connection closure process adheres to the specified timeout. If the close operation exceeds this timeout duration, the system may forcibly terminate the operation, allowing the program to proceed without waiting indefinitely for the connection to close.

### ftps.connectionTimeOut
Defines the duration the system should wait for the connection to be established. If the connection process takes longer than the specified timeout duration, it will result in a timeout exception or a failure to establish the connection within the specified time frame.

### ftps.connectionType
Set security options such as FTPS (FTP over SSL/TLS) or FTPES (explicit FTP over SSL/TLS), enabling secure connections for FTP operations. By invoking this method, the FTPS client can establish secure communication channels between the client and server.

### ftps.cwd
Allow changing directory just after the login (a "cd" command is issued with the specified directory).

### ftps.deleteOnRename
If set, the FTPS client tries to delete the target file before doing a rename (e.g. when using temporary file names with the "ftps.usetmp" option).

### ftps.ignoreCheck
If set, the remote size of the file is not checked after an upload has been completed. If not set, the remote size of the uploaded file is checked and compared against the size of the source file.

### ftps.ignoreDelete
If not set, the FTPS client tries to delete the target file before the upload is processed.

### ftps.ignoreMkdirsCmdErrors
If set, ignore any error occurring during the execution of the command specified in "ftps.preMkdirsCmd" or "ftps.postMkdirsCmd".

### ftps.keepAlive
Allow keeping FTPS control connections alive in a pool. If the connection is unused for longer than the duration specified by this option then the connection is closed and removed from the pool. A duration of zero is interpreted as no pooling.

### ftps.listenAddress
Allow specifying the listen address used by the FTPS client (e.g. when waiting for an incoming data connection). By default, the system will pick up a valid local address. A value of "0.0.0.0" will cause the binding of all the valid network interfaces.

### ftps.login
Allow setting the login while logging into the remote FTPS server. This is overwriting the login set through the interface.

### ftps.md5Ext
When requesting a checksum with "ectrans.createChecksum", allow configuring the MD5 extension.

### ftps.mkdirs
Allow creating the directory named by the target pathname, including any necessary but non-existent parent directories.

### ftps.mkdirsCmdIndex
When using the "ftps.preMkdirsCmd" and "ftps.postMkdirsCmd" options, this index allow selecting the directories for which a command should be triggered. For example, if the directory is "/home/uid/test/data/out/bin", an index of 3 will make sure the FTP client start the "preMkdirsCmd" and/or "postMkdirsCmd" commands for the "home", "uid" and "test" directories. If the index is negative then the selection start from the end of the path instead of the beginning. In the previous example it would make sure the FTPS client start the "preMkdirsCmd" and/or "postMkdirsCmd" commands for the "data", "out" and "bin" directories. Please note that the full path is taken into account as defined in the directory field. To define a home directory that should not be taken into account in this process, it should be defined through the "ftps.cwd" option.

### ftps.mksuffix
If set, the FTPS client will generate a random suffix for the "ftps.usetmp" option with 3 characters (e.g. ".3te"). If this option is used then the "ftps.prefix" and "ftps.suffix" options are ignored. The "ftps.usesuffix" option can be used to customize the created suffix.

### ftps.passive
Allow selecting the Passive or Active FTPS mode. In Passive mode, the FTPS client initiates both connections to the server, solving the problem of firewalls filtering the incoming data port connection to the client from the server. In Active mode, the FTPS client connects from a random unprivileged port (N &gt; 1023) to the FTPS server command PORT, usually port 21. Then, the client starts listening to port N+1 and sends the FTP command PORT N+1 to the FTPS server. The server will then connect back to the client specified data port from its local data port, which is usually port 20.

### ftps.password
Allow setting the password while logging into the remote FTPS server. This is overwriting the password set through the interface.

### ftps.port
Allow specifying the port number of the remote FTPS server to connect to (used to open the control channel).

### ftps.postConnectCmd
Allow issuing an FTP command on the remote FTPS server right after the control connection is established (multiple commands can be specified using the semi columns separator).

### ftps.postGetCmd
Allow issuing an FTP command on the remote FTPS server right after getting a file (multiple commands can be specified using the semi columns separator). The parameter "$filename" can be introduced in the commands and it gives the name of the file which was just retrieved.

### ftps.postMkdirsCmd
Allow issuing an FTP command on the remote FTPS server right after creating a directory (multiple commands can be specified using the semi columns separator). The parameter "$dirname" can be introduced in the commands and it gives the name of the directory just created (including all the parent directories). The parameter "$uid" can also be introduced, and it gives the login name. Example: ftps.postMkdirsCmd="SITE CHMOD 0755 $dirname".

### ftps.postPutCmd
Allow issuing an FTP command on the remote FTPS server right after putting a file (multiple commands can be specified using the semi columns separator). The parameter "$filename" can be introduced in the commands and it gives the name of the file which was just uploaded. If the "ftps.usetmp" option is used then it will give the temporary name. Example: ftps.postPutCmd="SITE CHMOD 0644 $filename".

### ftps.preCloseCmd
Allow issuing an FTP command on the remote FTPS server right before closing the control connection (multiple commands can be specified using the semi columns separator).

### ftps.preGetCmd
Allow issuing an FTP command on the remote FTPS server right before getting a file (multiple commands can be specified using the semi columns separator). The parameter "$filename" can be introduced in the commands and it gives the name of the file which is going to be downloaded.

### ftps.preMkdirsCmd
Allow issuing an FTP command on the remote FTPS server right before creating a directory (multiple commands can be specified using the semi columns separator). The parameter "$dirname" can be introduced in the commands and it gives the name of the directory to create (including all the parent directories). The parameter "$uid" can also be introduced, and it gives the login name.

### ftps.prePutCmd
Allow issuing an FTP command on the remote FTPS server right before putting a file (multiple commands can be specified using the semi columns separator). The parameter "$filename" can be introduced in the commands and it gives the name of the file which is going to be uploaded.

### ftps.prefix
When using the "ftps.usetmp" option, this option allow setting the prefix to use for the temporary file name (e.g. ".").

### ftps.protocol
Allows the selection of the SSL/TLS (Secure Sockets Layer/Transport Layer Security) protocol to use.

### ftps.readTimeOut
Defines the maximum time the system will wait for a response or data to be read from the server once the connection is established. If no data is received within the specified timeout duration, it may result in a timeout exception or failure to read data within the defined time frame.

### ftps.receiveBuffSize
Set the size of the buffer used for receiving data from the FTPS server. Adjusting the receive buffer size can potentially affect the efficiency of data transfer operations, especially when dealing with large amounts of data.

### ftps.sendBuffSize
Set the size of the buffer used for sending data to the FTPS server. Adjusting the send buffer size can potentially impact the efficiency of data transfer operations, particularly when dealing with larger volumes of data.

### ftps.strict
If enabled, enforce SSL certificate validation.

### ftps.suffix
When using the "ftps.usetmp" option, this option allow setting the suffix to use for the temporary file name (e.g. ".tmp").

### ftps.useAppend
Force the use of the APPE FTP command (for appending) instead of the STOR FTP command when pushing a file. This is useful, for example, when one wants to push files to "/dev/null" in order to test network performance and bypass disk storage.

### ftps.useNoop
When the "ftps.keepAlive" option is configured, then this option allow sending NOOPS commands to the remote server in order to keep the connection alive. The value of this option specify the interval between 2 sending of the NOOPS command. A value of 0 deactivate the sending of the NOOPS command.

### ftps.usecleanpath
When using the "ftps.mkdirs" command this option allow forcing the FTPS client to fix directory pathnames (e.g. remove multiple instances of /).

### ftps.usesuffix
If set and the "ftps.mksuffix" is set as well then the suffix specified in the "ftps.suffix" option will be concatenated to the generated suffix (e.g. ".3te.tmp").

### ftps.usetmp
Force using a temporary name when transmitting a file to the remote FTPS server. Once the transmission has completed successfully, the file is renamed with its final target name. The temporary file name is by default the file name with the ".tmp" suffix concatenated to it, however this behavior can be customized with the "ftps.mksuffix", "ftps.usesuffix", "ftps.prefix" and "ftps.suffix" options.

### ftps.wmoLikeFormat
When initiating a file upload, try converting the target filename to its WMO format (for example, "TEY12080000122300001" becomes "ZTEY01ECMF080000RRA.TEY12080000122300001.LT"). If the transformation process fails, the original filename will be used instead.

## Gcs Options

These options help fine-tune the Google Cloud Storage transfer module (gcs) and are accessible via the host editor.

### gcs.allowEmptyBucketName
Allow empty bucket names (no "gcs.bucketName" option). In that case the prefix and path specified in the directory field are used.

### gcs.bucketName
This option allows for the provision of the GCS bucket name. If the bucket name is not explicitly defined, it is extracted from the path specified in the directory field.

### gcs.ftpgroup
Allow forcing a group name in the listing output. Default group name is the login name.

### gcs.ftpuser
Allow forcing a user name in the listing output. Default user name is the login name.

### gcs.mkBucket
Allow creating missing buckets when pushing data. This requires specific permissions.

### gcs.port
Allow specifying the port number of the remote GCS endpoint to connect to.

### gcs.prefix
Allow specifying a prefix, which refers to a part of an object name that comes before the object name itself and is used to organize and categorize objects within a bucket.

### gcs.protocol
Allows the selection of the SSL/TLS (Secure Sockets Layer/Transport Layer Security) protocol to use.

### gcs.region
Sets the region where the GCS service client will operate. Setting the region is crucial because it determines the geographical location of the GCS bucket and the Google servers that will handle the requests.

### gcs.scheme
Allow specifying the scheme when connecting to the endpoint.

### gcs.sslValidation
If enabled, enforce SSL certificate validation.

### gcs.url
Allow specifying an alternative URL to connect to the endpoint. By default, the URL is constructed using the "gcs.scheme", "gcs.port" options and hostname field (scheme://hostname:port).

## Http Options

These options help fine-tune the http transfer module and are accessible via the host editor.

### http.allowCircularRedirects
If enabled, permits the HTTP client to automatically follow redirects even if they form a circular loop, where a request redirects to a URL that has previously been visited in the same redirect chain.

### http.alternativePath
When parsing the listing output, it allows the addition of a path to the found filename. This can be useful if the HTTP server provides only the filename without its associated path. However, this suppose the path is known in advance.

### http.attribute
Specify the anchor element within the parsed HTML document to use for extracting filenames. For instance, using "href" selects all elements with an "href" attribute. By default, it retrieves the text content of each element, excluding HTML tags, while including the textual content of its descendant elements.

### http.authcache
Allow handling Basic authentication by creating a cache and associating it with the target host. This is only used when "http.credentials" is enabled.

### http.authheader
Allow creating a Basic Authentication header. This header is commonly used for HTTP Basic Authentication, where the user-name and password are Base64-encoded and sent in the HTTP request headers to authenticate the client.

### http.credentials
When enabled, this feature configures the credentials for HTTP client authentication, using the provided user-name and password credentials to authenticate requests.

### http.dodir
When enabled (default), a GET request is processed to retrieve the listing output from the specified URL. If disabled, the listing output is the specified URL itself.

### http.enableContentCompression
If content compression is enabled (default), it means that the HTTP client requests compressed content from the server, aiming to reduce data transfer size and improve performance. This allows the HTTP client to handle compressed content from the server, if available and supported.

### http.encodeURL
If enabled, encode URL strings according to the URL encoding rules defined in the UTF-8 character set. This method replaces certain characters in the string with their respective hexadecimal representation, making the string safe for use in a URL.

### http.failOnEmptySymlink
When enabled and a symbolic link is detected (URL does not end with "/" and no "Content-Length" found in header) with no "filename" attribute found in the "Content-Disposition" header, force the listing task to fail.

### http.filenameAttribute
In the context of a file upload (PUT or POST), this option allow specifying the name of the parameter or the key under which the binary content will be sent in the multipart request.

### http.ftpLike
When enabled, during the processing of a listing, it attempts to collect the necessary information to create an output similar to what would be obtained from an FTP server listing.

### http.ftpgroup
When "http.ftpLike" option is selected allow forcing a group name in the listing output. Default group name is the login name. 

### http.ftpuser
When "http.ftpLike" option is selected allow forcing a user name in the listing output. Default user name is the login name.

### http.headers
Allow the specification of additional headers to be included in the header list, alongside standard headers such as "User-Agent", "Accept" and authentication headers (if "http.authheader" is set). These headers will be sent to the HTTP server with every request.

### http.isSymlink
Allow forcing the symbolic link detector to always return the provided value.

### http.listMaxDirs
Specify the depth level when listing sub-directories.

### http.listMaxFiles
Allow specifying a maximum number of files which can be listed.

### http.listMaxThreads
Specify the maximum concurrent connections allowed for listing sub-directories.

### http.listMaxWaiting
Set the upper limit for the capacity of the queue handling requests for listing sub-directories. Attempt to add elements beyond the specified capacity will block until a listing has completed.

### http.listRecursive
Allow listing sub-directories recursively.

### http.maxSize
Allow setting the maximum size for an HTML document when processing a GET request to retrieve the listing output.

### http.mqttAwait
While waiting for incoming notifications from the MQTT broker, this parameter represents the maximum duration to await before initiating a disconnection. This parameter works in conjunction with the "http.mqttMaxFiles" parameter, terminating the process upon the first occurrence, whichever comes first.

### http.mqttCleanStart
Sets the "Clean Start" flag for the MQTT connection. True indicates a new session and requests the broker to clear any previous session state (the broker will not restore previously saved information, such as subscriptions or message queues). False indicates a continuation of an existing session, and the broker maintains any previous session state (e.g. subscriptions and message queues).

### http.mqttConnectionTimeout
Set the maximum time that the client will wait for the MQTT connection to be established.

### http.mqttHref
The MQTT messages are expected to be in JSON format, and this option allows specifying which field to use in the JSON message for extracting the file reference.

### http.mqttKeepAliveInterval
Set the interval at which the client should send a "keep alive" message to the MQTT broker. "Keep alive" messages are used to maintain the connection between the client and the broker. If there is no other communication between the client and the broker within the specified interval, the client sends a "keep alive" message to ensure the connection remains active.

### http.mqttMaxFiles
While waiting for incoming notifications from the MQTT broker, this parameter represents the maximum number of files to await before initiating a disconnection. This parameter works in conjunction with the "http.mqttAwait" parameter, terminating the process upon the first occurrence, whichever comes first.

### http.mqttMode
True indicates the server nature as an MQTT server and the necessity to subscribe to obtain the list of data file references.

### http.mqttPersistence
Request for a persistence mechanism to be used, such as in-memory storage or file-based storage, responsible for handling the persistence of MQTT-related data (e.g., storing messages and subscriptions). This enables the MQTT client to resume its operation from the point of disconnection or during subsequent reconnects. In a multi-data mover environment, it is essential to use this parameter in conjunction with the "ecpds.moverListForProcessing" option, ensuring that the persistence mechanism consistently uses the same data mover for all subsequent requests.

### http.mqttPersistenceDirectory
If "http.mqttPersistence" is configured, and the persistence mechanism is set to file-based storage, this option allows for the modification of the directory used for persistence. By default, it uses the temporary directory of the data mover.

### http.mqttPersistenceMode
If "http.mqttPersistence" is set, this option allows the selection of the persistence mechanism, either in-memory storage (memory) or file-based storage (file).

### http.mqttPort
Allow specifying the MQTT port number of the remote MQTT/S server to connect to.

### http.mqttQos
Set the Quality of Service (QoS) level for messages published by the MQTT client. For a value of "0", the message is delivered at most once without any confirmation, offering the lowest delivery guarantee. For a value of "1", the message is delivered at least once, which may result in duplicates but ensures message delivery. For a value of "2", the message is delivered exactly once through a handshake process, guaranteeing no duplicates but involving higher communication overhead.

### http.mqttScheme
Allow specifying the MQTT scheme of the remote MQTT/S server to connect to.

### http.mqttSessionExpiryInterval
Allow specifying the maximum time duration that the broker should maintain the client session state after the client disconnects unexpectedly or intentionally. If the client does not reconnect within this specified interval, the broker will clean up the client session state.

### http.mqttSubscriberId
Allow specifying the client identifier for the MQTT client being created. must be unique among all clients connected to the MQTT broker. It identifies the client to the broker during the connection process. It allows the MQTT broker to remember the client subscriptions and other session-related information, enabling features such as message persistence, retained messages, and resumption of subscriptions upon reconnection.

### http.mqttUrl
Allow specifying an alternative URL to connect to the MQTT/S server. By default, the URL is constructed using the "http.mqttScheme", "http.mqttPort" options and hostname field (mqttScheme://hostname:mqttPort).

### http.multipartMode
In the context of a file upload (PUT or POST), defines how the multipart entity handles and encodes the parts within it. This value should be set according to the requirements of the HTTP server.

### http.port
Allow specifying the port number of the remote HTTP/S server to connect to.

### http.protocol
Allows the selection of the SSL/TLS (Secure Sockets Layer/Transport Layer Security) protocol to use.

### http.proxy
Allow specifying a HTTP proxy when connecting to the remote site (e.g. "host=proxy.domain.ms,protocol=https,port=8080").

### http.scheme
Allow specifying the scheme when connecting to the remote site.

### http.select
Enable the specification of a CSS query when listing the HTML document. The default query selects all anchor (&lt;a&gt;) elements that contain an "href" attribute within the HTML document, essentially extracting all links from the document.

### http.sslValidation
If enabled, enforce SSL certificate validation.

### http.strict
If enabled, enforce hostname verification for SSL connections.

### http.supportedProtocols
This parameter defines which protocols can be used during the SSL handshake process to establish a secure connection with the HTTPS server.

### http.uploadEndPoint
In the context of a file upload (PUT or POST), this option allow specifying the URL or route on the remote server that expects incoming data using the PUT or POST method for uploading purposes. This endpoint is designed to handle and process the incoming data according to the server logic or application requirements.

### http.urldir
When performing a listing, a "/" is appended to the end of the URL if it lacks parameters (i.e. no "?" is found in the URL) or does not end with an extension such as ".html", ".htm", or ".txt". This option allows overriding the default behavior and enforcing a specific choice.

### http.useHead
This option allows the use of the HEAD HTTP method whenever feasible (i.e. allowed by the remote site and compliant with the underlying HTTP request). If the HEAD HTTP method is not viable, a GET request will be used as an alternative, retrieving only the pertinent information from the headers. While this method provides the necessary information, it can be resource-intensive for the remote HTTP server. Enabling this option is advisable whenever possible.

### http.useMultipart
In the context of a file upload (PUT or POST) specify if a multipart entity should be used.

### http.usePost
In the context of a file upload, specify whether to use an HTTP POST method instead of an HTTP PUT method.

## Incoming Options

These options help fine-tune the data portal for the underlying destination and are accessible via the destination editor.

### incoming.dateformat
Allow specifying the date format when using the "$date" parameter in the "incoming.uniquename" and "incoming.metadata" options.

### incoming.delay
Allow specifying a delay when creating the scheduled time for a file pushed by a user via the data portal. By default the scheduled time is the current time.

### incoming.event
Enable the generation of an event when a user pushes a file through the data portal, triggering a notification system push (e.g. MQTT).

### incoming.failOnMetadataParsingError
Enable failure upon unsuccessful parsing of metadata provided through the "incoming.metadata" option. When enabled, parsing errors result in the file being discarded.

### incoming.lifetime
Allow specifying a lifetime for a file pushed by a user via the data portal. When a data transfer has exceeded its designated lifetime, it is marked as expired and becomes unavailable for dissemination or download.

### incoming.maxBytesPerSecForInput
Allow capping the transfer rate for downloads via the data portal.

### incoming.maxBytesPerSecForOutput
Allow capping the transfer rate for uploads via the data portal.

### incoming.metadata
Enable the specification of metadata for a file uploaded by a user through the data portal. Use placeholders like "$date", "$timestamp", "$destination", "$target", "$original", and "$timefile" within the metadata, which will be substituted by their respective values.

### incoming.order
If sorting is requested with "incoming.sort", indicate whether the listing should be in ascending (asc) or descending (desc) order.

### incoming.priority
Enable the setting of a priority for a file pushed by a user via the data portal.

### incoming.rootdir
Allow specifying an alternative name than the destination name when displaying the path to this destination through the data portal.

### incoming.sort
Allow specifying if the listing through the data portal should be sorted by size, target name, or time.

### incoming.standby
Enable the enforcement of standby mode for a file pushed by a user via the data portal.

### incoming.tmp
When a user uploads a file through the data portal, this feature permits the specification of a regex pattern to identify if the file is temporary. If flagged as temporary, the file remains in standby mode until the user renames it to its definitive name.

### incoming.version
Allow specifying an optional version number for a file pushed by a user via the data portal.  Use placeholders like "$date", "$timestamp", "$destination", "$target", "$original", and "$timefile" within the version, which will be substituted by their respective values.

## Master Options

This option help fine-tune the virtual FTP server for accessing the underlying host and is accessible via the host editor.

### master.homeDir
Define the home directory when accessing this host via the virtual FTP server on the master. This is particularly helpful when the host is dedicated to acquisition and contains multiple directories.

## Mqtt Options

These options help fine-tune the notification system with MQTT and are accessible via the destination editor.

### mqtt.clientId
The client identifier (client ID) is a unique identifier that distinguishes one MQTT client from another. This option allows publishing all messages to a specific MQTT client connected to the HiveMQ broker.

### mqtt.contentType
This property allow to define the format or type of the payload data being transmitted in the MQTT messages. It specifies how the payload should be interpreted or processed by the receiving end (e.g. "application/json").

### mqtt.expiryInterval
This property specifies how long the broker should retain the message if it is not delivered due to subscriber unavailability. By default, the message remains retained until the expiry date of the data transfer is reached.

### mqtt.publish
If data transfer requests directed to this destination trigger events ("acquisition.event" or "incoming.event"), enabling this option ensures notifications are published to the MQTT broker upon completion of these transfer requests.

### mqtt.qos
Allow selecting the Quality of Service (QoS) when publishing messages to the MQTT broker. The options are 0 (at most once), 1 (at least once) or 2 (exactly once).

### mqtt.retain
This option allow setting the retain flag. This ensures that all messages are saved by the broker and sent to new subscribers upon their connection. Retained messages remain available until it expires or a newer message is published on the same topic with the retain flag.

### mqtt.topic
Build the MQTT topic. It is either defined in the destination setup (Properties or JavaScript), or by default it is using the destination name. If the topic ends with a "/" (the default when no topic is specified), the target name is appended.

## Portal Options

These options help fine-tune the data portal for the underlying data user and are accessible via the data user editor.

### portal.anonymous
Allow treating this data user as anonymous, requiring no authentication.

### portal.color
Allow specifying the color to be used for the header and footer sections (e.g. "black" or "#000000").

### portal.deletePathPermRegex
Allow the configuration of a regular expression (regex) to control whether the "delete" operation is permitted. The regex is applied to the full path, including the domain name. A valid value could be: "(.*):/data/incoming/(.*)".

### portal.destination
Allow setting a default destination to prevent displaying the DATA/DESNAME directory within the path. For this to work, the destination must be activated and associated with this data user.

### portal.dirPathPermRegex
Allow the configuration of a regular expression (regex) to control whether the "dir" operation is permitted. The regex is applied to the full path, including the domain name. A valid value could be: "(.*):/data/incoming/(.*)".

### portal.domain
Allow setting a default domain to prevent displaying the DATA directory within the path. This option is ignored if "portal.destination" is defined as it takes over.

### portal.footer
Allow specifying the title to be displayed at the bottom of the page (e.g. "Copyright ECMWF").

### portal.geoblocking
This option accepts a list (separated by a comma) of continents, country ISO codes, and/or city names to restrict access to the data portal exclusively from those specified locations (e.g. "Europe" or "FR,Paris"). If there is no available source IP from the incoming connection or if the IP source does not map to any location (e.g. localhost or unknown), access will be denied.

### portal.getPathPermRegex
Allow the configuration of a regular expression (regex) to control whether the "get" operation is permitted. The regex is applied to the full path, including the domain name. A valid value could be: "(.*):/data/incoming/(.*)".

### portal.maxConnections
Define the maximum concurrent connections allowed for this user.

### portal.maxRangesAllowed
This option allow setting the maximum count of ranges that can be provided in HTTP multipart/byteranges requests.

### portal.mkdirPathPermRegex
Allow the configuration of a regular expression (regex) to control whether the "mkdir" operation is permitted. The regex is applied to the full path, including the domain name. A valid value could be: "(.*):/data/incoming/(.*)".

### portal.mqttPermission
This parameter defines the list of MQTT topics the data user is allowed to subscribe to (separated by a comma). In the pattern, the "+" symbol matches a single MQTT topic level and can occur at any position. For instance, "foo/+/bar" will match "foo/one/bar" or "foo/two/bar". The "#" symbol matches multiple entire MQTT topic levels but is solely applicable at the end of the topic pattern. For example, "foo/#" will match "foo/bar", "foo/bar/one", and "foo/bar/one/two".

### portal.msgDown
Allow specifying a text message to be displayed right after the listing.

### portal.msgTop
Allow specifying a text message to be displayed right before the listing.

### portal.mtimePathPermRegex
Allow the configuration of a regular expression (regex) to control whether the "mtime" operation is permitted. The regex is applied to the full path, including the domain name. A valid value could be: "(.*):/data/incoming/(.*)".

### portal.order
If sorting is requested with "portal.sort", indicate whether the listing should be in ascending (asc) or descending (desc) order.

### portal.putPathPermRegex
Allow the configuration of a regular expression (regex) to control whether the "put" operation is permitted. The regex is applied to the full path, including the domain name. A valid value could be: "(.*):/data/incoming/(.*)".

### portal.recordHistory
If enabled, specify that any upload or download must be added to the data transfer history list. Disabling this option can be useful if this is an anonymous user triggering too many requests.

### portal.recordSplunk
If enabled, specify that any upload or download must be logged in Splunk as an INH (INcoming History) entry. Only valid if Splunk is configured.

### portal.renamePathPermRegex
Allow the configuration of a regular expression (regex) to control whether the "rename" operation is permitted. The regex is applied to the full path, including the domain name. A valid value could be: "(.*):/data/incoming/(.*)".

### portal.rmdirPathPermRegex
Allow the configuration of a regular expression (regex) to control whether the "rmdir" operation is permitted. The regex is applied to the full path, including the domain name. A valid value could be: "(.*):/data/incoming/(.*)".

### portal.simpleList
Indicate whether the listing should be displayed as an HTML page or a simple text list of files. Enabling this option can be advantageous when accessing through a command-line interface.

### portal.sizePathPermRegex
Allow the configuration of a regular expression (regex) to control whether the "size" operation is permitted. The regex is applied to the full path, including the domain name. A valid value could be: "(.*):/data/incoming/(.*)".

### portal.sort
Allow specifying if the listing should be sorted by size, target name, or time.

### portal.tab
Allow specifying the text to be displayed on the browser tab (&lt;title&gt; HTML tag).

### portal.title
Allow specifying the title to be displayed at the top of the page (e.g. "Personal Data Store (PDS)").

### portal.triggerEvent
If deactivated, block any transfer history or Splunk recording from being initiated.

### portal.triggerLastRangeOnly
If activated, and in the case of an HTTP multipart/byteranges request, an event is generated only upon the completion of the final range download. Otherwise, an event is generated for each individual range.

### portal.updateLastLoginInformation
Specify whether the last login information should be recorded for this user. For anonymous access, it might be desirable to deactivate recording to prevent flooding the underlying database.

### portal.usePasscode
Specify whether the TOTP authentication is using a passcode obtained from a Token or is using a password.

### portal.warning
Allow specifying a warning message to be displayed after the header (e.g. "Service downtime scheduled, apologies for the inconvenience!").

### portal.welcome
When accessing the data portal with the FTP protocol, this option enables the configuration of a welcome banner for the user.

## Proxy Options

These options help fine-tune the replication process for the underlying proxy host and are accessible via the host editor.

### proxy.httpMoverUrl
Allow specifying an alternative HTTP URL for the data mover. This URL is used by the continental data mover to relay information regarding its activity. By default, the data mover responsible for sending the request to the continental data mover is used. This feature can be beneficial if only a single data mover is accessible to the continental data mover.

### proxy.httpProxyUrl
Allow specifying a HTTP proxy when connecting to the continental data mover.

### proxy.modulo
Specify the number of unsuccessful data transmission on the continental data mover before trying from the local data mover (once in every "proxy.modulo" attempts).

### proxy.timeout
Specify the connect timeout duration for connecting to the continental data mover HTTP URL.

## Retrieval Options

These options help fine-tune the data retrieval process for the underlying acquisition host and are accessible via the acquisition host editor.

### retrieval.interruptSlow
Terminate slow data retrievals upon reaching the specified maximum duration in "retrieval.maximumDuration" or the minimum rate defined in "retrieval.minimumRate".

### retrieval.maximumDuration
Maximum duration allowed for a data retrieval.

### retrieval.minimumDuration
Minimum duration before starting to check the transfer rate and maximum duration allowed.

### retrieval.minimumRate
Minimum rate allowed for a data retrieval.

### retrieval.rateThrottling
Maximum rate allowed for a data retrieval.

## S3 Options

These options help fine-tune the Amazon S3 transfer module (s3) and are accessible via the host editor.

### s3.acceleration
Enable the transfer acceleration mode for uploading and downloading objects in the S3 buckets. When transfer acceleration is enabled for an S3 bucket, Amazon S3 uses the CloudFront global content delivery network (CDN) to accelerate the transfer of data between the client and the S3 bucket by optimizing the network path between them. Transfer acceleration might incur additional costs compared to standard S3 transfers, but it can significantly improve data transfer speeds in some scenarios, especially for long distance communication. Therefore, it is recommended to evaluate the use of transfer acceleration based on your specific requirements and use cases.

### s3.allowEmptyBucketName
Allow empty bucket names (no "s3.bucketName" option). In that case the prefix and path specified in the directory field are used.

### s3.bucketName
This option allows for the provision of the S3 bucket name. If the bucket name is not explicitly defined, it is extracted from the path specified in the directory field.

### s3.disableChunkedEncoding
Allow disabling chunked encoding for PutObject and UploadPart requests. Setting this option has performance implications since the checksum for the payload will have to be pre-calculated before sending the data. If the payload is large this will affect the overall time required to upload an object. Using this option is recommended only if the endpoint does not implement chunked uploading.

### s3.dualstack
Enabling this option allows the client to utilize AWS-provided dual-stack endpoints when communicating with the Amazon S3 service. However, it assumes that the network environment of the data movers supports IPv6 and is appropriately configured to use it. This setting ensures attempts to utilize IPv6-capable endpoints for interactions with S3.

### s3.enableMarkAndReset
Used to enable mark-and-reset for non-mark-and-resettable non-file input stream for up to 128K memory buffering.

### s3.enablePathStyleAccess
Enabling path-style access means that requests to Amazon S3 will use the path-style URL format, explicitly adding the bucket name in the request path, irrespective of whether the bucket name adheres to DNS naming conventions. This feature can be beneficial in scenarios where there are challenges with DNS-compliant bucket names or when accessing buckets not configured for virtual-hosted-style access. It is essential to note that although path-style access can be enabled, AWS strongly recommends using virtual-hosted-style access for S3 buckets whenever possible due to its superior performance and scalability.

### s3.forceGlobalBucketAccess
Enable global access to S3 buckets via the global S3 endpoint (s3.amazonaws.com). This functionality allows accessing buckets globally, utilizing their DNS-compliant names across all AWS regions, rather than region-specific endpoints. This can be beneficial in scenarios where bucket names are DNS-compliant and uniquely global across AWS regions. However, it is important to note that not all bucket names are suitable for global access due to DNS restrictions, so the use of this feature should be considered carefully based on the application requirements.

### s3.ftpgroup
Allow forcing a group name in the listing output. Default group name is the login name.

### s3.ftpuser
Allow forcing a user name in the listing output. Default user name is the login name.

### s3.listenAddress
Allow specifying the source address used by the S3 client when connecting to the endpoint. By default, the system will pick up a valid local address.

### s3.mkBucket
Allow creating missing buckets when pushing data. This requires specific permissions.

### s3.multipartSize
When the file size is predetermined and exceeds this specified value, a multipart upload is initiated. If the file size is not known in advance (e.g. due to on-the-fly compression), a multipart upload is always performed.

### s3.numUploadThreads
This option allow the configuration of the number of upload threads to be used during multipart uploads.

### s3.partSize
This option allows for configuring the size of parts, measured in megabytes, used during multipart uploads.

### s3.port
Allow specifying the port number of the remote S3 endpoint to connect to.

### s3.prefix
Allow specifying a prefix, which refers to a part of an object key name that comes before the object name itself and is used to organize and categorize objects within a bucket.

### s3.protocol
Allows the selection of the SSL/TLS (Secure Sockets Layer/Transport Layer Security) protocol to use.

### s3.queueCapacity
This option enables the configuration of the queue capacity used during multipart uploads to store parts for processing. When the queue reaches its maximum capacity, any attempts to add more elements will result in blocking.

### s3.recursiveLevel
Allow specifying a depth level while recursively listing sub-directories.

### s3.region
Sets the region where the AWS service client will operate. Setting the region is crucial because it determines the geographical location of the S3 bucket and the AWS servers that will handle the requests.

### s3.roleArn
This option allows the provision of the Amazon Resource Name (ARN) of the IAM role to be assumed. It is part of the AWS Security Token Service (STS) and is used to request temporary security credentials by assuming an IAM role.

### s3.roleSessionName
This option enables the provision of the assumed role session, serving as a unique identifier or a descriptive name for the session. It is a component of the AWS Security Token Service (STS) and is used for requesting temporary security credentials by assuming an IAM role.

### s3.scheme
Allow specifying the scheme when connecting to the endpoint.

### s3.singlepartSize
When used alongside the "s3.useByteArrayInputStream" option, this allows for configuring the maximum size allowed for buffering during data transmission.

### s3.sslValidation
If enabled, enforce SSL certificate validation.

### s3.strict
If enabled, enforce hostname verification for SSL connections.

### s3.url
Allow specifying an alternative URL to connect to the endpoint. By default, the URL is constructed using the "s3.scheme", "s3.port" options and hostname field (scheme://hostname:port).

### s3.useByteArrayInputStream
When the file size is not known in advance (e.g. due to on-the-fly compression), a PutObject request is initiated. In this scenario, this option allows specifying whether a buffer should be used during data transmission. The buffer is used only if the file size does not exceed the value specified in the "s3.singlepartSize" option.

## Scheduler Options

These options help fine-tune the transfer scheduler for the underlying destination and are accessible via the destination editor.

### scheduler.activeTimeRange
Allow the specification of a list of time ranges, separated by commas, during which the scheduler for this destination is active. This feature enables the processing of dissemination only within the selected time ranges.

### scheduler.asap
Enabling this option schedules a data transfer submitted to this destination to start immediately (as soon as possible) by setting the schedule time to the current time. It is important to note that this action will override any option specified in the ecpds command (-asap option). Please be aware that the transfer may not start immediately if there are other pending files in the queue. Additionally, priorities will also influence the scheduling.

### scheduler.delay
Allow adding a delay to the scheduled time of a data transfer submitted to this destination. This delay will be in addition to any existing delay already set up through the ecpds (-delay) command or the "incoming.delay" option for this destination.

### scheduler.force
When a file is pushed to this destination, this option allows modification of various scheduler parameters such as "scheduler.lifetime", "scheduler.delay", "scheduler.noRetrieval", "scheduler.asap", "scheduler.transfergroup" and "scheduler.standby". The "pattern" and "ignore" parameters use regular expressions (regex) to select specific files in a single line. For instance: scheduler.force = "asap=yes;standby=never;pattern=E1(.*)". This setting takes precedence over other parameters defined outside the "scheduler.force" option. If necessary, this option can be divided across multiple lines to provide specific rules for different groups of data transfers. In this case, each line should follow this format: " ({operator} filename) {options}". The {operator} can be one of the following: ".=" (starts with), "==" (equals to; if the filename part is between {} then it is considered a regex), "=." (ends with), "!=" (not equal to). For example: "(== {avhrr_n.*}) standby=never;delay=2h".

### scheduler.forceStop
If the "failOnDestinationNotFound" option is enabled in the "ECpdsPlugin" of the master (ecmwf.properties files), this setting allows any data transfer request submitted to this destination to be forcibly set to the STOP status.

### scheduler.lifetime
This option allows for changing the lifetime of any data transfer request submitted to this destination. When a data transfer has exceeded its designated lifetime, it is marked as expired and becomes unavailable for dissemination or download.

### scheduler.masterToNotifyOnDone
Allow notifying another master that a data transfer request has completed successfully and that the same transfer request on this other master should be stopped. This option requires the DNS name of the other master. The match for the transfer request on the other master is executed by conducting a search based on the destination name, target name, and transfer unique key. If the data transfer is located, it is then stopped; otherwise, no action is taken. For this to function, the other master must be accessible through its RMI port.

### scheduler.noRetrieval
This option allows for changing the "noRetrieval" flag of any data transfer request submitted to this destination.

### scheduler.requeueignore
When used alongside the "scheduler.requeueon" option, this feature allows the provision of a regex pattern to select the target names that do not need to be checked for duplicates.

### scheduler.requeueon
When a new data transfer request is processed in the queue, this option allows for enabling a check to find any other data transfer in the destination with the same target name but a different data transfer identifier. This situation may arise when multiple acquisition hosts are receiving data transfers from different sources but with identical file names. The check involves a JavaScript expression that can take the following parameters: "$time1" (the generation date of the initial file), "$time2" (the generation date of the found duplicate file), "$size1", "$size2", "$destination", and "$target" (e.g., "$size2 &gt; $size1"). If a duplicate is found according to the provided rule, then the current data transfer request is set on hold.

### scheduler.requeuepattern
When used alongside the "scheduler.requeueon" option, this feature allows the provision of a regex pattern to select the target names that need to be checked for duplicates.

### scheduler.resetQueueOnChange
When a new data file is retrieved via the retrieval scheduler, this option allows for requesting a reset of the queue for each destination containing a data transfer request associated with this file. For instance, if these new transfer requests have higher priority than the current ones in the queues, resetting the queues ensures that the latest order is immediately prioritized, bypassing the need to wait for ongoing data transfers in memory to complete.

### scheduler.standby
This option allows for changing the "standby" flag of any data transfer request submitted to this destination.

### scheduler.transfergroup
This option allows for changing the "transfergroup" of any data transfer request submitted to this destination.

## Sftp Options

These options help fine-tune the sftp transfer module and are accessible via the host editor.

### sftp.allocate
This parameter allows forcing ECPDS to request the target hostname from a web service. When activated, the provided hostname via the interface is disregarded. This feature proves beneficial when the remote site aims to implement a load-balanced mechanism based on factors such as the filename or file size to select the target host for a data transfer. This option requires two parameters: "url" and "req". "url" specifies the HTTP/S service to which the request is sent (using $filename and $filesize), while "req" details how to extract the hostname from the JSON-formatted response of the web service (e.g. "url=http://host/service?file=$filename&length=$filesize;req=json.host[0]").

### sftp.bulkRequestNumber
Specify how many requests may be sent at any one time. Increasing this value may slightly improve file transfer speed but will increase memory usage.

### sftp.chmod
If set, the file mode will be changed according to the value of this option once it has uploaded a file successfully (e.g. "640").

### sftp.cipher
Allow specifying the cipher algorithms to allow (e.g. "aes128-cbc,3des-cbc,blowfish-cbc"). The list of valid algorithms can be found at http://www.jcraft.com/jsch/README (multiple algorithms can be specified using the columns separator). By default all supported algorithms are allowed.

### sftp.clientVersion
Allow setting the client version string that will be presented to the SSH server when initiating a connection. The SSH protocol defines that the client sends its version string to the server as part of the initial handshake.

### sftp.commit
When the "sftp.allocate" option is enabled, this parameter becomes mandatory. It is used to signal the successful file transmission to the target host that was selected through the "sftp.allocate" process. The option requires two parameters: "url" and "req". "url" specifies the HTTP/S service where the notification is directed, while "req" specifies the expected HTTP status code returned from the service. The service hosted at the provided URL is expected to receive a POST request with the original JSON-formatted response obtained during the "sftp.allocate" process. If the received HTTP status code does not match the expected code, the data transmission is marked as failed within ECPDS. Depending on the configuration, this may result in the transfer request being re-queued.

### sftp.compression
Allow specifying the compression algorithms to allow (e.g. "zlib,none"). The list of valid algorithms can be found at http://www.jcraft.com/jsch/README (multiple algorithms can be specified using the columns separator). By default all supported algorithms are allowed.

### sftp.connectTimeOut
This value is used as the connection timeout. The value must be &gt; 0. A timeout of zero is interpreted as an infinite timeout. The default value is the same as the "sessionTimeOut" value.

### sftp.cwd
Allow changing directory just after the login (a "cd" command is issued with the specified directory).

### sftp.execCmd
Allow specifying a command (or list of commands) to execute on the SSH server once a file has been transmitted (multiple commands can be specified using the semi columns separator). The parameter "$filename" can be introduced in the commands and it gives the name of the file transmitted (source or target depending if the transmission was a upload or a download).

### sftp.execCode
Allow specifying the expected exit code of the command (or list of commands) started with the "sftp.execCmd" option. If the exit code is different from the one specified in this parameter then the transmission is tagged as failed.

### sftp.fingerPrint
Allow specifying a key fingerprint. It is a lower-case hexadecimal representation of the MD5 of a key (e.g. "22:fb:ee:fe:18:cd:aa:9a:9c:78:89:9f:b4:78:75:b4"). If a key fingerprint is defined then it is compared against the key fingerprint of the remote SFTP server when connecting. If it does not match then the connection is rejected.

### sftp.ignoreCheck
If set, the remote size of the file is not checked after an upload has been completed. If not set, the remote size of the uploaded file is checked and compared against the size of the source file.

### sftp.ignoreMkdirsCmdErrors
If set, ignore any error occurring during the execution of the command specified in "sftp.preMkdirsCmd" or "sftp.postMkdirsCmd".

### sftp.kex
Allow specifying the key exchange algorithms to allow (e.g. "ecdh-sha2-nistp256,ecdh-sha2-nistp384"). The list of valid algorithms can be found at http://www.jcraft.com/jsch/README (multiple algorithms can be specified using the columns separator). By default all supported algorithms are allowed.

### sftp.listMaxDirs
Specify the depth level when listing sub-directories.

### sftp.listMaxThreads
Specify the maximum concurrent connections allowed for listing sub-directories.

### sftp.listMaxWaiting
Set the upper limit for the capacity of the queue handling requests for listing sub-directories. Attempt to add elements beyond the specified capacity will block until a listing has completed.

### sftp.listRecursive
Allow listing sub-directories recursively.

### sftp.listenAddress
Allow specifying the source address used by the SFTP client when connecting to the remote SFTP server. By default, the system will pick up a valid local address.

### sftp.login
Allow setting the login while logging into the remote SFTP server. This is overwriting the login set through the interface.

### sftp.mac
Allow specifying the Message Authentication Code (MAC) algorithms to allow (e.g. "hmac-md5,hmac-md5-96,hmac-sha1,hmac-sha1-96"). The list of valid algorithms can be found at http://www.jcraft.com/jsch/README (multiple algorithms can be specified using the columns separator). By default all supported algorithms are allowed.

### sftp.md5Ext
When requesting a checksum with "ectrans.createChecksum", allow configuring the MD5 extension.

### sftp.mkdirs
Allow creating the directory named by the target pathname, including any necessary but non-existent parent directories.

### sftp.mkdirsCmdIndex
When using the "sftp.preMkdirsCmd" and "sftp.postMkdirsCmd" options, this index allow selecting the directories for which a command should be triggered. For example, if the directory is "/home/uid/test/data/out/bin", an index of 3 will make sure the SFTP client start the "preMkdirsCmd" and/or "postMkdirsCmd" commands for the "home", "uid" and "test" directories. If the index is negative then the selection start from the end of the path instead of the beginning. In the previous example it would make sure the SFTP client start the "preMkdirsCmd" and/or "postMkdirsCmd" commands for the "data", "out" and "bin" directories. Please note that the full path is taken into account as defined in the directory field. To define a home directory that should not be taken into account in this process, it should be defined through the "sftp.cwd" option.

### sftp.mksuffix
If set, the SFTP client will generate a random suffix for the "sftp.usetmp" option with 3 characters (e.g. ".3te"). If this option is used then the "sftp.prefix" and "sftp.suffix" options are ignored.

### sftp.options
Allows configuring various available parameters within the underlying SFTP implementation, as detailed in the JSFTP documentation. Any parameters specified here can potentially override settings from other options, as this particular one takes precedence (e.g. "PubkeyAcceptedAlgorithms=ssh-dss"). Multiple parameters can be set across multiple lines.

### sftp.passPhrase
The passphrase necessary to access the private key when the "sftp.privateKey" or "sftp.privateKeyFile" option is used. This option is only required if the private key is protected.

### sftp.password
Allow setting the password while logging into the remote SFTP server. This is overwriting the password set through the interface.

### sftp.port
Allow specifying the port number of the remote SFTP server to connect to.

### sftp.postMkdirsCmd
Allow issuing an SSH command on the remote SSH server right after creating a directory (multiple commands can be specified using the semi columns separator). The parameter "$dirname" can be introduced in the commands and it gives the name of the directory just created (including all the parent directories). The parameter "$uid" can also be introduced, and it gives the login name. Example: sftp.postMkdirsCmd="chmod 0755 $dirname".

### sftp.preMkdirsCmd
Allow issuing an SSH command on the remote SSH server right before creating a directory (multiple commands can be specified using the semi columns separator). The parameter "$dirname" can be introduced in the commands and it gives the name of the directory to create (including all the parent directories). The parameter "$uid" can also be introduced, and it gives the login name.

### sftp.preferredAuthentications
Allow specifying the list of supported authentication methods (e.g. "password,publickey,keyboard-interactive").

### sftp.prefix
When using the "sftp.usetmp" option, this option allow setting the prefix to use for the temporary file name (e.g. ".").

### sftp.privateKey
Adds a private key to be used for public-key authentication. The private key should be in the PEM or OPENSSH format.

### sftp.privateKeyFile
Adds a private key to be used for public-key authentication. The private key should be in the PEM format in a file accessible from the ECaccess Gateway (the file permissions should be set accordingly). This option contains the name of the file (e.g. "/home/uid/.ssh/mykey.pem"). If both options, "sftp.privateKeyFile" and "sftp.privateKey" are defined, then the latest will be ignored.

### sftp.properties
Allow setting options for the HTTP client used to interact with the Web service defined in the context of the "sftp.allocate" and "sftp.commit" options (e.g. "wink.client.readTimeout=600000;wink.client.connectTimeout=60000").

### sftp.serverAliveCountMax
Used to set the maximum number of server-alive messages that can be sent without a reply from the server. This method is related to the SSH protocol keep-alive mechanism.

### sftp.serverAliveInterval
Used to define the interval between consecutive "server-alive" messages sent by the client to the server. These messages serve as a means to maintain the liveliness of the SSH connection.

### sftp.serverHostKey
Allow specifying the host key type to allow (e.g. "ssh-dss,ssh-rsa,ecdsa-sha2-nistp256,ecdsa-sha2-nistp384,ecdsa-sha2-nistp521"). The list of valid key types can be found at http://www.jcraft.com/jsch/README (multiple algorithms can be specified using the columns separator). By default all supported key types are allowed.

### sftp.sessionTimeOut
This value is used as the socket timeout parameter, and also as the default connection timeout. The value must be &gt; 0. A timeout of zero is interpreted as an infinite timeout.

### sftp.suffix
When using the "sftp.usetmp" option, this option allow setting the suffix to use for the temporary file name (e.g. ".tmp").

### sftp.useWriteFlush
When this option is enabled, it applies a workaround to ensure that the data written to the SFTP server is promptly flushed to the underlying filesystem. This workaround is particularly useful in cases where immediate flushing of writes is necessary, such as when writing critical data or when ensuring data integrity. However, this workaround may not be necessary or desirable in all situations. Therefore, you have the option to disable it if you do not require the behavior it provides or if it causes compatibility issues with the SFTP servers.

### sftp.usecleanpath
When using the "sftp.mkdirs" command this option allow forcing the SFTP client to fix directory pathnames (e.g. remove multiple instances of /).

### sftp.usetmp
Force using a temporary name when transmitting a file to the remote SFTP server. Once the transmission has completed successfully, the file is renamed with its final target name. The temporary file name is by default the file name with the ".tmp" suffix concatenated to it, however this behavior can be customized with the "sftp.mksuffix", "sftp.prefix" and "sftp.suffix" options.

### sftp.wmoLikeFormat
When initiating a file upload, try converting the target filename to its WMO format (for example, "TEY12080000122300001" becomes "ZTEY01ECMF080000RRA.TEY12080000122300001.LT"). If the transformation process fails, the original filename will be used instead.

## Test Options

These options help fine-tune the test transfer module and are accessible via the host editor.

### test.bytesPerSec
Define the number of bytes per second when simulating a "put" and "copy".

### test.delay
Specify the amount of time to wait before starting a "close", "connect", "copy", "del", "put" and "size" request.

### test.errorsFrequency
Specify the frequency of simulated errors. When a consecutive number of successful data transmissions reaches this count, a new error is generated.

## Upload Options

These options help fine-tune the data upload process for the underlying dissemination host and are accessible via the dissemination host editor.

### upload.interruptSlow
Terminate slow data transmissions upon reaching the specified maximum duration in "upload.maximumDuration" or the minimum rate defined in "upload.minimumRate".

### upload.maximumDuration
Maximum duration allowed for a data transmission.

### upload.minimumDuration
Minimum duration before starting to check the transfer rate and maximum duration allowed.

### upload.minimumRate
Minimum rate allowed for a data transmission.

### upload.rateThrottling
Maximum rate allowed for a data transmission.
