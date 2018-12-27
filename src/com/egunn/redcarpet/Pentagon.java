/* 
 * Copyright (C) 2018 Tyler J. Gunn
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.egunn.redcarpet;

import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

/**
 * A representation of a regular pentagon.
 * The points of a pentagon are defined as:
 *        A
 *       ,'.
 *     ,'   `.
 * E ,'       `. B
 *   \         /
 *    \       /
 *     \_____/ 
 *    D       C
 * 
 * @author tyler
 */
public class Pentagon {
    /**
     * The width of the pentagon (maximal distance as defined by point E-B, for
     * example).
     */
    private final double mWidth;
    /**
     * The length of a side of the pentagon (e.g. A-B).
     */
    private final double mSide;
    /**
     * The radius of the pentagon (origin to A, for example).
     */
    private final double mRadius;
    /**
     * A rotation (in radians) which is applied to the pentagon vertices.
     */
    private final double mRotation;
    
    // Points of the pentagon.
    private Point2D.Double mA;
    private Point2D.Double mB;
    private Point2D.Double mC;
    private Point2D.Double mD;
    private Point2D.Double mE;
    
    /**
     * Create a new pentagon with the given width, centered about the origin,
     * rotated as specified.
     * @param width The width of the pentagon.
     * @param rotation The rotation of the pentagon.
     */
    public Pentagon(double width, double rotation) {
        mWidth = width;
        mSide = calculatePentagonSide(width);
        mRadius = calculatePentagonRadius(width);
        mRotation = rotation;
        calculatePoints();
        rotatePoints();
    }
    
    /**
     * @return A list containing all vertices of the pentagon.
     */
    public List<Point2D> getVertices() {
        List<Point2D> p = new ArrayList<Point2D>();
        p.add(mA);
        p.add(mB);
        p.add(mC);
        p.add(mD);
        p.add(mE);
        return p;
    }
    
    /**
     * @return A {@link Path2D} representing the vertices of the pentagon.
     */
    public Path2D getPath() {
        Path2D path = new Path2D.Double();
        boolean isFirst = true;
        for (Point2D pt : getVertices()) {
            if (isFirst) {
                path.moveTo(pt.getX(), pt.getY());
                isFirst = false;
            } else {
                path.lineTo(pt.getX(), pt.getY());
            }
        }
        path.closePath();
        return path;
    }
    
    /**
     * Rotate all points of the pentagon as specified by the {@link #mRotation}
     * parameter.
     */
    private void rotatePoints() {
        AffineTransform trans = AffineTransform.getRotateInstance(mRotation);
        trans.transform(mA, mA);
        trans.transform(mB, mB);
        trans.transform(mC, mC);
        trans.transform(mD, mD);
        trans.transform(mE, mE);
    }
    
    /**
     * Calculates the points of the pentagon.
     * 
     * Source: Source: http://mathworld.wolfram.com/RegularPentagon.html
     */
    private void calculatePoints() {
        double c1 = Math.cos(2.0*Math.PI/5.0) * mRadius;
        double c2 = Math.cos(Math.PI/5.0) * mRadius;
        double s1 = Math.sin(2.0*Math.PI/5.0) * mRadius;
        double s2 = Math.sin(4.0*Math.PI/5.0) * mRadius;
        mA = new Point2D.Double(0f, mRadius);
        mB = new Point2D.Double(s1, c1);
        mC = new Point2D.Double(s2, -1.0 * c2);
        mD = new Point2D.Double(-1.0 * s2, -1.0 * c2);
        mE = new Point2D.Double(-1.0 * s1, c1);
    }
    
    /**
     * Given the width of a pentagon, determine the length of a side of the
     * pentagon.
     * Source: https://en.wikipedia.org/wiki/Pentagon
     * 
     * @param width Width of the pentagon.
     * @return length of a side of the pentagon.
     */
    private double calculatePentagonSide(double width) {
        return width / ((1.0+Math.sqrt(5.0))/2.0f);     
    }
    
    /**
     * Given the width(diagonal) of a pentagon, determine the radius of the
     * circumference of the pentagon.
     * Source: https://en.wikipedia.org/wiki/Pentagon
     * 
     * @param width Width of the pentagon
     * @return radius of circumference.
     */
    private double calculatePentagonRadius(double width) {
        return width / (2.0 * Math.cos(Math.PI / 10.0));
    }
}
