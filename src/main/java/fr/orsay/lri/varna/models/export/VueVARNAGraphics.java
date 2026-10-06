package fr.orsay.lri.varna.models.export;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.geom.GeneralPath;

public interface VueVARNAGraphics {
  Dimension getStringDimension(String s);

  void drawStringCentered(String res, double x, double y);

  void setColor(Color c);

  Color getColor();

  void drawLine(double x1, double y1, double x2, double y2);

  void drawRect(double x, double y, double w, double h);

  void fillRect(double x, double y, double w, double h);

  void drawCircle(double x, double y, double r);

  void fillCircle(double x, double y, double r);

  void drawRoundRect(double x, double y, double w, double h, double rx, double ry);

  void fillRoundRect(double x, double y, double w, double h, double rx, double ry);

  void drawArc(double x, double y, double rx, double ry, double angleStart, double angleEnd);

  // public void drawString(String s, double x, double y);
  void draw(GeneralPath s);

  void fill(GeneralPath s);

  void setFont(Font f);

  void setSelectionStroke();

  void setPlainStroke();

  void setStrokeThickness(double t);
}
