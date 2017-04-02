#!/usr/bin/env python

import socket

server_address = ("192.168.12.13", 5000)

sock = socket.socket(socket.AF_INET, # Internet
                     socket.SOCK_DGRAM) # UDP

msg = 'c'
sock.sendto(msg, server_address)

while True:
	data, addr = sock.recvfrom(1024) # buffer size is 1024 bytes
	print "received message:", data
