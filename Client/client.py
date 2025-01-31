#!/usr/bin/python2

import sys
from amr_client_lib import amr_connection

if len(sys.argv) != 4:
    print "[Usage: ./client.py <server_ip> <server_port> <listener_port]"
    print "[SpecificUsage: ./client.py <server_ip> 1111 2222]"
    sys.exit(0)

feed = amr_connection(sys.argv[1], sys.argv[2], sys.argv[3])

while 1:
    # print feed.get_data("TYPE_ACCELEROMETER")
    print feed.get_data("TYPE_ROTATION_VECTOR")
    # print feed.get_data("TYPE_GRAVITY")
    # print feed.get_data("TYPE_LINEAR_ACCELERATION")
    # print feed.get_data("TYPE_GYROSCOPE")
