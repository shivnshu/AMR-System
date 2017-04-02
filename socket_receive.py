#!/usr/bin/env python

import socket
import math
server_address = ("192.168.12.13", 5000)

sock = socket.socket(socket.AF_INET, # Internet
                     socket.SOCK_DGRAM) # UDP

msg = 'c'
sock.sendto(msg, server_address)

while True:
        data, addr = sock.recvfrom(1024) # buffer size is 1024 bytes
        arr =  data.split()
        x = round(float(arr[0]), 2)
        y = round(float(arr[1]), 2)
        z = round(float(arr[2]), 2)
        print "x: %.2f, y: %.2f, z: %.2f\n" % (x, y, z)
	sin_theta = math.sqrt(x*x+y*y+z*z)
	theta =  math.asin(sin_theta)
