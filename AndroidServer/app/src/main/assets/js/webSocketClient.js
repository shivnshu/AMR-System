ws = new WebSocket("ws://" + self.location.hostname + ":8887");
var sensorsData;

ws.onmessage = function(event) {
  if(event.data !== "Uploaded!!"){
	  sensorsData = JSON.parse(event.data)
		//sensorsData = event.data;
		//console.log(data['sensor']['x']);
  } else {
    location.reload();
  }
};

ws.onclose = function() {
	console.log("Socket closed");
};

ws.onopen = function() {
	console.log("Connected");
	ws.send("Hello from " + navigator.userAgent);
};

//$("#new-message").bind("submit", function(event) {
//event.preventDefault();
//ws.send($("#message-text").val());
//$("#message-text").val("");
//});
