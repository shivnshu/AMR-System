#!/usr/bin/env python

from __future__ import print_function

from OCC.STEPControl import STEPControl_Reader
from OCC.IFSelect import IFSelect_RetDone, IFSelect_ItemsByEntity
from OCC.Display.SimpleGui import init_display
from OCC.gp import gp_Ax1, gp_Pnt, gp_Dir, gp_Trsf
from OCC.BRepPrimAPI import BRepPrimAPI_MakeBox
from OCC.TopLoc import TopLoc_Location


import socket
import json
import math

step_reader = STEPControl_Reader()
status = step_reader.ReadFile('./models/ice/Assembly1.stp')
 
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
    server_address = ("192.168.0.107", 5000)                                 
    sockSend = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    sockRecv = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    sockRecv.bind(("", 8888))

    msg = 'c'                                                              
    sockSend.sendto(msg, server_address)                                       
    zoomIn_old = 0
    zoomOut_old = 0
    while True:                                                            
        data, addr = sockRecv.recvfrom(1024) # buffer size is 1024 byte        
	j = json.loads(data)
        x = round(j['rotation_vector']['x'], 2)                                
        y = round(j['rotation_vector']['y'], 2)                                
        z = round(j['rotation_vector']['z'], 2)                           
        w = round(j['rotation_vector']['w'], 2) 
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
