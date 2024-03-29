/*
VARNA is a tool for the automated drawing, visualization and annotation of the secondary structure of RNA, designed as a companion software for web servers and databases.
Copyright (C) 2008  Kevin Darty, Alain Denise and Yann Ponty.
electronic mail : Yann.Ponty@lri.fr
paper mail : LRI, bat 490 Universit Paris-Sud 91405 Orsay Cedex France

This file is part of VARNA version 3.1.
VARNA version 3.1 is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.

VARNA version 3.1 is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with VARNA version 3.1.
If not, see http://www.gnu.org/licenses.
*/
package fr.orsay.lri.varna.models.export;

import java.awt.geom.Point2D;

public class ArcCommand extends GraphicElement {

  private Point2D.Double center;
  private double width, height;
  private double startAngle, endAngle;

  public ArcCommand(
      Point2D.Double origine, double width, double height, double startAngle, double endAngle) {
    this.center = origine;
    this.width = width;
    this.height = height;
    this.startAngle = startAngle;
    this.endAngle = endAngle;
  }

  public Point2D.Double getCenter() {
    return center;
  }

  public double getWidth() {
    return width;
  }

  public double getHeight() {
    return height;
  }

  public double getStartAngle() {
    return startAngle;
  }

  public double getEndAngle() {
    return endAngle;
  }
}
