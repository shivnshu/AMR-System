#!/usr/bin/env python

##Copyright 2009-2014 Thomas Paviot (tpaviot@gmail.com)
##
##This file is part of pythonOCC.
##
##pythonOCC is free software: you can redistribute it and/or modify
##it under the terms of the GNU Lesser General Public License as published by
##the Free Software Foundation, either version 3 of the License, or
##(at your option) any later version.
##
##pythonOCC is distributed in the hope that it will be useful,
##but WITHOUT ANY WARRANTY; without even the implied warranty of
##MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
##GNU Lesser General Public License for more details.
##
##You should have received a copy of the GNU Lesser General Public License
##along with pythonOCC.  If not, see <http://www.gnu.org/licenses/>.

from __future__ import print_function

import time
import socket
import math
from math import pi

from OCC.gp import gp_Ax1, gp_Pnt, gp_Dir, gp_Trsf
from OCC.BRepPrimAPI import BRepPrimAPI_MakeBox
from OCC.TopLoc import TopLoc_Location
from OCC.Display.SimpleGui import init_display

display, start_display, add_menu, add_function_to_menu = init_display()

ais_boxshp = None


def build_shape():
    boxshp = BRepPrimAPI_MakeBox(50., 50., 50.).Shape()
    ais_boxshp = display.DisplayShape(boxshp, update=True)
    return ais_boxshp


def rotating_cube_1_axis(event=None):
    server_address = ("172.27.30.2", 5000)
    sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
 
    msg = 'c'
    sock.sendto(msg, server_address)
    c = 0
    zoomIn_old = 0
    zoomOut_old = 0
    ais_boxshp = build_shape()
    while True:
        data, addr = sock.recvfrom(1024) # buffer size is 1024 byte
        arr =  data.split()
        x = round(float(arr[0]), 2)
        y = round(float(arr[1]), 2)
        z = round(float(arr[2]), 2)
        w = round(float(arr[3]), 2)
        zoomIn = int(arr[4])
        zoomOut = int(arr[5])
        c = c + 1
        if c==100:
            # display.EraseAll()
            if(zoomIn != zoomIn_old):
                display.ZoomFactor(1.1)
                zoomIn_old = zoomIn
            if(zoomOut != zoomOut_old):
                display.ZoomFactor(0.9)
                zoomOut_old = zoomOut
            ax1 = gp_Ax1(gp_Pnt(25., 25., 25.), gp_Dir(x, y, z))
            aCubeTrsf = gp_Trsf()
            angle = 2*math.acos(w) 
            aCubeTrsf.SetRotation(ax1, angle)
            aCubeToploc = TopLoc_Location(aCubeTrsf)
            display.Context.SetLocation(ais_boxshp, aCubeToploc)
            display.Context.UpdateCurrentViewer()
            c = 0

if __name__ == '__main__':
    add_menu('animation')
    add_function_to_menu('animation', rotating_cube_1_axis)
    start_display()
