package pl.poznan.put;

import fr.orsay.lri.varna.models.export.SecStrDrawingProducer;
import fr.orsay.lri.varna.models.rna.ModeleBP;
import java.awt.Color;
import java.awt.geom.Point2D;

public final class AlternativeLeontisWesthofDrawing {
  public static void drawAlternativeSymbol(
      final SecStrDrawingProducer out,
      final Point2D.Double orig,
      final Point2D.Double dest,
      final ModeleBP style,
      final double thickness,
      final double unit,
      final boolean isCis) {
    final ModeleBP.Edge edge5 = style.getEdgePartner5();
    final ModeleBP.Edge edge3 = style.getEdgePartner3();
    assert edge5 != edge3;

    if (edge5 == ModeleBP.Edge.WC) {
      if (edge3 == ModeleBP.Edge.HOOGSTEEN) {
        AlternativeLeontisWesthofDrawing.drawSquareInCircle(
            out, orig, dest, thickness, unit, isCis);
      } else if (edge3 == ModeleBP.Edge.SUGAR) {
        AlternativeLeontisWesthofDrawing.drawTriangleInCircle(
            out, orig, dest, thickness, unit, isCis);
      }
    } else if (edge5 == ModeleBP.Edge.HOOGSTEEN) {
      if (edge3 == ModeleBP.Edge.WC) {
        AlternativeLeontisWesthofDrawing.drawCircleInSquare(
            out, orig, dest, thickness, unit, isCis);
      } else if (edge3 == ModeleBP.Edge.SUGAR) {
        AlternativeLeontisWesthofDrawing.drawTriangleInSquare(
            out, orig, dest, thickness, unit, isCis);
      }
    } else if (edge5 == ModeleBP.Edge.SUGAR) {
      if (edge3 == ModeleBP.Edge.WC) {
        AlternativeLeontisWesthofDrawing.drawCircleInTriangle(
            out, orig, dest, thickness, unit, isCis);
      } else if (edge3 == ModeleBP.Edge.HOOGSTEEN) {
        AlternativeLeontisWesthofDrawing.drawSquareInTriangle(
            out, orig, dest, thickness, unit, isCis);
      }
    }
  }

  private static void drawSquareInCircle(
      final SecStrDrawingProducer out,
      final Point2D.Double orig,
      final Point2D.Double dest,
      final double thickness,
      final double side,
      final boolean isCis) {
    // draw a circle
    final Point2D.Double center =
        new Point2D.Double((orig.x + dest.x) / 2.0, (orig.y + dest.y) / 2.0);
    final double diameter = (side * Math.sqrt(2)) + (2.0 * thickness);
    final double radius = diameter / 2.0;
    out.fillCircle(center.x, center.y, radius, thickness, Color.WHITE);
    out.drawCircle(center.x, center.y, radius, thickness);

    // calculate rotation of regular coordinate system and vector between points
    final Point2D.Double vectorHorizontal = new Point2D.Double(1, 0);
    final Point2D.Double vector = new Point2D.Double(dest.x - orig.x, dest.y - orig.y);
    final double dotProduct = (vector.x * vectorHorizontal.x) + (vector.y * vectorHorizontal.y);
    final double magnitudeHorizontal =
        Math.sqrt(
            (vectorHorizontal.x * vectorHorizontal.x) + (vectorHorizontal.y * vectorHorizontal.y));
    final double magnitude = Math.sqrt((vector.x * vector.x) + (vector.y * vector.y));
    final double cosine = dotProduct / (magnitudeHorizontal * magnitude);
    final double angleRadians = StrictMath.acos(cosine);

    // initial coordinates = half the side along the (0,0) point
    final Point2D.Double[] square = {
      new Point2D.Double(-side / 2.0, -side / 2.0),
      new Point2D.Double(-side / 2.0, side / 2.0),
      new Point2D.Double(side / 2.0, side / 2.0),
      new Point2D.Double(side / 2.0, -side / 2.0),
    };

    // rotate & translate coordinates
    for (final Point2D.Double point : square) {
      AlternativeLeontisWesthofDrawing.rotate(point, angleRadians);
      AlternativeLeontisWesthofDrawing.translate(point, center);
    }

    // draw square
    final double[] x = {square[0].x, square[1].x, square[2].x, square[3].x};
    final double[] y = {square[0].y, square[1].y, square[2].y, square[3].y};
    if (isCis) {
      out.fillPolygon(x, y, out.getCurrentColor());
    } else {
      out.drawPolygon(x, y, thickness);
    }
  }

  private static void drawTriangleInCircle(
      final SecStrDrawingProducer out,
      final Point2D.Double orig,
      final Point2D.Double dest,
      final double thickness,
      final double side,
      final boolean isCis) {
    // calculate triangle properties
    final double height = (side * Math.sqrt(3.0)) / 2.0;
    final double radius = ((2.0 * height) / 3.0) + thickness;

    // draw a circle
    final Point2D.Double center =
        new Point2D.Double((orig.x + dest.x) / 2.0, (orig.y + dest.y) / 2.0);
    out.fillCircle(center.x, center.y, radius, thickness, Color.WHITE);
    out.drawCircle(center.x, center.y, radius, thickness);

    // calculate rotation of regular coordinate system and vector between points
    final Point2D.Double vectorHorizontal = new Point2D.Double(1, 0);
    final Point2D.Double vector = new Point2D.Double(dest.x - orig.x, dest.y - orig.y);
    final double dotProduct = (vector.x * vectorHorizontal.x) + (vector.y * vectorHorizontal.y);
    final double magnitudeHorizontal =
        Math.sqrt(
            (vectorHorizontal.x * vectorHorizontal.x) + (vectorHorizontal.y * vectorHorizontal.y));
    final double magnitude = Math.sqrt((vector.x * vector.x) + (vector.y * vector.y));
    final double cosine = dotProduct / (magnitudeHorizontal * magnitude);
    final double angleRadians = StrictMath.acos(cosine);

    // initial coordinates = half the side along the (0,0) point
    final Point2D.Double[] triangle = {
      new Point2D.Double(-height / 3.0, -side / 2.0),
      new Point2D.Double(-height / 3.0, side / 2.0),
      new Point2D.Double((2.0 * height) / 3.0, 0),
    };

    // rotate & translate coordinates
    for (final Point2D.Double point : triangle) {
      AlternativeLeontisWesthofDrawing.rotate(point, angleRadians);
      AlternativeLeontisWesthofDrawing.translate(point, center);
    }

    // draw triangle
    final double[] x = {triangle[0].x, triangle[1].x, triangle[2].x};
    final double[] y = {triangle[0].y, triangle[1].y, triangle[2].y};
    if (isCis) {
      out.fillPolygon(x, y, out.getCurrentColor());
    } else {
      out.drawPolygon(x, y, thickness);
    }
  }

  private static void drawCircleInSquare(
      final SecStrDrawingProducer out,
      final Point2D.Double orig,
      final Point2D.Double dest,
      final double thickness,
      final double unit,
      final boolean isCis) {}

  private static void drawTriangleInSquare(
      final SecStrDrawingProducer out,
      final Point2D.Double orig,
      final Point2D.Double dest,
      final double thickness,
      final double unit,
      final boolean isCis) {}

  private static void drawCircleInTriangle(
      final SecStrDrawingProducer out,
      final Point2D.Double orig,
      final Point2D.Double dest,
      final double thickness,
      final double unit,
      final boolean isCis) {}

  private static void drawSquareInTriangle(
      final SecStrDrawingProducer out,
      final Point2D.Double orig,
      final Point2D.Double dest,
      final double thickness,
      final double unit,
      final boolean isCis) {}

  private static void rotate(final Point2D.Double point, final double angleRadians) {
    final double cosine = StrictMath.cos(angleRadians);
    final double sine = StrictMath.sin(angleRadians);
    final double x = (point.x * cosine) - (point.y * sine);
    final double y = (point.x * sine) + (point.y * cosine);
    point.x = x;
    point.y = y;
  }

  private static void translate(final Point2D.Double point, final Point2D.Double vector) {
    point.x += vector.x;
    point.y += vector.y;
  }

  private AlternativeLeontisWesthofDrawing() {
    super();
  }
}
