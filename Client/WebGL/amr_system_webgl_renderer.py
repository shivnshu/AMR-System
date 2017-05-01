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

from OCC.STEPControl import STEPControl_Reader
from OCC.IFSelect import IFSelect_RetDone, IFSelect_ItemsByEntity
from OCC.Display.SimpleGui import init_display
from OCC.gp import gp_Ax1, gp_Pnt, gp_Dir, gp_Trsf
from OCC.BRepPrimAPI import BRepPrimAPI_MakeBox
from OCC.TopLoc import TopLoc_Location

# from amr_client_lib import amr_connection

import socket
import json
import math
# TODO: Above are my includes

from OCC.Display.WebGl import x3dom_renderer
from OCC.BRep import BRep_Builder
from OCC.TopoDS import TopoDS_Shape
from OCC.BRepTools import breptools_Read

# loads brep shape
cylinder_head = TopoDS_Shape()  # TODO: Comment at end
builder = BRep_Builder()
breptools_Read(cylinder_head, './models/cylinder_head.brep', builder)


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

# render cylinder head in x3dom
my_renderer = x3dom_renderer.X3DomRenderer()
my_renderer.DisplayShape(aResShape)
my_renderer.render()
