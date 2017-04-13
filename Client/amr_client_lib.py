import socket, json

class amr_connection:
	def __init__(self, ip, port_receive, port_send):
		self.ip = ip;
		self.port_receive = int(port_receive);
		self.port_send = int(port_send);
		self.sensor = "";
		self.sockSend = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
		self.sockRecv = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
		self.sockRecv.bind(("", self.port_receive))
		

	def get_data(self, sensor):	# Give the name of the sensor to receive
		if(sensor == self.sensor):
			data, addr = self.sockRecv.recvfrom(1024)
			data_json = json.loads(data)
			return data_json
		else:
			self.sensor = sensor
			self.sockSend.sendto(self.sensor, (self.ip, self.port_send)) 
			data, addr = self.sockRecv.recvfrom(1024)
			data_json = json.loads(data)
			return data_json

