import socket, json

class amr_connection:
	def __init__(self, server_ip, server_port, listener_port):
		self.server_ip = server_ip;
		self.server_port = int(server_port);
		self.listener_port = int(listener_port);
		self.sensors = [];
		self.sockSend = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
		self.sockRecv = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
		self.sockRecv.bind(("", self.listener_port))
		

	def get_data(self, sensors):	# TODO: Concerned about performance problems change the implementation in that cast to a seperate listen and send functions
		if(sensors == self.sensors):
			data, addr = self.sockRecv.recvfrom(1024)
			data_json = json.loads(data)
			return data_json
		else:
			self.sensors = sensors
			self.sockSend.sendto(self.sensors, (self.server_ip, self.server_port)) 
			data, addr = self.sockRecv.recvfrom(1024)
			data_json = json.loads(data)
			return data_json

