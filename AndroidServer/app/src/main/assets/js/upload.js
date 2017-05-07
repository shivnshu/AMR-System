var sensorsData;
//function connectChatServer() {
	//ws = new WebSocket("ws://" + self.location.hostname + ":8887");

	//ws.binaryType = "arraybuffer";
	//ws.onopen = function() {
		//alert("Connected.")
	//};

	//ws.onmessage = function(evt) {
		////alert(evt.msg);
		//sensorsData = JSON.parse(evt.data);
	//};

	//ws.onclose = function() {
		//alert("Connection is closed...");
	//};
	//ws.onerror = function(e) {
		//alert(e.msg);
	//}

//}

function sendFile() {
	var file = document.getElementById('filename').files[0];
	ws.send('filename:'+file.name);
	var reader = new FileReader();
	var rawData = new ArrayBuffer();            
	//alert(file.name);

	reader.onloadend = function() {
		//location.reload();
	}
	reader.onload = function(e) {
		rawData = e.target.result;
		ws.send(rawData);
		ws.send('end');
	}

  reader.onmessage = function(e) {
    if(e.data === "Uploaded!!") {
      location.reload();
    }   
  }

	reader.readAsArrayBuffer(file);
}
