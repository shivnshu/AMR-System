$(document).ready(function() {
	ws = new WebSocket("ws://" + "192.168.43.1:8887");

	ws.onmessage = function(event) {
    var data = JSON.parse(event.data)
    console.log(data['sensor']['x']);
	};
	
	ws.onclose = function() {
		console.log("Socket closed");
	};
	
	ws.onopen = function() {
		console.log("Connected");
		ws.send("Hello from " + navigator.userAgent);
	};
	
	$("#new-message").bind("submit", function(event) {
		event.preventDefault();
		ws.send($("#message-text").val());
		$("#message-text").val("");
	});
});
