	ws = new WebSocket("ws://" + "192.168.43.1:8887");
  
  var sensorsData;

	ws.onmessage = function(event) {
    sensorsData = JSON.parse(event.data)
    //sensorsData = event.data;
    //console.log(data['sensor']['x']);
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
