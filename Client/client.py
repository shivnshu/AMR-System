#!/usr/bin/python2

import sys, socket, math

if len(sys.argv) != 4:
    print "[Usage: ./client.py <server_ip> <server_port> <listening_port>]"
    sys.exit(0)

serverAddress = (sys.argv[1], int(sys.argv[2]))
listeningPort = int(sys.argv[3])

sockSend = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
sockRecv = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)

print "Listening to", listeningPort
sockRecv.bind(("", listeningPort))
msg = 'c'
print "Sending a packet to server"
sockSend.sendto(msg, serverAddress)
print "Packet sent"

while 1:
    data, addr = sockRecv.recvfrom(1024)
    print data
