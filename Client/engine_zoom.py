#!/usr/bin/env python

from __future__ import print_function

from OCC.STEPControl import STEPControl_Reader
from OCC.IFSelect import IFSelect_RetDone, IFSelect_ItemsByEntity
from OCC.Display.SimpleGui import init_display
from OCC.gp import gp_Ax1, gp_Pnt, gp_Dir, gp_Trsf
from OCC.BRepPrimAPI import BRepPrimAPI_MakeBox
from OCC.TopLoc import TopLoc_Location

from amr_client_lib import amr_connection

import sys
import socket
import json
import math

if len(sys.argv) != 4:
    print("[Usage: ./client.py <server_ip> <server_port> <listener_port]")
    print("[SpecificUsage: ./client.py <server_ip> 1111 2222]")
    sys.exit(0)

step_reader = STEPControl_Reader()
status = step_reader.ReadFile('./models/Assembly1.stp')
 
if status == IFSelect_RetDone:  # check status
    failsonly = False
    step_reader.PrintCheckLoad(failsonly, IFSelect_ItemsByEntity)
    step_reader.PrintCheckTransfer(failsonly, IFSelect_ItemsByEntity)
 
    ok = step_reader.TransferRoot(1)
    _nbs = step_reader.NbShapes()
    aResShape = step_reader.Shape(1)
else:
    print("Error: can't read file.")
    sys.exit(0)


def start_animation(event=None):
    feed = amr_connection(sys.argv[1], sys.argv[2], sys.argv[3])
    zoomIn_old = 0
    zoomOut_old = 0
    while True:
        j = feed.get_data("TYPE_ROTATION_VECTOR")
        x = round(j['sensor']['x'], 2)
        y = round(j['sensor']['y'], 2)
        z = round(j['sensor']['z'], 2)
        w = round(j['sensor']['w'], 2)
        zoomIn = j['volume_keys']['volumeUp']
        zoomOut = j['volume_keys']['volumeDown']
        if(zoomIn != zoomIn_old):
            display.ZoomFactor(1.1)
            zoomIn_old = zoomIn
        if(zoomOut != zoomOut_old):
            display.ZoomFactor(0.9)
            zoomOut_old = zoomOut
        ax1 = gp_Ax1(gp_Pnt(10., 10., 10.), gp_Dir(x, y, z))
        aCubeTrsf = gp_Trsf()
        angle = 2*math.acos(w)
        aCubeTrsf.SetRotation(ax1, angle)
        aCubeToploc = TopLoc_Location(aCubeTrsf)
        display.Context.SetLocation(ais_boxshp, aCubeToploc)
        display.Context.UpdateCurrentViewer()                          

display, start_display, add_menu, add_function_to_menu = init_display()
add_menu('animation')
add_function_to_menu('animation', start_animation)
ais_boxshp = display.DisplayShape(aResShape, update=True)
start_display()
