#!/usr/bin/python2

import sys
from amr_client_lib import amr_connection

if len(sys.argv) != 4:
    print "[Usage: ./client.py <server_ip> <server_port> <listening_port>]"
    sys.exit(0)

feed = amr_connection(sys.argv[1], sys.argv[2], sys.argv[3])

while 1:
	print feed.get_data(["rotation_sensor"])
