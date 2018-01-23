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
    // calculate radius
    final double diameter = (side * Math.sqrt(2)) + (2.0 * thickness);
    final double radius = diameter / 2.0;

    // draw a circle
    final Point2D.Double center =
        new Point2D.Double((orig.x + dest.x) / 2.0, (orig.y + dest.y) / 2.0);
    out.fillCircle(center.x, center.y, radius, thickness, Color.WHITE);
    out.drawCircle(center.x, center.y, radius, thickness);

    // initial coordinates = half the side along the (0,0) point
    final Point2D.Double[] square = {
      new Point2D.Double(-side / 2.0, -side / 2.0),
      new Point2D.Double(-side / 2.0, side / 2.0),
      new Point2D.Double(side / 2.0, side / 2.0),
      new Point2D.Double(side / 2.0, -side / 2.0),
    };

    // draw everything
    final double angle = AlternativeLeontisWesthofDrawing.calculateRotationAngle(orig, dest);
    AlternativeLeontisWesthofDrawing.transformPoints(angle, center, square);
    AlternativeLeontisWesthofDrawing.drawPolygon(out, square, thickness, isCis);
  }

  private static void drawTriangleInCircle(
      final SecStrDrawingProducer out,
      final Point2D.Double orig,
      final Point2D.Double dest,
      final double thickness,
      final double side,
      final boolean isCis) {
    // calculate radius
    final double height = (side * Math.sqrt(3.0)) / 2.0;
    final double radius = ((2.0 * height) / 3.0) + thickness;

    // draw a circle
    final Point2D.Double center =
        new Point2D.Double((orig.x + dest.x) / 2.0, (orig.y + dest.y) / 2.0);
    out.fillCircle(center.x, center.y, radius, thickness, Color.WHITE);
    out.drawCircle(center.x, center.y, radius, thickness);

    // initial coordinates = center is in 2/3 of triangle height
    final Point2D.Double[] triangle = {
      new Point2D.Double(-height / 3.0, -side / 2.0),
      new Point2D.Double(-height / 3.0, side / 2.0),
      new Point2D.Double((2.0 * height) / 3.0, 0),
    };

    // draw everything
    final double angle = AlternativeLeontisWesthofDrawing.calculateRotationAngle(orig, dest);
    AlternativeLeontisWesthofDrawing.transformPoints(angle, center, triangle);
    AlternativeLeontisWesthofDrawing.drawPolygon(out, triangle, thickness, isCis);
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

  /**
   * Calculate rotation of regular coordinate system and a vector between given points.
   *
   * @param orig First point.
   * @param dest Second point.
   * @return An angle between X-axis and given vector.
   */
  private static double calculateRotationAngle(
      final Point2D.Double orig, final Point2D.Double dest) {
    final Point2D.Double vectorHorizontal = new Point2D.Double(1, 0);
    final Point2D.Double vector = new Point2D.Double(dest.x - orig.x, dest.y - orig.y);
    final double dotProduct = (vector.x * vectorHorizontal.x) + (vector.y * vectorHorizontal.y);
    final double magnitudeHorizontal =
        Math.sqrt(
            (vectorHorizontal.x * vectorHorizontal.x) + (vectorHorizontal.y * vectorHorizontal.y));
    final double magnitude = Math.sqrt((vector.x * vector.x) + (vector.y * vector.y));
    final double cosine = dotProduct / (magnitudeHorizontal * magnitude);
    return StrictMath.acos(cosine);
  }

  /**
   * Rotate & translate coordinates.
   *
   * @param rotationAngle Angle of rotation.
   * @param translationVector 2D vector of translation.
   * @param points Array of points to be transformed.
   */
  private static void transformPoints(
      final double rotationAngle,
      final Point2D.Double translationVector,
      final Point2D.Double[] points) {
    for (final Point2D.Double point : points) {
      AlternativeLeontisWesthofDrawing.rotate(point, rotationAngle);
      AlternativeLeontisWesthofDrawing.translate(point, translationVector);
    }
  }

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

  /**
   * Draw a polygon filled or with just lines depending on whether the interaction is cis or trans.
   *
   * @param out An instance of secondary structure drawer.
   * @param points A set of points.
   * @param thickness A parameter of line thickness.
   * @param isCis A parameter deciding if the polygon is filled (true) or just lines (false).
   */
  private static void drawPolygon(
      final SecStrDrawingProducer out,
      final Point2D.Double[] points,
      final double thickness,
      final boolean isCis) {
    // draw square
    final double[] x = new double[points.length];
    final double[] y = new double[points.length];

    for (int i = 0; i < points.length; i++) {
      x[i] = points[i].x;
      y[i] = points[i].y;
    }

    if (isCis) {
      out.fillPolygon(x, y, out.getCurrentColor());
    } else {
      out.drawPolygon(x, y, thickness);
    }
  }

  private AlternativeLeontisWesthofDrawing() {
    super();
  }
}
