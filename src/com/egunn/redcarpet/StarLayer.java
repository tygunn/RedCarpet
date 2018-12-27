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

import com.egunn.redcarpet.PixelStar.HoleFormat;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Defines a single layer in a {@link PixelStar}.
 * 
 * @author tyler
 */
public class StarLayer {
   
    /**
     * The star which makes up this layer.
     */
    private final Star mStar;
    
    /**
     * The spacing between each of the holes, along the sides of the stars.
     */
    private final double mHoleSpacing;
    
    /**
     * The size of each hole.
     */
    private final double mHoleSize;
    
    /**
     * The number of holes per edge of the star.  This can either be calculated
     * or passed in.
     */
    private Integer mNumHolesPerEdge;
    
    /**
     * The translation which will be applied to the star.
     * The stars are generated using a typical coordinate system centered at
     * (0,0) with +/- X and Y.  The translation is used to move to a typical
     * graphics coordinate system based at 0,0 in the top left of the graphics
     * area.
     */
    private AffineTransform mTranslation;
    
    /**
     * The holes placed on the star.
     * Hole placement begins at the top vertex of the star and continues in a
     * counter-clockwise direction.
     */
    private List<Ellipse2D> mHoles = new ArrayList<Ellipse2D>();
    
    /**
     * Where to start labelling the holes in this layer; used when drawing
     * hole numbers.
     */
    private int mHoleCountStart = 0;
    
    /**
     * Creates a new star layer.
     * @param widthInches
     * @param holeSpacingInches
     * @param holeSizeInches 
     */
    public StarLayer(double widthInches, double ratio, double holeSpacingInches,
            double holeSizeInches) {
        mStar = new Star(widthInches, ratio);
        mHoleSpacing = holeSpacingInches;
        mHoleSize = holeSizeInches;
        mNumHolesPerEdge = calculateHolesPerEdge();
        
        placeHoles();
    }
    
    /**
     * Creates a new star layer.
     * @param widthInches
     * @param ratio
     * @param holeSpacingInches
     * @param holeSizeInches
     * @param numHoles
     * @param holeCountStart 
     */
    public StarLayer(double widthInches, double ratio, double holeSpacingInches,
            double holeSizeInches, int numHoles, int holeCountStart) {
        mStar = new Star(widthInches, ratio);
        mHoleSpacing = holeSpacingInches;
        mHoleSize = holeSizeInches;
        mNumHolesPerEdge = numHoles;
        mHoleCountStart = holeCountStart;
        placeHoles();
    }
    
    /**
     * Calculates the number of holes which will be present on an edge of the
     * star.
     * @return The number of holes per edge.
     */
    private int calculateHolesPerEdge() {
        return (int) (getEdgeLength() / mHoleSpacing);
    }
    
    /**
     * Gets the number of holes which will be present per edge of the star.
     * When {@link #StarLayer(double, double, double) is used to construct
     * the layer, this will be {@link #calculateHolesPerEdge()}.
     * When {@link #StarLayer(double, double, double, int, int)  is used to
     * construct the layer, then the supplied value will be used.
     * @return 
     */
    public int getNumHolesPerEdge() {
        return mNumHolesPerEdge;
    }
    
    /**
     * Sets the number of holes which will be present on each edge of the star.
     * @param holes 
     */
    public void setNumHolesPerEdge(int holes) {
        mNumHolesPerEdge = holes;
    }
    
    /**
     * Place the holes along the edges of the star.
     */
    private void placeHoles() {
        double sideLength = getEdgeLength();
        
        int pixelsToAdd = mNumHolesPerEdge;
        // The start vertex is not included in the calculations below..
        pixelsToAdd--;
        
        List<Point2D> vertices = mStar.getVertices();
        for (int ix = 0 ; ix < vertices.size(); ix++) {
            // Pick two vertices on the star to place holes.
            Point2D start = vertices.get(ix);
            Point2D end = vertices.get((ix + 1)
                    % vertices.size());
            
            // Add the hole at the start of the segment on the star vertex
            Ellipse2D startHole = new Ellipse2D.Double(
                    /** top left coordinate */
                    start.getX() - (mHoleSize / 2.0), 
                    start.getY() - (mHoleSize / 2.0),
                    /** x and y width */
                    mHoleSize, 
                    mHoleSize);
            mHoles.add(startHole);
            
            // Divide up the space and add the pixels
            List<Ellipse2D> localHoles = new ArrayList<Ellipse2D>();
            for (int addIx = 1 ; addIx < pixelsToAdd+1; addIx++) {
                // Determine where between the start and end point we are
                // placing the hole.
                double offset = (double) addIx / (double) (pixelsToAdd + 1);
                double xDistance = end.getX() - start.getX();
                double yDistance = end.getY() - start.getY();
                
                // This code can be used to place the holes such that they are
                // not evenly spaced on each edge.
                // double offsetX = xDistance * ((double)(pixelsToAdd+1) * mHoleSpacing / sideLength) * offset;
                // double offsetY = yDistance * ((double)(pixelsToAdd+1) * mHoleSpacing / sideLength) * offset;
                double offsetX = xDistance * offset;
                double offsetY = yDistance * offset;
                Ellipse2D newHole;
                
                // We place holes starting at the end and work our way back
                newHole = new Ellipse2D.Double(
                    end.getX() - offsetX - (mHoleSize / 2.0),
                    end.getY() - offsetY - (mHoleSize / 2.0),
                    /** x and y width */
                    mHoleSize, 
                    mHoleSize);
                localHoles.add(newHole);
            }
            // This seems strange, but we generate the holes backwards from the
            // end.  We place them into the list of holes in reverse order so
            // that when we're draing the numbers they are sequential.
            Collections.reverse(localHoles);
            mHoles.addAll(localHoles);
        }
    }
    
    /**
     * @return returns a copy of the holes used in the star layer.
     */
    public List<Ellipse2D> getHoles() {
        List<Ellipse2D> list = new ArrayList<Ellipse2D>(mHoles.size());
        for (Ellipse2D hole : mHoles) {
            list.add(new Ellipse2D.Double(
                    hole.getX(), 
                    hole.getY(), 
                    hole.getWidth(), 
                    hole.getHeight()));
        }
        return list;
    }
    
    /**
     * Draws this star layer on the specifeid graphics canvas.
     * @param g The {@link Graphics2D} to draw to.
     * @param isLabellingHoles
     * @param isDrawingOutline
     */
    public void draw(Graphics2D g, boolean isLabellingHoles, 
            boolean isDrawingOutline, HoleFormat holeFormat) {
        AffineTransform at = getOffsetTransform();
        
        if (isDrawingOutline) {
            // Draw the outline of the star.
            Path2D starPath = mStar.getPath();
            starPath.transform(getOffsetTransform());
            g.draw(starPath);
        }
        
        // Draw the holes.
        int holeCount = 1;
        for (Ellipse2D e : getHoles()) {
            // Draw the hole
            Ellipse2D eTran = new Ellipse2D.Double(
                    e.getX() + at.getTranslateX(),
                    e.getY() + at.getTranslateY(),
                    e.getWidth(),
                    e.getHeight());
            
            if (holeFormat == HoleFormat.SOLID) {
                g.setColor(Color.black);
                g.fill(eTran);
                g.draw(eTran);
            } else {
                g.setColor(Color.white);
                g.fill(eTran);
                g.setColor(Color.black);
                g.draw(eTran);
                if (holeFormat == HoleFormat.OUTLINE_TARGET) {
                    // Draw a crosshair in the hole.
                    g.drawLine(
                            (int) (e.getX() + at.getTranslateX() 
                                    + (e.getWidth() / 2.0)),
                            (int) (e.getY() + at.getTranslateY()),
                            (int) (e.getX() + at.getTranslateX() 
                                    + (e.getWidth() / 2.0)),
                            (int) (e.getY() + at.getTranslateY() + e.getHeight())
                    );

                    g.drawLine(
                            (int) (e.getX() + at.getTranslateX()),
                            (int) (e.getY() + at.getTranslateY()
                                    + (e.getHeight() / 2)),
                            (int) (e.getX() + at.getTranslateX() 
                                    + e.getWidth()),
                            (int) (e.getY() + at.getTranslateY() 
                                    + (e.getHeight() / 2))
                    );
                }
            }
            
            if (isLabellingHoles) {
                // Label each hole with a hole number.
                String num = "" + (mHoleCountStart + holeCount) + "";
                g.drawString(num, 
                        (float)(e.getX() + at.getTranslateX()), 
                        (float) ( e.getY() + at.getTranslateY()));
            }
            holeCount++;
        }
    }
    
    /**
     * @return Determine the length of an edge of the star.
     */
    public double getEdgeLength() {
        return Point2D.distance(
                mStar.getVertices().get(0).getX(), 
                mStar.getVertices().get(0).getY(), 
                mStar.getVertices().get(1).getX(),
                mStar.getVertices().get(1).getY());
    }
    
    public double getWidth() {
        return getMaxX() - getMinX();
    }
    
    public double getHeight() {
        return getMaxY() - getMinY();
    }
    
    /**
     * @return The minimum X value bounding the star layer.
     */
    public double getMinX() {
        double starMinX = mStar.getMinX();
        double holeMinX = 0.0;
        for (Ellipse2D e : getHoles()) {
            holeMinX = Math.min(holeMinX, e.getX());
        }
        return Math.min(starMinX, holeMinX);
    }
    
    /**
     * @return The maximum X value bounding the star layer.
     */
    public double getMaxX() {
        double starMaxX = mStar.getMaxX();
        double holeMaxX = 0.0;
        for (Ellipse2D e : getHoles()) {
            holeMaxX = Math.max(holeMaxX, e.getX() + e.getWidth());
        }
        return Math.max(starMaxX, holeMaxX);
    }
    
    /**
     * @return The minimum Y value bounding the star layer.
     */
    public double getMinY() {
        double starMinY= mStar.getMinY();
        double holeMinY = 0.0;
        for (Ellipse2D e : getHoles()) {
            holeMinY = Math.min(holeMinY, e.getY());
        }
        return Math.min(starMinY, holeMinY);
    }

    /**
     * @return The maximum Y value bounding the star layer.
     */
    public double getMaxY() {
        double starMaxY = mStar.getMaxY();
        double holeMaxY = 0.0;
        for (Ellipse2D e : getHoles()) {
            holeMaxY = Math.max(holeMaxY, e.getY() + e.getHeight());
        }
        return Math.max(starMaxY, holeMaxY);
    }
    
    /**
     * Get the translation applied to move this star layer into alignment with
     * the other star layers.
     * @return 
     */
    public AffineTransform getOffsetTransform() {
        if (mTranslation != null ) {
            return mTranslation;
        }

        return AffineTransform.getTranslateInstance(
                -1.0 * getMinX(), 
                -1.0 * getMinY());
    }
    
    /**
     * @param trans Sets the transform used to align this layer with the others.
     */
    public void setOffsetTransform(AffineTransform trans) {
        mTranslation = trans;
    }
    
    /**
     * @return The number of pixels in this layer.
     */
    public int getPixelCount() {
        return mHoles.size();
    }
    
    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("[StarLayer ");
        
        sb.append("topLeft = (");
        sb.append(getMinX());
        sb.append(",");
        sb.append(getMinY());
        sb.append(")");
         
        sb.append(", bottomRight=(");
        sb.append(getMaxX());
        sb.append(",");
        sb.append(getMaxY());
        sb.append(")");
        
        sb.append(", width=");
        sb.append(getWidth());
        sb.append(", height=");
        sb.append(getHeight());
        
        sb.append(", sideLength=");
        sb.append(Point2D.distance(
                mStar.getVertices().get(0).getX(), 
                mStar.getVertices().get(0).getY(), 
                mStar.getVertices().get(1).getX(),
                mStar.getVertices().get(1).getY()) / PixelStar.PTS_PER_INCH);
        
        mStar.getVertices().get(1);
        
        sb.append("]");
      
        return sb.toString();
    }
}