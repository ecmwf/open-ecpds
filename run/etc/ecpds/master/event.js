// This javascript is triggered asynchronously whenever a data transfer is completed. The default behaviour is to
// build a json message and send it to the log. Only transfer for data files with a time step (!=-1), a meta strean
// and a meta time can trigger this javascript.

var eventNotification = "$metadata[event]" != "" ? "$metadata[event]" : "dissemination";

var request = eventNotification == "dissemination" ?
	{ // Coming from dissemination destination (pgen/q2diss tasks)
		"class": "od",
		"date": "$metadata[date]" != "" ? "$metadata[date]" : "$date",
		"destination": "$destination",
		"target": "$metadata[target]".toUpperCase(),
		"domain": "$metadata[domain]".toLowerCase(),
		"expver": "1",
		"step": "$step",
		"stream": "$metadata[stream]".toLowerCase(),
		"time": "$time".substring(0, 2)
	} : { // Dedicated to DestinE
		"class": "$metadata[class]",
		"date": "$metadata[date]" != "" ? "$metadata[date]" : "$date",
		"destination": "$destination",
		"target": "$metadata[target]".toUpperCase(),
		"expver": "$metadata[expver]",
		"time": "$time".substring(0, 2)
	}

var message = {
	"type": "aviso",
	"data": {
		"event": eventNotification,
		"request": request,
		"location": ("$standby" == "false" && "$location" != "" ? "$location/$filename" :
			"https://aux.ecmwf.int/ecpds/data/file/$destination/DataTransferId=$datatransferid")
	},
	"datacontenttype": "application/json",
	"id": UUID.randomUUID().toString(),
	"source": "/ecpds/emos",
	"specversion": "1.0",
	"time": new Date().toISOString()
};

function read(inputStream) {
	var inReader = new BufferedReader(
		new InputStreamReader(inputStream));
	var response = new StringBuffer();
	while ((inputLine = inReader.readLine()) != null)
		response.append(inputLine);
	inReader.close();
	return response.toString();
}

function getBytes(string) {
	var bytes = [];
	for (var i = 0; i < string.length; ++i)
		bytes.push(string.charCodeAt(i));
	return bytes;
}

var httpPost = function(theUrl, json) {
	var con = new URL(theUrl).openConnection();
	con.setConnectTimeout(30000);
	con.setReadTimeout(30000);
	con.setDoOutput(true);
	con.setRequestMethod("POST");
	con.setRequestProperty("Content-Type", "application/json");
	var out = con.getOutputStream();
	out.write(getBytes(JSON.stringify(json)));
	out.flush();
	if ((code = con.getResponseCode()) != 200) {
		var message = read(con.getErrorStream());
		if (message.indexOf("is not valid for key stream") == -1) {
			throw "Notification error " + code + ": " + message;
		} else {
			log.warn(message);
		}
	}
}

//httpPost("http://k8s-applications-prod-controller-00.ecmwf.int:30003/api/v1/notification", message);
log.debug("JSON: " + JSON.stringify(message));