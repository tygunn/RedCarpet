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

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Determines the coordinates of a star.
 * A star is defined by two pentagons, with the outer pentagon rotated 180
 * degrees from the inner pentagon.
 * @author tyler
 */
public class Star {
    public final static double DEFAULT_OUTER_TO_INNER_RATIO = 11.0f / 5.3f;
    
    /**
     * Determines the ratio in size of the outer to the inner pentagon which
     * makes up the star.
     */
    private final double mOuterToInnerRatio = DEFAULT_OUTER_TO_INNER_RATIO;
    
    /**
     * The width of the outer pentagon (ie the overall width of the star).
     */
    private final double mOuterPentagonWidth;
    
    /**
     * The length of a side on the outer pentagon.
     */
    private double mOuterPentagonSide;
    
    /**
     * The outer pentagon.
     */
    private Pentagon mOuterPentagon;
    
    /**
     * The inner pentagon.
     */
    private Pentagon mInnerPentagon;
    
    /**
     * The resulting vertices of the star.
     */
    private List<Point2D> mStarVertices = new ArrayList<>(10);
    
    /**
     * Creates a new instance of a star for a given width, with the default
     * outer to inner ratio.
     * @param outerPentagonWidth the width
     */
    public Star(double outerPentagonWidth) {
        this(outerPentagonWidth, DEFAULT_OUTER_TO_INNER_RATIO);
    }
    
    /**
     * Creates a new instance of a star with the given width and outer to
     * inner ratio.
     * @param outerPentagonWidth width of the pentagon
     * @param outerInnerRatio the ratio in width of the outer to inner
     *                        pentagons.
     */
    public Star(double outerPentagonWidth, double outerInnerRatio) {
        mOuterPentagonWidth = outerPentagonWidth;
        mOuterPentagon = new Pentagon(outerPentagonWidth, Math.PI);
        mInnerPentagon = new Pentagon(outerPentagonWidth / 
                outerInnerRatio, 0.0);
        collectVertices();
    }
    
    /**
     * Collects the vertices of the star based on the pentagons that make it up.
     * The inner vertex indices are offset by 3 to account for the fact that
     * the outer pentagon is oriented point up, and the inner pentagon is
     * oriented point down.
     */
    private void collectVertices() {
        List<Point2D> innerVertices = mInnerPentagon.getVertices();
        List<Point2D> outerVertices = mOuterPentagon.getVertices();
        for (int ix = 0; ix < innerVertices.size(); ix++) {
            mStarVertices.add(outerVertices.get(ix));
            mStarVertices.add(innerVertices.get((ix + 3) % 5));
        }
    }
    
    /**
     * @return copy of the star vertices.
     */
    public List<Point2D> getVerticeCopy() {
        return new ArrayList<>(mStarVertices);
    }
    
    /**
     * @return the vertices of the star.
     */
    public List<Point2D> getVertices() {
        return mStarVertices;
    }

    /**
     * @return a path representing the star.
     */
    public Path2D getPath() {
        Path2D path = new Path2D.Double();
        boolean isFirst = true;
        for (Point2D pt : getVerticeCopy()) {
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
     * @return Length of one side of the star in inches.
     */
    public double getSideLengthInches() {
        return Point2D.distance(mStarVertices.get(0).getX(),
                mStarVertices.get(0).getY(),
                mStarVertices.get(1).getX(),
                mStarVertices.get(1).getY());
    }
    
    /**
     * Get the width of the star.
     * @return the width of the star.
     */
    public double getWidth() {
        return getMaxX() - getMinX();
    }
    
    /**
     * Get the height of the star.
     * @return the height of the star.
     */
    public double getHeight() {
        return getMaxY() - getMinY();
    }
    
    /**
     * @return the maximal X value of the star.
     */
    public double getMaxX() {
        double maxX = 0.0;
        for (Point2D p : mStarVertices) {
            maxX = Math.max(maxX, p.getX());
        }
        return maxX;
    }
    
    /**
     * @return the minimal X value of the star.
     */
    public double getMinX() {
        double minX = 0.0;
        for (Point2D p : mStarVertices) {
            minX = Math.min(minX, p.getX());
        }
        return minX;
    }
    
    /**
     * @return the maximal Y value of the star.
     */
    public double getMaxY() {
        double maxY = 0.0;
        for (Point2D p : mStarVertices) {
            maxY = Math.max(maxY, p.getY());
        }
        return maxY;
    }
    
    /**
     * @return the minimal Y value of the star.
     */
    public double getMinY() {
        double minY = 0.0;
        for (Point2D p : mStarVertices) {
            minY = Math.min(minY, p.getY());
        }
        return minY;
    }
    
    /**
     * Get the translation applied to move this star onto the regular graphics
     * canvas.
     * @return 
     */
    public AffineTransform getOffsetTransform() {
        return AffineTransform.getTranslateInstance(
                -1.0 * getMinX(), 
                -1.0 * getMinY());
    }
}

