#!/usr/bin/env python

from __future__ import print_function

from OCC.STEPControl import STEPControl_Reader
from OCC.IFSelect import IFSelect_RetDone, IFSelect_ItemsByEntity
from OCC.Display.SimpleGui import init_display
from OCC.gp import gp_Ax1, gp_Pnt, gp_Dir, gp_Trsf
from OCC.BRepPrimAPI import BRepPrimAPI_MakeBox
from OCC.TopLoc import TopLoc_Location


import socket
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
    server_address = ("172.27.30.2", 5000)                                 
    sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)                                                                                           
    msg = 'c'                                                              
    sock.sendto(msg, server_address)                                       
    c = 0                                                                  
    while True:                                                            
        data, addr = sock.recvfrom(1024) # buffer size is 1024 byte        
        arr =  data.split()                                                
        x = round(float(arr[0]), 2)                                        
        y = round(float(arr[1]), 2)                                        
        z = round(float(arr[2]), 2)                                        
        w = round(float(arr[3]), 2) 
        c = c + 1                                                          
        if c==10:                                                          
            ax1 = gp_Ax1(gp_Pnt(0., 0., 0.), gp_Dir(x, y, z))           
            aCubeTrsf = gp_Trsf()                                          
            angle = 2*math.acos(w)                                         
            aCubeTrsf.SetRotation(ax1, angle)                              
            aCubeToploc = TopLoc_Location(aCubeTrsf)                       
            display.Context.SetLocation(ais_boxshp, aCubeToploc)           
            display.Context.UpdateCurrentViewer()                          
            c = 0                                                          

display, start_display, add_menu, add_function_to_menu = init_display()
add_menu('animation')
add_function_to_menu('animation', start_animation)
ais_boxshp = display.DisplayShape(aResShape, update=True)
start_display()
