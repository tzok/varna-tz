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

    final Point2D.Double center =
        new Point2D.Double((orig.x + dest.x) / 2.0, (orig.y + dest.y) / 2.0);

    if (edge5 == ModeleBP.Edge.WC) {
      if (edge3 == ModeleBP.Edge.HOOGSTEEN) {
        AlternativeLeontisWesthofDrawing.drawSquareInCircle(
            out, orig, dest, center, thickness, unit, isCis);
      } else if (edge3 == ModeleBP.Edge.SUGAR) {
        AlternativeLeontisWesthofDrawing.drawTriangleInCircle(
            out, orig, dest, center, thickness, unit, isCis);
      }
    } else if (edge5 == ModeleBP.Edge.HOOGSTEEN) {
      if (edge3 == ModeleBP.Edge.WC) {
        AlternativeLeontisWesthofDrawing.drawCircleInSquare(
            out, orig, dest, center, thickness, unit, isCis);
      } else if (edge3 == ModeleBP.Edge.SUGAR) {
        AlternativeLeontisWesthofDrawing.drawTriangleInSquare(
            out, orig, dest, center, thickness, unit, isCis);
      }
    } else if (edge5 == ModeleBP.Edge.SUGAR) {
      if (edge3 == ModeleBP.Edge.WC) {
        AlternativeLeontisWesthofDrawing.drawCircleInTriangle(
            out, orig, dest, center, thickness, unit, isCis);
      } else if (edge3 == ModeleBP.Edge.HOOGSTEEN) {
        AlternativeLeontisWesthofDrawing.drawSquareInTriangle(
            out, orig, dest, center, thickness, unit, isCis);
      }
    }
  }

  private static void drawSquareInCircle(
      final SecStrDrawingProducer out,
      final Point2D.Double orig,
      final Point2D.Double dest,
      final Point2D.Double center,
      final double thickness,
      final double side,
      final boolean isCis) {
    // calculate radius
    final double diameter = side * Math.sqrt(2);
    final double radius = (diameter / 2.0) + (1.5 * thickness);

    // draw a circle
    out.fillCircle(center.x, center.y, radius, thickness, Color.WHITE);
    out.drawCircle(center.x, center.y, radius, thickness);

    // initial square coordinates = half the side along the (0,0) point
    final double normalizedSide = isCis ? side : (side - thickness);
    final Point2D.Double[] square = {
      new Point2D.Double(-normalizedSide / 2.0, -normalizedSide / 2.0),
      new Point2D.Double(-normalizedSide / 2.0, normalizedSide / 2.0),
      new Point2D.Double(normalizedSide / 2.0, normalizedSide / 2.0),
      new Point2D.Double(normalizedSide / 2.0, -normalizedSide / 2.0),
    };

    // draw the square
    final double angle = AlternativeLeontisWesthofDrawing.calculateRotationAngle(orig, dest);
    AlternativeLeontisWesthofDrawing.transformPoints(angle, center, square);
    AlternativeLeontisWesthofDrawing.drawPolygon(
        out, square, thickness, isCis, out.getCurrentColor());
  }

  private static void drawTriangleInCircle(
      final SecStrDrawingProducer out,
      final Point2D.Double orig,
      final Point2D.Double dest,
      final Point2D.Double center,
      final double thickness,
      final double side,
      final boolean isCis) {
    // calculate radius
    final double height = (side * Math.sqrt(3.0)) / 2.0;
    final double radius = ((2.0 * height) / 3.0) + (1.5 * thickness);

    // draw a circle
    out.fillCircle(center.x, center.y, radius, thickness, Color.WHITE);
    out.drawCircle(center.x, center.y, radius, thickness);

    // initial triangle coordinates = center is in 2/3 of triangle height
    final double normalizedSide = isCis ? side : (side - thickness);
    final double normalizedHeight = isCis ? height : ((normalizedSide * Math.sqrt(3.0)) / 2.0);
    final Point2D.Double[] triangle = {
      new Point2D.Double(-normalizedHeight / 3.0, -normalizedSide / 2.0),
      new Point2D.Double(-normalizedHeight / 3.0, normalizedSide / 2.0),
      new Point2D.Double((2.0 * normalizedHeight) / 3.0, 0),
    };

    // draw the triangle
    final double angle = AlternativeLeontisWesthofDrawing.calculateRotationAngle(orig, dest);
    AlternativeLeontisWesthofDrawing.transformPoints(angle, center, triangle);
    AlternativeLeontisWesthofDrawing.drawPolygon(
        out, triangle, thickness, isCis, out.getCurrentColor());
  }

  private static void drawCircleInSquare(
      final SecStrDrawingProducer out,
      final Point2D.Double orig,
      final Point2D.Double dest,
      final Point2D.Double center,
      final double thickness,
      final double diameter,
      final boolean isCis) {
    // calculate side
    final double side = diameter + (3.0 * thickness);

    // initial coordinates = half the side along the (0,0) point
    final Point2D.Double[] square = {
      new Point2D.Double(-side / 2.0, -side / 2.0),
      new Point2D.Double(-side / 2.0, side / 2.0),
      new Point2D.Double(side / 2.0, side / 2.0),
      new Point2D.Double(side / 2.0, -side / 2.0),
    };

    // draw the square
    final double angle = AlternativeLeontisWesthofDrawing.calculateRotationAngle(orig, dest);
    AlternativeLeontisWesthofDrawing.transformPoints(angle, center, square);
    AlternativeLeontisWesthofDrawing.drawPolygon(out, square, thickness, true, Color.WHITE);
    AlternativeLeontisWesthofDrawing.drawPolygon(
        out, square, thickness, false, out.getCurrentColor());

    // draw the circle
    final double radius = diameter / 2.0;
    final double normalizedRadius = isCis ? radius : (radius - thickness);
    if (isCis) {
      out.fillCircle(center.x, center.y, normalizedRadius, thickness, out.getCurrentColor());
    } else {
      out.drawCircle(center.x, center.y, normalizedRadius, thickness);
    }
  }

  private static void drawTriangleInSquare(
      final SecStrDrawingProducer out,
      final Point2D.Double orig,
      final Point2D.Double dest,
      final Point2D.Double center,
      final double thickness,
      final double triangleSide,
      final boolean isCis) {
    // calculate square side
    final double squareSide = triangleSide + (3.0 * thickness);

    // initial coordinates = half the squareSide along the (0,0) point
    final Point2D.Double[] square = {
      new Point2D.Double(-squareSide / 2.0, -squareSide / 2.0),
      new Point2D.Double(-squareSide / 2.0, squareSide / 2.0),
      new Point2D.Double(squareSide / 2.0, squareSide / 2.0),
      new Point2D.Double(squareSide / 2.0, -squareSide / 2.0),
    };

    // draw the square
    final double angle = AlternativeLeontisWesthofDrawing.calculateRotationAngle(orig, dest);
    AlternativeLeontisWesthofDrawing.transformPoints(angle, center, square);
    AlternativeLeontisWesthofDrawing.drawPolygon(out, square, thickness, true, Color.WHITE);
    AlternativeLeontisWesthofDrawing.drawPolygon(
        out, square, thickness, false, out.getCurrentColor());

    // initial triangle coordinates = center is in 2/3 of triangle height
    final double normalizedSide = isCis ? triangleSide : (triangleSide - (2.0 * thickness));
    final double normalizedHeight = (normalizedSide * Math.sqrt(3.0)) / 2.0;
    final Point2D.Double[] triangle = {
      new Point2D.Double((-normalizedHeight / 3.0) - thickness, -normalizedSide / 2.0),
      new Point2D.Double((-normalizedHeight / 3.0) - thickness, normalizedSide / 2.0),
      new Point2D.Double(((2.0 * normalizedHeight) / 3.0) - thickness, 0),
    };

    // draw the triangle
    AlternativeLeontisWesthofDrawing.transformPoints(angle, center, triangle);
    AlternativeLeontisWesthofDrawing.drawPolygon(
        out, triangle, thickness, isCis, out.getCurrentColor());
  }

  private static void drawCircleInTriangle(
      final SecStrDrawingProducer out,
      final Point2D.Double orig,
      final Point2D.Double dest,
      final Point2D.Double center,
      final double thickness,
      final double diameter,
      final boolean isCis) {
    // calculate side
    final double radius = (diameter / 2.0) + thickness;
    final double height = 3.0 * radius;
    final double side = (2.0 * height) / Math.sqrt(3.0);

    // initial triangle coordinates = center is in 1/3 of triangle height
    final Point2D.Double[] triangle = {
      new Point2D.Double(-height / 3.0, -side / 2.0),
      new Point2D.Double(-height / 3.0, side / 2.0),
      new Point2D.Double((2.0 * height) / 3.0, 0),
    };

    // draw the triangle
    final double angle = AlternativeLeontisWesthofDrawing.calculateRotationAngle(orig, dest);
    AlternativeLeontisWesthofDrawing.transformPoints(angle, center, triangle);
    AlternativeLeontisWesthofDrawing.drawPolygon(out, triangle, thickness, true, Color.WHITE);
    AlternativeLeontisWesthofDrawing.drawPolygon(
        out, triangle, thickness, false, out.getCurrentColor());

    // draw the circle
    final double normalizedRadius = isCis ? radius : (radius - thickness);
    if (isCis) {
      out.fillCircle(center.x, center.y, normalizedRadius, thickness, out.getCurrentColor());
    } else {
      out.drawCircle(center.x, center.y, normalizedRadius, thickness);
    }
  }

  private static void drawSquareInTriangle(
      final SecStrDrawingProducer out,
      final Point2D.Double orig,
      final Point2D.Double dest,
      final Point2D.Double center,
      final double thickness,
      final double squareSide,
      final boolean isCis) {
    // calculate side
    // https://math.stackexchange.com/questions/545594/
    //     what-is-the-maximum-area-of-a-square-inscribed-in-an-equilateral-triangle
    final double triangleSide = (squareSide * (2.0 + Math.sqrt(3.0))) / Math.sqrt(3.0);
    final double triangleHeight = (triangleSide * Math.sqrt(3.0)) / 2.0;

    // initial triangle coordinates = center is in 1/3 of triangle height
    final Point2D.Double[] triangle = {
      new Point2D.Double(-triangleHeight / 3.0, -triangleSide / 2.0),
      new Point2D.Double(-triangleHeight / 3.0, triangleSide / 2.0),
      new Point2D.Double((2.0 * triangleHeight) / 3.0, 0),
    };

    // draw the triangle
    final double angle = AlternativeLeontisWesthofDrawing.calculateRotationAngle(orig, dest);
    AlternativeLeontisWesthofDrawing.transformPoints(angle, center, triangle);
    AlternativeLeontisWesthofDrawing.drawPolygon(out, triangle, thickness, true, Color.WHITE);
    AlternativeLeontisWesthofDrawing.drawPolygon(
        out, triangle, thickness, false, out.getCurrentColor());

    // initial square coordinates = half the side along the (0,0) point
    final double normalizedSide = isCis ? squareSide : (squareSide - thickness);
    final Point2D.Double[] square = {
      new Point2D.Double((-triangleHeight / 3.0) + thickness, -normalizedSide / 2.0),
      new Point2D.Double((-triangleHeight / 3.0) + thickness, normalizedSide / 2.0),
      new Point2D.Double((-triangleHeight / 3.0) + normalizedSide, normalizedSide / 2.0),
      new Point2D.Double((-triangleHeight / 3.0) + normalizedSide, -normalizedSide / 2.0)
    };

    // draw the square
    AlternativeLeontisWesthofDrawing.transformPoints(angle, center, square);
    AlternativeLeontisWesthofDrawing.drawPolygon(
        out, square, thickness, isCis, out.getCurrentColor());
  }

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
   * @param color Color used to draw the symbol.
   */
  private static void drawPolygon(
      final SecStrDrawingProducer out,
      final Point2D.Double[] points,
      final double thickness,
      final boolean isCis,
      final Color color) {
    // draw square
    final double[] x = new double[points.length];
    final double[] y = new double[points.length];

    for (int i = 0; i < points.length; i++) {
      x[i] = points[i].x;
      y[i] = points[i].y;
    }

    if (isCis) {
      out.fillPolygon(x, y, color);
    } else {
      out.drawPolygon(x, y, thickness);
    }
  }

  private AlternativeLeontisWesthofDrawing() {
    super();
  }
}
